package kz.sitetracker

import android.app.Application
import android.content.Context
import kz.sitetracker.data.InspectionRepository
import kz.sitetracker.data.SiteRepository
import kz.sitetracker.data.local.AppDatabase

/** Tiny hand-written "DI container": builds the database and repositories once per process. */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    val siteRepository = SiteRepository(database.siteDao())
    val inspectionRepository = InspectionRepository(database.inspectionDao())
}

class SiteTrackerApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
