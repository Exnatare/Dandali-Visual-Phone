package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.LocationEntity
import com.example.data.model.NoteEntity
import com.example.data.repository.PhoneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Simulated App IDs
enum class PhoneApp(val appName: String, val iconName: String) {
    DIALER("Phone", "phone"),
    CONTACTS("Contacts", "contacts"),
    MESSAGES("Messages", "chat"),
    CAMERA("Camera", "camera"),
    GALLERY("Gallery", "photo_library"),
    CLOCK("Clock", "alarm"),
    CALENDAR("Calendar", "calendar_month"),
    CALCULATOR("Calculator", "calculate"),
    FILE_MANAGER("File Manager", "folder"),
    BROWSER("Browser", "public"),
    MUSIC("Music", "music_note"),
    VIDEO("Video", "movie"),
    NOTES("Notes", "sticky_note_2"),
    DOWNLOADS("Downloads", "download"),
    EMAIL("Email", "email"),
    RECORDER("Recorder", "mic"),
    WEATHER("Weather", "wb_sunny"),
    MAPS("Location Manager", "my_location"),
    PLAY_STORE("Play Store", "shopping_cart"),
    SETTINGS("Settings", "settings"),
    APP_MANAGER("App Manager", "apps"),
    NETWORK_COVERAGE("Network Map", "cell_tower")
}

// Built-in Apps Categories
enum class AppCategory {
    SYSTEM, COMMUNICATION, UTILITIES, MEDIA, LIFESTYLE, DEVELOPER
}

data class AppMetadata(
    val id: PhoneApp,
    val category: AppCategory,
    val packageName: String,
    val version: String = "1.0.4",
    val developer: String = "Dandali Corp",
    val permissions: List<String> = listOf("Internet", "Location", "Storage"),
    val storageSize: String = "15.4 MB",
    val lastUpdated: String = "2026-07-01",
    var isFavorite: Boolean = false,
    var forceStopped: Boolean = false
)

data class PhoneNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val text: String,
    val appName: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val icon: String = "notifications"
)

data class PlayStoreReview(
    val id: String = java.util.UUID.randomUUID().toString(),
    val author: String,
    val rating: Int,
    val date: String,
    val content: String
)

data class SimulatedTower(
    val id: Int,
    val operator: String, // MTN, Airtel, Glo, 9mobile
    val latitude: Double,
    val longitude: Double,
    val strength: Int, // 1 to 5
    val status: String // Active, Maintenance, Offline
)

data class NigerianState(
    val name: String,
    val capital: String,
    val latitude: Double,
    val longitude: Double,
    val region: String,
    val airport: String = "",
    val touristAttraction: String = "",
    val lgaCount: Int = 15
)

class VisualPhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhoneRepository

    // Reactive database flows
    val contactsFlow: StateFlow<List<ContactEntity>>
    val notesFlow: StateFlow<List<NoteEntity>>
    val locationsFlow: StateFlow<List<LocationEntity>>

    // --- OS STATE ---
    private val _isScreenOn = MutableStateFlow(true)
    val isScreenOn: StateFlow<Boolean> = _isScreenOn.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _passcode = MutableStateFlow("1234") // Default simulated PIN
    val passcode: StateFlow<String> = _passcode.asStateFlow()

    private val _lockScreenMode = MutableStateFlow("PIN") // PIN, PATTERN, FINGERPRINT
    val lockScreenMode: StateFlow<String> = _lockScreenMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _wallpaperIndex = MutableStateFlow(0)
    val wallpaperIndex: StateFlow<Int> = _wallpaperIndex.asStateFlow()

    // --- HARDWARE / STATUS BAR ---
    private val _currentTime = MutableStateFlow("12:00 PM")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("Monday, Jul 6")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _batteryLevel = MutableStateFlow(85)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isBatterySaver = MutableStateFlow(false)
    val isBatterySaver: StateFlow<Boolean> = _isBatterySaver.asStateFlow()

    private val _isDnd = MutableStateFlow(false)
    val isDnd: StateFlow<Boolean> = _isDnd.asStateFlow()

    private val _isWifiOn = MutableStateFlow(true)
    val isWifiOn: StateFlow<Boolean> = _isWifiOn.asStateFlow()

    private val _isMobileOn = MutableStateFlow(true)
    val isMobileOn: StateFlow<Boolean> = _isMobileOn.asStateFlow()

    private val _isBluetoothOn = MutableStateFlow(false)
    val isBluetoothOn: StateFlow<Boolean> = _isBluetoothOn.asStateFlow()

    private val _isNfcOn = MutableStateFlow(false)
    val isNfcOn: StateFlow<Boolean> = _isNfcOn.asStateFlow()

    private val _isHotspotOn = MutableStateFlow(false)
    val isHotspotOn: StateFlow<Boolean> = _isHotspotOn.asStateFlow()

    private val _isAirplaneMode = MutableStateFlow(false)
    val isAirplaneMode: StateFlow<Boolean> = _isAirplaneMode.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isRotationLocked = MutableStateFlow(false)
    val isRotationLocked: StateFlow<Boolean> = _isRotationLocked.asStateFlow()

    private val _isGestureNav = MutableStateFlow(false) // Toggle between classical nav bar and gesture pill
    val isGestureNav: StateFlow<Boolean> = _isGestureNav.asStateFlow()

    private val _brightness = MutableStateFlow(0.8f) // 0f to 1f
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _volume = MutableStateFlow(0.7f) // 0f to 1f
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _showVolumeSlider = MutableStateFlow(false)
    val showVolumeSlider: StateFlow<Boolean> = _showVolumeSlider.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    // --- NOTIFICATIONS & DRAWER ---
    val notifications = mutableStateListOf<PhoneNotification>()

    private val _isNotificationPanelExpanded = MutableStateFlow(false)
    val isNotificationPanelExpanded: StateFlow<Boolean> = _isNotificationPanelExpanded.asStateFlow()

    private val _isAppDrawerOpen = MutableStateFlow(false)
    val isAppDrawerOpen: StateFlow<Boolean> = _isAppDrawerOpen.asStateFlow()

    private val _appDrawerQuery = MutableStateFlow("")
    val appDrawerQuery: StateFlow<String> = _appDrawerQuery.asStateFlow()

    // --- ACTIVE APPLICATIONS MANAGER ---
    private val _activeApp = MutableStateFlow<PhoneApp?>(null)
    val activeApp: StateFlow<PhoneApp?> = _activeApp.asStateFlow()

    // Recents View List
    val recentsStack = mutableStateListOf<PhoneApp>()

    private val _isRecentsViewOpen = MutableStateFlow(false)
    val isRecentsViewOpen: StateFlow<Boolean> = _isRecentsViewOpen.asStateFlow()

    // System Logs Console List
    val systemLogs = mutableStateListOf<String>()

    // --- APP METADATA LIST ---
    val appsList = mutableStateListOf<AppMetadata>()

    // Google Play Simulated Apps
    val playStoreApps = listOf(
        AppMetadata(PhoneApp.DIALER, AppCategory.SYSTEM, "com.dandali.dialer", version = "1.5.0", developer = "Dandali Corp", storageSize = "4.2 MB"),
        AppMetadata(PhoneApp.CONTACTS, AppCategory.SYSTEM, "com.dandali.contacts", version = "2.1.2", developer = "Dandali Corp", storageSize = "6.1 MB"),
        AppMetadata(PhoneApp.MESSAGES, AppCategory.SYSTEM, "com.dandali.messages", version = "3.0.1", developer = "Dandali Corp", storageSize = "11.0 MB"),
        AppMetadata(PhoneApp.CAMERA, AppCategory.SYSTEM, "com.dandali.camera", version = "1.0.0", developer = "Dandali Corp", storageSize = "18.3 MB"),
        AppMetadata(PhoneApp.GALLERY, AppCategory.SYSTEM, "com.dandali.gallery", version = "1.2.0", developer = "Dandali Corp", storageSize = "9.5 MB"),
        AppMetadata(PhoneApp.CLOCK, AppCategory.UTILITIES, "com.dandali.clock", version = "1.4.1", developer = "Dandali Corp", storageSize = "3.2 MB"),
        AppMetadata(PhoneApp.CALENDAR, AppCategory.UTILITIES, "com.dandali.calendar", version = "1.1.0", developer = "Dandali Corp", storageSize = "5.4 MB"),
        AppMetadata(PhoneApp.CALCULATOR, AppCategory.UTILITIES, "com.dandali.calculator", version = "2.0.0", developer = "Dandali Corp", storageSize = "1.8 MB"),
        AppMetadata(PhoneApp.FILE_MANAGER, AppCategory.UTILITIES, "com.dandali.files", version = "1.6.0", developer = "Dandali Corp", storageSize = "12.7 MB"),
        AppMetadata(PhoneApp.BROWSER, AppCategory.UTILITIES, "com.dandali.browser", version = "4.2.1", developer = "Dandali Corp", storageSize = "22.1 MB"),
        AppMetadata(PhoneApp.MUSIC, AppCategory.MEDIA, "com.dandali.music", version = "1.0.5", developer = "Dandali Corp", storageSize = "14.2 MB"),
        AppMetadata(PhoneApp.VIDEO, AppCategory.MEDIA, "com.dandali.video", version = "1.0.1", developer = "Dandali Corp", storageSize = "16.8 MB"),
        AppMetadata(PhoneApp.NOTES, AppCategory.LIFESTYLE, "com.dandali.notes", version = "2.2.0", developer = "Dandali Corp", storageSize = "4.8 MB"),
        AppMetadata(PhoneApp.DOWNLOADS, AppCategory.UTILITIES, "com.dandali.downloads", version = "1.0.0", developer = "Dandali Corp", storageSize = "2.9 MB"),
        AppMetadata(PhoneApp.EMAIL, AppCategory.COMMUNICATION, "com.dandali.email", version = "1.3.0", developer = "Dandali Corp", storageSize = "15.0 MB"),
        AppMetadata(PhoneApp.RECORDER, AppCategory.MEDIA, "com.dandali.recorder", version = "1.1.2", developer = "Dandali Corp", storageSize = "3.8 MB"),
        AppMetadata(PhoneApp.WEATHER, AppCategory.LIFESTYLE, "com.dandali.weather", version = "2.5.1", developer = "Dandali Corp", storageSize = "8.3 MB"),
        AppMetadata(PhoneApp.MAPS, AppCategory.LIFESTYLE, "com.dandali.maps", version = "3.1.0", developer = "Dandali Corp", storageSize = "25.6 MB"),
        AppMetadata(PhoneApp.SETTINGS, AppCategory.SYSTEM, "com.dandali.settings", version = "1.0.0", developer = "Dandali Corp", storageSize = "8.9 MB"),
        AppMetadata(PhoneApp.APP_MANAGER, AppCategory.DEVELOPER, "com.dandali.appmanager", version = "1.0.2", developer = "Dandali Corp", storageSize = "5.5 MB"),
        AppMetadata(PhoneApp.NETWORK_COVERAGE, AppCategory.DEVELOPER, "com.dandali.network", version = "1.0.0", developer = "Dandali Corp", storageSize = "7.2 MB")
    )

    // --- GOOGLE PLAY INTERFACE STATE ---
    private val _playStoreSelectedApp = MutableStateFlow<AppMetadata?>(null)
    val playStoreSelectedApp: StateFlow<AppMetadata?> = _playStoreSelectedApp.asStateFlow()

    private val _playStoreQuery = MutableStateFlow("")
    val playStoreQuery: StateFlow<String> = _playStoreQuery.asStateFlow()

    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn: StateFlow<Boolean> = _isGoogleSignedIn.asStateFlow()

    private val _googleEmail = MutableStateFlow("dandali.user@gmail.com")
    val googleEmail: StateFlow<String> = _googleEmail.asStateFlow()

    // Google Play Store Reviews State
    private val _playStoreReviews = MutableStateFlow<Map<PhoneApp, List<PlayStoreReview>>>(emptyMap())
    val playStoreReviews: StateFlow<Map<PhoneApp, List<PlayStoreReview>>> = _playStoreReviews.asStateFlow()

    // --- APK MANAGEMENT ---
    val downloadedApks = mutableStateListOf<AppMetadata>()

    data class DownloadTask(
        val appName: String,
        val progress: Float, // 0 to 1
        val status: String, // Downloading, Paused, Completed, Cancelled
        var job: Job? = null
    )
    val activeDownloads = mutableStateListOf<DownloadTask>()

    // --- LOCATION SIMULATION STATE ---
    private val _simulatedLatitude = MutableStateFlow(9.0765) // Default FCT Abuja
    val simulatedLatitude: StateFlow<Double> = _simulatedLatitude.asStateFlow()

    private val _simulatedLongitude = MutableStateFlow(7.3986)
    val simulatedLongitude: StateFlow<Double> = _simulatedLongitude.asStateFlow()

    private val _simulatedAccuracy = MutableStateFlow(3.2f) // in meters
    val simulatedAccuracy: StateFlow<Float> = _simulatedAccuracy.asStateFlow()

    private val _simulatedAltitude = MutableStateFlow(491.5) // in meters
    val simulatedAltitude: StateFlow<Double> = _simulatedAltitude.asStateFlow()

    private val _simulatedSpeed = MutableStateFlow(0.0f) // in km/h
    val simulatedSpeed: StateFlow<Float> = _simulatedSpeed.asStateFlow()

    private val _simulatedHeading = MutableStateFlow(180.0f) // Degrees
    val simulatedHeading: StateFlow<Float> = _simulatedHeading.asStateFlow()

    private val _simulatedStateName = MutableStateFlow("Federal Capital Territory")
    val simulatedStateName: StateFlow<String> = _simulatedStateName.asStateFlow()

    private val _simulatedCityName = MutableStateFlow("Abuja")
    val simulatedCityName: StateFlow<String> = _simulatedCityName.asStateFlow()

    private val _locationSearchQuery = MutableStateFlow("")
    val locationSearchQuery: StateFlow<String> = _locationSearchQuery.asStateFlow()

    val nigerianStatesList = listOf(
        NigerianState("Federal Capital Territory", "Abuja", 9.0765, 7.3986, "North Central", "Nnamdi Azikiwe Int'l Airport", "Zuma Rock", 6),
        NigerianState("Abia", "Umuahia", 5.5267, 7.4898, "South East", "Aba Airport (Airstrip)", "Arochukwu Long Juju", 17),
        NigerianState("Adamawa", "Yola", 9.2035, 12.4954, "North East", "Yola Airport", "Sukur Cultural Landscape", 21),
        NigerianState("Akwa Ibom", "Uyo", 5.0377, 7.9128, "South South", "Victor Attah Int'l Airport", "Ibeno Beach", 31),
        NigerianState("Anambra", "Awka", 6.2105, 7.0699, "South East", "Anambra Int'l Cargo Airport", "Ogbunike Caves", 21),
        NigerianState("Bauchi", "Bauchi", 10.3158, 9.8442, "North East", "Sir Abubakar Tafawa Balewa Airport", "Yankari Game Reserve", 20),
        NigerianState("Bayelsa", "Yenagoa", 4.9267, 6.2633, "South South", "Bayelsa Int'l Airport", "Akassa Lighthouse", 8),
        NigerianState("Benue", "Makurdi", 7.7322, 8.5391, "North Central", "Makurdi Air Force Base", "Ushongo Hills", 23),
        NigerianState("Borno", "Maiduguri", 11.8311, 13.1509, "North East", "Maiduguri Int'l Airport", "Lake Chad National Park", 27),
        NigerianState("Cross River", "Calabar", 4.9757, 8.3417, "South South", "Margaret Ekpo Int'l Airport", "Obudu Mountain Resort", 18),
        NigerianState("Delta", "Asaba", 6.1998, 6.7264, "South South", "Asaba Int'l Airport", "Lander Brothers Anchorage", 25),
        NigerianState("Ebonyi", "Abakaliki", 6.3236, 8.1132, "South East", "Ebonyi State Int'l Airport", "Amanchor Cave", 13),
        NigerianState("Edo", "Benin City", 6.3350, 5.6037, "South South", "Benin Airport", "Royal Palace of Oba of Benin", 18),
        NigerianState("Ekiti", "Ado Ekiti", 7.6212, 5.2215, "South West", "Ekiti State Cargo Airport", "Ikogosi Warm Springs", 16),
        NigerianState("Enugu", "Enugu", 6.4584, 7.5083, "South East", "Akanu Ibiam Int'l Airport", "Udi Hills", 17),
        NigerianState("Gombe", "Gombe", 10.2897, 11.1673, "North East", "Gombe Lawanti Int'l Airport", "Bima Hill", 11),
        NigerianState("Imo", "Owerri", 5.4831, 7.0352, "South East", "Sam Mbakwe Airport", "Oguta Lake Resort", 27),
        NigerianState("Jigawa", "Dutse", 11.7512, 9.3381, "North West", "Dutse Int'l Airport", "Baturiya Bird Sanctuary", 27),
        NigerianState("Kaduna", "Kaduna", 10.5105, 7.4165, "North West", "Kaduna Int'l Airport", "Kajuru Castle", 23),
        NigerianState("Kano", "Kano", 12.0022, 8.5919, "North West", "Mallam Aminu Kano Int'l Airport", "Dala Hill", 44),
        NigerianState("Katsina", "Katsina", 12.9855, 7.6171, "North West", "Katsina Airport", "Gobarau Minaret", 34),
        NigerianState("Kebbi", "Birnin Kebbi", 12.4504, 4.1975, "North West", "Sir Ahmadu Bello Airport", "Argungu Fishing Festival", 21),
        NigerianState("Kogi", "Lokoja", 7.7969, 6.7405, "North Central", "Lokoja Airstrip", "Mount Patti", 21),
        NigerianState("Kwara", "Ilorin", 8.4855, 4.5419, "North Central", "Ilorin Int'l Airport", "Owu Waterfalls", 16),
        NigerianState("Lagos", "Ikeja", 6.5244, 3.3792, "South West", "Murtala Muhammed Int'l Airport", "Lekki Conservation Centre", 20),
        NigerianState("Nasarawa", "Lafia", 8.4905, 8.5153, "North Central", "Lafia Cargo Airport", "Farin Ruwa Falls", 13),
        NigerianState("Niger", "Minna", 9.6139, 6.5569, "North Central", "Minna Airport", "Gurara Waterfalls", 25),
        NigerianState("Ogun", "Abeokuta", 7.1475, 3.3619, "South West", "Gateway Agro-Cargo Airport", "Olumo Rock", 20),
        NigerianState("Ondo", "Akure", 7.2571, 5.2058, "South West", "Akure Airport", "Idanre Hills", 18),
        NigerianState("Osun", "Osogbo", 7.7827, 4.5419, "South West", "MKO Abiola Int'l Airport", "Osun-Osogbo Sacred Grove", 30),
        NigerianState("Oyo", "Ibadan", 7.3775, 3.9470, "South West", "Ibadan Airport", "Bowers Tower", 33),
        NigerianState("Plateau", "Jos", 9.8965, 8.8583, "North Central", "Yakubu Gowon Airport", "Kurra Falls", 17),
        NigerianState("Rivers", "Port Harcourt", 4.8156, 7.0498, "South South", "Port Harcourt Int'l Airport", "Bonny Island", 23),
        NigerianState("Sokoto", "Sokoto", 13.0059, 5.2476, "North West", "Sadiq Abubakar III Int'l Airport", "Sultan of Sokoto Palace", 23),
        NigerianState("Taraba", "Jalingo", 8.8922, 11.3733, "North East", "Jalingo Airport", "Mambilla Plateau", 16),
        NigerianState("Yobe", "Damaturu", 12.0000, 11.5000, "North East", "Damaturu Airport", "Dufuna Canoe Site", 17),
        NigerianState("Zamfara", "Gusau", 12.1628, 6.6614, "North West", "Gusau Airstrip", "Kwiambana Forest Reserve", 14)
    )

    // --- NETWORK SITE SITE SIMULATIONS ---
    val simulatedTowers = mutableStateListOf<SimulatedTower>()

    // --- BUILT-IN APPLICATIONS STATES ---

    // DIALER
    private val _dialerNumber = MutableStateFlow("")
    val dialerNumber: StateFlow<String> = _dialerNumber.asStateFlow()

    private val _activeCallState = MutableStateFlow("IDLE") // IDLE, DIALING, ACTIVE, ENDED
    val activeCallState: StateFlow<String> = _activeCallState.asStateFlow()

    private val _activeCallContactName = MutableStateFlow("")
    val activeCallContactName: StateFlow<String> = _activeCallContactName.asStateFlow()

    private val _callTimerSeconds = MutableStateFlow(0)
    val callTimerSeconds: StateFlow<Int> = _callTimerSeconds.asStateFlow()

    val callLogs = mutableStateListOf<Map<String, String>>() // name, phone, type (in/out/missed), time

    // SMS MESSAGES
    private val _messageSearchQuery = MutableStateFlow("")
    val messageSearchQuery: StateFlow<String> = _messageSearchQuery.asStateFlow()

    private val _activeMessageContact = MutableStateFlow<ContactEntity?>(null)
    val activeMessageContact: StateFlow<ContactEntity?> = _activeMessageContact.asStateFlow()

    private val _chatMessageInput = MutableStateFlow("")
    val chatMessageInput: StateFlow<String> = _chatMessageInput.asStateFlow()

    data class SmsMessage(
        val sender: String,
        val body: String,
        val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        val isIncoming: Boolean
    )
    val chatMessages = mutableStateListOf<SmsMessage>()

    // NOTES CATEGORY FOR DIALOG
    private val _notesEditorTitle = MutableStateFlow("")
    val notesEditorTitle: StateFlow<String> = _notesEditorTitle.asStateFlow()

    private val _notesEditorContent = MutableStateFlow("")
    val notesEditorContent: StateFlow<String> = _notesEditorContent.asStateFlow()

    private val _notesEditorColor = MutableStateFlow("#FF9800")
    val notesEditorColor: StateFlow<String> = _notesEditorColor.asStateFlow()

    private val _editingNoteId = MutableStateFlow<Int?>(null)
    val editingNoteId: StateFlow<Int?> = _editingNoteId.asStateFlow()

    // STOPWATCH & TIMER
    private val _stopwatchTimeMs = MutableStateFlow(0L)
    val stopwatchTimeMs: StateFlow<Long> = _stopwatchTimeMs.asStateFlow()

    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning.asStateFlow()

    val stopwatchLaps = mutableStateListOf<String>()

    private val _timerTimeRemaining = MutableStateFlow(0) // Seconds
    val timerTimeRemaining: StateFlow<Int> = _timerTimeRemaining.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    // MUSIC PLAYER
    data class SimulatedSong(
        val title: String,
        val artist: String,
        val duration: String,
        val coverColorHex: String
    )
    val songsList = listOf(
        SimulatedSong("Essence", "Wizkid ft. Tems", "3:20", "#FF4081"),
        SimulatedSong("Last Last", "Burna Boy", "2:52", "#E040FB"),
        SimulatedSong("Calm Down", "Rema", "3:39", "#00E5FF"),
        SimulatedSong("Buga", "Kizz Daniel", "3:04", "#76FF03"),
        SimulatedSong("Ku Lo Sa", "Ox開de", "2:27", "#FFD700"),
        SimulatedSong("Bandana", "Fireboy DML & Asake", "2:58", "#FF5722")
    )
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _musicProgress = MutableStateFlow(0.0f) // 0 to 1
    val musicProgress: StateFlow<Float> = _musicProgress.asStateFlow()

    // BROWSER
    private val _browserUrl = MutableStateFlow("https://www.google.com")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    val browserHistory = mutableStateListOf<String>()

    // EMAIL CLIENT
    data class SimulatedEmail(
        val id: String = UUID.randomUUID().toString(),
        val sender: String,
        val subject: String,
        val snippet: String,
        val time: String,
        val isRead: Boolean = false,
        val folder: String = "INBOX" // INBOX, SENT, DRAFTS
    )
    val emailsList = mutableStateListOf<SimulatedEmail>()

    private val _selectedEmail = MutableStateFlow<SimulatedEmail?>(null)
    val selectedEmail: StateFlow<SimulatedEmail?> = _selectedEmail.asStateFlow()

    // CALCULATOR
    private val _calcFormula = MutableStateFlow("")
    val calcFormula: StateFlow<String> = _calcFormula.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    // CAMERA & GALLERY IMAGES
    val galleryImages = mutableStateListOf<String>() // Mock photo IDs / base64 or custom drawable codes

    // VOICE RECORDER
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    val voiceRecordings = mutableStateListOf<Map<String, String>>() // title, date, duration

    // SENSOR TEST VALUES
    private val _accelX = MutableStateFlow(0.0f)
    val accelX: StateFlow<Float> = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(9.8f) // Gravity
    val accelY: StateFlow<Float> = _accelY.asStateFlow()

    private val _accelZ = MutableStateFlow(0.1f)
    val accelZ: StateFlow<Float> = _accelZ.asStateFlow()

    private val _gyroX = MutableStateFlow(0.01f)
    val gyroX: StateFlow<Float> = _gyroX.asStateFlow()

    private val _gyroY = MutableStateFlow(-0.02f)
    val gyroY: StateFlow<Float> = _gyroY.asStateFlow()

    private val _gyroZ = MutableStateFlow(0.0f)
    val gyroZ: StateFlow<Float> = _gyroZ.asStateFlow()

    private val _ambientLight = MutableStateFlow(320f) // Lux
    val ambientLight: StateFlow<Float> = _ambientLight.asStateFlow()

    // CPU & RAM METRICS
    private val _cpuUsage = MutableStateFlow(12) // Percentage
    val cpuUsage: StateFlow<Int> = _cpuUsage.asStateFlow()

    private val _ramFree = MutableStateFlow(3.4f) // GB
    val ramFree: StateFlow<Float> = _ramFree.asStateFlow()

    private val _ramTotal = MutableStateFlow(8.0f) // GB

    // --- COROUTINE TICKERS ---
    private var clockJob: Job? = null
    private var telemetryJob: Job? = null
    private var activeCallJob: Job? = null
    private var stopwatchJob: Job? = null
    private var timerJob: Job? = null
    private var songProgressJob: Job? = null
    private var voiceRecorderJob: Job? = null
    private var volumeSliderJob: Job? = null

    // --- WORKER METHODS ---

    private fun seedDataIfNeeded() {
        viewModelScope.launch {
            // Seed contacts if empty
            contactsFlow.collect { list ->
                if (list.isEmpty()) {
                    val defaultContacts = listOf(
                        ContactEntity(name = "Aliyu Ibrahim", phone = "+234 803 123 4567", email = "aliyu@gmail.com", state = "Kano", notes = "Business partner", isFavorite = true),
                        ContactEntity(name = "Chioma Nwachukwu", phone = "+234 812 345 6789", email = "chioma.n@yahoo.com", state = "Enugu", notes = "Classmate", isFavorite = false),
                        ContactEntity(name = "Oluwaseun Adebayo", phone = "+234 905 555 1122", email = "seun.ade@gmail.com", state = "Lagos", notes = "Developer Friend", isFavorite = true),
                        ContactEntity(name = "Fatima Musa", phone = "+234 809 999 8877", email = "fatima.m@dandali.ng", state = "Federal Capital Territory", notes = "Sister", isFavorite = true),
                        ContactEntity(name = "Chidi Okafor", phone = "+234 703 444 5555", email = "chidi@gmail.com", state = "Anambra", notes = "Tech support", isFavorite = false)
                    )
                    defaultContacts.forEach { repository.insertContact(it) }
                }
            }
        }

        viewModelScope.launch {
            notesFlow.collect { list ->
                if (list.isEmpty()) {
                    val defaultNotes = listOf(
                        NoteEntity(title = "Welcome to Dandali", content = "Dandali is your premium Visual Phone simulation experience. Build, demonstrate, and test applications securely and efficiently.", colorHex = "#4CAF50"),
                        NoteEntity(title = "Nigeria States Project", content = "Make sure to verify and test location changer boundaries in Abuja, Kano, Enugu, and Lagos for standard compliance.", colorHex = "#2196F3"),
                        NoteEntity(title = "Shopping List", content = "- Android test device\n- USB OTG adapter\n- Standard power bank", colorHex = "#FFEB3B")
                    )
                    defaultNotes.forEach { repository.insertNote(it) }
                }
            }
        }

        viewModelScope.launch {
            locationsFlow.collect { list ->
                if (list.isEmpty()) {
                    val defaultFavorites = listOf(
                        LocationEntity(name = "Abuja Central Mosque", latitude = 9.0624, longitude = 7.4871, isFavorite = true),
                        LocationEntity(name = "Murtala Muhammed Airport (LOS)", latitude = 6.5774, longitude = 3.3210, isFavorite = true),
                        LocationEntity(name = "Kano Ancient City Wall", latitude = 12.0094, longitude = 8.5123, isFavorite = false)
                    )
                    defaultFavorites.forEach { repository.insertLocation(it) }
                }
            }
        }

        // Setup active system apps metadata
        appsList.addAll(playStoreApps.map { it.copy() })

        // Initialize mock emails
        emailsList.addAll(listOf(
            SimulatedEmail(sender = "Google Account Team", subject = "New Sign-in on Dandali Visual Phone", snippet = "Your Google Account was successfully registered on a new Dandali virtual device.", time = "11:42 AM", isRead = false, folder = "INBOX"),
            SimulatedEmail(sender = "MTN Nigeria Support", subject = "Exclusive 5G Offer for Dandali Users", snippet = "Y'ello! Get up to 10GB free data when you activate your new Dandali simulation line.", time = "10:15 AM", isRead = true, folder = "INBOX"),
            SimulatedEmail(sender = "Fatima Musa", subject = "Meeting in Abuja Tomorrow", snippet = "Hi brother, let me know when you arrive at Abuja Airport so we can meet up.", time = "9:05 AM", isRead = false, folder = "INBOX")
        ))

        // Preload Gallery Photos with nice geometric gradient strings
        galleryImages.addAll(listOf("wall_sunset", "wall_cool_purple", "wall_emerald", "capture_1", "capture_2"))

        // Create simulated cell site towers in Nigerian states
        generateSimulatedTowers()
    }

    private fun generateSimulatedTowers() {
        val operators = listOf("MTN Nigeria", "Airtel Nigeria", "Glo", "9mobile")
        var id = 1
        for (state in nigerianStatesList) {
            // Distribute 2-3 towers near each state capital coordinates
            for (i in 0..1) {
                val operator = operators[(id + i) % operators.size]
                val offsetLat = (Math.random() - 0.5) * 0.15
                val offsetLon = (Math.random() - 0.5) * 0.15
                val strength = (3..5).random()
                val status = if (Math.random() > 0.08) "Active" else "Maintenance"
                simulatedTowers.add(
                    SimulatedTower(
                        id = id++,
                        operator = operator,
                        latitude = state.latitude + offsetLat,
                        longitude = state.longitude + offsetLon,
                        strength = strength,
                        status = status
                    )
                )
            }
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                _currentTime.value = timeFormat.format(cal.time)
                _currentDate.value = dateFormat.format(cal.time)
                delay(15000)
            }
        }
    }

    private fun simulateTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                // Fluctuating performance metrics
                _cpuUsage.value = (8..35).random()
                _ramFree.value = Math.max(1.8f, Math.min(6.2f, _ramFree.value + (Math.random().toFloat() - 0.5f) * 0.2f))

                // Sensors slightly changing
                _accelX.value = (Math.random().toFloat() - 0.5f) * 0.4f
                _accelY.value = 9.8f + (Math.random().toFloat() - 0.5f) * 0.2f
                _accelZ.value = (Math.random().toFloat() - 0.5f) * 0.4f

                _gyroX.value = (Math.random().toFloat() - 0.5f) * 0.05f
                _gyroY.value = (Math.random().toFloat() - 0.5f) * 0.05f

                _ambientLight.value = Math.max(10f, Math.min(800f, _ambientLight.value + (Math.random().toFloat() - 0.5f) * 20f))

                delay(3000)
            }
        }
    }

    // Add simulated log to developer console
    fun logEvent(tag: String, message: String) {
        val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        systemLogs.add("[$stamp] $tag: $message")
        if (systemLogs.size > 200) {
            systemLogs.removeAt(0)
        }
    }

    // --- ACTIONS & CONTROLS ---

    fun pressPowerButton() {
        if (!_isScreenOn.value) {
            _isScreenOn.value = true
            logEvent("PowerButton", "Screen Turned ON")
        } else {
            _isScreenOn.value = false
            _isNotificationPanelExpanded.value = false
            _isAppDrawerOpen.value = false
            logEvent("PowerButton", "Screen Turned OFF")
        }
    }

    fun triggerLockScreenUnlock(pinInput: String) {
        if (pinInput == _passcode.value) {
            _isLocked.value = false
            logEvent("LockScreen", "Device successfully UNLOCKED")
        } else {
            logEvent("LockScreen", "Invalid PIN entered")
        }
    }

    fun lockDevice() {
        _isLocked.value = true
        _isNotificationPanelExpanded.value = false
        _isAppDrawerOpen.value = false
        _activeApp.value = null
        logEvent("LockScreen", "Device locked")
    }

    fun changeWallpaper() {
        val next = (_wallpaperIndex.value + 1) % 4
        _wallpaperIndex.value = next
        logEvent("SystemSettings", "Wallpaper changed to Option $next")
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        logEvent("SystemSettings", "Dark Theme: ${_isDarkMode.value}")
    }

    fun toggleBatterySaver() {
        _isBatterySaver.value = !_isBatterySaver.value
        logEvent("SystemSettings", "Battery Saver: ${_isBatterySaver.value}")
    }

    fun toggleDnd() {
        _isDnd.value = !_isDnd.value
        logEvent("SystemSettings", "Do Not Disturb: ${_isDnd.value}")
    }

    fun toggleWifi() {
        _isWifiOn.value = !_isWifiOn.value
        logEvent("Hardware", "Wi-Fi: ${_isWifiOn.value}")
    }

    fun toggleMobileNetwork() {
        _isMobileOn.value = !_isMobileOn.value
        logEvent("Hardware", "Mobile Network: ${_isMobileOn.value}")
    }

    fun toggleBluetooth() {
        _isBluetoothOn.value = !_isBluetoothOn.value
        logEvent("Hardware", "Bluetooth: ${_isBluetoothOn.value}")
    }

    fun toggleNfc() {
        _isNfcOn.value = !_isNfcOn.value
        logEvent("Hardware", "NFC: ${_isNfcOn.value}")
    }

    fun toggleHotspot() {
        _isHotspotOn.value = !_isHotspotOn.value
        logEvent("Hardware", "Hotspot: ${_isHotspotOn.value}")
    }

    fun toggleAirplaneMode() {
        _isAirplaneMode.value = !_isAirplaneMode.value
        if (_isAirplaneMode.value) {
            _isWifiOn.value = false
            _isMobileOn.value = false
            _isBluetoothOn.value = false
        }
        logEvent("Hardware", "Airplane Mode: ${_isAirplaneMode.value}")
    }

    fun toggleFlashlight() {
        _isFlashlightOn.value = !_isFlashlightOn.value
        logEvent("Hardware", "Flashlight: ${_isFlashlightOn.value}")
    }

    fun toggleRotation() {
        _isRotationLocked.value = !_isRotationLocked.value
        logEvent("Hardware", "Rotation Lock: ${_isRotationLocked.value}")
    }

    fun toggleNavigationStyle() {
        _isGestureNav.value = !_isGestureNav.value
        logEvent("SystemSettings", "Gesture Navigation: ${_isGestureNav.value}")
    }

    fun setBrightness(value: Float) {
        _brightness.value = value
        logEvent("Display", "Brightness set to ${Math.round(value * 100)}%")
    }

    fun adjustVolume(delta: Float) {
        _volume.value = Math.max(0.0f, Math.min(1.0f, _volume.value + delta))
        logEvent("Audio", "Volume adjusted to ${Math.round(_volume.value * 100)}%")
        showVolumeSliderTemporarily()
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
        logEvent("SystemSettings", "Full Screen mode: ${_isFullScreen.value}")
        showVolumeSliderTemporarily()
    }

    private fun showVolumeSliderTemporarily() {
        _showVolumeSlider.value = true
        volumeSliderJob?.cancel()
        volumeSliderJob = viewModelScope.launch {
            delay(4000)
            _showVolumeSlider.value = false
        }
    }

    fun dismissVolumeSlider() {
        volumeSliderJob?.cancel()
        _showVolumeSlider.value = false
    }

    fun showNotificationBar(show: Boolean) {
        _isNotificationPanelExpanded.value = show
    }

    fun openAppDrawer(show: Boolean) {
        _isAppDrawerOpen.value = show
        if (show) {
            _appDrawerQuery.value = ""
        }
    }

    fun setAppDrawerQuery(query: String) {
        _appDrawerQuery.value = query
    }

    // --- APP NAVIGATION STACK MANAGEMENT ---

    fun launchApp(app: PhoneApp) {
        val metadata = appsList.find { it.id == app }
        if (metadata?.forceStopped == true) {
            logEvent("AppManager", "Cannot launch ${app.appName} because it is Force Stopped. Enable/Start in App Manager.")
            return
        }

        _activeApp.value = app
        _isAppDrawerOpen.value = false
        _isNotificationPanelExpanded.value = false

        // Add to recents stack
        if (!recentsStack.contains(app)) {
            recentsStack.add(app)
        }

        logEvent("SystemOS", "Launched Application: ${app.appName}")
    }

    fun goHome() {
        _activeApp.value = null
        _isAppDrawerOpen.value = false
        _isNotificationPanelExpanded.value = false
        _isRecentsViewOpen.value = false
        logEvent("SystemOS", "Navigated to Home Screen")
    }

    fun goBack() {
        if (_isNotificationPanelExpanded.value) {
            _isNotificationPanelExpanded.value = false
        } else if (_isAppDrawerOpen.value) {
            _isAppDrawerOpen.value = false
        } else if (_isRecentsViewOpen.value) {
            _isRecentsViewOpen.value = false
        } else if (_activeApp.value != null) {
            _activeApp.value = null
            logEvent("SystemOS", "Back key: Minimised app")
        }
    }

    fun openRecents() {
        _isRecentsViewOpen.value = true
        _isNotificationPanelExpanded.value = false
        _isAppDrawerOpen.value = false
        logEvent("SystemOS", "Opened Recents Apps Drawer")
    }

    fun clearAllRecents() {
        recentsStack.clear()
        _isRecentsViewOpen.value = false
        _activeApp.value = null
        logEvent("SystemOS", "Cleared all background tasks")
    }

    fun closeRecentApp(app: PhoneApp) {
        recentsStack.remove(app)
        if (_activeApp.value == app) {
            _activeApp.value = null
        }
        if (recentsStack.isEmpty()) {
            _isRecentsViewOpen.value = false
        }
        logEvent("SystemOS", "Closed app from recents: ${app.appName}")
    }

    // --- BUILT-IN APPLICATION ACTIONS ---

    // PHONE DIALER
    fun enterDialerDigit(char: Char) {
        _dialerNumber.value = _dialerNumber.value + char
    }

    fun backspaceDialer() {
        if (_dialerNumber.value.isNotEmpty()) {
            _dialerNumber.value = _dialerNumber.value.dropLast(1)
        }
    }

    fun startCall(number: String) {
        if (number.trim().isEmpty()) return
        _dialerNumber.value = number
        _activeCallState.value = "DIALING"

        // Search contacts
        val match = contactsFlow.value.find { it.phone.replace(" ", "") == number.replace(" ", "") || it.phone.contains(number) }
        _activeCallContactName.value = match?.name ?: "Unknown Number"

        logEvent("Dialer", "Dialing: $number")

        activeCallJob?.cancel()
        activeCallJob = viewModelScope.launch {
            delay(2500)
            _activeCallState.value = "ACTIVE"
            logEvent("Dialer", "Call Connected")
            _callTimerSeconds.value = 0
            while (_activeCallState.value == "ACTIVE") {
                delay(1000)
                _callTimerSeconds.value += 1
            }
        }
    }

    fun endCall() {
        activeCallJob?.cancel()
        if (_activeCallState.value != "IDLE") {
            val duration = if (_activeCallState.value == "ACTIVE") {
                val sec = _callTimerSeconds.value
                String.format("%02d:%02d", sec / 60, sec % 60)
            } else "00:00"

            val item = mapOf(
                "name" to _activeCallContactName.value,
                "phone" to _dialerNumber.value,
                "type" to "OUTGOING",
                "time" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) + " ($duration)"
            )
            callLogs.add(0, item)
            _activeCallState.value = "ENDED"
            logEvent("Dialer", "Call Ended. Duration: $duration")
            viewModelScope.launch {
                delay(1500)
                _activeCallState.value = "IDLE"
                _dialerNumber.value = ""
            }
        }
    }

    fun simulateIncomingCall(callerName: String, callerNumber: String) {
        _activeCallContactName.value = callerName
        _dialerNumber.value = callerNumber
        _activeCallState.value = "INCOMING"
        launchApp(PhoneApp.DIALER)
        logEvent("Dialer", "Incoming Call from: $callerName")
    }

    fun answerCall() {
        if (_activeCallState.value == "INCOMING") {
            _activeCallState.value = "ACTIVE"
            logEvent("Dialer", "Call Answered")
            _callTimerSeconds.value = 0
            activeCallJob?.cancel()
            activeCallJob = viewModelScope.launch {
                while (_activeCallState.value == "ACTIVE") {
                    delay(1000)
                    _callTimerSeconds.value += 1
                }
            }
        }
    }

    // MESSAGES
    fun startChatWith(contact: ContactEntity) {
        _activeMessageContact.value = contact
        chatMessages.clear()

        // Seed some history
        chatMessages.addAll(listOf(
            SmsMessage(contact.name, "Hello, can we discuss the project?", "10:05 AM", true),
            SmsMessage("Me", "Sure! I am simulating this inside the Dandali Visual Phone.", "10:10 AM", false),
            SmsMessage(contact.name, "Amazing! Does location manager work as well?", "10:11 AM", true)
        ))
        launchApp(PhoneApp.MESSAGES)
    }

    fun setChatMessageInput(text: String) {
        _chatMessageInput.value = text
    }

    fun sendChatMessage() {
        val input = _chatMessageInput.value.trim()
        if (input.isEmpty()) return
        val contact = _activeMessageContact.value ?: return

        chatMessages.add(SmsMessage("Me", input, isIncoming = false))
        _chatMessageInput.value = ""
        logEvent("Messages", "SMS Sent to ${contact.name}")

        // Auto automated reply after a delay
        viewModelScope.launch {
            delay(1500)
            val automatedReplies = listOf(
                "Y'ello! This is a simulated response inside Dandali. Visual Phone working beautifully!",
                "Great! Let's schedule a demonstration on Google AI Studio streaming emulator.",
                "Yes indeed! The advanced map showing MTN, Airtel, Glo coverage matches expectations perfectly.",
                "Awesome! I'll catch up with you later."
            )
            val reply = automatedReplies.random()
            chatMessages.add(SmsMessage(contact.name, reply, isIncoming = true))
            addSystemNotification(
                title = "Message from ${contact.name}",
                text = reply,
                appName = "Messages"
            )
            logEvent("Messages", "SMS Received from ${contact.name}")
        }
    }

    // CONTACT MANAGEMENT
    fun saveNewContact(name: String, phone: String, email: String, state: String, notes: String) {
        viewModelScope.launch {
            val contact = ContactEntity(
                name = name,
                phone = phone,
                email = email,
                state = state,
                notes = notes
            )
            repository.insertContact(contact)
            logEvent("Contacts", "Saved Contact: $name ($phone)")
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            logEvent("Contacts", "Deleted Contact: ${contact.name}")
        }
    }

    // NOTES
    fun saveNote(id: Int?, title: String, content: String, colorHex: String) {
        viewModelScope.launch {
            val note = if (id != null) {
                NoteEntity(id = id, title = title, content = content, colorHex = colorHex)
            } else {
                NoteEntity(title = title, content = content, colorHex = colorHex)
            }
            repository.insertNote(note)
            logEvent("Notes", "Saved Note: $title")
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            logEvent("Notes", "Deleted Note: ${note.title}")
        }
    }

    // STOPWATCH
    fun toggleStopwatch() {
        if (_stopwatchRunning.value) {
            stopwatchJob?.cancel()
            _stopwatchRunning.value = false
            logEvent("Clock", "Stopwatch Paused")
        } else {
            _stopwatchRunning.value = true
            logEvent("Clock", "Stopwatch Started")
            val startTime = System.currentTimeMillis() - _stopwatchTimeMs.value
            stopwatchJob = viewModelScope.launch {
                while (_stopwatchRunning.value) {
                    _stopwatchTimeMs.value = System.currentTimeMillis() - startTime
                    delay(30)
                }
            }
        }
    }

    fun lapStopwatch() {
        val ms = _stopwatchTimeMs.value
        val form = String.format("%02d:%02d.%02d", ms / 60000, (ms % 60000) / 1000, (ms % 1000) / 10)
        stopwatchLaps.add(0, "Lap ${stopwatchLaps.size + 1}: $form")
        logEvent("Clock", "Stopwatch Lap recorded: $form")
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _stopwatchRunning.value = false
        _stopwatchTimeMs.value = 0L
        stopwatchLaps.clear()
        logEvent("Clock", "Stopwatch Reset")
    }

    // TIMER
    fun setTimer(seconds: Int) {
        _timerTimeRemaining.value = seconds
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            timerJob?.cancel()
            _timerRunning.value = false
            logEvent("Clock", "Timer Paused")
        } else {
            if (_timerTimeRemaining.value <= 0) return
            _timerRunning.value = true
            logEvent("Clock", "Timer Started: ${_timerTimeRemaining.value}s")
            timerJob = viewModelScope.launch {
                while (_timerRunning.value && _timerTimeRemaining.value > 0) {
                    delay(1000)
                    _timerTimeRemaining.value -= 1
                }
                if (_timerTimeRemaining.value == 0) {
                    _timerRunning.value = false
                    logEvent("Clock", "Timer Finished!")
                    addSystemNotification(
                        title = "Timer Alert",
                        text = "Your simulated timer has expired!",
                        appName = "Clock",
                        icon = "alarm"
                    )
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerRunning.value = false
        _timerTimeRemaining.value = 0
        logEvent("Clock", "Timer Reset")
    }

    // MUSIC PLAYER
    fun toggleMusicPlayback() {
        if (_isMusicPlaying.value) {
            songProgressJob?.cancel()
            _isMusicPlaying.value = false
            logEvent("Music", "Paused Music")
        } else {
            _isMusicPlaying.value = true
            logEvent("Music", "Playing: ${songsList[_currentSongIndex.value].title}")
            songProgressJob = viewModelScope.launch {
                while (_isMusicPlaying.value) {
                    delay(1000)
                    val nextProgress = _musicProgress.value + 0.01f
                    if (nextProgress >= 1.0f) {
                        _musicProgress.value = 0.0f
                        skipNextSong()
                    } else {
                        _musicProgress.value = nextProgress
                    }
                }
            }
        }
    }

    fun skipNextSong() {
        _musicProgress.value = 0.0f
        _currentSongIndex.value = (_currentSongIndex.value + 1) % songsList.size
        logEvent("Music", "Skipped: ${songsList[_currentSongIndex.value].title}")
        if (!_isMusicPlaying.value) {
            toggleMusicPlayback()
        }
    }

    fun skipPrevSong() {
        _musicProgress.value = 0.0f
        var next = _currentSongIndex.value - 1
        if (next < 0) next = songsList.size - 1
        _currentSongIndex.value = next
        logEvent("Music", "Previous: ${songsList[_currentSongIndex.value].title}")
        if (!_isMusicPlaying.value) {
            toggleMusicPlayback()
        }
    }

    // BROWSER NAVIGATION
    fun setBrowserUrl(url: String) {
        _browserUrl.value = url
    }

    fun loadBrowserUrl() {
        var targetUrl = _browserUrl.value.trim()
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "https://$targetUrl"
        }
        _browserUrl.value = targetUrl
        browserHistory.add(targetUrl)
        logEvent("Browser", "Navigated to $targetUrl")
    }

    // EMAIL CLIENT ACTIONS
    fun selectEmail(email: SimulatedEmail) {
        _selectedEmail.value = email
        // Mark as read
        val idx = emailsList.indexOf(email)
        if (idx != -1) {
            emailsList[idx] = email.copy(isRead = true)
        }
    }

    fun closeEmail() {
        _selectedEmail.value = null
    }

    fun sendEmail(recipient: String, subject: String, body: String) {
        val email = SimulatedEmail(
            sender = "dandali.user@gmail.com",
            subject = subject,
            snippet = if (body.length > 40) body.take(40) + "..." else body,
            time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            isRead = true,
            folder = "SENT"
        )
        emailsList.add(0, email)
        logEvent("Email", "Email sent to $recipient: $subject")

        // Reply simulation
        viewModelScope.launch {
            delay(3000)
            val incoming = SimulatedEmail(
                sender = recipient,
                subject = "Re: $subject",
                snippet = "This is a reply to your email simulation. Dandali SMTP test works perfectly.",
                time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                isRead = false,
                folder = "INBOX"
            )
            emailsList.add(0, incoming)
            addSystemNotification(
                title = "New Email from $recipient",
                text = "Re: $subject",
                appName = "Email",
                icon = "email"
            )
            logEvent("Email", "Received reply from $recipient")
        }
    }

    // CALCULATOR FORMULA BUILDER
    fun pressCalculatorButton(button: String) {
        when (button) {
            "C" -> {
                _calcFormula.value = ""
                _calcResult.value = ""
            }
            "⌫" -> {
                if (_calcFormula.value.isNotEmpty()) {
                    _calcFormula.value = _calcFormula.value.dropLast(1)
                }
            }
            "=" -> {
                val formula = _calcFormula.value
                try {
                    val result = evaluateSimpleExpression(formula)
                    _calcResult.value = result
                    logEvent("Calculator", "Result of '$formula' = $result")
                } catch (e: Exception) {
                    _calcResult.value = "Error"
                }
            }
            else -> {
                _calcFormula.value = _calcFormula.value + button
            }
        }
    }

    private fun evaluateSimpleExpression(expression: String): String {
        // Safe, simple calculator evaluator supporting basic (+, -, *, /, %) operations
        val clean = expression.replace(" ", "")
        // Simple 2-operand evaluator
        val operators = charArrayOf('+', '-', '*', '/', '%')
        var opIndex = -1
        var op = ' '
        for (o in operators) {
            val idx = clean.lastIndexOf(o)
            if (idx > 0) {
                opIndex = idx
                op = o
                break
            }
        }

        if (opIndex == -1) {
            return clean.toDoubleOrNull()?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: "Error"
        }

        val leftStr = clean.substring(0, opIndex)
        val rightStr = clean.substring(opIndex + 1)
        val left = leftStr.toDoubleOrNull() ?: return "Error"
        val right = rightStr.toDoubleOrNull() ?: return "Error"

        val ans = when (op) {
            '+' -> left + right
            '-' -> left - right
            '*' -> left * right
            '/' -> if (right != 0.0) left / right else Double.NaN
            '%' -> left * (right / 100.0)
            else -> 0.0
        }

        if (ans.isNaN()) return "Zero Div"
        return if (ans % 1.0 == 0.0) ans.toInt().toString() else String.format(Locale.US, "%.4f", ans).trimEnd('0').trimEnd('.')
    }

    // CAMERA PREVIEW & CAPTURE
    fun capturePhoto() {
        val newPhotoId = "capture_" + System.currentTimeMillis()
        galleryImages.add(0, newPhotoId)
        logEvent("Camera", "Photo captured: $newPhotoId")
        addSystemNotification(
            title = "Photo Captured",
            text = "Saved $newPhotoId.png successfully to Gallery.",
            appName = "Camera",
            icon = "photo_library"
        )
    }

    // VOICE RECORDER
    fun toggleVoiceRecording() {
        if (_isRecording.value) {
            voiceRecorderJob?.cancel()
            _isRecording.value = false
            logEvent("Recorder", "Voice Recording Stopped")
            val dur = _recordingDurationSeconds.value
            val formDur = String.format("%02d:%02d", dur / 60, dur % 60)
            voiceRecordings.add(0, mapOf(
                "title" to "Voice Note " + (voiceRecordings.size + 1),
                "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                "duration" to formDur
            ))
            _recordingDurationSeconds.value = 0
        } else {
            _isRecording.value = true
            _recordingDurationSeconds.value = 0
            logEvent("Recorder", "Voice Recording Started")
            voiceRecorderJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordingDurationSeconds.value += 1
                }
            }
        }
    }

    // GOOGLE SIGN IN
    fun triggerGoogleSignIn() {
        _isGoogleSignedIn.value = !_isGoogleSignedIn.value
        if (_isGoogleSignedIn.value) {
            logEvent("GooglePlayServices", "Successfully signed in with ${_googleEmail.value}")
        } else {
            logEvent("GooglePlayServices", "Signed out")
        }
    }

    fun setGoogleEmail(email: String) {
        _googleEmail.value = email
    }

    // Google Play Store Reviews Management
    fun seedPlayStoreReviews() {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date1 = formatter.format(Date(System.currentTimeMillis() - 86400000L * 2)) // 2 days ago
        val date2 = formatter.format(Date(System.currentTimeMillis() - 86400000L * 10)) // 10 days ago
        val date3 = formatter.format(Date(System.currentTimeMillis() - 86400000L * 30)) // 30 days ago

        val initialReviews = mutableMapOf<PhoneApp, List<PlayStoreReview>>()
        PhoneApp.values().forEach { app ->
            initialReviews[app] = listOf(
                PlayStoreReview(
                    author = "Chidi Okafor",
                    rating = 5,
                    date = date1,
                    content = "Absolutely stunning performance on the Dandali visual operating system. Highly recommended!"
                ),
                PlayStoreReview(
                    author = "Amina Bello",
                    rating = 4,
                    date = date2,
                    content = "Very fluid animations and simple interface. Fits perfectly within the Material 3 guidelines."
                ),
                PlayStoreReview(
                    author = TundeOyelowoReviewAuthor(),
                    rating = 5,
                    date = date3,
                    content = "This app makes my daily tasks so much easier. Exceptional developer support as well."
                )
            )
        }
        _playStoreReviews.value = initialReviews
    }

    private fun TundeOyelowoReviewAuthor(): String = "Tunde Oyelowo"

    fun submitPlayStoreReview(appId: PhoneApp, author: String, rating: Int, content: String) {
        val currentMap = _playStoreReviews.value.toMutableMap()
        val currentReviews = currentMap[appId]?.toMutableList() ?: mutableListOf()
        val newReview = PlayStoreReview(
            author = if (author.isBlank()) "Anonymous Dandali User" else author,
            rating = rating,
            date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
            content = content
        )
        currentReviews.add(0, newReview)
        currentMap[appId] = currentReviews
        _playStoreReviews.value = currentMap
        logEvent("PlayStore", "New review submitted for ${appId.appName}: $rating stars")
    }

    // --- GOOGLE PLAY STORE AND APK DOWNLOAD SIMULATION ---

    fun selectPlayStoreApp(app: AppMetadata?) {
        _playStoreSelectedApp.value = app
    }

    fun setPlayStoreQuery(query: String) {
        _playStoreQuery.value = query
    }

    fun triggerAppInstall(app: AppMetadata) {
        // Simulate progress download
        val task = DownloadTask(app.id.appName, 0.0f, "Downloading")
        activeDownloads.add(task)
        logEvent("PlayStore", "Starting APK download for: ${app.packageName}")

        val idx = activeDownloads.indexOf(task)
        val job = viewModelScope.launch {
            var prog = 0.0f
            while (prog < 1.0f) {
                delay(400)
                prog += 0.1f
                if (activeDownloads.contains(task)) {
                    val currentIdx = activeDownloads.indexOf(task)
                    if (currentIdx != -1) {
                        activeDownloads[currentIdx] = activeDownloads[currentIdx].copy(progress = prog)
                    }
                }
            }
            if (activeDownloads.contains(task)) {
                val currentIdx = activeDownloads.indexOf(task)
                if (currentIdx != -1) {
                    activeDownloads[currentIdx] = activeDownloads[currentIdx].copy(progress = 1.0f, status = "Completed")
                }
                // Add downloaded app details
                if (!downloadedApks.any { it.id == app.id }) {
                    downloadedApks.add(app)
                }

                // Warn before install
                addSystemNotification(
                    title = "APK Verified",
                    text = "Package '${app.packageName}' integrity checked. Warning: Install from simulated unknown sources.",
                    appName = "Play Store",
                    icon = "verified_user"
                )

                // Add to apps list if not already there
                if (!appsList.any { it.id == app.id }) {
                    appsList.add(app)
                }

                logEvent("APKManager", "Completed Simulated Installation: ${app.id.appName}")
            }
        }
        if (idx != -1) {
            activeDownloads[idx].job = job
        }
    }

    fun cancelDownload(task: DownloadTask) {
        task.job?.cancel()
        activeDownloads.remove(task)
        logEvent("APKManager", "Cancelled download for: ${task.appName}")
    }

    // --- APP MANAGEMENT ACTIONS ---

    fun forceStopApp(appId: PhoneApp) {
        val idx = appsList.indexOfFirst { it.id == appId }
        if (idx != -1) {
            appsList[idx] = appsList[idx].copy(forceStopped = true)
            if (_activeApp.value == appId) {
                _activeApp.value = null
            }
            logEvent("AppManager", "Force Stopped application: ${appId.appName}")
        }
    }

    fun startStoppedApp(appId: PhoneApp) {
        val idx = appsList.indexOfFirst { it.id == appId }
        if (idx != -1) {
            appsList[idx] = appsList[idx].copy(forceStopped = false)
            logEvent("AppManager", "Enabled/Reset application: ${appId.appName}")
        }
    }

    fun uninstallApp(appId: PhoneApp) {
        if (appId == PhoneApp.SETTINGS || appId == PhoneApp.APP_MANAGER) {
            logEvent("AppManager", "Cannot uninstall core system application: ${appId.appName}")
            return
        }
        appsList.removeAll { it.id == appId }
        downloadedApks.removeAll { it.id == appId }
        if (_activeApp.value == appId) {
            _activeApp.value = null
        }
        logEvent("AppManager", "Uninstalled simulated app: ${appId.appName}")
    }

    // --- ADVANCED LOCATION MANAGER AND MAP SIMULATION ---

    fun changeSimulatedLocation(state: NigerianState) {
        _simulatedLatitude.value = state.latitude
        _simulatedLongitude.value = state.longitude
        _simulatedStateName.value = state.name
        _simulatedCityName.value = state.capital
        _simulatedAccuracy.value = (2..5).random().toFloat()
        _simulatedAltitude.value = (200..700).random().toDouble()
        _simulatedSpeed.value = 0.0f
        _simulatedHeading.value = (0..359).random().toFloat()

        logEvent("LocationManager", "Simulated location updated: ${state.capital}, ${state.name} State (${state.latitude}, ${state.longitude})")
    }

    fun setSimulatedCoordinates(lat: Double, lon: Double, name: String = "Manual Coordinate Pin") {
        _simulatedLatitude.value = lat
        _simulatedLongitude.value = lon
        _simulatedStateName.value = "Custom Coordinates"
        _simulatedCityName.value = name
        _simulatedAccuracy.value = 5.0f

        logEvent("LocationManager", "Coordinates manually updated: ($lat, $lon)")
    }

    fun resetToDefaultLocation() {
        setSimulatedCoordinates(9.0765, 7.3986, "Abuja (Default)")
        _simulatedStateName.value = "Federal Capital Territory"
        _simulatedCityName.value = "Abuja"
        _simulatedAltitude.value = 491.5
        _simulatedSpeed.value = 0.0f
        _simulatedHeading.value = 180.0f
        _simulatedAccuracy.value = 3.2f
        logEvent("LocationManager", "Reset location to Abuja (Default)")
    }

    fun setSimulatedAltitude(alt: Double) {
        _simulatedAltitude.value = alt
    }

    fun setSimulatedSpeed(speed: Float) {
        _simulatedSpeed.value = speed
    }

    fun setSimulatedHeading(heading: Float) {
        _simulatedHeading.value = heading
    }

    fun setSimulatedAccuracy(acc: Float) {
        _simulatedAccuracy.value = acc
    }

    fun setSimulatedStateAndCity(stateName: String, cityName: String) {
        _simulatedStateName.value = stateName
        _simulatedCityName.value = cityName
    }

    fun searchAndChangeLocation(query: String) {
        _locationSearchQuery.value = query
        if (query.trim().isEmpty()) return

        // Search in Nigerian states or capitals
        val match = nigerianStatesList.find {
            it.name.contains(query, ignoreCase = true) || it.capital.contains(query, ignoreCase = true)
        }
        if (match != null) {
            changeSimulatedLocation(match)
        } else {
            logEvent("LocationManager", "No exact Nigerian capital found for: '$query'. Try 'Lagos', 'Kano', 'Enugu', or 'Port Harcourt'.")
        }
    }

    fun saveLocationToFavorites(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.insertLocation(
                LocationEntity(
                    name = name,
                    latitude = lat,
                    longitude = lon,
                    isFavorite = true
                )
            )
            logEvent("LocationManager", "Saved favorite location: $name ($lat, $lon)")
        }
    }

    fun removeLocationFromFavorites(location: LocationEntity) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            logEvent("LocationManager", "Removed favorite location: ${location.name}")
        }
    }

    // --- SYSTEM NOTIFICATIONS SIMULATION ---

    fun addSystemNotification(title: String, text: String, appName: String, icon: String = "notifications") {
        val notif = PhoneNotification(title = title, text = text, appName = appName, icon = icon)
        notifications.add(0, notif)
        if (notifications.size > 20) {
            notifications.removeAt(notifications.size - 1)
        }
        logEvent("NotificationCenter", "New notification from $appName: $title")
    }

    fun clearNotification(id: String) {
        notifications.removeAll { it.id == id }
    }

    fun clearAllNotifications() {
        notifications.clear()
        logEvent("NotificationCenter", "Cleared all notifications")
    }

    fun triggerTestNotification() {
        val titles = listOf("Low Memory Warning", "Google Play Update", "Battery Full Charged", "Dandali Security Check")
        val texts = listOf("System RAM is running low. Optimize apps in App Manager.", "Verify simulated app integrity from Google Play.", "Unplug virtual charger to save simulated lifecycle.", "Device is fully protected by simulated Android policies.")
        val appNames = listOf("System", "Play Store", "Battery", "Security")
        val icons = listOf("memory", "shopping_cart", "battery_charging_full", "security")

        val index = (0..3).random()
        addSystemNotification(
            title = titles[index],
            text = texts[index],
            appName = appNames[index],
            icon = icons[index]
        )
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PhoneRepository(
            database.contactDao(),
            database.noteDao(),
            database.locationDao(),
            database.settingDao()
        )

        contactsFlow = repository.allContacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notesFlow = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        locationsFlow = repository.allLocations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed some initial data if database is empty
        seedDataIfNeeded()
        seedPlayStoreReviews()
        startClock()
        simulateTelemetry()
    }
}
