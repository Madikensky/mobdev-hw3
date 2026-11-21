package kz.sitetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kz.sitetracker.ui.SiteTrackerApp
import kz.sitetracker.ui.theme.SiteTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SiteTrackerTheme {
                SiteTrackerApp()
            }
        }
    }
}
