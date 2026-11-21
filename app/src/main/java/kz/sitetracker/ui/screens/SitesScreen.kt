package kz.sitetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.sitetracker.data.SiteStatus
import kz.sitetracker.ui.components.EmptyState
import kz.sitetracker.ui.components.LoadingBox
import kz.sitetracker.ui.components.OptionChip
import kz.sitetracker.ui.components.SearchField
import kz.sitetracker.ui.components.SiteCard
import kz.sitetracker.ui.viewmodel.AppViewModelProvider
import kz.sitetracker.ui.viewmodel.SitesUiState
import kz.sitetracker.ui.viewmodel.SitesViewModel

/** Stateful wrapper: gets the ViewModel and collects its state with the lifecycle. */
@Composable
fun SitesRoute(
    onOpenSite: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SitesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SitesScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onStatusFilterChange = viewModel::onStatusFilterChange,
        onOpenSite = onOpenSite,
        modifier = modifier
    )
}

/** Stateless list screen: draws [state] and reports user actions through callbacks. */
@Composable
fun SitesScreen(
    state: SitesUiState,
    onQueryChange: (String) -> Unit,
    onStatusFilterChange: (SiteStatus?) -> Unit,
    onOpenSite: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        LoadingBox(modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text("Sites", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "${state.totalSites} sites in total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            placeholder = "Search by name, address or city",
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item(key = "all") {
                OptionChip("All", state.statusFilter == null) { onStatusFilterChange(null) }
            }
            items(SiteStatus.entries, key = { it.name }) { status ->
                OptionChip(status.title, state.statusFilter == status) { onStatusFilterChange(status) }
            }
        }

        when {
            state.totalSites == 0 -> EmptyState("No sites yet. Tap + to add the first one.")
            state.items.isEmpty() -> EmptyState("No sites match your search or filter")
            else -> LazyColumn(
                // bottom padding keeps the last card clear of the floating action button
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // stable keys: the database id of each site
                items(state.items, key = { it.site.id }) { item ->
                    SiteCard(
                        site = item.site,
                        latestInspection = item.latestInspection,
                        onClick = { onOpenSite(item.site.id) }
                    )
                }
            }
        }
    }
}
