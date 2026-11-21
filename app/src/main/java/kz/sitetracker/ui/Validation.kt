package kz.sitetracker.ui

import kz.sitetracker.data.InspectionResult
import java.time.LocalDate
import java.time.format.DateTimeParseException

const val MAX_SITE_NAME = 50
const val MAX_ADDRESS = 100
const val MAX_INSPECTOR = 50
const val MAX_NOTES = 200

/** Pure input rules used by the forms. Each returns an error text, or null when the value is valid. */

fun validateSiteName(text: String): String? {
    val value = text.trim()
    return when {
        value.isEmpty() -> "Name is required"
        value.length < 3 -> "At least 3 characters"
        value.length > MAX_SITE_NAME -> "At most $MAX_SITE_NAME characters"
        else -> null
    }
}

fun validateInspector(text: String): String? {
    val value = text.trim()
    return when {
        value.isEmpty() -> "Inspector name is required"
        value.length < 2 -> "At least 2 characters"
        value.length > MAX_INSPECTOR -> "At most $MAX_INSPECTOR characters"
        else -> null
    }
}

/** Parses "YYYY-MM-DD"; null if the text is not a real date. */
fun parseDate(text: String): LocalDate? =
    try {
        LocalDate.parse(text.trim())
    } catch (e: DateTimeParseException) {
        null
    }

fun validateInspectionDate(text: String, today: LocalDate = LocalDate.now()): String? {
    if (text.isBlank()) return "Date is required"
    val date = parseDate(text) ?: return "Use the format YYYY-MM-DD"
    return if (date.isAfter(today)) "Date cannot be in the future" else null
}

/** Notes are optional for a passed inspection, but required when something is wrong. */
fun validateNotes(notes: String, result: InspectionResult): String? =
    if (result != InspectionResult.PASSED && notes.isBlank()) {
        "Describe the problem for \"${result.title}\""
    } else {
        null
    }
