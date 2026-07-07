package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.phone.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val application = application
                val viewModel: VisualPhoneViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )

                val isFullScreen by viewModel.isFullScreen.collectAsState()

                LaunchedEffect(isFullScreen) {
                    val window = this@MainActivity.window
                    val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                    if (isFullScreen) {
                        insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhoneFrame(viewModel = viewModel) {
                        PhoneContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneContent(viewModel: VisualPhoneViewModel) {
    val isScreenOn by viewModel.isScreenOn.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val activeApp by viewModel.activeApp.collectAsState()
    val isNotificationExpanded by viewModel.isNotificationPanelExpanded.collectAsState()
    val isAppDrawerOpen by viewModel.isAppDrawerOpen.collectAsState()
    val isRecentsOpen by viewModel.isRecentsViewOpen.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val wallpaperIndex by viewModel.wallpaperIndex.collectAsState()

    if (!isScreenOn) {
        // Pure black screen when screen is off
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.pressPowerButton() }
        )
        return
    }

    // Dynamic wallpapers
    val wallpaperBrushes = listOf(
        Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF121212))), // Midnight Blue
        Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFFBF360C))), // Sunset Red
        Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF0D5C3A))), // Emerald Tech
        Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF311B92)))  // Cosmic Purple
    )
    val wallpaperBrush = wallpaperBrushes[wallpaperIndex % wallpaperBrushes.size]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaperBrush)
    ) {
        if (isLocked) {
            LockScreen(viewModel = viewModel)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // OS Status Bar (Custom built-in status bar drawing)
                StatusBar(viewModel = viewModel, onSwipeDown = { viewModel.showNotificationBar(true) })

                // Main Display Screen Space
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (activeApp != null) {
                        // Render Active application
                        AppSwitcher(activeApp!!, viewModel)
                    } else {
                        // Desktop home layout
                        HomeScreen(viewModel)
                    }

                    // Slide down notification center overlays
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isNotificationExpanded,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NotificationPanel(viewModel)
                    }

                    // Recents stack app overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isRecentsOpen,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RecentsPanel(viewModel)
                    }
                }

                // Interactive Navigation bar buttons or Gesture Pill
                NavigationBar(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppSwitcher(app: PhoneApp, viewModel: VisualPhoneViewModel) {
    when (app) {
        PhoneApp.DIALER -> DialerApp(viewModel)
        PhoneApp.CONTACTS -> ContactsApp(viewModel)
        PhoneApp.MESSAGES -> MessagesApp(viewModel)
        PhoneApp.CAMERA -> CameraApp(viewModel)
        PhoneApp.GALLERY -> GalleryApp(viewModel)
        PhoneApp.CLOCK -> ClockApp(viewModel)
        PhoneApp.CALENDAR -> CalendarApp(viewModel)
        PhoneApp.CALCULATOR -> CalculatorApp(viewModel)
        PhoneApp.FILE_MANAGER -> FileManagerApp(viewModel)
        PhoneApp.BROWSER -> BrowserApp(viewModel)
        PhoneApp.MUSIC -> MusicApp(viewModel)
        PhoneApp.NOTES -> NotesApp(viewModel)
        PhoneApp.WEATHER -> WeatherApp(viewModel)
        PhoneApp.MAPS -> MapsApp(viewModel)
        PhoneApp.PLAY_STORE -> PlayStoreApp(viewModel)
        PhoneApp.SETTINGS -> SettingsApp(viewModel)
        PhoneApp.APP_MANAGER -> AppManagerApp(viewModel)
        PhoneApp.NETWORK_COVERAGE -> NetworkCoverageApp(viewModel)
        PhoneApp.VIDEO -> VideoApp(viewModel)
        PhoneApp.DOWNLOADS -> DownloadsApp(viewModel)
        PhoneApp.EMAIL -> EmailApp(viewModel)
        PhoneApp.RECORDER -> RecorderApp(viewModel)
    }
}

// ==========================================
// DESKTOP HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(viewModel: VisualPhoneViewModel) {
    val isAppDrawerOpen by viewModel.isAppDrawerOpen.collectAsState()
    val time by viewModel.currentTime.collectAsState()
    val date by viewModel.currentDate.collectAsState()
    val city by viewModel.simulatedCityName.collectAsState()

    // Dock apps (Dialer, Messages, Contacts, Camera, Settings)
    val dockApps = listOf(
        PhoneApp.DIALER,
        PhoneApp.MESSAGES,
        PhoneApp.CONTACTS,
        PhoneApp.CAMERA,
        PhoneApp.SETTINGS
    )

    // Standard Desktop Shortcut grids
    val desktopApps = listOf(
        PhoneApp.CALCULATOR,
        PhoneApp.WEATHER,
        PhoneApp.MAPS,
        PhoneApp.PLAY_STORE,
        PhoneApp.MUSIC,
        PhoneApp.CLOCK,
        PhoneApp.NOTES,
        PhoneApp.FILE_MANAGER,
        PhoneApp.NETWORK_COVERAGE,
        PhoneApp.APP_MANAGER
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isAppDrawerOpen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Widgets Space (Clock & Weather summary)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = time,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$date | $city, NG",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    // Swipe down instruction banner
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.showNotificationBar(true) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Swipe Quick Panel", color = Color.White, fontSize = 10.sp)
                    }
                }

                // Grid of Main Desktop App Shortcuts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(desktopApps) { app ->
                        AppShortcutIcon(app) { viewModel.launchApp(app) }
                    }

                    // A static "All Apps" trigger to open the App Drawer
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.openAppDrawer(true) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Apps, "All Apps", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "All Apps",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Bottom Dock (Fixed standard dial/chat apps)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        AppShortcutIcon(app, iconSize = 44.dp) { viewModel.launchApp(app) }
                    }
                }
            }
        } else {
            // Sliding App Drawer overlay
            AppDrawer(viewModel)
        }
    }
}

@Composable
fun AppShortcutIcon(
    app: PhoneApp,
    iconSize: androidx.compose.ui.unit.Dp = 42.dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when (app) {
                        PhoneApp.DIALER -> Color(0xFF4CAF50)
                        PhoneApp.MESSAGES -> Color(0xFF2196F3)
                        PhoneApp.CONTACTS -> Color(0xFFFF9800)
                        PhoneApp.CAMERA -> Color(0xFF9E9E9E)
                        PhoneApp.GALLERY -> Color(0xFFE91E63)
                        PhoneApp.PLAY_STORE -> Color(0xFF0F9D58)
                        PhoneApp.CLOCK -> Color(0xFF673AB7)
                        PhoneApp.WEATHER -> Color(0xFF00BCD4)
                        PhoneApp.MAPS -> Color(0xFF009688)
                        PhoneApp.SETTINGS -> Color(0xFF607D8B)
                        PhoneApp.NOTES -> Color(0xFFFFC107)
                        PhoneApp.MUSIC -> Color(0xFFE91E63)
                        PhoneApp.NETWORK_COVERAGE -> Color(0xFF3F51B5)
                        else -> Color(0xFF795548)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val vector = when (app) {
                PhoneApp.DIALER -> Icons.Default.Call
                PhoneApp.MESSAGES -> Icons.Default.ChatBubble
                PhoneApp.CONTACTS -> Icons.Default.People
                PhoneApp.CAMERA -> Icons.Default.CameraAlt
                PhoneApp.GALLERY -> Icons.Default.PhotoLibrary
                PhoneApp.PLAY_STORE -> Icons.Default.Shop
                PhoneApp.CLOCK -> Icons.Default.Schedule
                PhoneApp.WEATHER -> Icons.Default.WbSunny
                PhoneApp.MAPS -> Icons.Default.MyLocation
                PhoneApp.SETTINGS -> Icons.Default.Settings
                PhoneApp.NOTES -> Icons.Default.StickyNote2
                PhoneApp.MUSIC -> Icons.Default.MusicNote
                PhoneApp.FILE_MANAGER -> Icons.Default.FolderOpen
                PhoneApp.CALCULATOR -> Icons.Default.Calculate
                PhoneApp.NETWORK_COVERAGE -> Icons.Default.CellTower
                PhoneApp.APP_MANAGER -> Icons.Default.SettingsApplications
                PhoneApp.VIDEO -> Icons.Default.Movie
                PhoneApp.DOWNLOADS -> Icons.Default.Download
                PhoneApp.EMAIL -> Icons.Default.Email
                PhoneApp.RECORDER -> Icons.Default.Mic
                else -> Icons.Default.Apps
            }
            Icon(vector, app.appName, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.appName,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================
// CATEGORIZED APP DRAWER
// ==========================================
@Composable
fun AppDrawer(viewModel: VisualPhoneViewModel) {
    val searchQuery by viewModel.appDrawerQuery.collectAsState()
    var activeCategoryFilter by remember { mutableStateOf<AppCategory?>(null) }

    val categories = AppCategory.values()
    val playApps = viewModel.playStoreApps

    val filteredApps = playApps.filter { app ->
        (activeCategoryFilter == null || app.category == activeCategoryFilter) &&
                app.id.appName.contains(searchQuery, ignoreCase = true)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_drawer_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search field & exit arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { viewModel.openAppDrawer(false) }) {
                    Icon(Icons.Default.ArrowBack, "Back to home")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setAppDrawerQuery(it) },
                    placeholder = { Text("Search App Drawer...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Categories horizontal slider Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val active = activeCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .background(
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { activeCategoryFilter = null }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("All", color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }

                items(categories) { cat ->
                    val active = cat == activeCategoryFilter
                    Box(
                        modifier = Modifier
                            .background(
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { activeCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(cat.name, color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }

            // Apps vertical flow Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredApps) { app ->
                    AppShortcutIcon(app.id) {
                        viewModel.openAppDrawer(false)
                        viewModel.launchApp(app.id)
                    }
                }
            }
        }
    }
}

// ==========================================
// NOTIFICATION & QUICK SETTINGS PANEL
// ==========================================
@Composable
fun NotificationPanel(viewModel: VisualPhoneViewModel) {
    val wifiOn by viewModel.isWifiOn.collectAsState()
    val mobileOn by viewModel.isMobileOn.collectAsState()
    val bluetoothOn by viewModel.isBluetoothOn.collectAsState()
    val airplaneOn by viewModel.isAirplaneMode.collectAsState()
    val dndOn by viewModel.isDnd.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val batterySaver by viewModel.isBatterySaver.collectAsState()
    val flashOn by viewModel.isFlashlightOn.collectAsState()

    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val cpuUsage by viewModel.cpuUsage.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A).copy(alpha = 0.98f)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("notification_quick_settings_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header (Time and Panel dismiss trigger)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Control", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.showNotificationBar(false) }) {
                        Icon(Icons.Default.KeyboardArrowUp, "Dismiss Panel", tint = Color.White)
                    }
                }

                // Quick Settings Tiles Grid
                val tiles = listOf(
                    QuadTile("Wi-Fi", wifiOn, Icons.Default.Wifi) { viewModel.toggleWifi() },
                    QuadTile("Data", mobileOn, Icons.Default.NetworkCell) { viewModel.toggleMobileNetwork() },
                    QuadTile("Bluetooth", bluetoothOn, Icons.Default.Bluetooth) { viewModel.toggleBluetooth() },
                    QuadTile("Airplane", airplaneOn, Icons.Default.AirplaneTicket) { viewModel.toggleAirplaneMode() },
                    QuadTile("DND", dndOn, Icons.Default.DoNotDisturb) { viewModel.toggleDnd() },
                    QuadTile("Dark Mode", isDarkMode, Icons.Default.DarkMode) { viewModel.toggleDarkMode() },
                    QuadTile("Saver", batterySaver, Icons.Default.BatterySaver) { viewModel.toggleBatterySaver() },
                    QuadTile("Flashlight", flashOn, Icons.Default.FlashlightOn) { viewModel.toggleFlashlight() }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tiles) { tile ->
                        val color = if (tile.active) MaterialTheme.colorScheme.primary else Color.DarkGray
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { tile.onClick() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(tile.icon, tile.name, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tile.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }

                // Hardware telemetry bar
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Battery Level: $batteryLevel%", color = Color.LightGray, fontSize = 12.sp)
                    Text("Sim CPU rate: $cpuUsage%", color = Color.LightGray, fontSize = 12.sp)
                }

                // Simulated Notifications List Area
                Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.notifications.isEmpty()) {
                        item {
                            Text("No new simulated notifications.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        items(viewModel.notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text(notif.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(notif.text, color = Color.LightGray, fontSize = 11.sp)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.notifications.remove(notif) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Dismiss", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.notifications.clear() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Notifications")
            }
        }
    }
}

data class QuadTile(val name: String, val active: Boolean, val icon: ImageVector, val onClick: () -> Unit)

// ==========================================
// RECENTS PANEL OVERLAY
// ==========================================
@Composable
fun RecentsPanel(viewModel: VisualPhoneViewModel) {
    val recents = viewModel.recentsStack

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("recents_view_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recents Apps Queue", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

            if (recents.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No recently opened applications", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(recents) { app ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp)
                                .clickable {
                                    viewModel.goBack()
                                    viewModel.launchApp(app)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    IconButton(
                                        onClick = { viewModel.closeRecentApp(app) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Apps, null, tint = Color.White)
                                }

                                Text("Tap to resume", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { viewModel.goBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                    Text("Close")
                }
                if (recents.isNotEmpty()) {
                    Button(onClick = { viewModel.clearAllRecents() }) {
                        Text("Clear All")
                    }
                }
            }
        }
    }
}

// ==========================================
// 19. VIDEO APP
// ==========================================
@Composable
fun VideoApp(viewModel: VisualPhoneViewModel) {
    AppContainer("Video Player", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Simulated widescreen video frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Moving simulated visual bars on Canvas
                var angle by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        angle = (angle + 4f) % 360f
                        delay(50)
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color.Blue.copy(alpha = 0.2f))
                    // Drawing loading circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 40f,
                        center = center
                    )
                }

                Icon(Icons.Default.PlayArrow, "Playing logo", tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Text("Nigeria State Tour Documentary.mp4", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Slider(value = 0.45f, onValueChange = {})

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null) }
                IconButton(onClick = {}) { Icon(Icons.Default.Pause, null) }
                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null) }
            }
        }
    }
}

// ==========================================
// 20. DOWNLOADS APP
// ==========================================
@Composable
fun DownloadsApp(viewModel: VisualPhoneViewModel) {
    val activeDownloads = viewModel.activeDownloads
    val downloadedApks = viewModel.downloadedApks

    AppContainer("Downloads Center", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Simulated Downloads Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activeDownloads.isEmpty() && downloadedApks.isEmpty()) {
                    item {
                        Text("No items downloaded or queued yet.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                    }
                }

                // Active Downloads progress list
                items(activeDownloads) { dl ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dl.appName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${Math.round(dl.progress * 100)}%", fontSize = 11.sp)
                            }
                            LinearProgressIndicator(progress = dl.progress, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                            Text("Status: ${dl.status}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                // Completed list of APK packages
                items(downloadedApks) { completed ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(completed.id.appName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("com.dandali.package", color = Color.Gray, fontSize = 11.sp)
                                Text("Size: ${completed.storageSize} | Saved as APK", fontSize = 10.sp)
                            }
                            Icon(Icons.Default.CheckCircle, "Finished", tint = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 21. EMAIL APP
// ==========================================
@Composable
fun EmailApp(viewModel: VisualPhoneViewModel) {
    var showComposer by remember { mutableStateOf(false) }
    var emailTo by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }

    val savedEmails = remember { mutableStateListOf(
        Triple("NIMC Identity Agency", "NIN Card Update Request", "Your visual identity details on the Dandali handset are successfully synced."),
        Triple("MTN Telecom", "Naija Weekly Bundle Promo", "Receive 5GB data pack for being a premium Dandali subscriber this Friday."),
        Triple("Google Accounts", "Simulated Play Store sign in", "Welcome to the custom developer emulator sandbox.")
    ) }

    AppContainer("Email Hub", viewModel) {
        if (!showComposer) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(savedEmails) { mail ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(mail.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text(mail.second, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(mail.third, fontSize = 11.sp, color = Color.DarkGray, maxLines = 2)
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showComposer = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Edit, "Compose")
                }
            }
        } else {
            // Composer View screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Compose New Mail", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(value = emailTo, onValueChange = { emailTo = it }, label = { Text("To:") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailSubject, onValueChange = { emailSubject = it }, label = { Text("Subject:") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailBody, onValueChange = { emailBody = it }, label = { Text("Body Details") }, modifier = Modifier.fillMaxWidth().height(180.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { showComposer = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (emailTo.isNotEmpty() && emailSubject.isNotEmpty()) {
                            savedEmails.add(0, Triple(emailTo, emailSubject, emailBody))
                            showComposer = false
                            emailTo = ""
                            emailSubject = ""
                            emailBody = ""
                            viewModel.logEvent("Email", "Sent simulated email package successfully.")
                        }
                    }) {
                        Text("Send Mail")
                    }
                }
            }
        }
    }
}

// ==========================================
// 22. VOICE RECORDER APP
// ==========================================
@Composable
fun RecorderApp(viewModel: VisualPhoneViewModel) {
    var isRecording by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }

    val recordingsList = remember { mutableStateListOf<String>() }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                seconds++
            }
        }
    }

    AppContainer("Voice Recorder", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Voice Memo Simulator", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            // Dynamic recorder wave drawing on Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (isRecording) {
                        val centerY = size.height / 2
                        val count = 20
                        val gap = size.width / count
                        for (i in 0 until count) {
                            val h = (Math.random() * 80).toFloat()
                            drawLine(
                                color = Color.Red,
                                start = Offset(i * gap + 10f, centerY - h),
                                end = Offset(i * gap + 10f, centerY + h),
                                strokeWidth = 5f
                            )
                        }
                    } else {
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, size.height/2),
                            end = Offset(size.width, size.height/2),
                            strokeWidth = 3f
                        )
                    }
                }

                // Clock overlay
                val min = seconds / 60
                val sec = seconds % 60
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Record action buttons
            IconButton(
                onClick = {
                    if (isRecording) {
                        val min = seconds / 60
                        val sec = seconds % 60
                        recordingsList.add("Voice Note - ${String.format("%02d:%02d", min, sec)}")
                        isRecording = false
                        seconds = 0
                    } else {
                        isRecording = true
                    }
                },
                modifier = Modifier
                    .size(68.dp)
                    .background(if (isRecording) Color.Red else Color.DarkGray, CircleShape)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Trigger Record",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Render previous recorded memos list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { Text("Recorded Audio Files", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                items(recordingsList) { memo ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                                Text(memo, fontSize = 12.sp)
                            }
                            IconButton(onClick = { recordingsList.remove(memo) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
