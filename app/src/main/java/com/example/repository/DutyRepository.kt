package com.example.repository

import android.content.Context
import com.example.database.AppDatabase
import com.example.firebase.FirebaseManager
import com.example.models.AdvanceEntry
import com.example.models.DutyEntry
import kotlinx.coroutines.flow.Flow

class DutyRepository(
    context: Context,
    private val database: AppDatabase,
    private val firebaseManager: FirebaseManager
) {
    val allDuties: Flow<List<DutyEntry>> = database.dutyDao().getAllDuties()
    val allAdvances: Flow<List<AdvanceEntry>> = database.advanceDao().getAllAdvances()

    suspend fun saveDuty(uid: String?, entry: DutyEntry) {
        // Write to local database Room instantly
        database.dutyDao().insertDuty(entry)
        
        // Write to Firebase if authenticated
        if (uid != null) {
            try {
                firebaseManager.saveDutyEntryToFirebase(uid, entry.date, entry)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteDuty(uid: String?, date: String) {
        // Delete from local
        database.dutyDao().deleteDuty(date)
        
        // Delete from Firebase
        if (uid != null) {
            try {
                firebaseManager.deleteDutyEntryFromFirebase(uid, date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveAdvance(uid: String?, id: String, entry: AdvanceEntry) {
        // Local
        database.advanceDao().insertAdvance(entry)
        
        // Remote
        if (uid != null) {
            try {
                firebaseManager.saveAdvanceEntryToFirebase(uid, id, entry)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteAdvance(uid: String?, id: String) {
        database.advanceDao().deleteAdvance(id)
        if (uid != null) {
            try {
                firebaseManager.deleteAdvanceEntryFromFirebase(uid, id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun clearLocalCache() {
        database.dutyDao().clearAllDuties()
        database.advanceDao().clearAllAdvances()
    }
}
