package kz.sitetracker.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kz.sitetracker.AppContainer
import kz.sitetracker.SiteTrackerApplication

/**
 * Tells Android how to build ViewModels that need constructor arguments (repositories).
 * Used as `viewModel(factory = AppViewModelProvider.Factory)` — this is the dependency-injection wiring.
 * createSavedStateHandle() gives each ViewModel the navigation arguments of its screen.
 */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SitesViewModel(container().siteRepository, container().inspectionRepository)
        }
        initializer {
            SiteDetailViewModel(
                createSavedStateHandle(),
                container().siteRepository,
                container().inspectionRepository
            )
        }
        initializer {
            SiteFormViewModel(createSavedStateHandle(), container().siteRepository)
        }
        initializer {
            InspectionFormViewModel(
                createSavedStateHandle(),
                container().inspectionRepository,
                container().siteRepository
            )
        }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SiteTrackerApplication).container
