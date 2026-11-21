package kz.sitetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import kz.sitetracker.ui.screens.InspectionFormRoute
import kz.sitetracker.ui.screens.SiteDetailRoute
import kz.sitetracker.ui.screens.SiteFormRoute
import kz.sitetracker.ui.screens.SitesRoute

/** All destinations. Routes are strings; arguments are written like parts of a URL. */
sealed class Screen(val route: String) {
    data object Sites : Screen("sites")

    data object SiteDetail : Screen("site/{siteId}") {
        fun create(siteId: Int) = "site/$siteId"
    }

    /** Create (no argument) or edit (siteId given) — one screen, two modes. */
    data object SiteForm : Screen("site_form?siteId={siteId}") {
        fun add() = "site_form"
        fun edit(siteId: Int) = "site_form?siteId=$siteId"
    }

    data object InspectionForm : Screen("inspection_form/{siteId}") {
        fun create(siteId: Int) = "inspection_form/$siteId"
    }
}

@Composable
fun SiteTrackerApp() {
    // rememberNavController keeps the back stack across rotation (it is saved internally).
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // One snackbar host for the whole app: every screen reports "Site added", "Inspection deleted", ...
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentRoute == Screen.Sites.route) {
                FloatingActionButton(onClick = { navController.navigate(Screen.SiteForm.add()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add site")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sites.route,
            modifier = Modifier.padding(padding)
        ) {
            // List screen
            composable(Screen.Sites.route) {
                SitesRoute(onOpenSite = { navController.navigate(Screen.SiteDetail.create(it)) })
            }

            // Detail screen
            composable(
                route = Screen.SiteDetail.route,
                arguments = listOf(navArgument("siteId") { type = NavType.IntType })
            ) {
                SiteDetailRoute(
                    onBack = { navController.popBackStack() },
                    onEditSite = { navController.navigate(Screen.SiteForm.edit(it)) },
                    onAddInspection = { navController.navigate(Screen.InspectionForm.create(it)) },
                    onMessage = showMessage
                )
            }

            // Create / edit site form
            composable(
                route = Screen.SiteForm.route,
                arguments = listOf(navArgument("siteId") {
                    type = NavType.IntType
                    defaultValue = -1          // -1 = create a new site
                })
            ) {
                SiteFormRoute(
                    onBack = { navController.popBackStack() },
                    onMessage = showMessage
                )
            }

            // Add inspection form (additional feature)
            composable(
                route = Screen.InspectionForm.route,
                arguments = listOf(navArgument("siteId") { type = NavType.IntType })
            ) {
                InspectionFormRoute(
                    onBack = { navController.popBackStack() },
                    onMessage = showMessage
                )
            }
        }
    }
}
