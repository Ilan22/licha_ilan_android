package fr.nextu.licha_ilan

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.nextu.licha_ilan.entity.Movie
import fr.nextu.licha_ilan.entity.MovieDAO

@Database(entities = [Movie::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDAO

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "first_app2.db"
            ).build()
        }
    }
}