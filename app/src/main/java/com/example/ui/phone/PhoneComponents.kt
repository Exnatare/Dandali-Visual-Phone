package com.example.ui.phone

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.ui.viewmodel.VisualPhoneViewModel
import com.example.ui.viewmodel.PhoneApp
import kotlinx.coroutines.delay

// Beautiful color palettes for gradient wallpapers
val Wallpapers = listOf(
    // 0: Sunrise Orange
    Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFFF57C00), Color(0xFFFFCC80))),
    // 1: Cool Purple
    Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C), Color(0xFF8E24AA))),
    // 2: Emerald Green
    Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF00796B), Color(0xFF80CBC4))),
    // 3: Slate Dark
    Brush.verticalGradient(listOf(Color(0xFF121212), Color(0xFF263238), Color(0xFF455A64)))
)

@Composable
fun PhoneFrame(
    viewModel: VisualPhoneViewModel,
    content: @Composable BoxScope.() -> Unit
) {
    val isScreenOn by viewModel.isScreenOn.collectAsState()
    val showVolumeSlider by viewModel.showVolumeSlider.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()

    val config = LocalConfiguration.current
    val frameWidth = if (config.screenWidthDp > 600) 450.dp else Modifier.fillMaxWidth()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E24)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Physical Phone Chassis (Hidden or scaled when in Full Screen mode)
            val chassisModifier = if (isFullScreen) {
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            } else {
                Modifier
                    .width(if (config.screenWidthDp > 600) 440.dp else config.screenWidthDp.dp)
                    .fillMaxHeight()
                    .padding(vertical = if (config.screenWidthDp > 600) 16.dp else 0.dp)
                    .shadow(24.dp, RoundedCornerShape(40.dp))
                    .border(8.dp, Color(0xFF2A2B36), RoundedCornerShape(40.dp))
                    .border(10.dp, Color(0xFF101014), RoundedCornerShape(40.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black)
            }

            Box(
                modifier = chassisModifier
            ) {
                // Simulated Device Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if (isScreenOn) {
                        // Render interactive screen state
                        Box(modifier = Modifier.fillMaxSize()) {
                            content()

                            // Fullscreen Immersive Mode Visual Indicator at the top center
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isFullScreen,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .testTag("fullscreen_active_indicator")
                                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                        .clickable { viewModel.toggleFullScreen() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Pulsing active dot
                                        var tick by remember { mutableStateOf(true) }
                                        LaunchedEffect(Unit) {
                                            while (true) {
                                                tick = !tick
                                                delay(800)
                                            }
                                        }
                                        val dotColor by animateColorAsState(
                                            targetValue = if (tick) Color(0xFF00E676) else Color(0xFF00E676).copy(alpha = 0.4f),
                                            animationSpec = spring(),
                                            label = "pulse"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(dotColor, CircleShape)
                                        )

                                        Text(
                                            text = "Fullscreen Active",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        VerticalDivider(
                                            modifier = Modifier.height(12.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )

                                        Icon(
                                            imageVector = Icons.Default.FullscreenExit,
                                            contentDescription = "Exit Fullscreen",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Darken screen by brightness factor
                            if (brightness < 1.0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = (1.0f - brightness) * 0.7f))
                                        .pointerInput(Unit) {} // Block touch
                                )
                            }
                        }
                    } else {
                        // Blank black screen when device is off
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.pressPowerButton()
                                }
                        ) {
                            Text(
                                text = "DANDALI\nVisual Phone\n\nTap to turn on",
                                color = Color.DarkGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    // Front Camera Notch / Punch Hole
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF08080C))
                            .border(2.dp, Color(0xFF1A1A22), CircleShape)
                            .clickable {
                                viewModel.launchApp(PhoneApp.CAMERA)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D253F))
                        )
                    }

                    // Simulated Volume Overlay Slider on right edge with built-in Full Screen toggle
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showVolumeSlider,
                        enter = slideInHorizontally(initialOffsetX = { it }),
                        exit = slideOutHorizontally(targetOffsetX = { it }),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .width(52.dp)
                                .height(245.dp)
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { viewModel.adjustVolume(0.1f) }) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Vol Up", tint = Color.White)
                                }
                                // Vertical Progress Bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(volume)
                                            .align(Alignment.BottomCenter)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                IconButton(onClick = { viewModel.adjustVolume(-0.1f) }) {
                                    Icon(imageVector = Icons.Default.VolumeDown, contentDescription = "Vol Down", tint = Color.White)
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 1.dp
                                )

                                IconButton(onClick = { viewModel.toggleFullScreen() }) {
                                    Icon(
                                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Toggle Fullscreen",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Physical side buttons for desk demonstration (Visible only in non-fullscreen desktop layouts)
            if (config.screenWidthDp > 600 && !isFullScreen) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Volume Up Physical Button
                    Button(
                        onClick = { viewModel.adjustVolume(0.1f) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E303A)),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.width(50.dp).height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, "+", tint = Color.White)
                    }
                    // Volume Down Physical Button
                    Button(
                        onClick = { viewModel.adjustVolume(-0.1f) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E303A)),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.width(50.dp).height(48.dp)
                    ) {
                        Icon(Icons.Default.Remove, "-", tint = Color.White)
                    }
                    // Fullscreen Toggle Physical Button
                    Button(
                        onClick = { viewModel.toggleFullScreen() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.width(50.dp).height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = Color.White
                        )
                    }
                    // Power Physical Button
                    Button(
                        onClick = { viewModel.pressPowerButton() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.width(50.dp).height(56.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, "Power", tint = Color.White)
                    }
                }
            }
        }

        // Floating overlay side buttons when IN fullscreen mode so the user can easily toggle back or control volume
        if (isFullScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { viewModel.adjustVolume(0.1f) }) {
                        Icon(Icons.Default.VolumeUp, "Vol Up", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.adjustVolume(-0.1f) }) {
                        Icon(Icons.Default.VolumeDown, "Vol Down", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.toggleFullScreen() }) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.pressPowerButton() }) {
                        Icon(Icons.Default.PowerSettingsNew, "Power", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBar(viewModel: VisualPhoneViewModel, onSwipeDown: () -> Unit) {
    val currentTime by viewModel.currentTime.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isWifiOn by viewModel.isWifiOn.collectAsState()
    val isMobileOn by viewModel.isMobileOn.collectAsState()
    val isBluetoothOn by viewModel.isBluetoothOn.collectAsState()
    val isDnd by viewModel.isDnd.collectAsState()
    val isAirplaneMode by viewModel.isAirplaneMode.collectAsState()
    val notifications = viewModel.notifications

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (dragAmount.y > 20) {
                        onSwipeDown()
                    }
                }
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side: Clock & Notification Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = currentTime.substringBefore(" "), // e.g. "12:00"
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            // Notifications mini icons
            if (notifications.isNotEmpty()) {
                val uniqueApps = notifications.map { it.appName }.distinct().take(3)
                uniqueApps.forEach { app ->
                    val icon = when (app) {
                        "Messages" -> Icons.Default.Chat
                        "Phone" -> Icons.Default.Phone
                        "Email" -> Icons.Default.Email
                        "Clock" -> Icons.Default.Alarm
                        else -> Icons.Default.Notifications
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Right Side: Hardware status icons (Wifi, cellular, battery)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isDnd) {
                Icon(Icons.Default.DoNotDisturbOn, "DND", tint = Color.White, modifier = Modifier.size(15.dp))
            }
            if (isBluetoothOn) {
                Icon(Icons.Default.Bluetooth, "Bluetooth", tint = Color.White, modifier = Modifier.size(15.dp))
            }
            if (isAirplaneMode) {
                Icon(Icons.Default.AirplanemodeActive, "Airplane Mode", tint = Color.White, modifier = Modifier.size(15.dp))
            } else {
                if (isWifiOn) {
                    Icon(Icons.Default.Wifi, "Wi-Fi Connected", tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.WifiOff, "Wi-Fi Disconnected", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
                if (isMobileOn) {
                    Icon(Icons.Default.SignalCellular4Bar, "Cellular Connected", tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.SignalCellularConnectedNoInternet0Bar, "Cellular Disconnected", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }

            // Battery Level representation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$batteryLevel%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = when {
                        batteryLevel > 80 -> Icons.Default.BatteryFull
                        batteryLevel > 30 -> Icons.Default.Battery3Bar
                        else -> Icons.Default.BatteryAlert
                    },
                    contentDescription = "Battery Status",
                    tint = if (batteryLevel <= 20) Color.Red else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun NavigationBar(viewModel: VisualPhoneViewModel) {
    val isGestureNav by viewModel.isGestureNav.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black.copy(alpha = 0.45f))
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (isGestureNav) {
            // Elegant modern gesture navigation bar
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { viewModel.goHome() }
                    .testTag("gesture_navigation_bar")
            )
        } else {
            // Standard classic three-button navigation bar
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Triangle style)
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.testTag("back_button").size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back Button",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Home Button (Circle style)
                IconButton(
                    onClick = { viewModel.goHome() },
                    modifier = Modifier.testTag("home_button").size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Home Button",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recents Tasks Button (Square style)
                IconButton(
                    onClick = { viewModel.openRecents() },
                    modifier = Modifier.testTag("recents_button").size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CropSquare,
                        contentDescription = "Recents Button",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LockScreen(viewModel: VisualPhoneViewModel) {
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val passcode by viewModel.passcode.collectAsState()
    val simulatedStateName by viewModel.simulatedStateName.collectAsState()
    val simulatedCityName by viewModel.simulatedCityName.collectAsState()
    val notifications = viewModel.notifications

    var pinInput by remember { mutableStateOf("") }
    var showNumpad by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Wallpapers[3]) // Dark wallpaper for locked style
            .testTag("lock_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Dynamic Clock & Location Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Device Indicator",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = currentDate,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, "Location Status", tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Text(
                        text = "$simulatedCityName, $simulatedStateName State",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Middle Section: Notifications list inside Lock Screen
            if (!showNumpad) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No recent notifications",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        notifications.take(3).forEach { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (notif.appName) {
                                            "Messages" -> Icons.Default.Chat
                                            "Phone" -> Icons.Default.Phone
                                            "Email" -> Icons.Default.Email
                                            "Clock" -> Icons.Default.Alarm
                                            else -> Icons.Default.Notifications
                                        }
                                        Icon(icon, notif.appName, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(notif.text, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(notif.timestamp, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section: Lock unlock gestures / PIN Keypad input
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showNumpad) {
                    // Enter PIN Screen UI
                    Text(
                        text = "Enter simulated PIN (Default is '1234')",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Dots visualizer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        repeat(4) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (idx < pinInput.length) Color.White else Color.White.copy(alpha = 0.3f))
                            )
                        }
                    }

                    // Numeric keypad 3x4
                    val nums = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Cancel", "0", "⌫")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        nums.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { char ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .clickable {
                                                if (char == "⌫") {
                                                    if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                } else if (char == "Cancel") {
                                                    showNumpad = false
                                                    pinInput = ""
                                                } else {
                                                    if (pinInput.length < 4) {
                                                        pinInput += char
                                                        if (pinInput.length == 4) {
                                                            viewModel.triggerLockScreenUnlock(pinInput)
                                                            pinInput = ""
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            color = Color.White,
                                            fontSize = if (char.length > 1) 12.sp else 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Fingerprint icon trigger shortcut & swipe to unlock trigger
                    IconButton(
                        onClick = { viewModel.triggerLockScreenUnlock("1234") },
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(8.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Simulated Fingerprint Sensor",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Tap fingerprint or swipe up to unlock",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { showNumpad = true }
                            .padding(top = 10.dp)
                    )
                }
            }
        }
    }
}
