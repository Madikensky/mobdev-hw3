package kz.sitetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.sitetracker.data.Site
import kz.sitetracker.ui.components.ConfirmDeleteDialog
import kz.sitetracker.ui.components.EmptyState
import kz.sitetracker.ui.components.InspectionItem
import kz.sitetracker.ui.components.InspectionSummaryCard
import kz.sitetracker.ui.components.LoadingBox
import kz.sitetracker.ui.components.ScreenHeader
import kz.sitetracker.ui.components.SiteIcon
import kz.sitetracker.ui.components.SiteStatusChip
import kz.sitetracker.ui.formatDate
import kz.sitetracker.ui.viewmodel.AppViewModelProvider
import kz.sitetracker.ui.viewmodel.SiteDetailEvent
import kz.sitetracker.ui.viewmodel.SiteDetailUiState
import kz.sitetracker.ui.viewmodel.SiteDetailViewModel

@Composable
fun SiteDetailRoute(
    onBack: () -> Unit,
    onEditSite: (Int) -> Unit,
    onAddInspection: (Int) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SiteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SiteDetailEvent.SiteDeleted -> {
                    onMessage("Site \"${event.name}\" deleted")
                    onBack()
                }
                SiteDetailEvent.InspectionDeleted -> onMessage("Inspection deleted")
            }
        }
    }

    SiteDetailScreen(
        state = state,
        onBack = onBack,
        onEditSite = onEditSite,
        onDeleteSite = viewModel::deleteSite,
        onAddInspection = onAddInspection,
        onDeleteInspection = viewModel::deleteInspection,
        modifier = modifier
    )
}

@Composable
fun SiteDetailScreen(
    state: SiteDetailUiState,
    onBack: () -> Unit,
    onEditSite: (Int) -> Unit,
    onDeleteSite: () -> Unit,
    onAddInspection: (Int) -> Unit,
    onDeleteInspection: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Which dialog is open is UI-only state -> rememberSaveable, so it stays open after rotation.
    var showDeleteSite by rememberSaveable { mutableStateOf(false) }
    var pendingInspectionId by rememberSaveable { mutableStateOf<Int?>(null) }

    val site = state.site

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(
            title = site?.name ?: "Site",
            onBack = onBack,
            actions = {
                if (site != null) {
                    IconButton(onClick = { onEditSite(site.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit site")
                    }
                    IconButton(onClick = { showDeleteSite = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete site")
                    }
                }
            }
        )

        when {
            state.isLoading -> LoadingBox()
            site == null -> EmptyState("This site no longer exists")
            else -> LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "info") { SiteInfoCard(site) }
                item(key = "summary") { InspectionSummaryCard(state.summary) }
                item(key = "history-title") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Inspection history", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { onAddInspection(site.id) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add")
                        }
                    }
                }
                if (state.inspections.isEmpty()) {
                    item(key = "empty") {
                        EmptyState("No inspections yet. Tap \"Add\" to record the first one.")
                    }
                } else {
                    items(state.inspections, key = { it.id }) { inspection ->
                        InspectionItem(
                            inspection = inspection,
                            onDelete = { pendingInspectionId = inspection.id }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteSite && site != null) {
        val count = state.summary.total
        ConfirmDeleteDialog(
            title = "Delete site?",
            message = "\"${site.name}\" and its $count " +
                (if (count == 1) "inspection" else "inspections") + " will be removed permanently.",
            onConfirm = {
                showDeleteSite = false
                onDeleteSite()
            },
            onDismiss = { showDeleteSite = false }
        )
    }

    pendingInspectionId?.let { id ->
        val inspection = state.inspections.firstOrNull { it.id == id }
        ConfirmDeleteDialog(
            title = "Delete inspection?",
            message = if (inspection != null) {
                "Inspection of ${formatDate(inspection.date)} by ${inspection.inspector} " +
                    "(${inspection.result.title}) will be removed permanently."
            } else {
                "This inspection will be removed permanently."
            },
            onConfirm = {
                pendingInspectionId = null
                onDeleteInspection(id)
            },
            onDismiss = { pendingInspectionId = null }
        )
    }
}

@Composable
private fun SiteInfoCard(site: Site) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SiteIcon(site.status)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(site.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("City: ${site.city.title}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Address: ${site.address.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodyMedium
                )
                SiteStatusChip(site.status)
            }
        }
    }
}
