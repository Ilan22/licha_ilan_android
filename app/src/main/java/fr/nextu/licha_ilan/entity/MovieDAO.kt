package fr.nextu.licha_ilan.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDAO {
    @Query("SELECT * FROM movie")
    fun getAll(): List<Movie>

    @Query("SELECT * FROM movie WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Movie>

    @Query("SELECT * FROM movie WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Movie

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg movies: Movie)

    @Delete
    fun delete(movie: Movie)

    @Query("SELECT * FROM movie")
    fun getFlowData(): Flow<List<Movie>>

    @Query("SELECT * FROM movie WHERE id = :id")
    fun get(id: Int): Movie

    @Query("SELECT * FROM movie WHERE id = :id")
    fun getFlow(id: Int): Flow<Movie>
}