package kz.sitetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

@Database(
    entities = [SiteEntity::class, InspectionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun inspectionDao(): InspectionDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "site_tracker.db")
                .addCallback(FirstRunSeed)
                .build()
    }
}

/**
 * Runs exactly once — when the database file is created on the very first launch — so a new user
 * starts with the same sample data as the web version of Site-Tracker instead of an empty screen.
 * After that every change comes only from the user through the UI.
 */
private object FirstRunSeed : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val sites = listOf(
            // id, name, address, city, status
            arrayOf<Any?>(1, "Central Tower", "12 Abay Ave", "ALMATY", "ACTIVE"),
            arrayOf<Any?>(2, "North Plant", "5 Lenin St", "ASTANA", "ACTIVE"),
            arrayOf<Any?>(3, "Southern Hub", "41 Tauke Khan Ave", "SHYMKENT", "UNDER_CONSTRUCTION"),
            arrayOf<Any?>(4, "Old Depot", "7 Zhandosov St", "ALMATY", "DECOMMISSIONED"),
            arrayOf<Any?>(5, "Roof Antenna 17", "", "KARAGANDA", "INACTIVE")
        )
        sites.forEach {
            db.execSQL("INSERT INTO sites (id, name, address, city, status) VALUES (?, ?, ?, ?, ?)", it)
        }

        val today = LocalDate.now()
        val inspections = listOf(
            // siteId, inspector, days ago, result, notes
            arrayOf<Any?>(1, "Aigerim S.", today.minusDays(95).toEpochDay(), "PASSED", "All systems normal."),
            arrayOf<Any?>(1, "Daniyar K.", today.minusDays(28).toEpochDay(), "NEEDS_ATTENTION", "Router fans are noisy; replacement scheduled."),
            arrayOf<Any?>(2, "Aigerim S.", today.minusDays(43).toEpochDay(), "PASSED", ""),
            arrayOf<Any?>(3, "Timur B.", today.minusDays(20).toEpochDay(), "FAILED", "Grounding does not meet requirements.")
        )
        inspections.forEach {
            db.execSQL(
                "INSERT INTO inspections (siteId, inspector, epochDay, result, notes) VALUES (?, ?, ?, ?, ?)",
                it
            )
        }
    }
}
