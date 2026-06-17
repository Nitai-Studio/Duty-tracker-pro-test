package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duty_entries")
data class DutyEntry(
    @PrimaryKey val date: String, // format "YYYY-MM-DD"
    val status: String, // Present, Half Day, Leave, Sick, Holiday, Off
    val otHours: Double = 0.0,
    val lateMinutes: Int = 0,
    val shift: String = "Morning" // Morning, Evening, Night
)

@Entity(tableName = "advance_entries")
data class AdvanceEntry(
    @PrimaryKey val id: String,
    val amount: Double,
    val date: String, // format "YYYY-MM-DD"
    val contractor: String = "",
    val note: String = ""
)

data class UserProfile(
    val name: String = "User",
    val email: String = "",
    val img: String = "",
    val subExp: Long = 0L,
    val refCode: String = "",
    val pin: String = ""
)

data class BinanceRequest(
    val id: String = "",
    val plan: String = "monthly",
    val amount: Double = 5.0,
    val txid: String = "",
    val screenshot: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val planDays: Int = 30
)

data class InAppNotification(
    val title: String = "",
    val message: String = "",
    val image: String = "",
    val url: String = ""
)
