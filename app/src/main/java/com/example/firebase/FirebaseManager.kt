package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.database.AppDatabase
import com.example.models.AdvanceEntry
import com.example.models.DutyEntry
import com.example.models.UserProfile
import com.example.models.BinanceRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseManager private constructor(context: Context) {

    private val dbRef: DatabaseReference

    init {
        var initializedApp: FirebaseApp? = null
        try {
            val apps = FirebaseApp.getApps(context)
            initializedApp = if (apps.isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDxqq2PBqrtkTsygyMcIDOvZyx2_-x4QRk")
                    .setApplicationId("1:716525293622:web:880f39084c86180fbb0897")
                    .setDatabaseUrl("https://duty-tracker-pro-default-rtdb.firebaseio.com")
                    .setProjectId("duty-tracker-pro")
                    .setStorageBucket("duty-tracker-pro.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            } else {
                apps[0]
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error initializing Firebase: ${e.message}")
        }

        dbRef = try {
            val db = if (initializedApp != null) {
                FirebaseDatabase.getInstance(initializedApp)
            } else {
                FirebaseDatabase.getInstance()
            }
            db.reference
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting database reference, retrying with explicit options: ${e.message}")
            // Last-resort fallback: force-create a fresh named app with explicit options
            try {
                val fallbackOptions = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDxqq2PBqrtkTsygyMcIDOvZyx2_-x4QRk")
                    .setApplicationId("1:716525293622:web:880f39084c86180fbb0897")
                    .setDatabaseUrl("https://duty-tracker-pro-default-rtdb.firebaseio.com")
                    .setProjectId("duty-tracker-pro")
                    .build()
                val fallbackApp = FirebaseApp.initializeApp(context, fallbackOptions, "fallback_app_${System.currentTimeMillis()}")
                FirebaseDatabase.getInstance(fallbackApp).reference
            } catch (e2: Exception) {
                Log.e("FirebaseManager", "Fallback Firebase init also failed: ${e2.message}")
                // Absolute last resort - this line should be unreachable in practice,
                // but if FirebaseDatabase.getInstance() truly cannot construct a reference,
                // rethrowing here at least gives a clear crash log instead of a silent one.
                throw e2
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Config nodes
    val maintenanceLive = MutableStateFlow(false)
    val maintenanceMessageLive = MutableStateFlow("We're currently performing scheduled maintenance...")
    val maintenanceTimeLive = MutableStateFlow("Approximately 30 minutes")
    val updateRequiredLive = MutableStateFlow(false)
    val updateTitleLive = MutableStateFlow("Update Available!")
    val updateMessageLive = MutableStateFlow("A new version is available.")
    val updateTelegramLinkLive = MutableStateFlow("https://telegram.me/DutyTrackerProapp")
    val updateDownloadLinkLive = MutableStateFlow("https://dutytracker-admin.vercel.app/")
    val rewardStatusLive = MutableStateFlow(true)
    val rewardTitleLive = MutableStateFlow("Unlock 7 Days Free Premium!")
    val rewardMessageLive = MutableStateFlow("Join BOTH our official channels to activate your reward.")
    val rewardLinkTelegramLive = MutableStateFlow("https://telegram.me/DutyTrackerProapp")
    val rewardLinkWhatsAppLive = MutableStateFlow("https://whatsapp.com/channel/0029Vb6SlL01dAw9flibc12I")

    // Target users rate pricing config
    val priceRazorMonthly = MutableStateFlow(5)
    val priceRazor6Months = MutableStateFlow(20)
    val priceRazorYearly = MutableStateFlow(50)
    val pricePaypalMonthly = MutableStateFlow(1)
    val pricePaypalYearly = MutableStateFlow(5)

    fun startListeningToAppConfig() {
        dbRef.child("config/maintenance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maintenanceLive.value = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("config/maintenanceMessage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maintenanceMessageLive.value = snapshot.getValue(String::class.java) ?: "We're currently performing scheduled maintenance..."
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("config/maintenanceTime").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maintenanceTimeLive.value = snapshot.getValue(String::class.java) ?: "Approximately 30 minutes"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("config/forcedUpdate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(Boolean::class.java) ?: false
                updateRequiredLive.value = status
                updateTitleLive.value = snapshot.child("title").getValue(String::class.java) ?: "Update Available!"
                updateMessageLive.value = snapshot.child("message").getValue(String::class.java) ?: "A new version is available."
                updateTelegramLinkLive.value = snapshot.child("telegramLink").getValue(String::class.java) ?: "https://telegram.me/DutyTrackerProapp"
                updateDownloadLinkLive.value = snapshot.child("downloadLink").getValue(String::class.java) ?: "https://dutytracker-admin.vercel.app/"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("config/reward").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rewardStatusLive.value = snapshot.child("status").getValue(Boolean::class.java) ?: true
                rewardTitleLive.value = snapshot.child("title").getValue(String::class.java) ?: "Unlock 7 Days Free Premium!"
                rewardMessageLive.value = snapshot.child("message").getValue(String::class.java) ?: "Join BOTH our official channels to activate your reward."
                rewardLinkTelegramLive.value = snapshot.child("telegram_link").getValue(String::class.java) ?: "https://telegram.me/DutyTrackerProapp"
                rewardLinkWhatsAppLive.value = snapshot.child("whatsapp_link").getValue(String::class.java) ?: "https://whatsapp.com/channel/0029Vb6SlL01dAw9flibc12I"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        dbRef.child("config/prices").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                priceRazorMonthly.value = snapshot.child("razorpay_monthly").getValue(Int::class.java) ?: 5
                priceRazor6Months.value = snapshot.child("razorpay_6months").getValue(Int::class.java) ?: 20
                priceRazorYearly.value = snapshot.child("razorpay_yearly").getValue(Int::class.java) ?: 50
                pricePaypalMonthly.value = snapshot.child("paypal_monthly").getValue(Int::class.java) ?: 1
                pricePaypalYearly.value = snapshot.child("paypal_yearly").getValue(Int::class.java) ?: 5
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Check if account exists
    suspend fun checkAccountExists(uid: String): Boolean {
        return try {
            val snapshot = dbRef.child("u").child(uid).child("profile/name").get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Verify PIN
    suspend fun verifyPin(uid: String, pin: String): Boolean {
        return try {
            val snapshot = dbRef.child("u").child(uid).child("pin").get().await()
            val savedPin = snapshot.getValue(String::class.java)
            if (savedPin == null) {
                // Pin not set, store this input as PIN (auto fallback from JS codebase)
                dbRef.child("u").child(uid).child("pin").setValue(pin).await()
                true
            } else {
                savedPin == pin
            }
        } catch (e: Exception) {
            false
        }
    }

    // Change PIN
    suspend fun changePin(uid: String, pin: String): Boolean {
        return try {
            dbRef.child("u").child(uid).child("pin").setValue(pin).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Create / Sign Up Account
    suspend fun createAccount(
        uid: String,
        name: String,
        email: String,
        pin: String
    ): UserProfile? {
        return try {
            val refCode = generateRandomRefCode()
            val defaultExpiry = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000) // 3 days free trial
            val profile = UserProfile(name, email, "", defaultExpiry, refCode, pin)

            val updates = HashMap<String, Any>()
            updates["u/$uid/profile"] = mapOf("name" to name, "email" to email, "img" to "")
            updates["u/$uid/pin"] = pin
            updates["u/$uid/refCode"] = refCode
            updates["u/$uid/subExp"] = defaultExpiry
            updates["refList/$refCode"] = uid

            dbRef.updateChildren(updates).await()
            profile
        } catch (e: Exception) {
            null
        }
    }

    private fun generateRandomRefCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // Load User Config & Metrics
    suspend fun fetchUserProfile(uid: String): UserProfile? {
        return try {
            val profileSnap = dbRef.child("u").child(uid).child("profile").get().await()
            val subExpSnap = dbRef.child("u").child(uid).child("subExp").get().await()
            val refCodeSnap = dbRef.child("u").child(uid).child("refCode").get().await()
            val pinSnap = dbRef.child("u").child(uid).child("pin").get().await()

            UserProfile(
                name = profileSnap.child("name").getValue(String::class.java) ?: "User",
                email = profileSnap.child("email").getValue(String::class.java) ?: "",
                img = profileSnap.child("img").getValue(String::class.java) ?: "",
                subExp = subExpSnap.getValue(Long::class.java) ?: 0L,
                refCode = refCodeSnap.getValue(String::class.java) ?: "",
                pin = pinSnap.getValue(String::class.java) ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfileAvatar(uid: String, imageUrl: String): Boolean {
        return try {
            dbRef.child("u").child(uid).child("profile").child("img").setValue(imageUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Sync Duty & Advance Data from Firebase into Room DB
    fun syncFirebaseToLocal(uid: String, database: AppDatabase, scope: CoroutineScope) {
        // Synchronize Duties
        dbRef.child("u").child(uid).child("att").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    database.dutyDao().clearAllDuties()
                    for (entrySnap in snapshot.children) {
                        try {
                            val tDate = entrySnap.key ?: continue
                            val status = entrySnap.child("t").getValue(String::class.java) ?: "Present"
                            val ot = entrySnap.child("ot").getValue(Double::class.java) ?: 0.0
                            val late = entrySnap.child("lt").getValue(Int::class.java) ?: 0
                            val shift = entrySnap.child("shift").getValue(String::class.java) ?: "Morning"

                            database.dutyDao().insertDuty(DutyEntry(tDate, status, ot, late, shift))
                        } catch (e: Exception) {
                            Log.e("FirebaseSync", "Error parsing duty: ${e.message}")
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Synchronize Advances
        dbRef.child("u").child(uid).child("adv").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    database.advanceDao().clearAllAdvances()
                    for (entrySnap in snapshot.children) {
                        try {
                            val id = entrySnap.key ?: continue
                            val amt = entrySnap.child("amt").getValue(Double::class.java) ?: 0.0
                            val date = entrySnap.child("date").getValue(String::class.java) ?: ""
                            val by = entrySnap.child("by").getValue(String::class.java) ?: ""
                            val note = entrySnap.child("note").getValue(String::class.java) ?: ""

                            database.advanceDao().insertAdvance(AdvanceEntry(id, amt, date, by, note))
                        } catch (e: Exception) {
                            Log.e("FirebaseSync", "Error parsing advance: ${e.message}")
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Save Attendance data
    suspend fun saveDutyEntryToFirebase(uid: String, date: String, entry: DutyEntry) {
        val payload = mapOf(
            "t" to entry.status,
            "ot" to entry.otHours,
            "lt" to entry.lateMinutes,
            "shift" to entry.shift
        )
        dbRef.child("u").child(uid).child("att").child(date).setValue(payload).await()
    }

    // Delete Attendance data
    suspend fun deleteDutyEntryFromFirebase(uid: String, date: String) {
        dbRef.child("u").child(uid).child("att").child(date).removeValue().await()
    }

    // Save Advance Entry
    suspend fun saveAdvanceEntryToFirebase(uid: String, id: String, entry: AdvanceEntry) {
        val payload = mapOf(
            "amt" to entry.amount,
            "date" to entry.date,
            "by" to entry.contractor,
            "note" to entry.note
        )
        dbRef.child("u").child(uid).child("adv").child(id).setValue(payload).await()
    }

    // Delete Advance
    suspend fun deleteAdvanceEntryFromFirebase(uid: String, id: String) {
        dbRef.child("u").child(uid).child("adv").child(id).removeValue().await()
    }

    // Save check in
    suspend fun saveCheckIn(uid: String, time: String, timestamp: Long, date: String) {
        dbRef.child("u").child(uid).child("checkin").setValue(mapOf(
            "time" to time,
            "timestamp" to timestamp,
            "date" to date
        )).await()
    }

    // Save check out
    suspend fun saveCheckOut(uid: String, time: String, timestamp: Long, date: String) {
        dbRef.child("u").child(uid).child("checkout").setValue(mapOf(
            "time" to time,
            "timestamp" to timestamp,
            "date" to date
        )).await()
    }

    // Get Check in out info
    suspend fun getCheckInOut(uid: String): Pair<String?, String?> {
        return try {
            val checkinSnap = dbRef.child("u").child(uid).child("checkin").get().await()
            val checkoutSnap = dbRef.child("u").child(uid).child("checkout").get().await()
            val checkinDate = checkinSnap.child("date").getValue(String::class.java)
            val checkoutDate = checkoutSnap.child("date").getValue(String::class.java)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

            val checkinTime = if (checkinDate == today) checkinSnap.child("time").getValue(String::class.java) else null
            val checkoutTime = if (checkoutDate == today) checkoutSnap.child("time").getValue(String::class.java) else null

            Pair(checkinTime, checkoutTime)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    // Update settings config
    suspend fun saveConfiguration(
        uid: String,
        currency: String,
        salary: Double,
        otr: Double,
        food: Double,
        pf: Double,
        target: Double
    ) {
        dbRef.child("u").child(uid).child("conf").setValue(mapOf(
            "cur" to currency,
            "sal" to salary,
            "otr" to otr,
            "food" to food,
            "pf" to pf,
            "target" to target
        )).await()
    }

    // Get Settings Config
    suspend fun fetchConfiguration(uid: String): Map<String, Any>? {
        return try {
            val raw = dbRef.child("u").child(uid).child("conf").get().await()
            val map = HashMap<String, Any>()
            for (child in raw.children) {
                map[child.key ?: continue] = child.value ?: continue
            }
            map
        } catch (e: Exception) {
            null
        }
    }

    // Submit Binance payment request
    suspend fun submitBinanceRequest(uid: String, request: BinanceRequest): Boolean {
        return try {
            val key = dbRef.child("binance_payments").child(uid).push().key ?: return false
            val payload = mapOf(
                "uid" to uid,
                "plan" to request.plan,
                "amount" to request.amount,
                "currency" to "INR",
                "txid" to request.txid,
                "screenshot" to request.screenshot,
                "status" to "pending",
                "createdAt" to request.createdAt,
                "planDays" to request.planDays
            )
            dbRef.child("binance_payments").child(uid).child(key).setValue(payload).await()
            // Submit notification
            val notificationPayload = mapOf(
                "title" to "New Binance Payment Request",
                "body" to "Payment of ${request.amount} INR sent for ${request.plan} plan.",
                "userId" to uid,
                "timestamp" to System.currentTimeMillis(),
                "type" to "binance_payment"
            )
            dbRef.child("adminNotifications/all").push().setValue(notificationPayload).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Listen on user's payment Requests status updates
    fun getBinanceRequests(uid: String, callback: (List<BinanceRequest>) -> Unit) {
        dbRef.child("binance_payments").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<BinanceRequest>()
                for (child in snapshot.children) {
                    try {
                        val req = BinanceRequest(
                            id = child.key ?: "",
                            plan = child.child("plan").getValue(String::class.java) ?: "monthly",
                            amount = child.child("amount").getValue(Double::class.java) ?: 5.0,
                            txid = child.child("txid").getValue(String::class.java) ?: "",
                            screenshot = child.child("screenshot").getValue(String::class.java) ?: "",
                            status = child.child("status").getValue(String::class.java) ?: "pending",
                            createdAt = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis(),
                            planDays = child.child("planDays").getValue(Int::class.java) ?: 30
                        )
                        list.add(req)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Try applying friend code
    suspend fun redeemReferralCode(uid: String, currentExp: Long, code: String): String {
        return try {
            val snapshot = dbRef.child("refList").child(code).get().await()
            val refereeUid = snapshot.getValue(String::class.java) ?: return "invalid_code"
            if (refereeUid == uid) return "own_code"

            // Check if already redeemed
            val checkSnapshot = dbRef.child("u").child(uid).child("redeemed").get().await()
            if (checkSnapshot.exists()) return "already_redeemed"

            val targetBase = if (currentExp < System.currentTimeMillis()) System.currentTimeMillis() else currentExp
            val newExp = targetBase + (10L * 24 * 60 * 60 * 1000) // add 10 days

            dbRef.child("u").child(uid).child("subExp").setValue(newExp).await()
            dbRef.child("u").child(uid).child("redeemed").setValue(true).await()

            // Fetch referee expiry and add 10 days too!
            val refereeExpSnap = dbRef.child("u").child(refereeUid).child("subExp").get().await()
            val refereeExp = refereeExpSnap.getValue(Long::class.java) ?: System.currentTimeMillis()
            val refereeBase = if (refereeExp < System.currentTimeMillis()) System.currentTimeMillis() else refereeExp
            dbRef.child("u").child(refereeUid).child("subExp").setValue(refereeBase + (10L * 24 * 60 * 60 * 1000)).await()

            "success"
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }

    // Update subscription expiry
    suspend fun updateSubscriptionExpiry(uid: String, expiryTime: Long) {
        dbRef.child("u").child(uid).child("subExp").setValue(expiryTime).await()
    }

    // Update Avatar image URL in Firebase
    suspend fun updateProfileImage(uid: String, imageUrl: String) {
        dbRef.child("u").child(uid).child("profile/img").setValue(imageUrl).await()
    }

    // Add Subscription purchase history list item
    suspend fun recordPurchaseHistory(uid: String, amount: Double, method: String, plan: String) {
        val payload = mapOf(
            "amount" to amount,
            "currency" to "INR",
            "date" to System.currentTimeMillis(),
            "method" to method,
            "plan" to plan
        )
        dbRef.child("u").child(uid).child("history").push().setValue(payload).await()
    }

    // Get Subscription Purchase lists
    fun fetchPurchaseHistory(uid: String, callback: (List<Map<String, Any>>) -> Unit) {
        dbRef.child("u").child(uid).child("history").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Map<String, Any>>()
                for (childSnap in snapshot.children) {
                    val map = HashMap<String, Any>()
                    map["amount"] = childSnap.child("amount").value ?: 0.0
                    map["currency"] = childSnap.child("currency").value ?: "INR"
                    map["date"] = childSnap.child("date").value ?: System.currentTimeMillis()
                    map["method"] = childSnap.child("method").value ?: ""
                    map["plan"] = childSnap.child("plan").value ?: ""
                    list.add(map)
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
