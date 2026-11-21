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
import kz.sitetracker.data.City
import kz.sitetracker.data.SiteStatus
import kz.sitetracker.ui.MAX_ADDRESS
import kz.sitetracker.ui.MAX_SITE_NAME
import kz.sitetracker.ui.components.EmptyState
import kz.sitetracker.ui.components.LoadingBox
import kz.sitetracker.ui.components.OptionChip
import kz.sitetracker.ui.components.ScreenHeader
import kz.sitetracker.ui.viewmodel.AppViewModelProvider
import kz.sitetracker.ui.viewmodel.FormEvent
import kz.sitetracker.ui.viewmodel.SiteFormUiState
import kz.sitetracker.ui.viewmodel.SiteFormViewModel

/** One form for both "New site" and "Edit site" — the ViewModel knows which mode it is in. */
@Composable
fun SiteFormRoute(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SiteFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // After a successful save: show feedback and leave the form (exactly once).
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

    SiteFormScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onCityChange = viewModel::onCityChange,
        onStatusChange = viewModel::onStatusChange,
        onSave = viewModel::save,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun SiteFormScreen(
    state: SiteFormUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (City) -> Unit,
    onStatusChange: (SiteStatus) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()   // keep the Save button visible above the keyboard
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = if (state.isEditing) "Edit site" else "New site", onBack = onBack)

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
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name *") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = {
                        Text(state.nameError ?: "${state.name.length}/$MAX_SITE_NAME")
                    }
                )

                OutlinedTextField(
                    value = state.address,
                    onValueChange = onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Address (optional)") },
                    singleLine = true,
                    supportingText = { Text("${state.address.length}/$MAX_ADDRESS") }
                )

                Text("City", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(City.entries, key = { it.name }) { city ->
                        OptionChip(city.title, city == state.city) { onCityChange(city) }
                    }
                }

                Text("Status", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SiteStatus.entries, key = { it.name }) { status ->
                        OptionChip(status.title, status == state.status) { onStatusChange(status) }
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(if (state.isEditing) "Save changes" else "Create site")
                }
            }
        }
    }
}
