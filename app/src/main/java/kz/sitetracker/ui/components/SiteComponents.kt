package kz.sitetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.sitetracker.data.Inspection
import kz.sitetracker.data.InspectionResult
import kz.sitetracker.data.InspectionSummary
import kz.sitetracker.data.Site
import kz.sitetracker.data.SiteStatus
import kz.sitetracker.ui.formatDate
import kz.sitetracker.ui.theme.Good
import kz.sitetracker.ui.theme.Warning

@Composable
fun siteStatusColor(status: SiteStatus): Color = when (status) {
    SiteStatus.ACTIVE -> Good
    SiteStatus.INACTIVE -> MaterialTheme.colorScheme.outline
    SiteStatus.UNDER_CONSTRUCTION -> Warning
    SiteStatus.DECOMMISSIONED -> MaterialTheme.colorScheme.error
}

@Composable
fun resultColor(result: InspectionResult): Color = when (result) {
    InspectionResult.PASSED -> Good
    InspectionResult.NEEDS_ATTENTION -> Warning
    InspectionResult.FAILED -> MaterialTheme.colorScheme.error
}

/** Small coloured pill with a label. */
@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f), modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun SiteStatusChip(status: SiteStatus, modifier: Modifier = Modifier) =
    Pill(status.title, siteStatusColor(status), modifier)

@Composable
fun ResultChip(result: InspectionResult, modifier: Modifier = Modifier) =
    Pill(result.title, resultColor(result), modifier)

/** Round location icon tinted with the site status colour. */
@Composable
fun SiteIcon(status: SiteStatus, modifier: Modifier = Modifier) {
    val color = siteStatusColor(status)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = color)
    }
}

fun Site.locationText(): String =
    if (address.isBlank()) city.title else "${city.title} · $address"

/** One row of the sites list: name, location, status and the result of the newest inspection. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteCard(
    site: Site,
    latestInspection: Inspection?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SiteIcon(site.status)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(site.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    site.locationText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SiteStatusChip(site.status)
                    if (latestInspection != null) {
                        ResultChip(latestInspection.result)
                    } else {
                        Pill("Not inspected", MaterialTheme.colorScheme.outline)
                    }
                }
                if (latestInspection != null) {
                    Text(
                        "Last inspection: ${formatDate(latestInspection.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** One entry of the inspection history, with a delete button. */
@Composable
fun InspectionItem(
    inspection: Inspection,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ResultChip(inspection.result)
                    Text(formatDate(inspection.date), style = MaterialTheme.typography.labelMedium)
                }
                Text("Inspector: ${inspection.inspector}", style = MaterialTheme.typography.bodyMedium)
                if (inspection.notes.isNotBlank()) {
                    Text(
                        inspection.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete inspection")
            }
        }
    }
}

/** Statistics for the inspection history of one site. */
@Composable
fun InspectionSummaryCard(summary: InspectionSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Inspection statistics", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatValue("Total", summary.total, MaterialTheme.colorScheme.onPrimaryContainer)
                StatValue("Passed", summary.passed, Good)
                StatValue("Attention", summary.needsAttention, Warning)
                StatValue("Failed", summary.failed, MaterialTheme.colorScheme.error)
            }
            val latest = summary.latest
            Text(
                text = if (latest == null) {
                    "Never inspected"
                } else {
                    "Pass rate ${summary.passRate}% · last: ${latest.result.title}, ${formatDate(latest.date)}"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatValue(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
