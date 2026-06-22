package fuzzy.cozy.com.data

import androidx.room.*

@Dao
interface CafeDao {
    @Query("SELECT * FROM cafes ORDER BY id DESC")
    suspend fun getAll(): List<Cafe>

    @Query("SELECT * FROM cafes WHERE name LIKE :query")
    suspend fun search(query: String): List<Cafe>

    @Insert
    suspend fun insert(cafe: Cafe)

    @Update
    suspend fun update(cafe: Cafe)

    @Delete
    suspend fun delete(cafe: Cafe)

    @Query("DELETE FROM cafes")
    suspend fun deleteAll()

    @Query("SELECT * FROM cafes WHERE id = :id")
    suspend fun getById(id: Int): Cafe?
}