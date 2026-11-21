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
import kz.sitetracker.data.Inspection
import kz.sitetracker.data.InspectionRepository
import kz.sitetracker.data.InspectionResult
import kz.sitetracker.data.SiteRepository
import kz.sitetracker.ui.MAX_INSPECTOR
import kz.sitetracker.ui.MAX_NOTES
import kz.sitetracker.ui.parseDate
import kz.sitetracker.ui.validateInspectionDate
import kz.sitetracker.ui.validateInspector
import kz.sitetracker.ui.validateNotes
import java.time.LocalDate

data class InspectionFormUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isSaving: Boolean = false,
    val siteName: String = "",
    val inspector: String = "",
    val dateText: String = LocalDate.now().toString(),   // "YYYY-MM-DD", today by default
    val result: InspectionResult = InspectionResult.PASSED,
    val notes: String = "",
    val showErrors: Boolean = false
) {
    val inspectorError: String?
        get() = if (inspector.isEmpty() && !showErrors) null else validateInspector(inspector)

    val dateError: String? get() = validateInspectionDate(dateText)

    val notesError: String? get() = if (!showErrors) null else validateNotes(notes, result)

    val isValid: Boolean
        get() = validateInspector(inspector) == null &&
            validateInspectionDate(dateText) == null &&
            validateNotes(notes, result) == null
}

/** Adds an inspection to the site whose id comes as the navigation argument "siteId". */
class InspectionFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val inspectionRepository: InspectionRepository,
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val siteId: Int = checkNotNull(savedStateHandle["siteId"])

    private val _uiState = MutableStateFlow(InspectionFormUiState())
    val uiState: StateFlow<InspectionFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<FormEvent>(Channel.BUFFERED)
    val events: Flow<FormEvent> = _events.receiveAsFlow()

    init {
        // Load the site name for the header (and make sure the site still exists).
        viewModelScope.launch {
            val site = siteRepository.get(siteId)
            _uiState.update {
                if (site == null) it.copy(isLoading = false, notFound = true)
                else it.copy(isLoading = false, siteName = site.name)
            }
        }
    }

    fun onInspectorChange(text: String) = _uiState.update { it.copy(inspector = text.take(MAX_INSPECTOR)) }

    fun onDateChange(text: String) = _uiState.update { it.copy(dateText = text.take(10)) }

    fun onResultChange(result: InspectionResult) = _uiState.update { it.copy(result = result) }

    fun onNotesChange(text: String) = _uiState.update { it.copy(notes = text.take(MAX_NOTES)) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.isValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        val date = parseDate(state.dateText) ?: return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            inspectionRepository.add(
                Inspection(
                    id = 0,
                    siteId = siteId,
                    inspector = state.inspector,
                    date = date,
                    result = state.result,
                    notes = state.notes
                )
            )
            _events.send(FormEvent.Saved("Inspection added: ${state.result.title}"))
        }
    }
}
