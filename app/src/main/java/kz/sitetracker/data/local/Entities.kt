package kz.sitetracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kz.sitetracker.data.City
import kz.sitetracker.data.Inspection
import kz.sitetracker.data.InspectionResult
import kz.sitetracker.data.Site
import kz.sitetracker.data.SiteStatus
import java.time.LocalDate

/**
 * Database row for a site. Room stores only simple types, so enums are saved by name
 * (no TypeConverters needed).
 */
@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val city: String,
    val status: String
)

/**
 * Database row for an inspection. `siteId` is a foreign key to `sites.id`;
 * CASCADE means that deleting a site also deletes all of its inspections.
 * The date is stored as an epoch day (days since 1970-01-01).
 */
@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(
            entity = SiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["siteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId")]
)
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteId: Int,
    val inspector: String,
    val epochDay: Long,
    val result: String,
    val notes: String
)

// ---- mappers: database model <-> domain model ----

fun SiteEntity.toDomain() = Site(
    id = id,
    name = name,
    address = address,
    city = City.fromName(city),
    status = SiteStatus.fromName(status)
)

fun Site.toEntity() = SiteEntity(
    id = id,
    name = name,
    address = address,
    city = city.name,
    status = status.name
)

fun InspectionEntity.toDomain() = Inspection(
    id = id,
    siteId = siteId,
    inspector = inspector,
    date = LocalDate.ofEpochDay(epochDay),
    result = InspectionResult.fromName(result),
    notes = notes
)

fun Inspection.toEntity() = InspectionEntity(
    id = id,
    siteId = siteId,
    inspector = inspector,
    epochDay = date.toEpochDay(),
    result = result.name,
    notes = notes
)
