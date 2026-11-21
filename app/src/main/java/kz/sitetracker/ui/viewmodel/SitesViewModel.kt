package kz.sitetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kz.sitetracker.data.Inspection
import kz.sitetracker.data.InspectionRepository
import kz.sitetracker.data.InspectionSummary
import kz.sitetracker.data.Site
import kz.sitetracker.data.SiteRepository
import kz.sitetracker.data.SiteStatus

/** A site as shown in the list, together with its newest inspection (null = never inspected). */
data class SiteListItem(
    val site: Site,
    val latestInspection: Inspection?
)

data class SitesUiState(
    val isLoading: Boolean = true,
    val items: List<SiteListItem> = emptyList(),   // after search + filter
    val totalSites: Int = 0,                       // to tell "empty database" from "no match"
    val query: String = "",
    val statusFilter: SiteStatus? = null           // null = "All"
)

class SitesViewModel(
    siteRepository: SiteRepository,
    inspectionRepository: InspectionRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<SiteStatus?>(null)

    // Two Room Flows + two filters merged into one immutable UI state. When a site is edited or an
    // inspection is added on another screen, Room re-emits and this list updates by itself.
    val uiState: StateFlow<SitesUiState> = combine(
        siteRepository.observeAll(),
        inspectionRepository.observeAll(),
        query,
        statusFilter
    ) { sites, inspections, q, status ->
        val latestBySite = inspections
            .groupBy { it.siteId }
            .mapValues { (_, list) -> InspectionSummary.latestOf(list) }
        val text = q.trim()
        SitesUiState(
            isLoading = false,
            items = sites
                .filter { site ->
                    (status == null || site.status == status) &&
                        (site.name.contains(text, ignoreCase = true) ||
                            site.address.contains(text, ignoreCase = true) ||
                            site.city.title.contains(text, ignoreCase = true))
                }
                .map { SiteListItem(it, latestBySite[it.id]) },
            totalSites = sites.size,
            query = q,
            statusFilter = status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SitesUiState())

    fun onQueryChange(value: String) = query.update { value }

    fun onStatusFilterChange(status: SiteStatus?) = statusFilter.update { status }
}
