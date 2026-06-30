package io.github.jaeyeonling.saju.cli

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.group.Gender
import io.github.jaeyeonling.saju.group.GroupAnalysis
import io.github.jaeyeonling.saju.group.GroupMember
import io.github.jaeyeonling.saju.group.GroupReport
import io.github.jaeyeonling.saju.korea.Birthplace
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.serialization.GroupReportDto
import io.github.jaeyeonling.saju.serialization.InterpretationReportDto
import io.github.jaeyeonling.saju.serialization.SajuChartDto
import io.github.jaeyeonling.saju.serialization.sajuJson
import io.github.jaeyeonling.saju.serialization.toDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
 * 그룹 사주 서브커맨드 — `saju group <members.json> [--json] [--seun=YYYY]`.
 *
 * members.json(vault Python 도구의 sample_members.json 스키마)을 받아 각 멤버의 사주판·해석·대운·세운을
 * KoreanSaju 로 조립한 뒤 GroupAnalysis 로 합성한다. 파일 IO 는 [runGroupCli] 의 readText 주입으로 격리해
 * [renderGroup] 은 순수 함수(입력 문자열 → 출력 문자열)로 유지한다.
 */

private const val DAEUN_COUNT = 8
private const val GROUP_EXIT_OK = 0
private const val GROUP_EXIT_USAGE = 2

/** members.json 입력 — 추가 키는 무시(스키마 진화 견고성). */
private val groupConfigJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class MembersConfig(
    @SerialName("group_name") val groupName: String = "그룹",
    @SerialName("seun_year") val seunYear: Int? = null,
    val members: List<MemberInput> = emptyList(),
)

@Serializable
internal data class MemberInput(
    val id: String,
    val name: String = "",
    val alias: String = "",
    val gender: String = "M",
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val longitude: Double? = null,
)

/** --json 출력 묶음 — 멤버 개인(차트+해석) + 그룹 합성. computed.json 의 persons+group 대응. */
@Serializable
internal data class GroupCliOutput(
    val groupName: String,
    val seunYear: Int,
    val members: List<GroupMemberOutput>,
    val group: GroupReportDto,
)

@Serializable
internal data class GroupMemberOutput(
    val id: String,
    val alias: String,
    val chart: SajuChartDto,
    val interpretation: InterpretationReportDto,
)

/**
 * 그룹 서브커맨드 진입 — 파일 읽기를 [readText] 로 주입받아 테스트 가능하게 한다.
 * `group` 토큰과 `--` 플래그를 제외한 첫 위치 인자가 members.json 경로다.
 */
internal fun runGroupCli(
    args: Array<String>,
    currentYear: Int,
    readText: (String) -> String,
): Pair<String, Int> {
    val flags = parseFlags(args)
    val path =
        args.filterNot { it.startsWith("--") }.firstOrNull { it != "group" }
            ?: return groupUsage("members.json 경로가 필요합니다") to GROUP_EXIT_USAGE
    return try {
        renderGroup(readText(path), flags.seunYear, currentYear, flags.json) to GROUP_EXIT_OK
    } catch (e: kotlinx.serialization.SerializationException) {
        groupUsage("members.json 파싱 실패: ${e.message}") to GROUP_EXIT_USAGE
    } catch (e: java.io.IOException) {
        groupUsage("파일 읽기 실패: ${e.message}") to GROUP_EXIT_USAGE
    } catch (e: IllegalArgumentException) {
        groupUsage("잘못된 입력: ${e.message}") to GROUP_EXIT_USAGE
    }
}

/**
 * 부작용 없는 순수 렌더 — members.json 문자열 → 출력 문자열.
 * 세운 우선순위: [flagSeunYear](명령행 --seun) > config.seun_year > [currentYear].
 */
internal fun renderGroup(
    configJson: String,
    flagSeunYear: Int?,
    currentYear: Int,
    asJson: Boolean,
): String {
    val config = groupConfigJson.decodeFromString(MembersConfig.serializer(), configJson)
    val seunYear = flagSeunYear ?: config.seunYear ?: currentYear
    val members = config.members.map { it.toGroupMember(seunYear) }
    val report = GroupAnalysis.of(members, seunYear)
    return if (asJson) {
        renderGroupJson(
            config,
            members,
            report,
            seunYear,
        )
    } else {
        renderGroupText(config, members, report, seunYear)
    }
}

private fun MemberInput.toGroupMember(seunYear: Int): GroupMember {
    val lon = longitude ?: Birthplace.SEOUL.longitudeDeg
    val isMale = Gender.fromCode(gender) == Gender.MALE
    val chart = KoreanSaju.fromCivilTime(year, month, day, hour, minute, lon)
    val daeun =
        KoreanSaju.daeun(year, month, day, hour, minute, isMale = isMale, longitudeDeg = lon, count = DAEUN_COUNT)
    val label = alias.ifEmpty { name.ifEmpty { id } }
    return GroupMember.of(
        id = id,
        alias = label,
        gender = Gender.fromCode(gender),
        birthYear = year,
        chart = chart,
        daeun = daeun,
        seun = Saju.seun(seunYear),
    )
}

private fun renderGroupJson(
    config: MembersConfig,
    members: List<GroupMember>,
    report: GroupReport,
    seunYear: Int,
): String {
    val output =
        GroupCliOutput(
            groupName = config.groupName,
            seunYear = seunYear,
            members = members.map { GroupMemberOutput(it.id, it.alias, it.chart.toDto(), it.report.toDto()) },
            group = report.toDto(),
        )
    return sajuJson.encodeToString(output)
}

private fun renderGroupText(
    config: MembersConfig,
    members: List<GroupMember>,
    report: GroupReport,
    seunYear: Int,
): String =
    buildString {
        val aliasOf = members.associate { it.id to it.alias }
        appendLine("════════ 그룹 사주 ════════")
        appendLine("그룹: ${config.groupName} · ${members.size}명 · 세운 $seunYear")
        appendLine()
        append(renderOhaengSection(report))
        appendLine()
        append(renderRoleSection(report, aliasOf))
        appendLine()
        append(renderRelationSection(report, aliasOf))
        appendLine()
        append(renderTimelineSection(report, seunYear))
        appendLine()
        appendLine("· ${report.disclaimer}")
    }

private fun renderOhaengSection(report: GroupReport): String =
    buildString {
        val ohaeng = report.ohaeng
        appendLine("───── 오행 균형 ─────")
        appendLine(Ohaeng.entries.joinToString(" ") { "${it.koreanName}${ohaeng.groupVector.getValue(it)}" })
        appendLine("결핍: ${labelList(ohaeng.deficient.map { it.koreanName })}")
        appendLine("과잉: ${labelList(ohaeng.excessive.map { it.koreanName })}")
        appendLine("끊긴 체인: ${labelList(ohaeng.brokenChains.map { it.description })}")
    }

private fun renderRoleSection(
    report: GroupReport,
    aliasOf: Map<String, String>,
): String =
    buildString {
        val roles = report.sipseong
        appendLine("───── 십성 역할 ─────")
        roles.roleComposition.forEach { (role, ids) ->
            appendLine("${role.koreanName}: ${ids.joinToString(", ") { aliasOf[it] ?: it }}")
        }
        appendLine("결핍: ${labelList(roles.deficit.map { it.koreanName })}")
        appendLine("과잉: ${labelList(roles.excess.map { it.koreanName })}")
        roles.triggers.forEach { appendLine("  - ${it.message}") }
    }

private fun renderRelationSection(
    report: GroupReport,
    aliasOf: Map<String, String>,
): String =
    buildString {
        appendLine("───── 멤버 관계 ─────")
        val active = report.relations.pairs.filter { it.relations.isNotEmpty() }
        if (active.isEmpty()) {
            appendLine("(뚜렷한 합충 관계 없음)")
        } else {
            active.forEach { pair ->
                val names = "${aliasOf[pair.a] ?: pair.a}-${aliasOf[pair.b] ?: pair.b}"
                val detail = pair.relations.joinToString(", ") { it.detail }
                appendLine("$names: ${pair.netLabel.koreanName} ($detail)")
            }
        }
    }

private fun renderTimelineSection(
    report: GroupReport,
    seunYear: Int,
): String =
    buildString {
        val timeline = report.timeline
        appendLine("───── 시간축 (세운 $seunYear) ─────")
        appendLine("공통 세운: ${timeline.groupSeun?.koreanName ?: "-"}")
        timeline.memberSeunSipseong.forEach { (id, seun) ->
            val current = timeline.currentDaeun[id]
            val daeunPart = current?.let { " · 현재 대운 ${it.ganji.koreanName}(${it.startAge}세~)" } ?: ""
            appendLine("${seun.alias}: 세운 십성 ${seun.sipSeong?.koreanName ?: "-"}$daeunPart")
        }
        if (timeline.daeunTransitions.isNotEmpty()) {
            val transitions = timeline.daeunTransitions.joinToString(", ") { "${it.alias}(${it.startAge}세)" }
            appendLine("대운 전환기: $transitions")
        }
    }

private fun labelList(items: List<String>): String = items.joinToString(", ").ifEmpty { "-" }

private fun groupUsage(reason: String): String =
    """
    [오류] $reason

    사용법: saju group <members.json> [--json] [--seun=YYYY]
      예) ./gradlew :saju-cli:run --args="group members.json"
          ./gradlew :saju-cli:run --args="group members.json --json --seun=2026"
    members.json 스키마: { "group_name", "seun_year"?, "members": [ {id,name,alias,gender,year,month,day,hour,minute,longitude?} ] }
    멤버는 최소 2명이 필요합니다.
    """.trimIndent()
