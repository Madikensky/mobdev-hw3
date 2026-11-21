package kz.sitetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.sitetracker.data.InspectionResult
import kz.sitetracker.ui.MAX_NOTES
import kz.sitetracker.ui.components.EmptyState
import kz.sitetracker.ui.components.LoadingBox
import kz.sitetracker.ui.components.OptionChip
import kz.sitetracker.ui.components.ScreenHeader
import kz.sitetracker.ui.viewmodel.AppViewModelProvider
import kz.sitetracker.ui.viewmodel.FormEvent
import kz.sitetracker.ui.viewmodel.InspectionFormUiState
import kz.sitetracker.ui.viewmodel.InspectionFormViewModel

@Composable
fun InspectionFormRoute(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FormEvent.Saved -> {
                    onMessage(event.message)
                    onBack()
                }
            }
        }
    }

    InspectionFormScreen(
        state = state,
        onInspectorChange = viewModel::onInspectorChange,
        onDateChange = viewModel::onDateChange,
        onResultChange = viewModel::onResultChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::save,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun InspectionFormScreen(
    state: InspectionFormUiState,
    onInspectorChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onResultChange: (InspectionResult) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = "New inspection", onBack = onBack)

        when {
            state.isLoading -> LoadingBox()
            state.notFound -> EmptyState("This site no longer exists")
            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Site: ${state.siteName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = state.inspector,
                    onValueChange = onInspectorChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Inspector *") },
                    singleLine = true,
                    isError = state.inspectorError != null,
                    supportingText = { state.inspectorError?.let { Text(it) } }
                )

                OutlinedTextField(
                    value = state.dateText,
                    onValueChange = onDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    isError = state.dateError != null,
                    supportingText = { Text(state.dateError ?: "Format: YYYY-MM-DD") }
                )

                Text("Result", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(InspectionResult.entries, key = { it.name }) { result ->
                        OptionChip(result.title, result == state.result) { onResultChange(result) }
                    }
                }

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (state.result == InspectionResult.PASSED) "Notes (optional)" else "Notes *")
                    },
                    minLines = 3,
                    maxLines = 5,
                    isError = state.notesError != null,
                    supportingText = {
                        Text(state.notesError ?: "${state.notes.length}/$MAX_NOTES")
                    }
                )

                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Save inspection")
                }
            }
        }
    }
}
