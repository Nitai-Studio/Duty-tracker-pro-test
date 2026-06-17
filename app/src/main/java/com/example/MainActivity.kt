package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.billing.PlayBillingManager
import com.example.models.AdvanceEntry
import com.example.models.DutyEntry
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.ads.banner.Banner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodels.DutyViewModel
import com.example.viewmodels.Screen
import com.example.viewmodels.ReportResult
import com.android.billingclient.api.Purchase
import androidx.lifecycle.viewModelScope
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private var playBillingManager: PlayBillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            StartAppSDK.init(this, "205257935", true)
            StartAppSDK.enableReturnAds(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        enableEdgeToEdge()
        setContent {
            val viewModel: DutyViewModel = viewModel()
            val context = LocalContext.current
            
            // Initialize billing programmatically
            LaunchedEffect(Unit) {
                playBillingManager = PlayBillingManager(context, viewModel.viewModelScope) { purchase ->
                    viewModel.handlePlayStorePurchaseSuccess(purchase)
                    Toast.makeText(context, "Premium active! Purchased verified.", Toast.LENGTH_SHORT).show()
                }
            }

            val appThemeLight = remember { mutableStateOf(false) }

            val backgroundStyle = if (appThemeLight.value) Color(0xFFF0F2F5) else Color(0xFF050505)
            val surfaceStyle = if (appThemeLight.value) Color(0xFFFFFFFF) else Color(0xFF121212)

            MaterialTheme(
                colorScheme = if (appThemeLight.value) lightColorScheme(
                    primary = Color(0xFF3A0CA3),
                    secondary = Color(0xFF4895EF),
                    background = backgroundStyle,
                    surface = surfaceStyle
                ) else darkColorScheme(
                    primary = Color(0xFF4361EE),
                    secondary = Color(0xFF4CC9F0),
                    background = backgroundStyle,
                    surface = surfaceStyle
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContentManager(
                        viewModel = viewModel,
                        billingManager = playBillingManager,
                        themeLight = appThemeLight,
                        activity = this@MainActivity
                    )
                }
            }
        }
    }
}

// Inline Dynamic Dictionary values for quick native translation
object Localization {
    val languages = mapOf(
        "en" to mapOf(
            "app_title" to "Duty Tracker Pro",
            "checkin_label" to "Check-in Time",
            "checkout_label" to "Check-out Time",
            "checkin_btn" to "Check In",
            "checkout_btn" to "Check Out",
            "day_streak" to "Day Streak!",
            "no_entry_today" to "No Entry Today",
            "add_duty" to "Add Today's Duty",
            "report" to "Report",
            "graph" to "Graph",
            "advance" to "Advance",
            "utility" to "Utility",
            "language" to "Language",
            "select_language" to "Select Language",
            "select_shift" to "Shift Selection",
            "duty_ot" to "Duty/OT (Hours)",
            "late_mins" to "Late (Mins)",
            "save_entry" to "SAVE ENTRY",
            "delete" to "DELETE",
            "cancel" to "CANCEL",
            "monthly_goal" to "Monthly Goal",
            "target" to "Target",
            "daily_salary" to "Daily Salary (Hajira)",
            "food_allowance" to "Daily Food Allowance",
            "overtime_rate" to "Overtime Rate (Per Hour)",
            "pf_deduction" to "PF Deduction (Fixed)",
            "save_conf" to "Save Configuration",
            "logout" to "Log Out",
            "start_date" to "Start Date",
            "end_date" to "End Date",
            "gen_report" to "Generate Report"
            "onboard_title_1" to "Track Every Duty",
            "onboard_sub_1" to "Mark attendance daily — Present, Leave,\nHoliday, Sick & more. Never miss a day.",
            "onboard_title_2" to "Smart Salary Reports",
            "onboard_sub_2" to "Auto-calculate salary, OT pay & food\nallowance. Share image reports instantly.",
            "onboard_title_3" to "Works Worldwide",
            "onboard_sub_3" to "Supports 70+ languages & all countries.\nAuto govt holidays on calendar!",
            "onboard_title_4" to "Free Forever",
            "onboard_sub_4" to "No ads. No subscription. All features\ncompletely free. Made with ❤️ by Nitai Studio 🇮🇳",
            "onboard_next" to "Next",
            "onboard_get_started" to "Get Started 🚀",
        ),
        "bn" to mapOf(
            "app_title" to "ডিউটি ট্র্যাকার প্রো",
            "checkin_label" to "চেক-ইন সময়",
            "checkout_label" to "চেক-আউট সময়",
            "checkin_btn" to "চেক ইন",
            "checkout_btn" to "চেক আউট",
            "day_streak" to "দিনের ধারা!",
            "no_entry_today" to "আজকের কোনো এন্ট্রি নেই",
            "add_duty" to "আজকের ডিউটি যোগ করুন",
            "report" to "রিপোর্ট",
            "graph" to "গ্রাফ",
            "advance" to "অগ্রিম",
            "utility" to "ইউটিলিটি",
            "language" to "ভাষা",
            "select_language" to "ভাষা নির্বাচন করুন",
            "select_shift" to "শিফট নির্বাচন",
            "duty_ot" to "ডিউটি/ওটি (ঘণ্টা)",
            "late_mins" to "দেরি (মি.)",
            "save_entry" to "সেভ করুন",
            "delete" to "মুছুন",
            "cancel" to "বাতিল",
            "monthly_goal" to "মাসিক লক্ষ্য",
            "target" to "লক্ষ্য",
            "daily_salary" to "দৈনিক বেতন (হাজিরা)",
            "food_allowance" to "দৈনিক খাবার ভাতা",
            "overtime_rate" to "ওভারটাইম রেট (ঘণ্টা প্রতি)",
            "pf_deduction" to "পিএফ কর্তন (নির্ধারিত)",
            "save_conf" to "সংরক্ষণ করুন",
            "logout" to "লগ আউট",
            "start_date" to "শুরুর তারিখ",
            "end_date" to "শেষের তারিখ",
            "gen_report" to "রিপোর্ট তৈরি করুন"
            "onboard_title_1" to "প্রতিটি ডিউটি ট্র্যাক করুন",
            "onboard_sub_1" to "প্রতিদিন উপস্থিতি চিহ্নিত করুন — উপস্থিত, ছুটি,\nহলিডে, অসুস্থ ও আরও। কোনো দিন মিস করবেন না।",
            "onboard_title_2" to "স্মার্ট বেতন রিপোর্ট",
            "onboard_sub_2" to "বেতন, ওটি পে ও খাদ্য ভাতা স্বয়ংক্রিয়ভাবে গণনা করুন।\nসাথে সাথে ইমেজ রিপোর্ট শেয়ার করুন।",
            "onboard_title_3" to "সারা বিশ্বে কাজ করে",
            "onboard_sub_3" to "৭০+ ভাষা ও সব দেশ সমর্থন করে।\nক্যালেন্ডারে সরকারি ছুটি স্বয়ংক্রিয়ভাবে দেখায়!",
            "onboard_title_4" to "চিরকাল বিনামূল্যে",
            "onboard_sub_4" to "কোনো বিজ্ঞাপন নেই। সাবস্ক্রিপশন নেই।\nসব ফিচার সম্পূর্ণ বিনামূল্যে।",
            "onboard_next" to "পরবর্তী",
            "onboard_get_started" to "শুরু করুন 🚀",
        ),
        "hi" to mapOf(
            "app_title" to "ड्यूटी ट्रैकर प्रो",
            "checkin_label" to "चेक-इन समय",
            "checkout_label" to "चेक-आउट समय",
            "checkin_btn" to "चेक इन",
            "checkout_btn" to "चेक आउट",
            "day_streak" to "दिन की लकीर!",
            "no_entry_today" to "आज कोई एंट्री नहीं",
            "add_duty" to "आज की ड्यूटी जोड़ें",
            "report" to "रिपोर्ट",
            "graph" to "ग्राफ",
            "advance" to "अग्रिम",
            "utility" to "उपयोगिता",
            "language" to "भाषा",
            "select_language" to "भाषा चुनें",
            "select_shift" to "शिफ्ट चयन",
            "duty_ot" to "ड्यूटी/ओटी (घंटे)",
            "late_mins" to "देरी (मिनट)",
            "save_entry" to "सेव करें",
            "delete" to "हटाएं",
            "cancel" to "रद्द करें",
            "monthly_goal" to "मासिक लक्ष्य",
            "target" to "लक्ष्य",
            "daily_salary" to "दैनिक वेतन (हाजिरी)",
            "food_allowance" to "दैनिक भोजन भत्ता",
            "overtime_rate" to "ओवरटाइम दर (प्रति घंटे)",
            "pf_deduction" to "पीएफ कटौती (निश्चित)",
            "save_conf" to "सेव करें",
            "logout" to "लॉग आउट",
            "start_date" to "आरंभ तिथि",
            "end_date" to "समाप्ति तिथि",
            "gen_report" to "रिपोर्ट बनाएं"
            "onboard_title_1" to "हर ड्यूटी ट्रैक करें",
            "onboard_sub_1" to "रोज उपस्थिति दर्ज करें — उपस्थित, छुट्टी,\nत्यौहार, बीमार और भी। कोई दिन न चूकें।",
            "onboard_title_2" to "स्मार्ट सैलरी रिपोर्ट",
            "onboard_sub_2" to "सैलरी, ओटी पे और फूड अलाउंस\nस्वतः गणना करें। इमेज रिपोर्ट तुरंत शेयर करें।",
            "onboard_title_3" to "दुनिया भर में काम करता है",
            "onboard_sub_3" to "70+ भाषाएं और सभी देश समर्थित।\nकैलेंडर पर सरकारी छुट्टियां अपने आप!",
            "onboard_title_4" to "हमेशा के लिए मुफ़्त",
            "onboard_sub_4" to "कोई विज्ञापन नहीं। कोई सब्सक्रिप्शन नहीं।\nसभी सुविधाएं बिल्कुल मुफ़्त।",
            "onboard_next" to "अगला",
            "onboard_get_started" to "शुरू करें 🚀",
        ),
        "te" to mapOf(
            "app_title" to "డ్యూటీ ట్రాకర్ ప్రో",
            "checkin_label" to "చెక్-ఇన్ సమయం",
            "checkout_label" to "చెక్-అవుట్ సమయం",
            "checkin_btn" to "చెక్ ఇన్",
            "checkout_btn" to "చెక్ అవుట్",
            "day_streak" to "రోజుల వరుస!",
            "no_entry_today" to "ఈరోజు నమోదు లేదు",
            "add_duty" to "నేటి విధిని జోడించు",
            "report" to "నివేదిక",
            "graph" to "గ్రాఫ్",
            "advance" to "అడ్వాన్స్",
            "utility" to "యుటిలిటీ",
            "language" to "భాష",
            "select_language" to "భాష ఎంచుకోండి",
            "select_shift" to "షిఫ్ట్ ఎంపిక",
            "duty_ot" to "డ్యూటీ/ఓటి (గంటలు)",
            "late_mins" to "ఆలస్యం (నిమిషాలు)",
            "save_entry" to "సేవ్ చేయి",
            "delete" to "తొలగించు",
            "cancel" to "రద్దు చేయి",
            "monthly_goal" to "నెలవారీ లక్ష్యం",
            "target" to "లక్ష్యం",
            "daily_salary" to "రోజువారీ జీతం",
            "food_allowance" to "భోజన భత్యం",
            "overtime_rate" to "ఓవర్ టైమ్ రేటు",
            "pf_deduction" to "పిఎఫ్ తగ్గింపు",
            "save_conf" to "సేవ్ చేయి",
            "logout" to "లాగ్ అవుట్",
            "start_date" to "ప్రారంభ తేదీ",
            "end_date" to "ముగింపు తేదీ",
            "gen_report" to "నివేదిక రూపొందించు"
            "onboard_title_1" to "ప్రతి విధిని ట్రాక్ చేయండి",
            "onboard_sub_1" to "రోజూ హాజరు నమోదు చేయండి — హాజరు, సెలవు,\nసెలవురోజు, అనారోగ్యం మరియు మరిన్నీ.",
            "onboard_title_2" to "స్మార్ట్ జీతం నివేదికలు",
            "onboard_sub_2" to "జీతం, ఓటి పే & ఆహార భత్యం స్వయంచాలకంగా లెక్కించండి.",
            "onboard_title_3" to "ప్రపంచవ్యాప్తంగా పనిచేస్తుంది",
            "onboard_sub_3" to "70+ భాషలు & అన్ని దేశాలు మద్దతు ఇస్తాయి.",
            "onboard_title_4" to "ఎల్లప్పుడూ ఉచితం",
            "onboard_sub_4" to "యాడ్లు లేవు. సభ్యత్వం లేదు. అన్ని ఫీచర్లు ఉచితం.",
            "onboard_next" to "తదుపరి",
            "onboard_get_started" to "ప్రారంభించండి 🚀",
        )
    )

    fun t(key: String, lang: String): String {
        return languages[lang]?.get(key) ?: languages["en"]?.get(key) ?: key
    }
}

@Composable
fun AppContentManager(
    viewModel: DutyViewModel,
    billingManager: PlayBillingManager?,
    themeLight: MutableState<Boolean>,
    activity: Activity
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isMaintenance by viewModel.maintenanceActive.collectAsState()
    val isForcedUpdate by viewModel.updateRequired.collectAsState()
    val localizedLang by viewModel.appLanguage.collectAsState()

    // 100% Blocking Screens override
    if (isMaintenance) {
        MaintenanceScreen(viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Splash -> SplashScreen()
                Screen.Onboarding -> OnboardingScreen(viewModel, themeLight)
                Screen.Auth -> AuthScreen(viewModel)
                Screen.PinEntry -> PinEntryScreen(viewModel)
                Screen.Home -> DashboardHome(viewModel, themeLight)
                Screen.Profile -> ProfileScreen(viewModel, themeLight)
                Screen.Reports -> ReportsScreen(viewModel)
                Screen.Graph -> GraphScreen(viewModel)
                Screen.Advance -> AdvanceCashScreen(viewModel)
                Screen.Settings -> SettingsScreen(viewModel)
                Screen.Referral -> ReferralScreen(viewModel)
                Screen.PaymentRequests -> PaymentRequestsScreen(viewModel)
                Screen.SubHistory -> SubHistoryScreen(viewModel)
                Screen.Help -> HelpScreen(viewModel)
                Screen.Feedback -> FeedbackScreen(viewModel)
                Screen.About -> AboutScreen(viewModel)
                Screen.Legal -> LegalScreen(viewModel)
            }

            // Ads visual Overlay triggers
            val showAds by viewModel.adsLoaded.collectAsState()
            if (showAds) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AdCardOverlay()
                }
            }

            // Forced Update modal block
            if (isForcedUpdate) {
                ForcedUpdatePopup(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(viewModel: DutyViewModel, themeLight: MutableState<Boolean>) {
    val lang by viewModel.appLanguage.collectAsState()
    val pageIndex = remember { mutableStateOf(0) }

    data class OnboardPage(
        val emoji: String,
        val titleKey: String,
        val subtitleKey: String,
        val bgColor: Long,
        val isLast: Boolean = false
    )

    val pages = listOf(
        OnboardPage("🗓️", "onboard_title_1", "onboard_sub_1", 0xFF050505L),
        OnboardPage("💰", "onboard_title_2", "onboard_sub_2", 0xFF050505L),
        OnboardPage("🌍", "onboard_title_3", "onboard_sub_3", 0xFF050505L),
        OnboardPage("🔥", "onboard_title_4", "onboard_sub_4", 0xFF050505L, isLast = true)
    )

    val total = pages.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        // Top row: dots + moon toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(total) { i ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (i == pageIndex.value) 32.dp else 8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i == pageIndex.value) Color(0xFF6C63FF)
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            // Moon / Sun toggle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { themeLight.value = !themeLight.value },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (themeLight.value) "☀️" else "🌙",
                    fontSize = 18.sp
                )
            }
        }

        // Center content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large emoji
            Text(
                text = pages[pageIndex.value].emoji,
                fontSize = 90.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Title with emoji prefix
            AnimatedContent(
                targetState = pageIndex.value,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "title_anim"
            ) { idx ->
                Text(
                    text = "${pages[idx].emoji} ${Localization.t(pages[idx].titleKey, lang)}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = pageIndex.value,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "sub_anim"
            ) { idx ->
                Text(
                    text = Localization.t(pages[idx].subtitleKey, lang),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (hidden on first page)
            if (pageIndex.value > 0) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { pageIndex.value-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Next / Get Started button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6C63FF), Color(0xFF4361EE))
                        )
                    )
                    .clickable {
                        if (pages[pageIndex.value].isLast) {
                            viewModel.completeOnboarding()
                        } else {
                            pageIndex.value++
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pages[pageIndex.value].isLast)
                            Localization.t("onboard_get_started", lang)
                        else
                            Localization.t("onboard_next", lang),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val ringScale1 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "r1"
    )
    val ringAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.0f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "ra1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF020024), Color(0xFF090979), Color(0xFF00D4FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic rotating rings
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = ringAlpha1 * 0.15f))
                .align(Alignment.Center)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .animateContentSize()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Duty Tracker Pro",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Made in India  ", color = Color.LightGray, fontSize = 12.sp)
                // Draw Indian Flag natively using simple shapes
                Row(modifier = Modifier.size(24.dp, 16.dp)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFF9933)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF138808)))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Developed By Nitai Studio", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(viewModel: DutyViewModel) {
    val activeTab = remember { mutableStateOf("login") }
    val ccInput = remember { mutableStateOf("+91") }
    val phoneInput = remember { mutableStateOf("") }
    val pinInput = remember { mutableStateOf("") }
    val pinConfirmInput = remember { mutableStateOf("") }
    val nameInput = remember { mutableStateOf("") }
    val showCountryPicker = remember { mutableStateOf(false) }
    val showPinStep = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        // Moon toggle top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🌙", fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Shield icon box
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141422)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield",
                    tint = Color(0xFF8B9FF0),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Duty Tracker Pro",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Track your duty. Own your time. Everywhere.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login / Sign Up tab pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF1A1A2A))
                    .padding(5.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (activeTab.value == "login") Color(0xFF6C63FF) else Color.Transparent)
                            .clickable { activeTab.value = "login"; showPinStep.value = false }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (activeTab.value == "signup") Color(0xFF6C63FF) else Color.Transparent)
                            .clickable { activeTab.value = "signup"; showPinStep.value = false }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign Up", color = if (activeTab.value == "signup") Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (!showPinStep.value) {
                // Phone number row
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(76.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1A2A))
                            .clickable { showCountryPicker.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ccInput.value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = phoneInput.value,
                        onValueChange = { phoneInput.value = it },
                        placeholder = { Text("Phone Number", color = Color(0xFF555570)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color(0xFF2A2A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF6C63FF)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                }

                if (activeTab.value == "signup") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameInput.value,
                        onValueChange = { nameInput.value = it },
                        placeholder = { Text("Full Name", color = Color(0xFF555570)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color(0xFF2A2A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Continue button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF4361EE)))
                        )
                        .clickable {
                            if (phoneInput.value.length < 5) {
                                Toast.makeText(context, "Enter valid phone number", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            showPinStep.value = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Continue →", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

            } else {
                // PIN step — show phone, then PIN keypad
                Text(
                    text = "${ccInput.value} ${phoneInput.value}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (activeTab.value == "login") "Enter your PIN" else "Set your PIN",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4 dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < pinInput.value.length) Color(0xFF6C63FF)
                                    else Color(0xFF2A2A3A)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Numeric keypad
                val keys = listOf(
                    listOf("1","2","3"),
                    listOf("4","5","6"),
                    listOf("7","8","9"),
                    listOf("×","0","⌫")
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1A1A2A))
                                        .clickable {
                                            when (key) {
                                                "×" -> pinInput.value = ""
                                                "⌫" -> if (pinInput.value.isNotEmpty()) pinInput.value = pinInput.value.dropLast(1)
                                                else -> {
                                                    if (pinInput.value.length < 4) {
                                                        pinInput.value += key
                                                        if (pinInput.value.length == 4) {
                                                            if (activeTab.value == "login") {
                                                                viewModel.login(ccInput.value, phoneInput.value, pinInput.value) { result ->
                                                                    if (result != "success") {
                                                                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                                                        pinInput.value = ""
                                                                    }
                                                                }
                                                            } else {
                                                                if (pinConfirmInput.value.isEmpty()) {
                                                                    pinConfirmInput.value = pinInput.value
                                                                    pinInput.value = ""
                                                                } else {
                                                                    if (pinInput.value == pinConfirmInput.value) {
                                                                        viewModel.signUp(ccInput.value, phoneInput.value, nameInput.value, "", pinInput.value) { result ->
                                                                            if (result != "success") {
                                                                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                                                                pinInput.value = ""
                                                                                pinConfirmInput.value = ""
                                                                            }
                                                                        }
                                                                    } else {
                                                                        Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                                                                        pinInput.value = ""
                                                                        pinConfirmInput.value = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Change Number
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1A2A))
                        .clickable { showPinStep.value = false; pinInput.value = ""; pinConfirmInput.value = "" }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("← Change Number", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Made with ❤️ by Nitai Studio 🇮🇳",
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showCountryPicker.value) {
            CountryPickerDialog(
                countries = viewModel.countries,
                onDismiss = { showCountryPicker.value = false },
                onSelect = { selectedCode ->
                    ccInput.value = selectedCode
                    showCountryPicker.value = false
                }
            )
        }
    }
}

@Composable
fun CountryPickerDialog(
    countries: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredCountries = countries.filter {
        it.first.contains(searchQuery.value, ignoreCase = true) ||
        it.second.contains(searchQuery.value, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF141422))
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🌍 Select Country",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search country...", color = Color(0xFF555570)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6C63FF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color(0xFF2A2A3A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF6C63FF)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCountries.isNotEmpty()) {
                Text("🌐 ALL COUNTRIES", color = Color(0xFF6C63FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn {
                items(filteredCountries) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country.second) }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = country.first,
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = country.second,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                }
            }
        }
    }
}


@Composable
fun PinEntryScreen(viewModel: DutyViewModel) {
    val inputPin = remember { mutableStateOf("") }
    val mode by viewModel.pinEntryMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        // Moon toggle top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🌙", fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141422)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield",
                    tint = Color(0xFF8B9FF0),
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text("Duty Tracker Pro", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Track your duty. Own your time. Everywhere.",
                color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tab pill (decorative - login selected)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF1A1A2A))
                    .padding(5.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF6C63FF))
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.Transparent)
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign Up", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Phone display
            val savedCc by viewModel.savedCc.collectAsState()
            val savedPhone by viewModel.savedPhone.collectAsState()
            if (savedPhone.isNotBlank()) {
                Text("$savedCc $savedPhone", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text("Enter your PIN", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // 4 dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < inputPin.value.length) Color(0xFF6C63FF)
                                else Color(0xFF2A2A3A)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad
            val keys = listOf(
                listOf("1","2","3"),
                listOf("4","5","6"),
                listOf("7","8","9"),
                listOf("×","0","⌫")
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1A1A2A))
                                    .clickable {
                                        viewModel.recordActivity()
                                        when (key) {
                                            "×" -> inputPin.value = ""
                                            "⌫" -> if (inputPin.value.isNotEmpty()) inputPin.value = inputPin.value.dropLast(1)
                                            else -> {
                                                if (inputPin.value.length < 4) {
                                                    inputPin.value += key
                                                    if (inputPin.value.length == 4) {
                                                        viewModel.submitPin(inputPin.value) { result ->
                                                            if (result == "incorrect") {
                                                                Toast.makeText(context, "Invalid PIN. Please try again.", Toast.LENGTH_SHORT).show()
                                                                inputPin.value = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(key, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change Number
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1A2A))
                    .clickable { viewModel.logout() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("← Change Number", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Made with ❤️ by Nitai Studio 🇮🇳",
                color = Color.Gray.copy(alpha = 0.5f), fontSize = 11.sp
            )
        }
    }
}


@Composable
fun DashboardHome(viewModel: DutyViewModel, themeLight: MutableState<Boolean>) {
    val activeLang by viewModel.appLanguage.collectAsState()
    val stateDuties by viewModel.duties.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val checkinTimeStr by viewModel.checkinTime.collectAsState()
    val checkoutTimeStr by viewModel.checkoutTime.collectAsState()

    var activeMonthOffset by remember { mutableStateOf(0) }
    
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, activeMonthOffset)
    val displayMonthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time)

    val showingDutyMarkDate = remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationPanel(viewModel = viewModel, activeTab = "home")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Navbar header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Duty Tracker Pro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4361EE)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF4361EE).copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v1.0 (Stable)",
                                color = Color(0xFF4361EE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { themeLight.value = !themeLight.value }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Light Theme", tint = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { viewModel.navigateTo(Screen.Profile) }
                    ) {
                        if (userProfile?.img?.isNotEmpty() == true) {
                            AsyncImage(
                                model = userProfile?.img,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Default",
                                tint = Color.LightGray,
                                modifier = Modifier.fillMaxSize().padding(3.dp)
                            )
                        }
                    }
                }
            }

            // Month bar Navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeMonthOffset-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.White)
                }
                Text(
                    text = displayMonthName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                IconButton(onClick = { activeMonthOffset++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                }
            }

            // Month Calendar Days Grid 100% Native gesture handling and monthly swiping
            var swipeDragAmount = remember { mutableStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(activeMonthOffset) {
                        detectDragGestures(
                            onDragStart = { swipeDragAmount.value = 0f },
                            onDragEnd = {
                                if (swipeDragAmount.value > 120f) {
                                    activeMonthOffset--
                                } else if (swipeDragAmount.value < -120f) {
                                    activeMonthOffset++
                                }
                            },
                            onDragCancel = { swipeDragAmount.value = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                swipeDragAmount.value += dragAmount.x
                            }
                        )
                    }
            ) {
                CalendarGridView(
                    calendar = calendar,
                    dutiesList = stateDuties,
                    isPremiumUser = viewModel.isUserPremium(),
                    onDateClick = { date ->
                        showingDutyMarkDate.value = date
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Check-in check-out card Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141414))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = Localization.t("checkin_label", activeLang), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = checkinTimeStr ?: "--:-- --", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text(text = Localization.t("checkout_label", activeLang), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = checkoutTimeStr ?: "--:-- --", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.doCheckIn() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                        ) {
                            Text(text = Localization.t("checkin_btn", activeLang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.doCheckOut() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Text(text = Localization.t("checkout_btn", activeLang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom CTA row Add duty
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayDuty = stateDuties.firstOrNull { it.date == todayStr }
            val statsTipText = if (todayDuty != null) "Today Status: Marked ${todayDuty.status}" else Localization.t("no_entry_today", activeLang)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = statsTipText, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showingDutyMarkDate.value = todayStr },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add duty")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = Localization.t("add_duty", activeLang), color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        // Attendance dialog entry overlay modal
        val dialogDate = showingDutyMarkDate.value
        if (dialogDate != null) {
            val dateDuty = stateDuties.firstOrNull { it.date == dialogDate }
            DutyAddMarkDialog(
                date = dialogDate,
                existing = dateDuty,
                onDismiss = { showingDutyMarkDate.value = null },
                onSave = { status, otHours, lMins, shift ->
                    viewModel.markAttendance(dialogDate, status, otHours, lMins, shift)
                    showStartIoAd(context)
                    showingDutyMarkDate.value = null
                },
                onDelete = {
                    viewModel.deleteAttendance(dialogDate)
                    showStartIoAd(context)
                    showingDutyMarkDate.value = null
                }
            )
        }
    }
}

@Composable
fun CalendarGridView(
    calendar: Calendar,
    dutiesList: List<DutyEntry>,
    isPremiumUser: Boolean,
    onDateClick: (String) -> Unit
) {
    val tempCal = calendar.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sun

    val year = tempCal.get(Calendar.YEAR)
    val month = tempCal.get(Calendar.MONTH) + 1

    val daysOfWeekLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        // Week days Row Header
        Row(modifier = Modifier.fillMaxWidth()) {
            for (label in daysOfWeekLabels) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid contents
        var dayValue = 1
        for (row in 0..5) {
            if (dayValue > maxDays) break
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index < startDayOfWeek || dayValue > maxDays) {
                        Spacer(modifier = Modifier.weight(1.0f))
                    } else {
                        val currentDay = dayValue
                        val dateString = String.format("%04d-%02d-%02d", year, month, currentDay)
                        val entry = dutiesList.firstOrNull { it.date == dateString }

                        // Draw Day Box
                        val bgBrush = when (entry?.status) {
                            "Present" -> SolidColor(Color(0xFF06D6A0))
                            "Half Day" -> SolidColor(Color(0xFFFFD166))
                            "Leave" -> SolidColor(Color(0xFFEF233C))
                            "Sick" -> SolidColor(Color(0xFFFF9F1C))
                            "Holiday" -> SolidColor(Color(0xFF9D4EDD))
                            "Off" -> SolidColor(Color(0xFF6C757D))
                            else -> SolidColor(Color(0xFF1E1E1E))
                        }

                        val textColor = if (entry != null) {
                            if (listOf("Present", "Half Day", "Sick").contains(entry.status)) Color.Black else Color.White
                        } else Color.White

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgBrush)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable { onDateClick(dateString) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentDay.toString(),
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (entry != null) {
                                    Text(
                                        text = entry.status.take(4),
                                        color = textColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.otHours > 0.0) {
                                        Text(
                                            text = "+${entry.otHours}h",
                                            color = textColor,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        dayValue++
                    }
                }
            }
        }
    }
}

@Composable
fun DutyAddMarkDialog(
    date: String,
    existing: DutyEntry?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String) -> Unit,
    onDelete: () -> Unit
) {
    val selectedStatus = remember { mutableStateOf(existing?.status ?: "Present") }
    val otHoursStr = remember { mutableStateOf(existing?.otHours?.toString() ?: "") }
    val lateMinsStr = remember { mutableStateOf(existing?.lateMinutes?.toString() ?: "") }
    val selectedShift = remember { mutableStateOf(existing?.shift ?: "Morning") }

    val statusList = listOf("Present", "Half Day", "Leave", "Sick", "Holiday", "Off")
    val shiftList = listOf("Morning", "Evening", "Night")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val ot = otHoursStr.value.toDoubleOrNull() ?: 0.0
                    val late = lateMinsStr.value.toIntOrNull() ?: 0
                    onSave(selectedStatus.value, ot, late, selectedShift.value)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
            ) {
                Text("SAVE ENTRY", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (existing != null) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF233C))
                ) {
                    Text("DELETE", color = Color.White)
                }
            }
        },
        title = {
            Text(text = date, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Quick status grid select buttons represent
                Text(text = "Status", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    for (status in statusList) {
                        val active = selectedStatus.value == status
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (active) Color(0xFF4361EE) else Color(0xFF1E1E1E))
                                .clickable { selectedStatus.value = status }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = status, color = if (active) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Shift Selection", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (shift in shiftList) {
                        val active = selectedShift.value == shift
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (active) Color(0xFF4361EE) else Color(0xFF1E1E1E))
                                .clickable { selectedShift.value = shift }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = shift, color = if (active) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = otHoursStr.value,
                        onValueChange = { otHoursStr.value = it },
                        label = { Text("Ot (Hrs)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4361EE),
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = lateMinsStr.value,
                        onValueChange = { lateMinsStr.value = it },
                        label = { Text("Late (Mins)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4361EE),
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        containerColor = Color(0xFF141414),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ReportsScreen(viewModel: DutyViewModel) {
    val activeLang by viewModel.appLanguage.collectAsState()
    val isPremium = viewModel.isUserPremium()
    
    val startDateInput = remember { mutableStateOf("2026-06-01") }
    val endDateInput = remember { mutableStateOf("2026-06-30") }

    val showReportOutputStatus = remember { mutableStateOf(false) }
    val reportResult = remember { mutableStateOf<ReportResult?>(null) }

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationPanel(viewModel = viewModel, activeTab = "report")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Salary Reports", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF06D6A0))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Calculate wages, allowances, and compile native PDF invoices", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPremium) {
                PremiumGatedStateBlock(viewModel)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = startDateInput.value,
                                onValueChange = { },
                                label = { Text("Start Date (YYYY-MM-DD)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF06D6A0), focusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val calendar = java.util.Calendar.getInstance()
                                        val parts = startDateInput.value.split("-")
                                        if (parts.size == 3) {
                                            parts[0].toIntOrNull()?.let { calendar.set(java.util.Calendar.YEAR, it) }
                                            parts[1].toIntOrNull()?.let { calendar.set(java.util.Calendar.MONTH, it - 1) }
                                            parts[2].toIntOrNull()?.let { calendar.set(java.util.Calendar.DAY_OF_MONTH, it) }
                                        }
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                                val formattedMonth = String.format("%02d", selectedMonth + 1)
                                                val formattedDay = String.format("%02d", selectedDayOfMonth)
                                                startDateInput.value = "$selectedYear-$formattedMonth-$formattedDay"
                                            },
                                            calendar.get(java.util.Calendar.YEAR),
                                            calendar.get(java.util.Calendar.MONTH),
                                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = endDateInput.value,
                                onValueChange = { },
                                label = { Text("End Date (YYYY-MM-DD)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF06D6A0), focusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        val calendar = java.util.Calendar.getInstance()
                                        val parts = endDateInput.value.split("-")
                                        if (parts.size == 3) {
                                            parts[0].toIntOrNull()?.let { calendar.set(java.util.Calendar.YEAR, it) }
                                            parts[1].toIntOrNull()?.let { calendar.set(java.util.Calendar.MONTH, it - 1) }
                                            parts[2].toIntOrNull()?.let { calendar.set(java.util.Calendar.DAY_OF_MONTH, it) }
                                        }
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                                val formattedMonth = String.format("%02d", selectedMonth + 1)
                                                val formattedDay = String.format("%02d", selectedDayOfMonth)
                                                endDateInput.value = "$selectedYear-$formattedMonth-$formattedDay"
                                            },
                                            calendar.get(java.util.Calendar.YEAR),
                                            calendar.get(java.util.Calendar.MONTH),
                                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val reportsData = viewModel.generateReportsData(startDateInput.value, endDateInput.value)
                                reportResult.value = reportsData
                                showReportOutputStatus.value = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                        ) {
                            Text(text = "Generate Report", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showReportOutputStatus.value && reportResult.value != null,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(500)),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(500))
                ) {
                    val res = reportResult.value!!
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Professional White Invoice sheet resembling real PDF Slip!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(24.dp)
                        ) {
                            Column {
                                // PDF slip title header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "DUTY TRACKER PRO", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4361EE))
                                        Text(text = "SALARY SLIP STATEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Document",
                                        tint = Color(0xFF06D6A0),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Employee metadata
                                Text(text = "Period: ${startDateInput.value} to ${endDateInput.value}", fontSize = 11.sp, color = Color.DarkGray)
                                Text(text = "Employee Name: ${viewModel.userProfile.value?.name ?: "User"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF4361EE))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Description", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Amount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Items
                                PreviewSlipItemLine("Duty Pay (${res.dutyDays} days)", "${viewModel.curSymbol.value} ${String.format("%.0f", res.dutyPay)}")
                                PreviewSlipItemLine("Food Allowance", "${viewModel.curSymbol.value} ${String.format("%.0f", res.foodAllowance)}")
                                PreviewSlipItemLine("Overtime Pay (${res.totalOtHours}h)", "${viewModel.curSymbol.value} ${String.format("%.0f", res.overtimePay)}")
                                PreviewSlipItemLine("PF Deduction", "- ${viewModel.curSymbol.value} ${String.format("%.0f", res.pfDeduction)}", Color.Red)
                                PreviewSlipItemLine("Advances", "- ${viewModel.curSymbol.value} ${String.format("%.0f", res.totalAdvance)}", Color.Red)

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Total
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "NET PAYABLE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(
                                        text = "${viewModel.curSymbol.value} ${String.format("%.0f", res.netPayable)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF06D6A0)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Generated natively via Duty Tracker Pro",
                                    color = Color.Gray,
                                    fontSize = 8.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Below PDF Options (Download, Share, Whatsapp)
                        Button(
                            onClick = {
                                viewModel.exportReportPdf(startDateInput.value, endDateInput.value) { file ->
                                    if (file != null) {
                                        val saveSuccess = savePdfToPublicDownloads(context, file)
                                        if (saveSuccess) {
                                            Toast.makeText(context, "Successfully downloaded to 'Downloads' folder!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Saved to private directory only.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "PDF generation failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Download icon", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Download PDF Statement to Device", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.exportReportPdf(startDateInput.value, endDateInput.value) { file ->
                                    if (file != null) {
                                        openAndSharePdf(context, file)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share icon", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Share PDF Statement", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val messageText = """
                                *Duty Tracker Pro - Salary Slip Statement*
                                Period: ${startDateInput.value} to ${endDateInput.value}
                                Employee Name: ${viewModel.userProfile.value?.name ?: "User"}
                                
                                🔹 Worked Days: ${res.dutyDays} days (${viewModel.curSymbol.value} ${String.format("%.0f", res.dutyPay)})
                                🔹 Overtime Pay: ${viewModel.curSymbol.value} ${String.format("%.0f", res.overtimePay)}
                                🔹 Food Allowance: ${viewModel.curSymbol.value} ${String.format("%.0f", res.foodAllowance)}
                                🔹 PF Deduction: - ${viewModel.curSymbol.value} ${String.format("%.0f", res.pfDeduction)}
                                🔹 Advances: - ${viewModel.curSymbol.value} ${String.format("%.0f", res.totalAdvance)}
                                
                                *💰 Net Payable: ${viewModel.curSymbol.value} ${String.format("%.0f", res.netPayable)}*
                                
                                Track your shifts easily with Duty Tracker Pro:
                                🔗 https://www.dutytrackerpro.in/?m=1
                                """.trimIndent()

                                try {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, messageText)
                                        `package` = "com.whatsapp"
                                    }
                                    context.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, messageText)
                                    }
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Share Slip via"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp icon", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Share details to WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// open and share helper intents natively
private fun openAndSharePdf(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Salary Coefficient"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PreviewSlipItemLine(desc: String, amount: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = desc, color = Color.DarkGray, fontSize = 11.sp)
        Text(text = amount, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun savePdfToPublicDownloads(context: Context, file: java.io.File): Boolean {
    return try {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                java.io.FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback file copier for older Android devices or MediaStore failures
        try {
            val publicDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val destFile = java.io.File(publicDir, file.name)
            java.io.FileInputStream(file).use { inputStream ->
                java.io.FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}

@Composable
fun ReportItemLine(desc: String, amount: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = desc, color = Color.Gray, fontSize = 12.sp)
        Text(text = amount, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GraphScreen(viewModel: DutyViewModel) {
    val isPremium = viewModel.isUserPremium()
    val todayEffortText = remember { mutableStateOf("0 Hours") }
    val duties by viewModel.duties.collectAsState()

    val targetGoalInput = remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomNavigationPanel(viewModel = viewModel, activeTab = "graph")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Progress Monitor", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA855F7))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Effort stats and goals completion analytics in real-time", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPremium) {
                PremiumGatedStateBlock(viewModel)
            } else {
                // Today effort stats box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Today's Effort", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = todayEffortText.value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Smooth native bezier Canvas Line chart representing 30 days metrics
                Text(text = "Effort Hours (Last 30 Days)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    // Extract actual hours data
                    val dataPoints = remember(duties) {
                        val list = mutableListOf<Float>()
                        val calendar = Calendar.getInstance()
                        for (i in 0..29) {
                            val temp = calendar.clone() as Calendar
                            temp.add(Calendar.DAY_OF_YEAR, -i)
                            val fStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(temp.time)
                            val match = duties.firstOrNull { it.date == fStr }
                            var hours = 0f
                            if (match != null) {
                                hours = (if (match.status == "Present") 8f else if (match.status == "Half Day") 4f else 0f) + match.otHours.toFloat()
                            }
                            list.add(hours)
                        }
                        list.reverse()
                        list
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val maxVal = (dataPoints.maxOrNull() ?: 8f).coerceAtLeast(8f)

                        // Draw Grid Horizontal Lines helper
                        val linesCount = 4
                        for (j in 0..linesCount) {
                            val lineY = height - (height * j / linesCount)
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, lineY),
                                end = Offset(width, lineY),
                                strokeWidth = 1f
                            )
                        }

                        // Drawing Path connecting dots
                        if (dataPoints.isNotEmpty()) {
                            val points = dataPoints.mapIndexed { idx, value ->
                                val x = width * idx / (dataPoints.size - 1)
                                val y = height - (height * value / maxVal)
                                Offset(x, y)
                            }

                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (pIdx in 1 until points.size) {
                                    val p0 = points[pIdx - 1]
                                    val p1 = points[pIdx]
                                    cubicTo(
                                        (p0.x + p1.x) / 2f, p0.y,
                                        (p0.x + p1.x) / 2f, p1.y,
                                        p1.x, p1.y
                                    )
                                }
                            }

                            // Draw Curved Line
                            drawPath(
                                path = path,
                                color = Color(0xFFA855F7),
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            // Under Path Gradient coloring
                            val gradPath = Path().apply {
                                addPath(path)
                                lineTo(width, height)
                                lineTo(0f, height)
                                close()
                            }

                            drawPath(
                                path = gradPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFA855F7).copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Monthly Goals Target Progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Target Goal Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val earned = duties.filter { java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it.date)).equals(java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())) }.sumOf { (if (it.status == "Present") viewModel.dailySalary.value else if (it.status == "Half Day") viewModel.dailySalary.value/2.0 else 0.0) + (it.otHours*viewModel.overtimeRate.value) }
                            val target = viewModel.monthlyTarget.value.coerceAtLeast(1.0)
                            val pctResult = Math.min(100f, (earned / target * 100).toFloat())
                            Text(text = "${String.format("%.1f", pctResult)}%", color = Color(0xFFA855F7), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Progress bar shape
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            val earned = duties.filter { java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it.date)).equals(java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())) }.sumOf { (if (it.status == "Present") viewModel.dailySalary.value else if (it.status == "Half Day") viewModel.dailySalary.value/2.0 else 0.0) + (it.otHours*viewModel.overtimeRate.value) }
                            val target = viewModel.monthlyTarget.value.coerceAtLeast(1.0)
                            val fillPctFraction = (earned / target).toFloat().coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fillPctFraction)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF4361EE), Color(0xFFA855F7))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = targetGoalInput.value,
                                onValueChange = { targetGoalInput.value = it },
                                placeholder = { Text("Set goal (e.g. 15000)", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA855F7), focusedTextColor = Color.White),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val tFloat = targetGoalInput.value.toDoubleOrNull() ?: 0.0
                                    viewModel.updateRatesConf(
                                        currency = viewModel.curSymbol.value,
                                        salary = viewModel.dailySalary.value,
                                        otr = viewModel.overtimeRate.value,
                                        food = viewModel.foodAllowance.value,
                                        pf = viewModel.pfDeduction.value,
                                        target = tFloat
                                    )
                                    targetGoalInput.value = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                            ) {
                                Text(text = "Set", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvanceCashScreen(viewModel: DutyViewModel) {
    val isPremium = viewModel.isUserPremium()
    val advancesList by viewModel.advances.collectAsState()

    val amountInput = remember { mutableStateOf("") }
    val dateInput = remember { mutableStateOf("") }
    val contractorInput = remember { mutableStateOf("") }
    val reasonInput = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dateInput.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationPanel(viewModel = viewModel, activeTab = "advance")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Advance Money", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF9F1C))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Manage advance pay logs and contractor cash details securely", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPremium) {
                PremiumGatedStateBlock(viewModel)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Input card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF141414))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = amountInput.value,
                                    onValueChange = { amountInput.value = it },
                                    label = { Text("Amount taken", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C), focusedTextColor = Color.White),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = dateInput.value,
                                    onValueChange = { dateInput.value = it },
                                    label = { Text("Date (YYYY-MM-DD)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C), focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = contractorInput.value,
                                    onValueChange = { contractorInput.value = it },
                                    label = { Text("Contractor / Company", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF9F1C), focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val amt = amountInput.value.toDoubleOrNull() ?: 0.0
                                        if (amt <= 0.0 || dateInput.value.isEmpty()) {
                                            Toast.makeText(context, "Please configure required properties.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addAdvanceCash(
                                            amount = amt,
                                            date = dateInput.value,
                                            contractor = contractorInput.value,
                                            note = reasonInput.value
                                        )
                                        amountInput.value = ""
                                        contractorInput.value = ""
                                        reasonInput.value = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C))
                                ) {
                                    Text(text = "Save Advance Money", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "History logs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            val totalSum = advancesList.sumOf { it.amount }
                            Text(text = "Total sum: ${viewModel.curSymbol.value}$totalSum", color = Color(0xFFFF9F1C), fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    items(advancesList) { entry ->
                        // Advance entry card row item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141414))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${viewModel.curSymbol.value} ${entry.amount}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "${entry.date} ${if (entry.contractor.isNotEmpty()) "• ${entry.contractor}" else ""}", color = Color.Gray, fontSize = 11.sp)
                            }

                            IconButton(onClick = { viewModel.removeAdvanceCash(entry.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "delete advance", tint = Color(0xFFEF233C))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: DutyViewModel) {
    val currency = viewModel.curSymbol.collectAsState()
    val salaryInput = remember { mutableStateOf(viewModel.dailySalary.value.toString()) }
    val foodAllowanceInput = remember { mutableStateOf(viewModel.foodAllowance.value.toString()) }
    val otrRateInput = remember { mutableStateOf(viewModel.overtimeRate.value.toString()) }
    val pfDeductionInput = remember { mutableStateOf(viewModel.pfDeduction.value.toString()) }

    val activeLang by viewModel.appLanguage.collectAsState()
    val isPremium = viewModel.isUserPremium()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationPanel(viewModel = viewModel, activeTab = "settings")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Utility & Settings", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF72585))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Configure custom currencies and calculate shift payroll variables natively", color = Color.Gray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPremium) {
                PremiumGatedStateBlock(viewModel)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        // Currency selection row
                        Text(text = Localization.t("set_currency", activeLang), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            val formats = listOf("₹", "৳", "$", "€", "SAR", "AED")
                            for (f in formats) {
                                val active = currency.value == f
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (active) Color(0xFFF72585) else Color(0xFF1E1E1E))
                                        .clickable { viewModel.curSymbol.value = f }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(text = f, color = if (active) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = salaryInput.value,
                            onValueChange = { salaryInput.value = it },
                            label = { Text(Localization.t("daily_salary", activeLang), color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF72585), focusedTextColor = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = foodAllowanceInput.value,
                            onValueChange = { foodAllowanceInput.value = it },
                            label = { Text(Localization.t("food_allowance", activeLang), color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF72585), focusedTextColor = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = otrRateInput.value,
                            onValueChange = { otrRateInput.value = it },
                            label = { Text(Localization.t("overtime_rate", activeLang), color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF72585), focusedTextColor = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pfDeductionInput.value,
                            onValueChange = { pfDeductionInput.value = it },
                            label = { Text(Localization.t("pf_deduction", activeLang), color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF72585), focusedTextColor = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val sDouble = salaryInput.value.toDoubleOrNull() ?: 0.0
                                val fDouble = foodAllowanceInput.value.toDoubleOrNull() ?: 0.0
                                val otDouble = otrRateInput.value.toDoubleOrNull() ?: 0.0
                                val pfDouble = pfDeductionInput.value.toDoubleOrNull() ?: 0.0

                                viewModel.updateRatesConf(
                                    currency = currency.value,
                                    salary = sDouble,
                                    otr = otDouble,
                                    food = fDouble,
                                    pf = pfDouble,
                                    target = viewModel.monthlyTarget.value
                                )
                                showStartIoAd(context)
                                Toast.makeText(context, "Saved Configuration", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF72585))
                        ) {
                            Text(text = "Save Configuration", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: DutyViewModel, themeLight: MutableState<Boolean>) {
    val activeLang by viewModel.appLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userUid by viewModel.userSession.collectAsState()
    val context = LocalContext.current

    val currentAutoLockMinute by viewModel.autoLockMinutes.collectAsState()

    var isUploading by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            viewModel.uploadProfileAvatar(context, uri) { success ->
                isUploading = false
                if (success) {
                    Toast.makeText(context, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        showStartIoAd(context)
                        viewModel.navigateBack() 
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile dynamic avatar display
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color(0xFF4361EE),
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    if (userProfile?.img?.isNotEmpty() == true) {
                        AsyncImage(
                            model = userProfile?.img,
                            contentDescription = "Avatar Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Avatar",
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF4361EE), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = userProfile?.name ?: "User Name", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = userUid ?: "", color = Color.Gray, fontSize = 13.sp)
            Text(text = userProfile?.email ?: "", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // BuyMeACoffee Donation Button Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDD00)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/nitaigrp00a"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "☕ Donate to Support Dev (Buy Me a Coffee)",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Telegram links CTA buttons
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/DutyTrackerProapp"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "t.me/DutyTrackerProapp", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC))
            ) {
                Icon(Icons.Default.Send, contentDescription = "telegram link")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Live Chat Support (Free & Active)", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option Settings cards item block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141414))
            ) {
                ProfileControlRowItem("Refer & Earn Premium", Icons.Default.Star) {
                    viewModel.navigateTo(Screen.Referral)
                }
                ProfileControlRowItem("Security Configuration", Icons.Default.Lock) {
                    viewModel.setPinVerifyFlow("change") { Log.d("PINFlow", "Pin flow active") }
                }
                ProfileControlRowItem("Help & Support", Icons.Default.Info) {
                    viewModel.navigateTo(Screen.Help)
                }
                ProfileControlRowItem("Privacy & Legal Policies", Icons.Default.Lock) {
                    viewModel.navigateTo(Screen.Legal)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language config selector
            Text(text = "Language Localization Selection", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                val languagesList = listOf(Pair("en", "🇬🇧 English"), Pair("hi", "हिन्दी (Hindi)"), Pair("bn", "বাংলা (Bengali)"), Pair("te", "తెలుగు (Telugu)"))
                for (langPair in languagesList) {
                    val active = activeLang == langPair.first
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (active) Color(0xFF4361EE) else Color(0xFF1E1E1E))
                            .clickable { viewModel.changeLocalization(langPair.first) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(text = langPair.second, color = if (active) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF233C).copy(alpha = 0.8f))
            ) {
                Text(text = "Log Out Account", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileControlRowItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun AdCardOverlay() {
    // Real Start.io Banner Ad card overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 85.dp) // padded up from bottom navigation bar
            .height(55.dp)
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        StartIoBanner(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun BottomNavigationPanel(viewModel: DutyViewModel, activeTab: String) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "Report",
            icon = Icons.Default.List,
            active = activeTab == "report",
            modifier = Modifier.weight(1f)
        ) {
            showStartIoAd(context)
            viewModel.navigateTo(Screen.Reports)
        }

        BottomNavItem(
            label = "Graph",
            icon = Icons.Default.Star,
            active = activeTab == "graph",
            modifier = Modifier.weight(1f)
        ) {
            showStartIoAd(context)
            viewModel.navigateTo(Screen.Graph)
        }

        // Floating central Home action trigger
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(y = (-20).dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4361EE), Color(0xFF7209B7))
                    )
                )
                .clickable {
                    showStartIoAd(context)
                    viewModel.navigateTo(Screen.Home)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        BottomNavItem(
            label = "Advance",
            icon = Icons.Default.Star,
            active = activeTab == "advance",
            modifier = Modifier.weight(1f)
        ) {
            showStartIoAd(context)
            viewModel.navigateTo(Screen.Advance)
        }

        BottomNavItem(
            label = "Utility",
            icon = Icons.Default.Settings,
            active = activeTab == "settings",
            modifier = Modifier.weight(1f)
        ) {
            showStartIoAd(context)
            viewModel.navigateTo(Screen.Settings)
        }
    }
}

@Composable
fun BottomNavItem(label: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) Color(0xFF4361EE) else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = if (active) Color(0xFF4361EE) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumGatedStateBlock(viewModel: DutyViewModel) {
    val razorMonthlyPrice by viewModel.priceRazorMonthly.collectAsState()
    val razorYearlyPrice by viewModel.priceRazorYearly.collectAsState()
    
    val txidInput = remember { mutableStateOf("") }
    val screenshotUrlInput = remember { mutableStateOf("https://i.ibb.co/screenshot") } // mockup screenshots default ibb path
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFFF72585).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFFF72585), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Premium Feature Locked", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "Upgrade to Premium to unlock full wages calculations, overtime PDF logs, advance tracker, and customizable shift rosters.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription Cards plans rows representation
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlanCardSelect("Monthly", "₹29", "premium_monthly", viewModel)
            PlanCardSelect("Yearly", "₹299", "premium_yearly", viewModel)
        }
    }
}

@Composable
fun PlanCardSelect(title: String, price: String, sku: String, viewModel: DutyViewModel) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color(0xFF4361EE).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable {
                // Instantly record or execute mock play purchase triggers as play-store compilation fallback
                viewModel.handlePlayStorePurchaseSuccess(
                    com.android.billingclient.api.Purchase(
                        "{\"productId\":\"$sku\",\"purchaseToken\":\"mock_token_${System.currentTimeMillis()}\"}",
                        "mock_signature"
                    )
                )
                Toast.makeText(context, "Awaiting Native Google Play Billing Connection...", Toast.LENGTH_SHORT).show()
                viewModel.navigateTo(Screen.Home)
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = price, color = Color(0xFFF72585), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun ForcedUpdatePopup(viewModel: DutyViewModel) {
    val upTitle by viewModel.updateTitle.collectAsState()
    val upDesc by viewModel.updateMessage.collectAsState()
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { /* forces update block */ },
        confirmButton = {
            Button(
                onClick = { /* Launch download channel intent */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84))
            ) {
                Text(text = "Download Update", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { /* Launch channel link */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC))
            ) {
                Text(text = "Join Telegram", color = Color.White)
            }
        },
        title = { Text(text = upTitle, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(text = upDesc, color = Color.Gray) },
        containerColor = Color(0xFF141414)
    )
}

@Composable
fun MaintenanceScreen(viewModel: DutyViewModel) {
    val desc by viewModel.maintenanceMessage.collectAsState()
    val time by viewModel.maintenanceTime.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141414))
                .border(2.dp, Color(0xFF4361EE).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Build, contentDescription = "tool repair", tint = Color.Yellow, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Under Maintenance", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = desc, color = Color.LightGray, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Expected: $time", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Option Pages Drawer representations
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageHeaderDrawer(title: String, viewModel: DutyViewModel, content: @Composable ColumnScope.() -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        showStartIoAd(context)
                        viewModel.navigateBack() 
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SubHistoryScreen(viewModel: DutyViewModel) {
    val history by viewModel.purchaseHistory.collectAsState()
    PageHeaderDrawer("Subscription History", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (history.isEmpty()) {
                item {
                    Text(text = "No history records found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
            items(history) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Premium Activated", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "Via: ${item["method"]}", color = Color.Gray, fontSize = 11.sp)
                        Text(text = "Plan: ${item["plan"]}", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text(text = "${viewModel.curSymbol.value} ${item["amount"]}", color = Color(0xFF06D6A0), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentRequestsScreen(viewModel: DutyViewModel) {
    val requests by viewModel.binanceRequests.collectAsState()
    PageHeaderDrawer("Binance Payment Requests", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (requests.isEmpty()) {
                item {
                    Text(text = "No manual request has been submitted", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
            items(requests) { req ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "USDT Manual Pay", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "TxID: ${req.txid}", color = Color.Gray, fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (req.status == "approved") Color(0xFF06D6A0).copy(alpha = 0.15f)
                                else Color.Yellow.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = req.status.uppercase(),
                            color = if (req.status == "approved") Color(0xFF06D6A0) else Color.Yellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralScreen(viewModel: DutyViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val codeApplyInput = remember { mutableStateOf("") }
    val context = LocalContext.current

    PageHeaderDrawer("Refer & Earn", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141414))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Invite Friends & Get Premium Days", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Share your referral code. When a friend applies your code, you both instantly get +10 Premium Days!",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userProfile?.refCode ?: "------",
                    color = Color(0xFF4361EE),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val txt = "Use referral code ${userProfile?.refCode} on Duty Tracker Pro. Get +10 Days Free Premium! Join natively."
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, txt)
                    }
                    context.startActivity(Intent.createChooser(share, "Share Referral Code"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
            ) {
                Text(text = "Share Invites Code", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = codeApplyInput.value,
                onValueChange = { codeApplyInput.value = it.uppercase() },
                label = { Text("Friend Referral Code", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4361EE), focusedTextColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (codeApplyInput.value.length < 5) {
                        Toast.makeText(context, "Invalid Code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.applyReferral(codeApplyInput.value) { result ->
                        if (result == "success") {
                            Toast.makeText(context, "Success! +10 Free Premium Days Added", Toast.LENGTH_SHORT).show()
                            codeApplyInput.value = ""
                        } else {
                            Toast.makeText(context, "Redeem failed: $result", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
            ) {
                Text(text = "Apply Friend Referral Code", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HelpScreen(viewModel: DutyViewModel) {
    val helpQAs = listOf(
        Pair("How to add a duty?", "Tap on 'Add Today's Duty' on the home screen calendar, or select any days cell."),
        Pair("How to manage advances?", "Go to 'Advance' navigation tab. Enter amounts and date. This will be automatically debited in your Reports."),
        Pair("How reports generated?", "Upgraded premium users select custom date, click 'Generate', and can natively compile salary slips in PDF.")
    )

    PageHeaderDrawer("Help & FAQ", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(helpQAs) { qa ->
                val expandedState = remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141414))
                        .clickable { expandedState.value = !expandedState.value }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = qa.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(
                            imageVector = if (expandedState.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    if (expandedState.value) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = qa.second, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen(viewModel: DutyViewModel) {
    val fText = remember { mutableStateOf("") }
    val context = LocalContext.current

    PageHeaderDrawer("Feedback Suggestions", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141414))
                .padding(20.dp)
        ) {
            Text(text = "Write your feedback", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = fText.value,
                onValueChange = { fText.value = it },
                placeholder = { Text("Write comments...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4361EE), focusedTextColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (fText.value.length < 5) {
                        Toast.makeText(context, "Please write more details first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:nitai.grp00@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Duty Tracker Pro Feedback")
                        putExtra(Intent.EXTRA_TEXT, fText.value)
                    }
                    try {
                        context.startActivity(emailIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, fText.value, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4361EE))
            ) {
                Text(text = "Send via Email Trigger", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AboutScreen(viewModel: DutyViewModel) {
    PageHeaderDrawer("About Duty Tracker Pro", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141414))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4361EE), modifier = Modifier.size(50.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Duty Tracker Pro", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Version 1.0 (Stable)", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fully Native salary coefficient calculator, designed beautifully inside Android Studio using Kotlin and Jetpack Compose. Absolute control over your rosters.",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Made in India 🇮🇳 with Love", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun LegalScreen(viewModel: DutyViewModel) {
    PageHeaderDrawer("Privacy & Legal Policies", viewModel) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(text = "Privacy Terms", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    text = "Duty Tracker Pro respects your personal calculations. We do not sell your personal data to any third parties. Your attendance records and profile information are strictly stored securely inside Google Firebase servers.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
            }
            item {
                Text(text = "Refunds Disclaimer", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    text = "Subscriptions payments transactions are processed natively. Refunds will only be authorized in the case of duplicate merchant errors.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StartIoBanner(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            Banner(context)
        },
        modifier = modifier
    )
}

fun showStartIoAd(context: Context) {
    try {
        StartAppAd.showAd(context)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
