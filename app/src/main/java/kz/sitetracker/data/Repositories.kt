package kz.sitetracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kz.sitetracker.data.local.InspectionDao
import kz.sitetracker.data.local.SiteDao
import kz.sitetracker.data.local.toDomain
import kz.sitetracker.data.local.toEntity

/**
 * The only way the rest of the app touches site data. ViewModels never see DAOs or
 * database entities — they get domain models and Flows that update by themselves.
 */
class SiteRepository(private val dao: SiteDao) {

    fun observeAll(): Flow<List<Site>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observe(id: Int): Flow<Site?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun get(id: Int): Site? = dao.getById(id)?.toDomain()

    suspend fun add(name: String, address: String, city: City, status: SiteStatus) {
        dao.insert(Site(0, name.trim(), address.trim(), city, status).toEntity())
    }

    suspend fun update(site: Site) {
        dao.update(site.copy(name = site.name.trim(), address = site.address.trim()).toEntity())
    }

    /** Also removes the site's inspections (foreign key with CASCADE). */
    suspend fun delete(id: Int) = dao.deleteById(id)
}

class InspectionRepository(private val dao: InspectionDao) {

    fun observeAll(): Flow<List<Inspection>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeForSite(siteId: Int): Flow<List<Inspection>> =
        dao.observeForSite(siteId).map { rows -> rows.map { it.toDomain() } }

    suspend fun add(inspection: Inspection) {
        dao.insert(
            inspection.copy(
                id = 0,
                inspector = inspection.inspector.trim(),
                notes = inspection.notes.trim()
            ).toEntity()
        )
    }

    suspend fun delete(id: Int) = dao.deleteById(id)
}
