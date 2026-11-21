package kz.sitetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {

    // READ — returning Flow makes Room re-emit automatically whenever the table changes
    @Query("SELECT * FROM sites ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE id = :id")
    fun observeById(id: Int): Flow<SiteEntity?>

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getById(id: Int): SiteEntity?

    // CREATE
    @Insert
    suspend fun insert(site: SiteEntity): Long

    // UPDATE
    @Update
    suspend fun update(site: SiteEntity)

    // DELETE (inspections of this site are removed by the foreign key CASCADE)
    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface InspectionDao {

    @Query("SELECT * FROM inspections ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE siteId = :siteId ORDER BY epochDay DESC, id DESC")
    fun observeForSite(siteId: Int): Flow<List<InspectionEntity>>

    @Insert
    suspend fun insert(inspection: InspectionEntity): Long

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteById(id: Int)
}
