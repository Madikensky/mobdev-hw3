package kz.sitetracker.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

/** 2026-11-03 -> "Nov 3, 2026" */
fun formatDate(date: LocalDate): String = date.format(dateFormatter)
