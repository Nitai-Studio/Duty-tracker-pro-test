package com.example.database

import android.content.Context
import androidx.room.*
import com.example.models.AdvanceEntry
import com.example.models.DutyEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyDao {
    @Query("SELECT * FROM duty_entries")
    fun getAllDuties(): Flow<List<DutyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuty(duty: DutyEntry)

    @Query("DELETE FROM duty_entries WHERE date = :date")
    suspend fun deleteDuty(date: String)

    @Query("DELETE FROM duty_entries")
    suspend fun clearAllDuties()
}

@Dao
interface AdvanceDao {
    @Query("SELECT * FROM advance_entries ORDER BY date DESC")
    fun getAllAdvances(): Flow<List<AdvanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvanceEntry)

    @Query("DELETE FROM advance_entries WHERE id = :id")
    suspend fun deleteAdvance(id: String)

    @Query("DELETE FROM advance_entries")
    suspend fun clearAllAdvances()
}

@Database(entities = [DutyEntry::class, AdvanceEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dutyDao(): DutyDao
    abstract fun advanceDao(): AdvanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "duty_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
