package kz.sitetracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.sitetracker.data.Inspection
import kz.sitetracker.data.InspectionRepository
import kz.sitetracker.data.InspectionSummary
import kz.sitetracker.data.Site
import kz.sitetracker.data.SiteRepository

data class SiteDetailUiState(
    val isLoading: Boolean = true,
    val site: Site? = null,                       // null after loading = not found
    val inspections: List<Inspection> = emptyList(),   // newest first
    val summary: InspectionSummary = InspectionSummary()
)

sealed interface SiteDetailEvent {
    data class SiteDeleted(val name: String) : SiteDetailEvent
    data object InspectionDeleted : SiteDetailEvent
}

class SiteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val siteRepository: SiteRepository,
    private val inspectionRepository: InspectionRepository
) : ViewModel() {

    // The navigation argument "siteId" arrives through SavedStateHandle.
    private val siteId: Int = checkNotNull(savedStateHandle["siteId"])

    // Site + its inspections. Editing the site on the form screen or adding an inspection
    // re-emits these Room Flows, so this screen always shows the current data.
    val uiState: StateFlow<SiteDetailUiState> = combine(
        siteRepository.observe(siteId),
        inspectionRepository.observeForSite(siteId)
    ) { site, inspections ->
        SiteDetailUiState(
            isLoading = false,
            site = site,
            inspections = inspections,
            summary = InspectionSummary.from(inspections)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SiteDetailUiState())

    // One-time events: handled exactly once (snackbar, navigation), so they go through a Channel.
    private val _events = Channel<SiteDetailEvent>(Channel.BUFFERED)
    val events: Flow<SiteDetailEvent> = _events.receiveAsFlow()

    fun deleteSite() {
        val name = uiState.value.site?.name ?: return
        viewModelScope.launch {
            siteRepository.delete(siteId)
            // navigate away only AFTER the row is really deleted
            _events.send(SiteDetailEvent.SiteDeleted(name))
        }
    }

    fun deleteInspection(id: Int) {
        viewModelScope.launch {
            inspectionRepository.delete(id)
            _events.send(SiteDetailEvent.InspectionDeleted)
        }
    }
}
