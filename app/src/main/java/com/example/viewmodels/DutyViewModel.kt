package com.example.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.firebase.FirebaseManager
import com.example.models.*
import com.example.repository.DutyRepository
import com.example.utils.PdfGenerator
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

enum class Screen {
    Splash,
    Onboarding,
    Auth,
    PinEntry,
    Home,
    Profile,
    Reports,
    Graph,
    Advance,
    Settings,
    Referral,
    PaymentRequests,
    SubHistory,
    Help,
    Feedback,
    About,
    Legal
}

data class ReportResult(
    val netPayable: Double = 0.0,
    val dutyDays: Double = 0.0,
    val dutyPay: Double = 0.0,
    val foodAllowance: Double = 0.0,
    val overtimePay: Double = 0.0,
    val pfDeduction: Double = 0.0,
    val totalAdvance: Double = 0.0,
    val totalOtHours: Double = 0.0
)

class DutyViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val firebaseManager = FirebaseManager.getInstance(context)
    private val repository = DutyRepository(context, database, firebaseManager)

    // Current Navigation Screen State
    private val _currentScreen = MutableStateFlow(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen

    // Pre-nav state in pop to return easily
    private val screenHistory = mutableListOf<Screen>()

    // Current logged-in user session
    private val _userSession = MutableStateFlow<String?>(null)
    val userSession: StateFlow<String?> = _userSession

    // User Profile Information
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // PIN Verification flows
    private val _pinEntryMode = MutableStateFlow("verify") // verify, set, change
    val pinEntryMode: StateFlow<String> = _pinEntryMode
    private var pinVerifyCallback: ((Boolean) -> Unit)? = null

    // Room local entities observed flow
    val duties: StateFlow<List<DutyEntry>> = repository.allDuties.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val advances: StateFlow<List<AdvanceEntry>> = repository.allAdvances.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Check-in and out timestamps
    private val _checkinTime = MutableStateFlow<String?>(null)
    val checkinTime: StateFlow<String?> = _checkinTime
    private val _checkoutTime = MutableStateFlow<String?>(null)
    val checkoutTime: StateFlow<String?> = _checkoutTime

    // Configuration preferences
    val curSymbol = MutableStateFlow("₹")
    val dailySalary = MutableStateFlow(0.0)
    val foodAllowance = MutableStateFlow(0.0)
    val overtimeRate = MutableStateFlow(0.0)
    val pfDeduction = MutableStateFlow(0.0)
    val monthlyTarget = MutableStateFlow(0.0)

    val autoLockMinutes = MutableStateFlow(5)
    private var lastActivityTime = System.currentTimeMillis()

    // Config nodes linked directly to database live streams
    val maintenanceActive = firebaseManager.maintenanceLive
    val maintenanceMessage = firebaseManager.maintenanceMessageLive
    val maintenanceTime = firebaseManager.maintenanceTimeLive
    val updateRequired = firebaseManager.updateRequiredLive
    val updateTitle = firebaseManager.updateTitleLive
    val updateMessage = firebaseManager.updateMessageLive
    val updateTelegramLink = firebaseManager.updateTelegramLinkLive
    val updateDownloadLink = firebaseManager.updateDownloadLinkLive
    val rewardStatus = firebaseManager.rewardStatusLive
    val rewardTitle = firebaseManager.rewardTitleLive
    val rewardMessage = firebaseManager.rewardMessageLive
    val rewardLinkTelegram = firebaseManager.rewardLinkTelegramLive
    val rewardLinkWhatsApp = firebaseManager.rewardLinkWhatsAppLive

    // Live payment prices stream
    val priceRazorMonthly = firebaseManager.priceRazorMonthly
    val priceRazor6Months = firebaseManager.priceRazor6Months
    val priceRazorYearly = firebaseManager.priceRazorYearly
    val pricePaypalMonthly = firebaseManager.pricePaypalMonthly
    val pricePaypalYearly = firebaseManager.pricePaypalYearly

    // Ads visual state trigger
    private val _adsLoaded = MutableStateFlow(false)
    val adsLoaded: StateFlow<Boolean> = _adsLoaded

    // Current country listing
    val countries = listOf(
        Pair("Afghanistan", "+93"),
        Pair("Albania", "+355"),
        Pair("Algeria", "+213"),
        Pair("Andorra", "+376"),
        Pair("Angola", "+244"),
        Pair("Argentina", "+54"),
        Pair("Armenia", "+374"),
        Pair("Australia", "+61"),
        Pair("Austria", "+43"),
        Pair("Azerbaijan", "+994"),
        Pair("Bahamas", "+1-242"),
        Pair("Bahrain", "+973"),
        Pair("Bangladesh", "+880"),
        Pair("Belarus", "+375"),
        Pair("Belgium", "+32"),
        Pair("Belize", "+501"),
        Pair("Benin", "+229"),
        Pair("Bhutan", "+975"),
        Pair("Bolivia", "+591"),
        Pair("Bosnia and Herzegovina", "+387"),
        Pair("Botswana", "+267"),
        Pair("Brazil", "+55"),
        Pair("Brunei", "+673"),
        Pair("Bulgaria", "+359"),
        Pair("Burkina Faso", "+226"),
        Pair("Burundi", "+257"),
        Pair("Cambodia", "+855"),
        Pair("Cameroon", "+237"),
        Pair("Canada", "+1"),
        Pair("Central African Republic", "+236"),
        Pair("Chad", "+235"),
        Pair("Chile", "+56"),
        Pair("China", "+86"),
        Pair("Colombia", "+57"),
        Pair("Congo", "+242"),
        Pair("Costa Rica", "+506"),
        Pair("Croatia", "+385"),
        Pair("Cuba", "+53"),
        Pair("Cyprus", "+357"),
        Pair("Czech Republic", "+420"),
        Pair("Denmark", "+45"),
        Pair("Djibouti", "+253"),
        Pair("Dominica", "+1-767"),
        Pair("Dominican Republic", "+1-809"),
        Pair("Ecuador", "+593"),
        Pair("Egypt", "+20"),
        Pair("El Salvador", "+503"),
        Pair("Estonia", "+372"),
        Pair("Ethiopia", "+251"),
        Pair("Fiji", "+679"),
        Pair("Finland", "+358"),
        Pair("France", "+33"),
        Pair("Gabon", "+241"),
        Pair("Gambia", "+220"),
        Pair("Georgia", "+995"),
        Pair("Germany", "+49"),
        Pair("Ghana", "+233"),
        Pair("Greece", "+30"),
        Pair("Grenada", "+1-473"),
        Pair("Guatemala", "+502"),
        Pair("Guinea", "+224"),
        Pair("Guyana", "+592"),
        Pair("Haiti", "+509"),
        Pair("Honduras", "+504"),
        Pair("Hungary", "+36"),
        Pair("Iceland", "+354"),
        Pair("India", "+91"),
        Pair("Indonesia", "+62"),
        Pair("Iran", "+98"),
        Pair("Iraq", "+964"),
        Pair("Ireland", "+353"),
        Pair("Israel", "+972"),
        Pair("Italy", "+39"),
        Pair("Jamaica", "+1-876"),
        Pair("Japan", "+81"),
        Pair("Jordan", "+962"),
        Pair("Kazakhstan", "+7"),
        Pair("Kenya", "+254"),
        Pair("Kuwait", "+965"),
        Pair("Kyrgyzstan", "+996"),
        Pair("Laos", "+856"),
        Pair("Latvia", "+371"),
        Pair("Lebanon", "+961"),
        Pair("Lesotho", "+266"),
        Pair("Liberia", "+231"),
        Pair("Libya", "+218"),
        Pair("Liechtenstein", "+423"),
        Pair("Lithuania", "+370"),
        Pair("Luxembourg", "+352"),
        Pair("Macedonia", "+389"),
        Pair("Madagascar", "+261"),
        Pair("Malawi", "+265"),
        Pair("Malaysia", "+60"),
        Pair("Maldives", "+960"),
        Pair("Mali", "+223"),
        Pair("Malta", "+356"),
        Pair("Mauritania", "+222"),
        Pair("Mauritius", "+230"),
        Pair("Mexico", "+52"),
        Pair("Moldova", "+373"),
        Pair("Monaco", "+377"),
        Pair("Mongolia", "+976"),
        Pair("Montenegro", "+382"),
        Pair("Morocco", "+212"),
        Pair("Mozambique", "+258"),
        Pair("Myanmar", "+95"),
        Pair("Namibia", "+264"),
        Pair("Nepal", "+977"),
        Pair("Netherlands", "+31"),
        Pair("New Zealand", "+64"),
        Pair("Nicaragua", "+505"),
        Pair("Niger", "+227"),
        Pair("Nigeria", "+234"),
        Pair("Norway", "+47"),
        Pair("Oman", "+968"),
        Pair("Pakistan", "+20"),
        Pair("Palestine", "+970"),
        Pair("Panama", "+507"),
        Pair("Papua New Guinea", "+675"),
        Pair("Paraguay", "+595"),
        Pair("Peru", "+51"),
        Pair("Philippines", "+63"),
        Pair("Poland", "+48"),
        Pair("Portugal", "+351"),
        Pair("Qatar", "+974"),
        Pair("Romania", "+40"),
        Pair("Russia", "+7"),
        Pair("Rwanda", "+250"),
        Pair("Saudi Arabia", "+966"),
        Pair("Senegal", "+221"),
        Pair("Serbia", "+381"),
        Pair("Singapore", "+65"),
        Pair("Slovakia", "+421"),
        Pair("Slovenia", "+386"),
        Pair("South Africa", "+27"),
        Pair("South Korea", "+82"),
        Pair("Spain", "+34"),
        Pair("Sri Lanka", "+94"),
        Pair("Sudan", "+249"),
        Pair("Sweden", "+46"),
        Pair("Switzerland", "+41"),
        Pair("Syria", "+963"),
        Pair("Taiwan", "+886"),
        Pair("Tajikistan", "+992"),
        Pair("Tanzania", "+255"),
        Pair("Thailand", "+66"),
        Pair("Togo", "+228"),
        Pair("Tunisia", "+216"),
        Pair("Turkey", "+90"),
        Pair("Uganda", "+256"),
        Pair("Ukraine", "+380"),
        Pair("UAE", "+971"),
        Pair("UK", "+44"),
        Pair("USA", "+1"),
        Pair("Uruguay", "+598"),
        Pair("Uzbekistan", "+998"),
        Pair("Venezuela", "+58"),
        Pair("Vietnam", "+84"),
        Pair("Yemen", "+967"),
        Pair("Zambia", "+260"),
        Pair("Zimbabwe", "+263")
    )

    // Sub history list
    private val _purchaseHistory = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val purchaseHistory: StateFlow<List<Map<String, Any>>> = _purchaseHistory

    // Binance requests list
    private val _binanceRequests = MutableStateFlow<List<BinanceRequest>>(emptyList())
    val binanceRequests: StateFlow<List<BinanceRequest>> = _binanceRequests

    // App localization setting state
    val appLanguage = MutableStateFlow("en")

    init {
        firebaseManager.startListeningToAppConfig()
        
        // Auto-load session if recorded in SharedPreferences
        val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
        val savedUid = prefs.getString("dt_id", null)
        val savedLang = prefs.getString("app_lang", "en")
        val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        _savedPhone.value = prefs.getString("dt_phone", "") ?: ""
        _savedCc.value = prefs.getString("dt_cc", "+91") ?: "+91"
        appLanguage.value = savedLang ?: "en"
        autoLockMinutes.value = prefs.getInt("auto_lock_minutes", 5)

        viewModelScope.launch {
            delay(1500) // Delay to show Splash screen
            if (maintenanceActive.value) {
                // screen remains blocked inside Splash/Maintenance
            } else if (savedUid != null) {
                _userSession.value = savedUid
                syncAndLoadSession(savedUid)
            } else if (!hasSeenOnboarding) {
                _currentScreen.value = Screen.Onboarding
            } else {
                _currentScreen.value = Screen.Auth
            }
        }

        // Configure ad visibility: Show ads on all main screens (Free and Ad-Supported!)
        viewModelScope.launch {
            _currentScreen.collect { screen ->
                _adsLoaded.value = screen != Screen.Splash && screen != Screen.Auth
            }
        }

        // Monitor activity for security PIN Entry locking
        startActivityMonitor()
    }

    fun startListeningToSessionUpdates(uid: String) {
        viewModelScope.launch {
            // Load Binance status
            firebaseManager.getBinanceRequests(uid) { reqs ->
                _binanceRequests.value = reqs
                // Check if any request was approved, and triggers premium sync
                reqs.forEach { req ->
                    if (req.status == "approved") {
                        viewModelScope.launch {
                            syncAndLoadSession(uid)
                        }
                    }
                }
            }

            // Load Purchase history
            firebaseManager.fetchPurchaseHistory(uid) { history ->
                _purchaseHistory.value = history
            }
        }
    }

    private suspend fun syncAndLoadSession(uid: String) {
        val profile = firebaseManager.fetchUserProfile(uid)
        if (profile != null) {
            _userProfile.value = profile
            
            // Sync Firebase database values down to local SQLite Room
            repository.clearLocalCache()
            firebaseManager.syncFirebaseToLocal(uid, database, viewModelScope)

            // Dynamic session sync
            startListeningToSessionUpdates(uid)

            // Fetch settings configuration
            val config = firebaseManager.fetchConfiguration(uid)
            if (config != null) {
                curSymbol.value = config["cur"] as? String ?: "₹"
                dailySalary.value = (config["sal"] as? Number)?.toDouble() ?: 0.0
                overtimeRate.value = (config["otr"] as? Number)?.toDouble() ?: 0.0
                foodAllowance.value = (config["food"] as? Number)?.toDouble() ?: 0.0
                pfDeduction.value = (config["pf"] as? Number)?.toDouble() ?: 0.0
                monthlyTarget.value = (config["target"] as? Number)?.toDouble() ?: 0.0
            }

            // Sync Check-in check-out timestamps
            val checkInOut = firebaseManager.getCheckInOut(uid)
            _checkinTime.value = checkInOut.first
            _checkoutTime.value = checkInOut.second

            // Check if PIN security setup is active
            if (profile.pin.isNotEmpty()) {
                _pinEntryMode.value = "verify"
                _currentScreen.value = Screen.PinEntry
            } else {
                _currentScreen.value = Screen.Home
            }
        } else {
            // Profile entry missing or broken fallback
            _currentScreen.value = Screen.Auth
        }
    }

    // Nav Helpers
    fun completeOnboarding() {
        val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        _currentScreen.value = Screen.Auth
    }

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Home) {
            screenHistory.clear()
        } else {
            screenHistory.add(_currentScreen.value)
        }
        _currentScreen.value = screen
        recordActivity()
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            _currentScreen.value = Screen.Home
        }
        recordActivity()
    }

    fun isUserPremium(): Boolean {
        return true
    }

    // Auth Flows
    fun login(cc: String, phone: String, pin: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val uid = cc + phone
            val exists = firebaseManager.checkAccountExists(uid)
            if (!exists) {
                onFinished("Account does not exist. Please sign up first.")
                return@launch
            }

            val verified = firebaseManager.verifyPin(uid, pin)
            if (verified) {
                _userSession.value = uid
                val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("dt_id", uid).putString("dt_cc", cc).putString("dt_phone", phone).apply()
                
                syncAndLoadSession(uid)
                onFinished("success")
            } else {
                onFinished("Incorrect PIN entered.")
            }
        }
    }

    fun signUp(cc: String, phone: String, name: String, email: String, pin: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val uid = cc + phone
            val exists = firebaseManager.checkAccountExists(uid)
            if (exists) {
                onFinished("Account already exists under this phone number.")
                return@launch
            }

            val profile = firebaseManager.createAccount(uid, name, email, pin)
            if (profile != null) {
                _userSession.value = uid
                _userProfile.value = profile
                
                val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("dt_id", uid).putString("dt_cc", cc).putString("dt_phone", phone).apply()
                
                syncAndLoadSession(uid)
                onFinished("success")
            } else {
                onFinished("An error occurred. Check connectivity and try again.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _userSession.value = null
            _userProfile.value = null
            screenHistory.clear()
            repository.clearLocalCache()
            
            val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("dt_id").apply()
            
            _currentScreen.value = Screen.Auth
        }
    }

    // Check PIN verified block
    fun setPinVerifyFlow(mode: String, callback: (Boolean) -> Unit) {
        _pinEntryMode.value = mode
        pinVerifyCallback = callback
        _currentScreen.value = Screen.PinEntry
    }

    fun submitPin(pin: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            if (_pinEntryMode.value == "verify") {
                val match = _userProfile.value?.pin == pin
                if (match) {
                    _currentScreen.value = Screen.Home
                    pinVerifyCallback?.invoke(true)
                    pinVerifyCallback = null
                    onResult("success")
                } else {
                    onResult("incorrect")
                }
            } else if (_pinEntryMode.value == "set" || _pinEntryMode.value == "change") {
                val success = firebaseManager.changePin(uid, pin)
                if (success) {
                    val currentProfile = _userProfile.value
                    if (currentProfile != null) {
                        _userProfile.value = currentProfile.copy(pin = pin)
                    }
                    _currentScreen.value = Screen.Home
                    onResult("success_set")
                } else {
                    onResult("failed_to_save")
                }
            }
        }
    }

    // Inactivity Lock Timer Security Flow
    private fun startActivityMonitor() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(30000) // check every 30 seconds
                val inactiveMs = System.currentTimeMillis() - lastActivityTime
                val thresholdMs = autoLockMinutes.value * 60 * 1000
                if (inactiveMs > thresholdMs && _currentScreen.value != Screen.Splash && _currentScreen.value != Screen.Auth && _currentScreen.value != Screen.PinEntry) {
                    if (_userProfile.value?.pin?.isNotEmpty() == true) {
                        _pinEntryMode.value = "verify"
                        _currentScreen.value = Screen.PinEntry
                        Log.d("AppSecurity", "App locked due to inactivity")
                    }
                }
            }
        }
    }

    fun recordActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun updateAutoLock(minutes: Int) {
        autoLockMinutes.value = minutes
        val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("auto_lock_minutes", minutes).apply()
    }

    // Localizations
    fun changeLocalization(lang: String) {
        appLanguage.value = lang
        val prefs = context.getSharedPreferences("duty_tracker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_lang", lang).apply()
    }

    // Attendance mark actions
    fun markAttendance(date: String, status: String, otHours: Double, lateMinutes: Int, shift: String) {
        viewModelScope.launch {
            val entry = DutyEntry(date, status, otHours, lateMinutes, shift)
            repository.saveDuty(_userSession.value, entry)
        }
    }

    fun deleteAttendance(date: String) {
        viewModelScope.launch {
            repository.deleteDuty(_userSession.value, date)
        }
    }

    // Checkin checkout native commands
    fun doCheckIn() {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            val now = Date()
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)
            val dateString = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(now)
            _checkinTime.value = timeString
            firebaseManager.saveCheckIn(uid, timeString, now.time, dateString)
        }
    }

    fun doCheckOut() {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            val now = Date()
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)
            val dateString = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(now)
            _checkoutTime.value = timeString
            firebaseManager.saveCheckOut(uid, timeString, now.time, dateString)
        }
    }

    // Earn rewards free premium days channel subscription
    fun claimRewardDays() {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            val base = if (isUserPremium()) _userProfile.value?.subExp ?: System.currentTimeMillis() else System.currentTimeMillis()
            val addition = 7L * 24 * 60 * 60 * 1000 // 7 days of premium reward
            val targetExpiry = base + addition
            
            firebaseManager.updateSubscriptionExpiry(uid, targetExpiry)
            firebaseManager.recordPurchaseHistory(uid, 0.0, "Channel Reward Free Premium", "Reward Promo")
            
            // Record activation locally
            val snap = _userProfile.value
            if (snap != null) {
                _userProfile.value = snap.copy(subExp = targetExpiry)
            }
        }
    }

    // Update settings configuration
    fun updateRatesConf(currency: String, salary: Double, otr: Double, food: Double, pf: Double, target: Double) {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            curSymbol.value = currency
            dailySalary.value = salary
            overtimeRate.value = otr
            foodAllowance.value = food
            pfDeduction.value = pf
            monthlyTarget.value = target

            firebaseManager.saveConfiguration(uid, currency, salary, otr, food, pf, target)
        }
    }

    // Advances money triggers
    fun addAdvanceCash(amount: Double, date: String, contractor: String, note: String) {
        viewModelScope.launch {
            val entry = AdvanceEntry(
                id = UUID.randomUUID().toString(),
                amount = amount,
                date = date,
                contractor = contractor,
                note = note
            )
            repository.saveAdvance(_userSession.value, entry.id, entry)
        }
    }

    fun removeAdvanceCash(id: String) {
        viewModelScope.launch {
            repository.deleteAdvance(_userSession.value, id)
        }
    }

    // Binance custom submission
    fun submitBinancePay(plan: String, txid: String, screenshotUrl: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val uid = _userSession.value
            if (uid == null) {
                onFinished(false)
                return@launch
            }
            val amount = if (plan == "monthly") dailySalary.value else if (plan == "6months") 20.0 else 50.0
            val days = if (plan == "monthly") 30 else if (plan == "6months") 180 else 365
            val request = BinanceRequest(
                plan = plan,
                amount = amount,
                txid = txid,
                screenshot = screenshotUrl,
                status = "pending",
                planDays = days
            )
            val result = firebaseManager.submitBinanceRequest(uid, request)
            onFinished(result)
        }
    }

    // Apply Referral Code
    fun applyReferral(code: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val uid = _userSession.value
            if (uid == null) {
                onFinished("Please login first")
                return@launch
            }
            val base = if (isUserPremium()) _userProfile.value?.subExp ?: System.currentTimeMillis() else System.currentTimeMillis()
            val result = firebaseManager.redeemReferralCode(uid, base, code)
            if (result == "success") {
                // Update profile subExp local UI state
                val syncProfile = firebaseManager.fetchUserProfile(uid)
                if (syncProfile != null) {
                    _userProfile.value = syncProfile
                }
            }
            onFinished(result)
        }
    }

    // Restore purchases from Play Store
    fun handlePlayStorePurchaseSuccess(purchase: Purchase) {
        viewModelScope.launch {
            val uid = _userSession.value ?: return@launch
            val productId = purchase.products.firstOrNull() ?: "premium_monthly"
            val days = if (productId == "premium_yearly") 365 else 30
            val base = if (isUserPremium()) _userProfile.value?.subExp ?: System.currentTimeMillis() else System.currentTimeMillis()
            val targetExpiry = base + (days.toLong() * 24 * 60 * 60 * 1000)

            firebaseManager.updateSubscriptionExpiry(uid, targetExpiry)
            firebaseManager.recordPurchaseHistory(uid, if (productId == "premium_yearly") 299.0 else 29.0, "Google Play Billing", productId)

            val currentProfile = _userProfile.value
            if (currentProfile != null) {
                _userProfile.value = currentProfile.copy(subExp = targetExpiry)
            }
        }
    }

    // Reports statistics generator
    fun generateReportsData(startDateStr: String, endDateStr: String): ReportResult {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sDate = try { sdf.parse(startDateStr) } catch(e: Exception) { Date(0) }
        val eDate = try { sdf.parse(endDateStr) } catch(e: Exception) { Date() }

        var dutyDays = 0.0
        var dutyPay = 0.0
        var foodPay = 0.0
        var otPay = 0.0
        var totalOtHours = 0.0

        val currentDuties = duties.value
        for (duty in currentDuties) {
            val dateObj = try { sdf.parse(duty.date) } catch(e: Exception) { null } ?: continue
            // Check if bounds inside selected dates
            if (dateObj.time in sDate.time .. eDate.time) {
                totalOtHours += duty.otHours
                if (duty.status == "Half Day") {
                    dutyDays += 0.5
                    dutyPay += (dailySalary.value / 2.0)
                    foodPay += (foodAllowance.value / 2.0)
                } else if (listOf("Present", "Holiday", "Sick", "Holiday").contains(duty.status)) {
                    dutyDays += 1.0
                    dutyPay += dailySalary.value
                    foodPay += foodAllowance.value
                }
                otPay += (duty.otHours * overtimeRate.value)
            }
        }

        var totalAdvance = 0.0
        val currentAdvances = advances.value
        for (adv in currentAdvances) {
            val dateObj = try { sdf.parse(adv.date) } catch(e: Exception) { null } ?: continue
            if (dateObj.time in sDate.time .. eDate.time) {
                totalAdvance += adv.amount
            }
        }

        val netPayable = (dutyPay + foodPay + otPay) - pfDeduction.value - totalAdvance

        return ReportResult(
            netPayable = netPayable,
            dutyDays = dutyDays,
            dutyPay = dutyPay,
            foodAllowance = foodPay,
            overtimePay = otPay,
            pfDeduction = pfDeduction.value,
            totalAdvance = totalAdvance,
            totalOtHours = totalOtHours
        )
    }

    // Export PDF Trigger
    fun exportReportPdf(startDateStr: String, endDateStr: String, onFinished: (File?) -> Unit) {
        val res = generateReportsData(startDateStr, endDateStr)
        val expFile = PdfGenerator.generateSalarySlip(
            context = context,
            fileName = "SalarySlip_${System.currentTimeMillis()}.pdf",
            userName = _userProfile.value?.name ?: "User",
            startDate = startDateStr,
            endDate = endDateStr,
            dutyDays = res.dutyDays,
            dutyPay = res.dutyPay,
            overtimePay = res.overtimePay,
            foodAllowance = res.foodAllowance,
            pfDeduction = res.pfDeduction,
            advance = res.totalAdvance,
            netPayable = res.netPayable,
            currencySymbol = curSymbol.value
        )
        onFinished(expFile)
    }

    fun uploadProfileAvatar(context: Context, fileUri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val bytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
                if (bytes == null) {
                    viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                    return@launch
                }

                val mediaType = "image/jpeg".toMediaTypeOrNull()
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "avatar.jpg", RequestBody.create(mediaType, bytes))
                    .build()

                val apiKey = "c762b5315ee9263c36ed04156b0ff758"
                val request = Request.Builder()
                    .url("https://api.imgbb.com/1/upload?key=$apiKey")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            if (response.isSuccessful) {
                                val bodyStr = response.body?.string()
                                if (bodyStr != null) {
                                    val rootJson = JSONObject(bodyStr)
                                    val dataJson = rootJson.getJSONObject("data")
                                    val imageUrl = dataJson.getString("url")
                                    
                                    viewModelScope.launch(Dispatchers.Main) {
                                        val uid = _userSession.value
                                        if (uid != null) {
                                            viewModelScope.launch(Dispatchers.IO) {
                                                val firebaseSuccess = firebaseManager.updateProfileAvatar(uid, imageUrl)
                                                if (firebaseSuccess) {
                                                    viewModelScope.launch(Dispatchers.Main) {
                                                        val currentProfile = _userProfile.value
                                                        if (currentProfile != null) {
                                                            _userProfile.value = currentProfile.copy(img = imageUrl)
                                                        }
                                                        onResult(true)
                                                    }
                                                } else {
                                                    viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                                                }
                                            }
                                        } else {
                                            onResult(false)
                                        }
                                    }
                                } else {
                                    viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                                }
                            } else {
                                viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            viewModelScope.launch(Dispatchers.Main) { onResult(false) }
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch(Dispatchers.Main) { onResult(false) }
            }
        }
    }
}
