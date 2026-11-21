package kz.sitetracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.sitetracker.data.City
import kz.sitetracker.data.Site
import kz.sitetracker.data.SiteRepository
import kz.sitetracker.data.SiteStatus
import kz.sitetracker.ui.MAX_ADDRESS
import kz.sitetracker.ui.MAX_SITE_NAME
import kz.sitetracker.ui.validateSiteName

data class SiteFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val address: String = "",
    val city: City = City.ALMATY,
    val status: SiteStatus = SiteStatus.ACTIVE,
    val showErrors: Boolean = false     // an empty field shows its error only after pressing "Save"
) {
    val nameError: String?
        get() = if (name.isEmpty() && !showErrors) null else validateSiteName(name)

    val isValid: Boolean get() = validateSiteName(name) == null
}

sealed interface FormEvent {
    data class Saved(val message: String) : FormEvent
}

/** Handles both "add site" (no id) and "edit site" (id passed as a navigation argument). */
class SiteFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SiteRepository
) : ViewModel() {

    private val editingId: Int? = savedStateHandle.get<Int>("siteId")?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(
        SiteFormUiState(isEditing = editingId != null, isLoading = editingId != null)
    )
    val uiState: StateFlow<SiteFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<FormEvent>(Channel.BUFFERED)
    val events: Flow<FormEvent> = _events.receiveAsFlow()

    init {
        if (editingId != null) load(editingId)
    }

    private fun load(id: Int) {
        viewModelScope.launch {
            val site = repository.get(id)
            _uiState.update {
                if (site == null) {
                    it.copy(isLoading = false, notFound = true)
                } else {
                    it.copy(
                        isLoading = false,
                        name = site.name,
                        address = site.address,
                        city = site.city,
                        status = site.status
                    )
                }
            }
        }
    }

    fun onNameChange(text: String) = _uiState.update { it.copy(name = text.take(MAX_SITE_NAME)) }

    fun onAddressChange(text: String) = _uiState.update { it.copy(address = text.take(MAX_ADDRESS)) }

    fun onCityChange(city: City) = _uiState.update { it.copy(city = city) }

    fun onStatusChange(status: SiteStatus) = _uiState.update { it.copy(status = status) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.isValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            if (editingId != null) {
                repository.update(Site(editingId, state.name, state.address, state.city, state.status))
                _events.send(FormEvent.Saved("Site updated"))
            } else {
                repository.add(state.name, state.address, state.city, state.status)
                _events.send(FormEvent.Saved("Site \"${state.name.trim()}\" added"))
            }
        }
    }
}
