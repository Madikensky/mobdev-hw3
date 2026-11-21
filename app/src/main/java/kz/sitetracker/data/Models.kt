package kz.sitetracker.data

import java.time.LocalDate

enum class City(val title: String) {
    ALMATY("Almaty"),
    ASTANA("Astana"),
    SHYMKENT("Shymkent"),
    KARAGANDA("Karaganda"),
    AKTOBE("Aktobe");

    companion object {
        /** Safe conversion from the string stored in the database. */
        fun fromName(name: String): City = entries.firstOrNull { it.name == name } ?: ALMATY
    }
}

enum class SiteStatus(val title: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    UNDER_CONSTRUCTION("Under construction"),
    DECOMMISSIONED("Decommissioned");

    companion object {
        fun fromName(name: String): SiteStatus = entries.firstOrNull { it.name == name } ?: INACTIVE
    }
}

enum class InspectionResult(val title: String) {
    PASSED("Passed"),
    NEEDS_ATTENTION("Needs attention"),
    FAILED("Failed");

    companion object {
        fun fromName(name: String): InspectionResult =
            entries.firstOrNull { it.name == name } ?: NEEDS_ATTENTION
    }
}

/** A telecom site (tower, antenna, hub...) — the main object of the app. */
data class Site(
    val id: Int,
    val name: String,
    val address: String,
    val city: City,
    val status: SiteStatus
)

/** One inspection of a site. A site has many inspections (one-to-many). */
data class Inspection(
    val id: Int,
    val siteId: Int,
    val inspector: String,
    val date: LocalDate,
    val result: InspectionResult,
    val notes: String
)

/** Statistics for the inspection history of one site. Pure logic, no Android. */
data class InspectionSummary(
    val total: Int = 0,
    val passed: Int = 0,
    val needsAttention: Int = 0,
    val failed: Int = 0,
    val latest: Inspection? = null
) {
    /** Share of passed inspections, 0..100. */
    val passRate: Int get() = if (total == 0) 0 else passed * 100 / total

    companion object {
        fun from(inspections: List<Inspection>) = InspectionSummary(
            total = inspections.size,
            passed = inspections.count { it.result == InspectionResult.PASSED },
            needsAttention = inspections.count { it.result == InspectionResult.NEEDS_ATTENTION },
            failed = inspections.count { it.result == InspectionResult.FAILED },
            latest = latestOf(inspections)
        )

        /** The newest inspection: latest date, and for the same date the one added last. */
        fun latestOf(inspections: List<Inspection>): Inspection? =
            inspections.maxWithOrNull(compareBy<Inspection> { it.date }.thenBy { it.id })
    }
}
