package com.example.ui.phone

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.BorderStroke
import kotlin.random.Random
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.draw.scale
import com.example.data.model.ContactEntity
import com.example.data.model.LocationEntity
import com.example.data.model.NoteEntity
import com.example.ui.viewmodel.VisualPhoneViewModel
import com.example.ui.viewmodel.PhoneApp
import com.example.ui.viewmodel.SimulatedTower
import com.example.ui.viewmodel.NigerianState
import com.example.ui.viewmodel.AppCategory
import com.example.ui.viewmodel.AppMetadata
import com.example.ui.viewmodel.PlayStoreReview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppContainer(
    appName: String,
    viewModel: VisualPhoneViewModel,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("app_window_${appName.replace(" ", "_").lowercase()}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Simulated App Header Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { viewModel.goHome() }) {
                        Icon(Icons.Default.Close, "Exit App")
                    }
                    Text(
                        text = appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Small Active Indicator Logs shortcut
                IconButton(onClick = { viewModel.launchApp(PhoneApp.SETTINGS) }) {
                    Icon(Icons.Default.Settings, "App Config", modifier = Modifier.size(20.dp))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

// ==========================================
// 1. DIALER APP
// ==========================================
@Composable
fun DialerApp(viewModel: VisualPhoneViewModel) {
    val dialerNumber by viewModel.dialerNumber.collectAsState()
    val activeCallState by viewModel.activeCallState.collectAsState()
    val activeCallContactName by viewModel.activeCallContactName.collectAsState()
    val callTimerSeconds by viewModel.callTimerSeconds.collectAsState()

    AppContainer("Phone Dialer", viewModel) {
        if (activeCallState == "IDLE" || activeCallState == "ENDED") {
            // Dialer Keypad Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialer display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dialerNumber.ifEmpty { "Enter Number" },
                        fontSize = if (dialerNumber.length > 12) 22.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dialerNumber.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    )

                    if (dialerNumber.isNotEmpty()) {
                        IconButton(onClick = { viewModel.backspaceDialer() }) {
                            Icon(Icons.Default.Backspace, "Backspace")
                        }
                    }
                }

                // Keypad Grid 3x4
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keys.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            row.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { viewModel.enterDialerDigit(digit.first()) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        digit,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Call Button
                IconButton(
                    onClick = { viewModel.startCall(dialerNumber) },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                ) {
                    Icon(Icons.Default.Call, "Dial Call", tint = Color.White)
                }
            }
        } else {
            // In Call / Dialing Simulation Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0C1D2A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCallContactName.take(1),
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = activeCallContactName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dialerNumber,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (activeCallState) {
                                "DIALING" -> "Dialing..."
                                "ACTIVE" -> {
                                    val m = callTimerSeconds / 60
                                    val s = callTimerSeconds % 60
                                    String.format("%02d:%02d", m, s)
                                }
                                "INCOMING" -> "Incoming Call..."
                                else -> "Call Connected"
                            },
                            color = if (activeCallState == "ACTIVE") Color(0xFF4CAF50) else Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    // Simulated Audio Call Waves drawing on Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val centerY = size.height / 2
                        val steps = 15
                        val stepWidth = size.width / steps
                        for (i in 0 until steps) {
                            val factor = if (activeCallState == "ACTIVE") (Math.random() * 40).toFloat() else 5f
                            drawLine(
                                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                                start = Offset(i * stepWidth + 10, centerY - factor),
                                end = Offset(i * stepWidth + 10, centerY + factor),
                                strokeWidth = 6f
                            )
                        }
                    }

                    // Call control action buttons
                    if (activeCallState == "INCOMING") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Decline
                            IconButton(
                                onClick = { viewModel.endCall() },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFD32F2F), CircleShape)
                            ) {
                                Icon(Icons.Default.CallEnd, "Decline Call", tint = Color.White)
                            }
                            // Accept
                            IconButton(
                                onClick = { viewModel.answerCall() },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            ) {
                                Icon(Icons.Default.Call, "Accept Call", tint = Color.White)
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.endCall() },
                            modifier = Modifier
                                            .size(64.dp)
                                .background(Color(0xFFD32F2F), CircleShape)
                        ) {
                            Icon(Icons.Default.CallEnd, "End Call", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. CONTACTS APP
// ==========================================
@Composable
fun ContactsApp(viewModel: VisualPhoneViewModel) {
    val contacts by viewModel.contactsFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newState by remember { mutableStateOf("Lagos") }
    var newNotes by remember { mutableStateOf("") }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    AppContainer("Contacts", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Add Contact trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search contact...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, "Add Contact", tint = Color.White)
                }
            }

            // Contact List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredContacts.isEmpty()) {
                    item {
                        Text(
                            text = "No contacts found",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                } else {
                    items(filteredContacts) { contact ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Column {
                                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(contact.phone, color = Color.Gray, fontSize = 12.sp)
                                        Text("State: ${contact.state}", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { viewModel.startCall(contact.phone) }) {
                                        Icon(Icons.Default.Call, "Call", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.startChatWith(contact) }) {
                                        Icon(Icons.Default.Chat, "Message", tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteContact(contact) }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("New Simulated Contact") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                        OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone Number") })
                        OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email Address") })
                        OutlinedTextField(value = newState, onValueChange = { newState = it }, label = { Text("Nigerian State") })
                        OutlinedTextField(value = newNotes, onValueChange = { newNotes = it }, label = { Text("Notes") })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                                viewModel.saveNewContact(newName, newPhone, newEmail, newState, newNotes)
                                showAddDialog = false
                                newName = ""
                                newPhone = ""
                                newEmail = ""
                                newNotes = ""
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// ==========================================
// 3. MESSAGES APP
// ==========================================
@Composable
fun MessagesApp(viewModel: VisualPhoneViewModel) {
    val activeMessageContact by viewModel.activeMessageContact.collectAsState()
    val chatMessageInput by viewModel.chatMessageInput.collectAsState()
    val contacts by viewModel.contactsFlow.collectAsState()

    AppContainer("Messages", viewModel) {
        if (activeMessageContact == null) {
            // Threads List Select
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Select a contact to message:",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.startChatWith(contact) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tap to simulate messaging", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Chat Message UI Conversation Screen
            val contact = activeMessageContact!!
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of active Chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Active simulated chat", color = Color.DarkGray, fontSize = 10.sp)
                    }
                }

                // Messages Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.chatMessages) { msg ->
                        val bubbleColor = if (msg.isIncoming) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                        val align = if (msg.isIncoming) Alignment.Start else Alignment.End
                        val txtColor = if (msg.isIncoming) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

                        Column(
                            horizontalAlignment = align,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(msg.body, color = txtColor, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        msg.timestamp,
                                        color = txtColor.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatMessageInput,
                        onValueChange = { viewModel.setChatMessageInput(it) },
                        placeholder = { Text("Simulate type SMS...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )

                    IconButton(
                        onClick = { viewModel.sendChatMessage() },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CAMERA APP
// ==========================================
@Composable
fun CameraApp(viewModel: VisualPhoneViewModel) {
    AppContainer("Camera Simulator", viewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Viewfinder representation with grid drawing on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridY1 = size.height / 3
                val gridY2 = (size.height / 3) * 2
                val gridX1 = size.width / 3
                val gridX2 = (size.width / 3) * 2

                // Draw vertical grid
                drawLine(Color.White.copy(alpha = 0.25f), Offset(gridX1, 0f), Offset(gridX1, size.height), strokeWidth = 2f)
                drawLine(Color.White.copy(alpha = 0.25f), Offset(gridX2, 0f), Offset(gridX2, size.height), strokeWidth = 2f)

                // Draw horizontal grid
                drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, gridY1), Offset(size.width, gridY1), strokeWidth = 2f)
                drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, gridY2), Offset(size.width, gridY2), strokeWidth = 2f)
            }

            // Simple aesthetic camera focus box
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .border(1.dp, Color.Yellow.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            )

            // Text indicating simulation
            Text(
                "Simulated Camera Viewfinder\nTap shutter to capture photo to Gallery",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            // Bottom camera controls bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shutter Thumbnail trigger (Gallery icon)
                IconButton(onClick = { viewModel.launchApp(PhoneApp.GALLERY) }) {
                    Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                // Master Capture Button (Circular Outer Ring, Filled Center)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { viewModel.capturePhoto() }
                )

                // Toggle Camera lens simulation
                IconButton(onClick = { viewModel.logEvent("Camera", "Swapped Lens Mode (Front/Rear)") }) {
                    Icon(Icons.Default.FlipCameraAndroid, "Lens Swap", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ==========================================
// 5. GALLERY APP
// ==========================================
@Composable
fun GalleryApp(viewModel: VisualPhoneViewModel) {
    val images = viewModel.galleryImages
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    AppContainer("Gallery", viewModel) {
        if (selectedImageId == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { imgId ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    imgId == "wall_sunset" -> Color(0xFFE65100)
                                    imgId == "wall_cool_purple" -> Color(0xFF4A148C)
                                    imgId == "wall_emerald" -> Color(0xFF004D40)
                                    else -> Color.DarkGray
                                }
                            )
                            .clickable { selectedImageId = imgId },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (imgId.startsWith("capture_")) Icons.Default.CameraAlt else Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (imgId.startsWith("capture_")) "Photo" else "Wallpaper",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Full screen photo viewer
            val imgId = selectedImageId!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Large color area simulating photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                imgId == "wall_sunset" -> Color(0xFFE65100)
                                imgId == "wall_cool_purple" -> Color(0xFF4A148C)
                                imgId == "wall_emerald" -> Color(0xFF004D40)
                                else -> Color.DarkGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simulated View\n$imgId.png",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Top action bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { selectedImageId = null }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Row {
                        IconButton(onClick = {
                            viewModel.galleryImages.remove(imgId)
                            selectedImageId = null
                            viewModel.logEvent("Gallery", "Deleted image: $imgId")
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. CLOCK APP
// ==========================================
@Composable
fun ClockApp(viewModel: VisualPhoneViewModel) {
    val stopwatchTimeMs by viewModel.stopwatchTimeMs.collectAsState()
    val stopwatchRunning by viewModel.stopwatchRunning.collectAsState()
    val timerTimeRemaining by viewModel.timerTimeRemaining.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: World Clock, 1: Stopwatch, 2: Timer

    AppContainer("Clock", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("World Clock", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Stopwatch", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Timer", modifier = Modifier.padding(12.dp))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> {
                        // World Clock
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Simulated World Clocks", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            listOf(
                                Pair("Abuja, Nigeria", "GMT +1"),
                                Pair("Lagos, Nigeria", "GMT +1"),
                                Pair("London, UK", "GMT +1"),
                                Pair("New York, USA", "GMT -4"),
                                Pair("Tokyo, Japan", "GMT +9")
                            ).forEach { pair ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(pair.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(pair.second, color = Color.Gray, fontSize = 11.sp)
                                        }

                                        // Dynamic local time simulated
                                        Text(
                                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Stopwatch
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Large digital display
                            val min = stopwatchTimeMs / 60000
                            val sec = (stopwatchTimeMs % 60000) / 1000
                            val ms = (stopwatchTimeMs % 1000) / 10
                            val formStr = String.format("%02d:%02d.%02d", min, sec, ms)

                            Text(
                                text = formStr,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 24.dp),
                                fontFamily = FontFamily.Monospace
                            )

                            // Controls Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.resetStopwatch() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) {
                                    Text("Reset")
                                }

                                Button(
                                    onClick = { viewModel.toggleStopwatch() },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (stopwatchRunning) Color.Red else Color.Green)
                                ) {
                                    Text(if (stopwatchRunning) "Pause" else "Start")
                                }

                                if (stopwatchRunning) {
                                    Button(onClick = { viewModel.lapStopwatch() }) {
                                        Text("Lap")
                                    }
                                }
                            }

                            // Lap List
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                items(viewModel.stopwatchLaps) { lap ->
                                    Text(
                                        text = lap,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Timer
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = if (timerTimeRemaining > 0) {
                                    val m = timerTimeRemaining / 60
                                    val s = timerTimeRemaining % 60
                                    String.format("%02d:%02d", m, s)
                                } else "00:00",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            if (timerTimeRemaining <= 0) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.setTimer(60) }) { Text("1 Min") }
                                    Button(onClick = { viewModel.setTimer(300) }) { Text("5 Min") }
                                    Button(onClick = { viewModel.setTimer(600) }) { Text("10 Min") }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = { viewModel.resetTimer() }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                    Text("Reset")
                                }
                                Button(onClick = { viewModel.toggleTimer() }, colors = ButtonDefaults.buttonColors(containerColor = if (timerRunning) Color.Red else Color.Green)) {
                                    Text(if (timerRunning) "Pause" else "Start")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. CALENDAR APP
// ==========================================
@Composable
fun CalendarApp(viewModel: VisualPhoneViewModel) {
    var selectedDay by remember { mutableStateOf(6) }
    var showEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    val savedEvents = remember { mutableStateListOf(
        Pair(6, "Dandali Demo Project Presentation"),
        Pair(15, "Location Changer validation"),
        Pair(20, "Nigerian State Coverage Check")
    ) }

    AppContainer("Calendar", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("July 2026", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { showEventDialog = true }) {
                    Icon(Icons.Default.AddBox, "Add Event")
                }
            }

            // Calendar simple grid 7x5 representation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(day, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                }
            }

            // Standard grid calculation
            val days = (1..31).toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(200.dp)
            ) {
                // Buffer offsets
                items(3) { Box(modifier = Modifier.size(36.dp)) }

                items(days) { day ->
                    val isSelected = day == selectedDay
                    val hasEvent = savedEvents.any { it.first == day }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    hasEvent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (hasEvent && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }

            // Event Logs list
            Divider()
            Text("Events for Jul $selectedDay", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            val currentDayEvents = savedEvents.filter { it.first == selectedDay }
            if (currentDayEvents.isEmpty()) {
                Text("No events scheduled for this day.", color = Color.Gray, fontSize = 12.sp)
            } else {
                currentDayEvents.forEach { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = ev.second,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (showEventDialog) {
            AlertDialog(
                onDismissRequest = { showEventDialog = false },
                title = { Text("Add Event for Jul $selectedDay") },
                text = {
                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        placeholder = { Text("Meeting / Reminder Title") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newEventTitle.isNotEmpty()) {
                            savedEvents.add(Pair(selectedDay, newEventTitle))
                            showEventDialog = false
                            newEventTitle = ""
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEventDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// ==========================================
// 8. CALCULATOR APP
// ==========================================
@Composable
fun CalculatorApp(viewModel: VisualPhoneViewModel) {
    val formula by viewModel.calcFormula.collectAsState()
    val result by viewModel.calcResult.collectAsState()

    val keys = listOf(
        listOf("C", "%", "⌫", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "00", "=")
    )

    AppContainer("Calculator", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formula.ifEmpty { "0" },
                    fontSize = 28.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = result,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }

            // Keys Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { char ->
                            val isOp = char in listOf("+", "-", "*", "/", "=", "%")
                            val isClear = char in listOf("C", "⌫")
                            val btnColor = when {
                                char == "=" -> MaterialTheme.colorScheme.primary
                                isOp -> MaterialTheme.colorScheme.secondaryContainer
                                isClear -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val txtColor = when {
                                char == "=" -> Color.White
                                isOp -> MaterialTheme.colorScheme.onSecondaryContainer
                                isClear -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(btnColor)
                                    .clickable { viewModel.pressCalculatorButton(char) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = txtColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. FILE MANAGER
// ==========================================
@Composable
fun FileManagerApp(viewModel: VisualPhoneViewModel) {
    val downloadedApks = viewModel.downloadedApks
    var activeCategory by remember { mutableStateOf<String?>(null) }

    AppContainer("Files", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (activeCategory == null) {
                // Storage Usage Header
                Text("Simulated Storage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                LinearProgressIndicator(
                    progress = 0.45f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("28.8 GB Used", fontSize = 12.sp, color = Color.Gray)
                    Text("64 GB Total", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Categories", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                listOf(
                    Triple("Downloads", Icons.Default.Download, "Contains saved files"),
                    Triple("APKs", Icons.Default.Apps, "Simulated Android APK files"),
                    Triple("Documents", Icons.Default.DocumentScanner, "Manuals & logs"),
                    Triple("Media", Icons.Default.PhotoLibrary, "Gallery & assets")
                ).forEach { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { activeCategory = cat.first },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(cat.second, cat.first, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(cat.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(cat.third, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // Subdirectory list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeCategory = null }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    Text(activeCategory!!, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeCategory == "APKs") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (downloadedApks.isEmpty()) {
                            item {
                                Text("No simulated APK files. Download them from Google Play Store app.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(downloadedApks) { apk ->
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
                                            Text(apk.id.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(apk.packageName, color = Color.Gray, fontSize = 11.sp)
                                            Text("Size: ${apk.storageSize} | v${apk.version}", fontSize = 10.sp)
                                        }

                                        Button(onClick = {
                                            viewModel.launchApp(apk.id)
                                        }) {
                                            Text("Open")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Directory is empty", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

// ==========================================
// 10. BROWSER APP
// ==========================================
@Composable
fun BrowserApp(viewModel: VisualPhoneViewModel) {
    val browserUrl by viewModel.browserUrl.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    val popularSites = listOf(
        Pair("Google", "https://www.google.com"),
        Pair("Dandali News", "https://dandali.ng"),
        Pair("Naija Tech", "https://techcabal.com"),
        Pair("Wikipedia", "https://wikipedia.org")
    )

    AppContainer("Browser", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Address Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = browserUrl,
                    onValueChange = { viewModel.setBrowserUrl(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            isLoading = true
                            viewModel.loadBrowserUrl()
                        }) {
                            Icon(Icons.Default.ArrowForward, "Go")
                        }
                    }
                )
            }

            // Simulated web frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Simulated Web Content",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Loaded Page: $browserUrl",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    Text("Shortcuts:", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        popularSites.forEach { site ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.setBrowserUrl(site.second)
                                        viewModel.loadBrowserUrl()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(site.first, color = Color.DarkGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. MUSIC APP
// ==========================================
@Composable
fun MusicApp(viewModel: VisualPhoneViewModel) {
    val currentIndex by viewModel.currentSongIndex.collectAsState()
    val isPlaying by viewModel.isMusicPlaying.collectAsState()
    val progress by viewModel.musicProgress.collectAsState()
    val song = viewModel.songsList[currentIndex]

    AppContainer("Music Player", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Album artwork block
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(android.graphics.Color.parseColor(song.coverColorHex))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, "Album Art", tint = Color.White, modifier = Modifier.size(72.dp))
            }

            // Song Metadata
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(song.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(song.artist, color = Color.Gray, fontSize = 14.sp)
            }

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:25", fontSize = 10.sp, color = Color.Gray)
                    Text(song.duration, fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Player Controls Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.skipPrevSong() }) {
                    Icon(Icons.Default.SkipPrevious, "Prev", modifier = Modifier.size(36.dp))
                }

                IconButton(
                    onClick = { viewModel.toggleMusicPlayback() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { viewModel.skipNextSong() }) {
                    Icon(Icons.Default.SkipNext, "Next", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

// ==========================================
// 12. NOTES APP
// ==========================================
@Composable
fun NotesApp(viewModel: VisualPhoneViewModel) {
    val notes by viewModel.notesFlow.collectAsState()
    var showNoteEditor by remember { mutableStateOf(false) }

    // Editor field states
    var idState by remember { mutableStateOf<Int?>(null) }
    var titleState by remember { mutableStateOf("") }
    var contentState by remember { mutableStateOf("") }
    var colorState by remember { mutableStateOf("#FF9800") }

    val noteColors = listOf("#F44336", "#E91E63", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800")

    AppContainer("Notes", viewModel) {
        if (!showNoteEditor) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notes) { note ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(android.graphics.Color.parseColor(note.colorHex)).copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    idState = note.id
                                    titleState = note.title
                                    contentState = note.content
                                    colorState = note.colorHex
                                    showNoteEditor = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(note.title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(note.content, fontSize = 11.sp, color = Color.Black.copy(alpha = 0.8f), maxLines = 4, overflow = TextOverflow.Ellipsis)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Floating Action Button trigger
                FloatingActionButton(
                    onClick = {
                        idState = null
                        titleState = ""
                        contentState = ""
                        colorState = "#FF9800"
                        showNoteEditor = true
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Note", tint = Color.White)
                }
            }
        } else {
            // Write/Edit note layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (idState != null) "Edit Note" else "New Note", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showNoteEditor = false }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    OutlinedTextField(
                        value = titleState,
                        onValueChange = { titleState = it },
                        placeholder = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contentState,
                        onValueChange = { contentState = it },
                        placeholder = { Text("Start typing note details...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    // Color swatches row
                    Text("Select Color Badge", fontSize = 11.sp, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(noteColors) { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .border(
                                        2.dp,
                                        if (colorState == col) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { colorState = col }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (titleState.isNotEmpty() && contentState.isNotEmpty()) {
                            viewModel.saveNote(idState, titleState, contentState, colorState)
                            showNoteEditor = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Note")
                }
            }
        }
    }
}

// ==========================================
// 13. WEATHER APP
// ==========================================
@Composable
fun WeatherApp(viewModel: VisualPhoneViewModel) {
    val simulatedCityName by viewModel.simulatedCityName.collectAsState()
    val simulatedStateName by viewModel.simulatedStateName.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }

    AppContainer("Weather", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Weather State Selector Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showDropdown = !showDropdown }
                ) {
                    Text(
                        text = "$simulatedCityName, Nigeria",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                Text(simulatedStateName, color = Color.Gray, fontSize = 13.sp)

                if (showDropdown) {
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(200.dp)
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        LazyColumn {
                            items(viewModel.nigerianStatesList) { state ->
                                Text(
                                    text = "${state.capital} (${state.name})",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.changeSimulatedLocation(state)
                                            showDropdown = false
                                        }
                                        .padding(12.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Temperature graphic
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Generate simulated temperature based on coordinate hash
                val hashValue = (simulatedCityName.hashCode() % 6) + 26
                val conditions = listOf("Sunny", "Cloudy", "Rainy Showers", "Thunderstorm", "Hazy", "Partly Cloudy")
                val activeCondition = conditions[Math.abs(simulatedCityName.hashCode() % conditions.size)]

                val weatherIcon = when (activeCondition) {
                    "Sunny" -> Icons.Default.WbSunny
                    "Cloudy" -> Icons.Default.Cloud
                    "Rainy Showers" -> Icons.Default.Umbrella
                    "Thunderstorm" -> Icons.Default.Thunderstorm
                    else -> Icons.Default.WbCloudy
                }

                Icon(
                    imageVector = weatherIcon,
                    contentDescription = null,
                    tint = if (activeCondition == "Sunny") Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("$hashValue°C", fontSize = 54.sp, fontWeight = FontWeight.Bold)
                Text(activeCondition, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }

            // 5 Day forecasts
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEachIndexed { idx, day ->
                        val tempOffset = (idx - 2) * 2
                        val baseTemp = (simulatedCityName.hashCode() % 6) + 26
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day, fontSize = 11.sp, color = Color.Gray)
                            Icon(Icons.Default.Cloud, null, modifier = Modifier.size(16.dp))
                            Text("${baseTemp + tempOffset}°C", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 14. MAPS & LOCATION CHANGER APP
// ==========================================
// ==========================================
// 14. MAPS & LOCATION CHANGER APP (Advanced Location Manager)
// ==========================================
data class MockLandmark(
    val name: String,
    val lat: Double,
    val lon: Double,
    val category: String,
    val state: String
)

@Composable
fun MapsApp(viewModel: VisualPhoneViewModel) {
    val simulatedLat by viewModel.simulatedLatitude.collectAsState()
    val simulatedLon by viewModel.simulatedLongitude.collectAsState()
    val simulatedCityName by viewModel.simulatedCityName.collectAsState()
    val simulatedStateName by viewModel.simulatedStateName.collectAsState()
    val simulatedAccuracy by viewModel.simulatedAccuracy.collectAsState()
    val simulatedAltitude by viewModel.simulatedAltitude.collectAsState()
    val simulatedSpeed by viewModel.simulatedSpeed.collectAsState()
    val simulatedHeading by viewModel.simulatedHeading.collectAsState()
    val favorites by viewModel.locationsFlow.collectAsState()

    // Interactive Map State
    var mapZoom by remember { mutableFloatStateOf(1.8f) }
    var mapCenterX by remember { mutableDoubleStateOf(7.3986) } // Abuja Longitude
    var mapCenterY by remember { mutableDoubleStateOf(9.0765) } // Abuja Latitude
    var centerOnActiveGPS by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf("Standard") } // Standard, Satellite, Terrain, Hybrid

    // View State
    var selectedTab by remember { mutableStateOf(0) } // 0: Map, 1: Controls, 2: Nigeria Explorer, 3: Cell Site, 4: Dev APIs
    var isSidebarExpanded by remember { mutableStateOf(true) }
    var searchVal by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf("All") }
    var isDriveModeActive by remember { mutableStateOf(false) }

    // Inputs
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var manualInputError by remember { mutableStateOf<String?>(null) }
    var showFavDialog by remember { mutableStateOf(false) }
    var favNameInput by remember { mutableStateOf("") }
    var importCsvInput by remember { mutableStateOf("") }
    var showImportExport by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Active Developer API Simulated Tab
    var activeApiTab by remember { mutableStateOf(0) } // 0: Geocoding, 1: Places, 2: Directions, 3: Weather, 4: Elevation

    val coroutineScope = rememberCoroutineScope()

    // Sync Map Center to Active GPS if locked
    LaunchedEffect(centerOnActiveGPS, simulatedLat, simulatedLon) {
        if (centerOnActiveGPS) {
            mapCenterX = simulatedLon
            mapCenterY = simulatedLat
        }
    }

    // Live Drive Simulation Movement Loop
    LaunchedEffect(isDriveModeActive) {
        if (isDriveModeActive) {
            while (true) {
                delay(1000)
                val speed = viewModel.simulatedSpeed.value
                if (speed > 0f) {
                    val heading = viewModel.simulatedHeading.value
                    val lat = viewModel.simulatedLatitude.value
                    val lon = viewModel.simulatedLongitude.value

                    // Distance in 1 second = speed (km/h) / 3600 (seconds) in km
                    val distanceKm = speed / 3600.0
                    // 1 degree latitude = 111.12 km
                    // 1 degree longitude = 111.12 * cos(lat) km
                    val deltaLat = (distanceKm / 111.12) * Math.cos(Math.toRadians(heading.toDouble()))
                    val deltaLon = (distanceKm / (111.12 * Math.cos(Math.toRadians(lat)))) * Math.sin(Math.toRadians(heading.toDouble()))

                    val nextLat = (lat + deltaLat).coerceIn(4.0, 14.0)
                    val nextLon = (lon + deltaLon).coerceIn(2.5, 15.0)

                    viewModel.setSimulatedCoordinates(nextLat, nextLon, viewModel.simulatedCityName.value)
                }
            }
        }
    }

    // List of Mock Landmarks across Nigeria for rich explorer mapping
    val landmarks = remember {
        listOf(
            MockLandmark("Zuma Rock", 9.1283, 7.2308, "Tourist Attraction", "Federal Capital Territory"),
            MockLandmark("Nnamdi Azikiwe Int'l Airport", 9.0068, 7.2631, "Airport", "Federal Capital Territory"),
            MockLandmark("National Hospital Abuja", 9.0435, 7.4725, "Hospital", "Federal Capital Territory"),
            MockLandmark("Ikeja City Mall", 6.6119, 3.3582, "Shopping Centre", "Lagos"),
            MockLandmark("Lekki Conservation Centre", 6.4281, 3.5350, "Tourist Attraction", "Lagos"),
            MockLandmark("Murtala Muhammed Airport", 6.5774, 3.3210, "Airport", "Lagos"),
            MockLandmark("Lagos University Teaching Hospital", 6.5255, 3.3615, "Hospital", "Lagos"),
            MockLandmark("Olumo Rock", 7.1608, 3.3500, "Tourist Attraction", "Ogun"),
            MockLandmark("Yankari Game Reserve", 9.7543, 10.5055, "Tourist Attraction", "Bauchi"),
            MockLandmark("Obudu Mountain Resort", 6.3812, 9.3800, "Tourist Attraction", "Cross River"),
            MockLandmark("Akanu Ibiam Airport", 6.4714, 7.5620, "Airport", "Enugu"),
            MockLandmark("Mallam Aminu Kano Airport", 12.0476, 8.5246, "Airport", "Kano"),
            MockLandmark("Kajuru Castle", 10.3117, 7.7889, "Tourist Attraction", "Kaduna"),
            MockLandmark("Zenith Bank HQ", 6.4312, 3.4251, "Bank", "Lagos"),
            MockLandmark("Jabi Lake Mall", 9.0768, 7.4208, "Shopping Centre", "Federal Capital Territory")
        )
    }

    AppContainer("Maps & Location Changer", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Material 3 Tabs Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf("Map", "Controls", "Explorer", "Coverage", "Dev APIs")
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Notification Banner if present
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificationMessage ?: "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { notificationMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                    }
                }
                LaunchedEffect(notificationMessage) {
                    if (notificationMessage != null) {
                        delay(4000)
                        notificationMessage = null
                    }
                }
            }

            // Tab Content Panes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        // ==========================================
                        // TAB 1: INTERACTIVE MAP (CANVAS COMPONENT WITH SIDEBAR)
                        // ==========================================
                        Row(modifier = Modifier.fillMaxSize()) {
                            AnimatedVisibility(
                                visible = isSidebarExpanded,
                                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                            ) {
                                LocationSidebar(
                                    viewModel = viewModel,
                                    latInput = latInput,
                                    onLatChange = { latInput = it },
                                    lonInput = lonInput,
                                    onLonChange = { lonInput = it },
                                    searchVal = searchVal,
                                    onSearchChange = { searchVal = it },
                                    favorites = favorites,
                                    onSaveFavorite = { showFavDialog = true },
                                    onNotification = { notificationMessage = it },
                                    onCloseSidebar = { isSidebarExpanded = false }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .fillMaxWidth()
                            ) {
                                // Background Colors depending on MapType
                                val mapBg = when (mapType) {
                                    "Satellite" -> Color(0xFF1B301B)
                                    "Terrain" -> Color(0xFFD7CCC8)
                                    "Hybrid" -> Color(0xFF1B301B)
                                    else -> Color(0xFFE8F5E9) // Standard green plains
                                }

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(mapBg)
                                        .pointerInput(mapCenterX, mapCenterY, mapZoom) {
                                            detectDragGestures { change, dragAmount ->
                                                // Disable Auto-GPS lock if panning manually
                                                centerOnActiveGPS = false
                                                
                                                // Convert drag pixels to geographic coordinates
                                                val pixelsPerDegree = 80f * mapZoom
                                                mapCenterX -= dragAmount.x / pixelsPerDegree
                                                mapCenterY += dragAmount.y / pixelsPerDegree // inverted Y
                                            }
                                        }
                                ) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val pixelsPerDegree = 80f * mapZoom

                                    fun getScreenOffset(lat: Double, lon: Double): Offset {
                                        val dx = (lon - mapCenterX) * pixelsPerDegree
                                        val dy = (mapCenterY - lat) * pixelsPerDegree
                                        return Offset(cx + dx.toFloat(), cy + dy.toFloat())
                                    }

                                    // DRAW GEOGRAPHIC GRID
                                    val step = 1.0 // 1 degree step
                                    val minGridLat = (mapCenterY - (cy / pixelsPerDegree)).toInt() - 1
                                    val maxGridLat = (mapCenterY + (cy / pixelsPerDegree)).toInt() + 1
                                    val minGridLon = (mapCenterX - (cx / pixelsPerDegree)).toInt() - 1
                                    val maxGridLon = (mapCenterX + (cx / pixelsPerDegree)).toInt() + 1

                                    for (la in minGridLat..maxGridLat) {
                                        val p1 = getScreenOffset(la.toDouble(), minGridLon.toDouble())
                                        val p2 = getScreenOffset(la.toDouble(), maxGridLon.toDouble())
                                        drawLine(
                                            color = if (mapType == "Satellite" || mapType == "Hybrid") Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                            start = p1,
                                            end = p2,
                                            strokeWidth = 1f
                                        )
                                    }
                                    for (lo in minGridLon..maxGridLon) {
                                        val p1 = getScreenOffset(minGridLat.toDouble(), lo.toDouble())
                                        val p2 = getScreenOffset(maxGridLat.toDouble(), lo.toDouble())
                                        drawLine(
                                            color = if (mapType == "Satellite" || mapType == "Hybrid") Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                            start = p1,
                                            end = p2,
                                            strokeWidth = 1f
                                        )
                                    }

                                    // DRAW RIVERS (Niger & Benue Meeting)
                                    val riverPathPoints = listOf(
                                        Pair(12.45, 4.19), // Birnin Kebbi
                                        Pair(11.0, 5.0),
                                        Pair(9.61, 6.55),  // Minna
                                        Pair(7.79, 6.74),  // Lokoja (Meeting Point)
                                        Pair(6.19, 6.72),  // Asaba
                                        Pair(4.92, 6.26)   // Yenagoa / Delta
                                    )
                                    val benuePathPoints = listOf(
                                        Pair(9.20, 12.49), // Yola
                                        Pair(10.28, 11.16), // Gombe
                                        Pair(8.5, 9.5),
                                        Pair(7.79, 6.74)   // Lokoja
                                    )

                                    val riverColor = if (mapType == "Satellite" || mapType == "Hybrid") Color(0xFF0D47A1) else Color(0xFF00B0FF)
                                    // Draw Niger River
                                    for (i in 0 until riverPathPoints.size - 1) {
                                        val o1 = getScreenOffset(riverPathPoints[i].first, riverPathPoints[i].second)
                                        val o2 = getScreenOffset(riverPathPoints[i+1].first, riverPathPoints[i+1].second)
                                        drawLine(color = riverColor, start = o1, end = o2, strokeWidth = 8f)
                                    }
                                    // Draw Benue River
                                    for (i in 0 until benuePathPoints.size - 1) {
                                        val o1 = getScreenOffset(benuePathPoints[i].first, benuePathPoints[i].second)
                                        val o2 = getScreenOffset(benuePathPoints[i+1].first, benuePathPoints[i+1].second)
                                        drawLine(color = riverColor, start = o1, end = o2, strokeWidth = 6f)
                                    }

                                    // DRAW HIGHWAYS
                                    val highwayPoints = listOf(
                                        Pair(6.52, 3.37),   // Lagos
                                        Pair(7.14, 3.36),   // Abeokuta
                                        Pair(7.79, 6.74),   // Lokoja
                                        Pair(9.07, 7.39),   // Abuja
                                        Pair(10.51, 7.41),  // Kaduna
                                        Pair(12.00, 8.59)   // Kano
                                    )
                                    val roadColor = if (mapType == "Satellite" || mapType == "Hybrid") Color(0xFFECEFF1) else Color(0xFFFFB74D)
                                    val roadBorderColor = if (mapType == "Satellite" || mapType == "Hybrid") Color(0xFF37474F) else Color(0xFFE65100)

                                    for (i in 0 until highwayPoints.size - 1) {
                                        val o1 = getScreenOffset(highwayPoints[i].first, highwayPoints[i].second)
                                        val o2 = getScreenOffset(highwayPoints[i+1].first, highwayPoints[i+1].second)
                                        // Draw border
                                        drawLine(color = roadBorderColor, start = o1, end = o2, strokeWidth = 5f)
                                        // Draw fill
                                        drawLine(color = roadColor, start = o1, end = o2, strokeWidth = 3f)
                                    }

                                    // DRAW TERRAIN CONTOUR CIRCLES IN JOS PLATEAU
                                    if (mapType == "Terrain") {
                                        val mountainCenter = getScreenOffset(9.8965, 8.8583) // Jos
                                        drawCircle(
                                            color = Color(0xFF8D6E63).copy(alpha = 0.2f),
                                            center = mountainCenter,
                                            radius = 80f * mapZoom,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                        )
                                        drawCircle(
                                            color = Color(0xFF8D6E63).copy(alpha = 0.3f),
                                            center = mountainCenter,
                                            radius = 45f * mapZoom,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                        )
                                    }

                                    // DRAW STATE CAPITALS OR STATES CENTERS
                                    for (st in viewModel.nigerianStatesList) {
                                        val offset = getScreenOffset(st.latitude, st.longitude)
                                        // Simple state boundary nodes
                                        drawCircle(
                                            color = if (mapType == "Satellite" || mapType == "Hybrid") Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f),
                                            center = offset,
                                            radius = 4f
                                        )
                                    }

                                    // DRAW LANDMARK NODES
                                    for (lm in landmarks) {
                                        val offset = getScreenOffset(lm.lat, lm.lon)
                                        val color = when (lm.category) {
                                            "Airport" -> Color(0xFF2196F3)
                                            "Hospital" -> Color(0xFFE91E63)
                                            "Shopping Centre" -> Color(0xFFFF9800)
                                            else -> Color(0xFF9C27B0)
                                        }
                                        drawCircle(color = color, center = offset, radius = 6f)
                                    }

                                    // DRAW CELLsite TOWERS IF IN HYBRID / SIGNAL VIEW
                                    if (mapType == "Hybrid") {
                                        for (tower in viewModel.simulatedTowers) {
                                            val offset = getScreenOffset(tower.latitude, tower.longitude)
                                            drawCircle(
                                                color = Color.Yellow.copy(alpha = 0.15f),
                                                center = offset,
                                                radius = 18f * mapZoom,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                                            )
                                        }
                                    }

                                    // DRAW ACTIVE GPS SIMULATION COORDINATE PIN
                                    val activeGpsOffset = getScreenOffset(simulatedLat, simulatedLon)
                                    
                                    // Accuracy boundary circle
                                    drawCircle(
                                        color = Color(0xFF2196F3).copy(alpha = 0.15f),
                                        center = activeGpsOffset,
                                        radius = simulatedAccuracy.coerceIn(10f, 150f) * (mapZoom * 0.25f)
                                    )
                                    // Pulsing ring
                                    drawCircle(
                                        color = Color(0xFF2196F3).copy(alpha = 0.4f),
                                        center = activeGpsOffset,
                                        radius = 10f,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                    )
                                    // Solid center dot
                                    drawCircle(
                                        color = Color(0xFF1976D2),
                                        center = activeGpsOffset,
                                        radius = 4f
                                    )
                                }

                                // Interactive Screen Crosshair targeting
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Aim reticle
                                    Divider(modifier = Modifier.width(20.dp), color = Color.Red.copy(alpha = 0.5f), thickness = 1.dp)
                                    Divider(modifier = Modifier.height(20.dp).width(1.dp), color = Color.Red.copy(alpha = 0.5f))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .border(1.dp, Color.Red.copy(alpha = 0.6f), CircleShape)
                                    )
                                }

                                // Map Controls Overlays (Panning buttons for perfect emulator accessibility)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(onClick = { mapZoom = (mapZoom + 0.3f).coerceAtMost(6.0f) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.ZoomIn, "Zoom In", tint = Color.White)
                                        }
                                        IconButton(onClick = { mapZoom = (mapZoom - 0.3f).coerceAtLeast(0.5f) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.ZoomOut, "Zoom Out", tint = Color.White)
                                        }
                                        IconButton(
                                            onClick = {
                                                // Retarget center to aiming crosshair position and teleport GPS
                                                val density = 3f
                                                val la = mapCenterY
                                                val lo = mapCenterX
                                                viewModel.setSimulatedCoordinates(la, lo, "Crosshair Location")
                                                notificationMessage = "Teleported simulated GPS to map center!"
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, "Teleport Here", tint = Color.Green)
                                        }
                                        IconButton(onClick = { viewModel.resetToDefaultLocation(); centerOnActiveGPS = true }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Refresh, "Reset Map", tint = Color.White)
                                        }
                                    }
                                }

                                // Map Type selector Overlay (Standard, Sat, Ter, Hyb)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Sidebar Toggle Button
                                        IconButton(
                                            onClick = { isSidebarExpanded = !isSidebarExpanded },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSidebarExpanded) Icons.Default.MenuOpen else Icons.Default.Menu,
                                                contentDescription = "Toggle Sidebar",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(2.dp))

                                        val types = listOf("Std", "Sat", "Ter", "Hyb")
                                        types.forEach { t ->
                                            val typeLabel = when(t) {
                                                "Sat" -> "Satellite"
                                                "Ter" -> "Terrain"
                                                "Hyb" -> "Hybrid"
                                                else -> "Standard"
                                            }
                                            val active = mapType == typeLabel
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable { mapType = typeLabel }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(t, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // GPS Simulation Warning Overlay Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                        .background(Color(0xFFE65100).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.Yellow, CircleShape)
                                        )
                                        Text("Spoofing Mode Active", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Simulated Compass Overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                        .size(42.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Compass Needle",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(simulatedHeading)
                                    )
                                    Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp), color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Bottom Map Actions Panel
                            Card(
                                shape = RoundedCornerShape(0.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Search Bar
                                    OutlinedTextField(
                                        value = searchVal,
                                        onValueChange = { searchVal = it },
                                        placeholder = { Text("Search states, cities or postal indices", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                                        trailingIcon = {
                                            if (searchVal.isNotEmpty()) {
                                                IconButton(onClick = { searchVal = "" }) {
                                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        singleLine = true
                                    )

                                    // Quick search trigger
                                    LaunchedEffect(searchVal) {
                                        if (searchVal.trim().isNotEmpty()) {
                                            viewModel.searchAndChangeLocation(searchVal)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "$simulatedCityName, $simulatedStateName",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Coordinates: Lat ${String.format("%.5f", simulatedLat)}, Lon ${String.format("%.5f", simulatedLon)}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Auto Lock toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("Lock Map", fontSize = 10.sp)
                                            Switch(
                                                checked = centerOnActiveGPS,
                                                onCheckedChange = { centerOnActiveGPS = it },
                                                modifier = Modifier.scale(0.7f)
                                            )
                                        }
                                    }

                                    // Display Live GPS telemetry data
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Accuracy", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format("%.1f", simulatedAccuracy)}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Altitude", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format("%.1f", simulatedAltitude)}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Speed", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format("%.1f", simulatedSpeed)} km/h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Heading", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${simulatedHeading.toInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                    1 -> {
                        // ==========================================
                        // TAB 2: ADVANCED CONTROLS & DRIVE SIMULATOR
                        // ==========================================
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section: Coordinate Spoofer
                            item {
                                Text("Manual GPS Lat/Lon Spoofer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = latInput,
                                        onValueChange = { latInput = it; manualInputError = null },
                                        label = { Text("Latitude (e.g. 9.076)") },
                                        modifier = Modifier.weight(1f),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = lonInput,
                                        onValueChange = { lonInput = it; manualInputError = null },
                                        label = { Text("Longitude (e.g. 7.398)") },
                                        modifier = Modifier.weight(1f),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        singleLine = true
                                    )
                                }
                                if (manualInputError != null) {
                                    Text(manualInputError ?: "", color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val latVal = latInput.toDoubleOrNull()
                                            val lonVal = lonInput.toDoubleOrNull()
                                            if (latVal != null && lonVal != null) {
                                                if (latVal in -90.0..90.0 && lonVal in -180.0..180.0) {
                                                    viewModel.setSimulatedCoordinates(latVal, lonVal, "Custom Marker Pin")
                                                    notificationMessage = "Updated coordinates to: ($latVal, $lonVal)"
                                                    latInput = ""
                                                    lonInput = ""
                                                } else {
                                                    manualInputError = "Error: Lat bounds -90..90, Lon -180..180"
                                                }
                                            } else {
                                                manualInputError = "Error: Enter valid floating points"
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text("Set Custom Pin", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { showFavDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save Favorite", fontSize = 11.sp)
                                    }
                                }
                            }

                            // Section: Live Drive Mode Simulator
                            item {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Drive Mode Simulation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Simulates vehicle travel based on heading and speed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = isDriveModeActive,
                                        onCheckedChange = { isDriveModeActive = it }
                                    )
                                }
                            }

                            // Section: Telemetry Sliders
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Speed slider
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Movement Speed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Text("${simulatedSpeed.toInt()} km/h", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = simulatedSpeed,
                                                onValueChange = { viewModel.setSimulatedSpeed(it) },
                                                valueRange = 0f..180f,
                                                steps = 18
                                            )
                                        }

                                        // Heading Slider
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Compass Direction / Heading", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
                                                val dirStr = directions[((simulatedHeading + 22.5f) % 360 / 45).toInt()]
                                                Text("${simulatedHeading.toInt()}° ($dirStr)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = simulatedHeading,
                                                onValueChange = { viewModel.setSimulatedHeading(it) },
                                                valueRange = 0f..359f
                                            )
                                        }

                                        // Altitude Slider
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Atmospheric Altitude", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Text("${simulatedAltitude.toInt()} m", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = simulatedAltitude.toFloat(),
                                                onValueChange = { viewModel.setSimulatedAltitude(it.toDouble()) },
                                                valueRange = 0f..10000f
                                            )
                                        }

                                        // Accuracy Slider
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("GPS Signal Accuracy Limit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Text("${String.format("%.1f", simulatedAccuracy)} m", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = simulatedAccuracy,
                                                onValueChange = { viewModel.setSimulatedAccuracy(it) },
                                                valueRange = 1f..150f
                                            )
                                        }
                                    }
                                }
                            }

                            // Section: Saved Locations (Favorites) with Import / Export features
                            item {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Saved Locations Directory", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    TextButton(onClick = { showImportExport = !showImportExport }) {
                                        Text(if (showImportExport) "Hide Data Controls" else "Import / Export", fontSize = 11.sp)
                                    }
                                }
                            }

                            if (showImportExport) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Import Saved Coordinates (CSV Format)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Format: Name, Latitude, Longitude (One per line)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            
                                            OutlinedTextField(
                                                value = importCsvInput,
                                                onValueChange = { importCsvInput = it },
                                                placeholder = { Text("Lekki Beach,6.42,3.53\nSukur Site,9.20,12.49", fontSize = 11.sp) },
                                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        if (importCsvInput.trim().isNotEmpty()) {
                                                            var count = 0
                                                            importCsvInput.lines().forEach { line ->
                                                                val parts = line.split(",")
                                                                if (parts.size >= 3) {
                                                                    val name = parts[0].trim()
                                                                    val la = parts[1].trim().toDoubleOrNull()
                                                                    val lo = parts[2].trim().toDoubleOrNull()
                                                                    if (name.isNotEmpty() && la != null && lo != null) {
                                                                        viewModel.saveLocationToFavorites(name, la, lo)
                                                                        count++
                                                                    }
                                                                }
                                                            }
                                                            notificationMessage = "Successfully imported $count locations!"
                                                            importCsvInput = ""
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Import", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        // Generate CSV from favorites
                                                        val csv = favorites.joinToString("\n") { "${it.name},${it.latitude},${it.longitude}" }
                                                        importCsvInput = csv
                                                        notificationMessage = "Exported favorites to text box below!"
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Export CSV", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (favorites.isEmpty()) {
                                item {
                                    Text("No favorite locations saved. Tap 'Save Favorite' above to bookmark.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                                }
                            } else {
                                items(favorites) { fav ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSimulatedCoordinates(fav.latitude, fav.longitude, fav.name)
                                                    centerOnActiveGPS = true
                                                    notificationMessage = "Spoofed to: ${fav.name}"
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                Column {
                                                    Text(fav.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Lat: ${String.format("%.4f", fav.latitude)}, Lon: ${String.format("%.4f", fav.longitude)}", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                            IconButton(onClick = { viewModel.removeLocationFromFavorites(fav) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ==========================================
                        // TAB 3: NIGERIA EXPLORER (STATE DIRECTORY)
                        // ==========================================
                        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            // Geopolitical Region filter buttons
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                val zones = listOf("All", "North Central", "North East", "North West", "South East", "South South", "South West")
                                items(zones) { zone ->
                                    val sel = selectedRegionFilter == zone
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedRegionFilter = zone }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(zone, fontSize = 11.sp, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Filtered State directory list
                            val filteredStates = viewModel.nigerianStatesList.filter { state ->
                                (selectedRegionFilter == "All" || state.region == selectedRegionFilter)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredStates) { state ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(state.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Capital: ${state.capital}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                // Region Badge tag
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.secondaryContainer,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(state.region, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Airport: ${state.airport}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Attraction: ${state.touristAttraction}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Local Government Areas: ${state.lgaCount} LGAs", fontSize = 10.sp, color = Color.Gray)

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.changeSimulatedLocation(state)
                                                        centerOnActiveGPS = true
                                                        selectedTab = 0 // jump to map
                                                        notificationMessage = "Teleported simulated location to: ${state.capital} (${state.name} State)"
                                                    },
                                                    modifier = Modifier.weight(1.2f)
                                                ) {
                                                    Text("Teleport Capital", fontSize = 10.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        // Find matching landmarks for this state
                                                        val stateLms = landmarks.filter { it.state == state.name }
                                                        if (stateLms.isNotEmpty()) {
                                                            val selectedLm = stateLms.first()
                                                            viewModel.setSimulatedCoordinates(selectedLm.lat, selectedLm.lon, selectedLm.name)
                                                            centerOnActiveGPS = true
                                                            selectedTab = 0 // jump to map
                                                            notificationMessage = "Teleported to Landmark: ${selectedLm.name}"
                                                        } else {
                                                            // Teleport to custom tourist attraction coordinate approximation
                                                            viewModel.setSimulatedCoordinates(state.latitude + 0.05, state.longitude - 0.05, state.touristAttraction)
                                                            centerOnActiveGPS = true
                                                            selectedTab = 0 // jump to map
                                                            notificationMessage = "Teleported to Attraction: ${state.touristAttraction}"
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Explore Landmark", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // ==========================================
                        // TAB 4: MOBILE COVERAGE MAP DIAGNOSTICS
                        // ==========================================
                        var selectedOperator by remember { mutableStateOf("MTN Nigeria") }
                        val operators = listOf("MTN Nigeria", "Airtel Nigeria", "Glo", "9mobile")
                        
                        var isTestingPerformance by remember { mutableStateOf(false) }
                        var downloadResult by remember { mutableFloatStateOf(0f) }
                        var uploadResult by remember { mutableFloatStateOf(0f) }
                        var latencyResult by remember { mutableIntStateOf(0) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Nigeria Network Site Coverage Analyzer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            // Operator filter pills
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(operators) { op ->
                                    val sel = selectedOperator == op
                                    val pillColor = when(op) {
                                        "MTN Nigeria" -> if (sel) Color(0xFFFFD54F) else Color(0xFFFFF9C4)
                                        "Airtel Nigeria" -> if (sel) Color(0xFFEF5350) else Color(0xFFFFCDD2)
                                        "Glo" -> if (sel) Color(0xFF66BB6A) else Color(0xFFC8E6C9)
                                        else -> if (sel) Color(0xFF29B6F6) else Color(0xFFB3E5FC)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(pillColor, RoundedCornerShape(12.dp))
                                            .border(1.2.dp, if (sel) Color.Black else Color.Transparent, RoundedCornerShape(12.dp))
                                            .clickable { selectedOperator = op }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(op, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Calculate proximity to nearest simulated tower of the active operator
                            val opTowers = viewModel.simulatedTowers.filter { it.operator == selectedOperator }
                            var closestTower by remember { mutableStateOf<SimulatedTower?>(null) }
                            var distanceToTower by remember { mutableDoubleStateOf(999.9) }

                            LaunchedEffect(selectedOperator, simulatedLat, simulatedLon) {
                                if (opTowers.isNotEmpty()) {
                                    var closest: SimulatedTower? = null
                                    var minDist = 99999.9
                                    for (t in opTowers) {
                                        val dist = Math.sqrt(Math.pow(t.latitude - simulatedLat, 2.0) + Math.pow(t.longitude - simulatedLon, 2.0))
                                        if (dist < minDist) {
                                            minDist = dist
                                            closest = t
                                        }
                                    }
                                    closestTower = closest
                                    // 1 degree difference is roughly 111 km
                                    distanceToTower = minDist * 111.0
                                }
                            }

                            // Signal diagnostics card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Live Cell Signal Diagnostics", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    
                                    val dbm = when {
                                        distanceToTower < 3.0 -> -64 - (distanceToTower * 3).toInt()
                                        distanceToTower < 10.0 -> -75 - (distanceToTower * 2).toInt()
                                        distanceToTower < 30.0 -> -95 - (distanceToTower * 1.1).toInt()
                                        else -> -115 - (distanceToTower * 0.2).toInt()
                                    }.coerceIn(-140, -50)

                                    val quality = when {
                                        dbm > -75 -> "Excellent (5G Ultra-Wideband)"
                                        dbm > -90 -> "Good (4G LTE Advanced)"
                                        dbm > -105 -> "Fair (3G HSPA+ Mode)"
                                        dbm > -118 -> "Poor (2G GSM Edge)"
                                        else -> "No Service / Emergency Calls Only"
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Signal Strength Metric:", fontSize = 11.sp, color = Color.Gray)
                                        Text("$dbm dBm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (dbm > -95) Color(0xFF4CAF50) else Color.Red)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Network Protocol Mode:", fontSize = 11.sp, color = Color.Gray)
                                        Text(quality, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Nearest Tower Distance:", fontSize = 11.sp, color = Color.Gray)
                                        Text("${String.format("%.2f", distanceToTower)} km", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tower Node State:", fontSize = 11.sp, color = Color.Gray)
                                        Text(closestTower?.status ?: "Unknown", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (closestTower?.status == "Active") Color(0xFF4CAF50) else Color(0xFFFF9800))
                                    }
                                }
                            }

                            // Signal Speed Tester Simulator
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Operator Signal Performance Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    if (isTestingPerformance) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                        Text("Measuring cellular throughput speed...", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                    } else {
                                        if (downloadResult > 0f) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Download", fontSize = 9.sp, color = Color.Gray)
                                                    Text("${String.format("%.1f", downloadResult)} Mbps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Upload", fontSize = 9.sp, color = Color.Gray)
                                                    Text("${String.format("%.1f", uploadResult)} Mbps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Ping", fontSize = 9.sp, color = Color.Gray)
                                                    Text("$latencyResult ms", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        } else {
                                            Text("No test run conducted yet for current coordinates.", fontSize = 10.sp, color = Color.Gray)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                isTestingPerformance = true
                                                coroutineScope.launch {
                                                    delay(1800)
                                                    val factor = if (distanceToTower < 5.0) 1.0 else if (distanceToTower < 15.0) 0.5 else 0.15
                                                    downloadResult = (120f + (0..120).random()).toFloat() * factor.toFloat()
                                                    uploadResult = (25f + (0..25).random()).toFloat() * factor.toFloat()
                                                    latencyResult = (15 + (0..40).random()) + (distanceToTower * 2).toInt()
                                                    isTestingPerformance = false
                                                    notificationMessage = "Performance metrics mapped successfully!"
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Run Performance Throughput Test")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // ==========================================
                        // TAB 5: MOCK DEVELOPER APIS (JSON VIEWER)
                        // ==========================================
                        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            Text("Dandali Platform Location APIs Mock Engine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Simulated server payloads returned to system client applications:", fontSize = 10.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal sub tabs for API selection
                            TabRow(
                                selectedTabIndex = activeApiTab,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val apis = listOf("Geocode", "Places", "Weather", "Elev")
                                apis.forEachIndexed { i, a ->
                                    Tab(
                                        selected = activeApiTab == i,
                                        onClick = { activeApiTab = i },
                                        text = { Text(a, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dynamic Simulated JSON construction
                            val jsonCode = when(activeApiTab) {
                                0 -> """
{
  "status": "OK",
  "results": [
    {
      "formatted_address": "$simulatedCityName, $simulatedStateName, Nigeria",
      "address_components": [
        { "long_name": "$simulatedCityName", "types": ["locality"] },
        { "long_name": "$simulatedStateName", "types": ["administrative_area_level_1"] },
        { "long_name": "Nigeria", "types": ["country"] }
      ],
      "geometry": {
        "location": { "lat": $simulatedLat, "lng": $simulatedLon },
        "location_type": "ROOFTOP",
        "viewport": {
          "northeast": { "lat": ${simulatedLat + 0.01}, "lng": ${simulatedLon + 0.01} },
          "southwest": { "lat": ${simulatedLat - 0.01}, "lng": ${simulatedLon - 0.01} }
        }
      },
      "place_id": "ChIJ_spoofed_dandali_node_${(simulatedLat * 100).toInt()}"
    }
  ]
}
                                """.trimIndent()

                                1 -> """
{
  "html_attributions": [],
  "results": [
    {
      "name": "Local Landmark in $simulatedCityName",
      "geometry": { "location": { "lat": ${simulatedLat + 0.002}, "lng": ${simulatedLon + 0.003} } },
      "rating": 4.5,
      "user_ratings_total": 412,
      "types": ["tourist_attraction", "point_of_interest", "establishment"]
    },
    {
      "name": "Dandali Telecom Office $simulatedCityName",
      "geometry": { "location": { "lat": ${simulatedLat - 0.004}, "lng": ${simulatedLon - 0.001} } },
      "rating": 4.8,
      "user_ratings_total": 128,
      "types": ["bank", "finance", "establishment"]
    }
  ],
  "status": "OK"
}
                                """.trimIndent()

                                2 -> """
{
  "coord": { "lon": $simulatedLon, "lat": $simulatedLat },
  "weather": [
    { "id": 801, "main": "Clouds", "description": "few clouds", "icon": "02d" }
  ],
  "main": {
    "temp": 28.5,
    "feels_like": 31.0,
    "temp_min": 25.0,
    "temp_max": 32.4,
    "pressure": 1011,
    "humidity": 68
  },
  "wind": { "speed": ${String.format("%.1f", simulatedSpeed / 3.6)}, "deg": ${simulatedHeading.toInt()} },
  "name": "$simulatedCityName",
  "cod": 200
}
                                """.trimIndent()

                                else -> """
{
  "results": [
    {
      "elevation": $simulatedAltitude,
      "location": { "lat": $simulatedLat, "lng": $simulatedLon },
      "resolution": 9.543019
    }
  ],
  "status": "OK"
}
                                """.trimIndent()
                            }

                            // Raw JSON rendering box with Copy simulator
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = jsonCode,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF9CDCF0),
                                            fontSize = 10.sp
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            notificationMessage = "Mock JSON payload copied to simulated clipboard!"
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favoriting Dialog Panel
        if (showFavDialog) {
            AlertDialog(
                onDismissRequest = { showFavDialog = false },
                title = { Text("Save Current Location Coordinates") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Save the currently simulated coordinates as a custom favorite bookmark shortcut.", fontSize = 11.sp, color = Color.Gray)
                        Text("Coordinates: Lat ${String.format("%.4f", simulatedLat)}, Lon ${String.format("%.4f", simulatedLon)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = favNameInput,
                            onValueChange = { favNameInput = it },
                            label = { Text("Custom Location Name") },
                            placeholder = { Text("e.g. My Lagos Office") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (favNameInput.trim().isNotEmpty()) {
                            viewModel.saveLocationToFavorites(favNameInput.trim(), simulatedLat, simulatedLon)
                            favNameInput = ""
                            showFavDialog = false
                            notificationMessage = "Saved favorite location!"
                        }
                    }) { Text("Save Coordinate") }
                },
                dismissButton = {
                    TextButton(onClick = { showFavDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// ==========================================
// 15. NETWORK COVERAGE MAP APP (Nigeria Operators)
// ==========================================
@Composable
fun NetworkCoverageApp(viewModel: VisualPhoneViewModel) {
    val simulatedLat by viewModel.simulatedLatitude.collectAsState()
    val simulatedLon by viewModel.simulatedLongitude.collectAsState()

    var activeOperator by remember { mutableStateOf("MTN Nigeria") }
    val operators = listOf("MTN Nigeria", "Airtel Nigeria", "Glo", "9mobile")

    AppContainer("Nigeria Network Coverage", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Operator filter selector row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(operators) { op ->
                    val selected = op == activeOperator
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { activeOperator = op }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = op,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Visual map cell site canvas
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .background(Color(0xFF263238)) // dark tech look
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw cell tower coordinates
                    val towers = viewModel.simulatedTowers.filter { it.operator == activeOperator }
                    for (tower in towers) {
                        // Map coordinates relative scale
                        val x = ((tower.longitude - 2.5) / 12.0) * size.width
                        val y = (1.0 - ((tower.latitude - 4.0) / 10.0)) * size.height

                        // Draw Coverage Radius circle
                        drawCircle(
                            color = when (tower.operator) {
                                "MTN Nigeria" -> Color(0xFFFFD54F).copy(alpha = 0.2f)
                                "Airtel Nigeria" -> Color(0xFFEF5350).copy(alpha = 0.2f)
                                "Glo" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                                else -> Color(0xFF29B6F6).copy(alpha = 0.2f)
                            },
                            center = Offset(x.toFloat(), y.toFloat()),
                            radius = (tower.strength * 22f)
                        )

                        // Draw Tower Node dot
                        drawCircle(
                            color = when (tower.operator) {
                                "MTN Nigeria" -> Color(0xFFFFEB3B)
                                "Airtel Nigeria" -> Color(0xFFE53935)
                                "Glo" -> Color(0xFF43A047)
                                else -> Color(0xFF1E88E5)
                            },
                            center = Offset(x.toFloat(), y.toFloat()),
                            radius = 6f
                        )
                    }
                }

                Text(
                    text = "Operator Towers Simulated Map\nCircles represent wireless signal boundary limits",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                )
            }

            // Stats details of selected tower area
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("$activeOperator Network Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val statusHash = Math.abs(simulatedLat.hashCode() % 3)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cellular Signal Strength:", fontSize = 12.sp)
                            Text(if (statusHash == 0) "Excellent (5G)" else "Good (4G LTE)", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nearby Cell Sites Node:", fontSize = 12.sp)
                            Text("2 Sites Active", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Simulated Ping latency:", fontSize = 12.sp)
                            Text("18 ms", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSidebar(
    viewModel: VisualPhoneViewModel,
    latInput: String,
    onLatChange: (String) -> Unit,
    lonInput: String,
    onLonChange: (String) -> Unit,
    searchVal: String,
    onSearchChange: (String) -> Unit,
    favorites: List<LocationEntity>,
    onSaveFavorite: () -> Unit,
    onNotification: (String) -> Unit,
    onCloseSidebar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
        modifier = Modifier
            .fillMaxHeight()
            .width(175.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "GPS Control",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Location Control",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Collapse Sidebar",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SECTION 1: SEARCH LOCATION
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Search Nigeria",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = searchVal,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Search...", fontSize = 9.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }

                // SECTION 2: MANUAL LAT/LON GPS SPOOFER
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "GPS Manual Spoof",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = latInput,
                            onValueChange = onLatChange,
                            label = { Text("Lat (e.g. 9.076)", fontSize = 8.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedTextField(
                            value = lonInput,
                            onValueChange = onLonChange,
                            label = { Text("Lon (e.g. 7.398)", fontSize = 8.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val latVal = latInput.toDoubleOrNull()
                                val lonVal = lonInput.toDoubleOrNull()
                                if (latVal != null && lonVal != null) {
                                    if (latVal in -90.0..90.0 && lonVal in -180.0..180.0) {
                                        viewModel.setSimulatedCoordinates(latVal, lonVal, "Manual Coordinate Spoofer")
                                        onNotification("Teleported to: ($latVal, $lonVal)")
                                    } else {
                                        onNotification("Lat bounds -90..90, Lon -180..180")
                                    }
                                } else {
                                    onNotification("Please enter valid coordinates.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Change Phone Loc", fontSize = 9.sp)
                        }
                    }
                }

                // SECTION 3: FAVORITES & SAVED
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Favorites",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = onSaveFavorite,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    "Save Current Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (favorites.isEmpty()) {
                            Text(
                                "No favorites saved.",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                favorites.forEach { fav ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setSimulatedCoordinates(fav.latitude, fav.longitude, fav.name)
                                                onNotification("Teleported to: ${fav.name}")
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    fav.name,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "${String.format("%.3f", fav.latitude)}, ${String.format("%.3f", fav.longitude)}",
                                                    fontSize = 7.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.removeLocationFromFavorites(fav) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "Delete",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 16. PLAY STORE APP
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreApp(viewModel: VisualPhoneViewModel) {
    val playStoreSelectedApp by viewModel.playStoreSelectedApp.collectAsState()
    val playStoreQuery by viewModel.playStoreQuery.collectAsState()
    val downloadedApks = viewModel.downloadedApks
    val activeDownloads = viewModel.activeDownloads
    val playStoreReviews by viewModel.playStoreReviews.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(1) } // Default to "Apps" tab (index 1)
    var showProfileDialog by remember { mutableStateOf(false) }
    var isScanningPlayProtect by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }

    // State for local dynamic games and books installation
    var installedGamesAndBooks by remember { mutableStateOf(setOf<String>()) }
    var localActiveDownloads by remember { mutableStateOf(mapOf<String, Float>()) }

    // State for writing a review
    var reviewAuthor by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewContent by remember { mutableStateOf("") }
    var reviewSuccessMessage by remember { mutableStateOf(false) }

    // Playable Simulated Game/Book screens
    var activeSimulatedGame by remember { mutableStateOf<PlayStoreItem?>(null) }
    var activeSimulatedBook by remember { mutableStateOf<PlayStoreItem?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Seeds for Games and Books
    val gamesList = remember {
        listOf(
            PlayStoreItem(
                id = "game_runner",
                name = "Dandali Runner",
                packageName = "com.dandali.games.runner",
                category = "Games",
                subCategory = "Arcade",
                rating = 4.7,
                storageSize = "42 MB",
                developer = "Dandali Game Studio",
                version = "1.0.2",
                iconVector = Icons.Default.DirectionsRun,
                iconColor = Color(0xFFF44336)
            ),
            PlayStoreItem(
                id = "game_quiz",
                name = "Math Challenge",
                packageName = "com.dandali.games.math",
                category = "Games",
                subCategory = "Educational",
                rating = 4.5,
                storageSize = "8 MB",
                developer = "EduLearn LLC",
                version = "1.1.0",
                iconVector = Icons.Default.Calculate,
                iconColor = Color(0xFF4CAF50)
            ),
            PlayStoreItem(
                id = "game_sudoku",
                name = "Sudoku Classic",
                packageName = "com.dandali.games.sudoku",
                category = "Games",
                subCategory = "Puzzle",
                rating = 4.6,
                storageSize = "12 MB",
                developer = "Brainy Apps",
                version = "2.1.0",
                iconVector = Icons.Default.Casino,
                iconColor = Color(0xFF9C27B0)
            ),
            PlayStoreItem(
                id = "game_space",
                name = "Space Invaders AI",
                packageName = "com.dandali.games.space",
                category = "Games",
                subCategory = "Action",
                rating = 4.8,
                storageSize = "38 MB",
                developer = "Retro Devs",
                version = "1.5.1",
                iconVector = Icons.Default.Extension,
                iconColor = Color(0xFF2196F3)
            ),
            PlayStoreItem(
                id = "game_chess",
                name = "Chess Master",
                packageName = "com.dandali.games.chess",
                category = "Games",
                subCategory = "Strategy",
                rating = 4.9,
                storageSize = "28 MB",
                developer = "Grandmaster Club",
                version = "3.0.0",
                iconVector = Icons.Default.Extension,
                iconColor = Color(0xFF607D8B)
            ),
            PlayStoreItem(
                id = "game_words",
                name = "Word Connect",
                packageName = "com.dandali.games.words",
                category = "Games",
                subCategory = "Word",
                rating = 4.4,
                storageSize = "15 MB",
                developer = "Wordy Games",
                version = "1.0.0",
                iconVector = Icons.Default.Extension,
                iconColor = Color(0xFFFF9800)
            )
        )
    }

    val booksList = remember {
        listOf(
            PlayStoreItem(
                id = "book_alchemist",
                name = "Dandali Alchemist",
                packageName = "com.dandali.books.alchemist",
                category = "Books",
                subCategory = "Fiction",
                rating = 4.9,
                storageSize = "14 MB",
                developer = "Dandali Publishing",
                version = "1.0.0",
                iconVector = Icons.Default.Book,
                iconColor = Color(0xFF795548)
            ),
            PlayStoreItem(
                id = "book_kotlin",
                name = "Modern Kotlin Guide",
                packageName = "com.dandali.books.kotlin",
                category = "Books",
                subCategory = "Technology",
                rating = 4.8,
                storageSize = "22 MB",
                developer = "JetBrains Press",
                version = "2026.1",
                iconVector = Icons.Default.MenuBook,
                iconColor = Color(0xFF673AB7)
            ),
            PlayStoreItem(
                id = "book_compose",
                name = "Jetpack Compose Pro",
                packageName = "com.dandali.books.compose",
                category = "Books",
                subCategory = "Programming",
                rating = 4.9,
                storageSize = "18 MB",
                developer = "Google Press",
                version = "3.2.0",
                iconVector = Icons.Default.Book,
                iconColor = Color(0xFF03A9F4)
            ),
            PlayStoreItem(
                id = "book_history",
                name = "Nigerian Chronicles",
                packageName = "com.dandali.books.nigeria",
                category = "Books",
                subCategory = "History",
                rating = 4.7,
                storageSize = "30 MB",
                developer = "Heritage Publishers",
                version = "1.2.0",
                iconVector = Icons.Default.MenuBook,
                iconColor = Color(0xFF009688)
            ),
            PlayStoreItem(
                id = "book_ai",
                name = "The AI Age",
                packageName = "com.dandali.books.ai",
                category = "Books",
                subCategory = "Science",
                rating = 4.6,
                storageSize = "25 MB",
                developer = "Future Tech",
                version = "1.0.5",
                iconVector = Icons.Default.Book,
                iconColor = Color(0xFFE91E63)
            ),
            PlayStoreItem(
                id = "book_scifi",
                name = "Sector 36 Chronicles",
                packageName = "com.dandali.books.scifi",
                category = "Books",
                subCategory = "Sci-Fi",
                rating = 4.5,
                storageSize = "16 MB",
                developer = "Galaxy Ink",
                version = "2.0.1",
                iconVector = Icons.Default.MenuBook,
                iconColor = Color(0xFF3F51B5)
            )
        )
    }

    // Dynamic state for reviews of games and books
    val initialLocalReviews = remember {
        mapOf(
            "game_runner" to listOf(
                PlayStoreReview(author = "Ade", rating = 5, date = "2026-07-01", content = "This game is highly addictive! Best runner on this store."),
                PlayStoreReview(author = "Chidi", rating = 4, date = "2026-06-28", content = "Runs smoothly on my simulated phone. Fun mechanics!")
            ),
            "game_quiz" to listOf(
                PlayStoreReview(author = "Fatima", rating = 5, date = "2026-07-02", content = "Very fun educational quiz! Great mathematical drills."),
                PlayStoreReview(author = "Tunde", rating = 4, date = "2026-06-30", content = "Simulated equations are accurate and testing is awesome!")
            ),
            "book_alchemist" to listOf(
                PlayStoreReview(author = "Ibrahim", rating = 5, date = "2026-07-03", content = "An amazing fictional story. Kept me hooked till the end!"),
                PlayStoreReview(author = "Ngozi", rating = 5, date = "2026-07-01", content = "Beautifully written. Highly recommend to literature lovers.")
            )
        )
    }
    var localReviews by remember { mutableStateOf(initialLocalReviews) }

    // Map system apps to PlayStoreItems
    val appsListMapped = remember(viewModel.playStoreApps) {
        viewModel.playStoreApps.map { app ->
            PlayStoreItem(
                id = "app_${app.id.name.lowercase()}",
                name = app.id.appName,
                packageName = app.packageName,
                category = "Apps",
                subCategory = app.category.name.lowercase().replaceFirstChar { it.uppercase() },
                rating = 4.8,
                storageSize = app.storageSize,
                developer = app.developer,
                version = app.version,
                iconVector = when (app.id) {
                    PhoneApp.DIALER -> Icons.Default.Call
                    PhoneApp.MESSAGES -> Icons.Default.ChatBubble
                    PhoneApp.CONTACTS -> Icons.Default.People
                    PhoneApp.CAMERA -> Icons.Default.CameraAlt
                    PhoneApp.WEATHER -> Icons.Default.WbSunny
                    PhoneApp.MAPS -> Icons.Default.MyLocation
                    PhoneApp.SETTINGS -> Icons.Default.Settings
                    PhoneApp.NOTES -> Icons.Default.StickyNote2
                    PhoneApp.MUSIC -> Icons.Default.MusicNote
                    else -> Icons.Default.Apps
                },
                iconColor = when (app.id) {
                    PhoneApp.DIALER -> Color(0xFF4CAF50)
                    PhoneApp.MESSAGES -> Color(0xFF2196F3)
                    PhoneApp.CONTACTS -> Color(0xFFFF9800)
                    PhoneApp.CAMERA -> Color(0xFF9E9E9E)
                    PhoneApp.WEATHER -> Color(0xFF00BCD4)
                    PhoneApp.MAPS -> Color(0xFF009688)
                    PhoneApp.SETTINGS -> Color(0xFF607D8B)
                    PhoneApp.NOTES -> Color(0xFFFFC107)
                    PhoneApp.MUSIC -> Color(0xFFE91E63)
                    else -> Color(0xFF795548)
                },
                appReference = app.id
            )
        }
    }

    // Combine all items for easy universal search and filtering
    val allItems = remember(appsListMapped, gamesList, booksList) {
        gamesList + appsListMapped + booksList
    }

    // Currently selected item (for details view)
    var selectedItem by remember { mutableStateOf<PlayStoreItem?>(null) }

    // Synchronize playStoreSelectedApp state with selectedItem state
    LaunchedEffect(playStoreSelectedApp) {
        if (playStoreSelectedApp == null) {
            if (selectedItem?.appReference != null) {
                selectedItem = null
            }
        } else {
            val app = playStoreSelectedApp!!
            selectedItem = appsListMapped.find { it.appReference == app.id }
        }
    }

    AppContainer("Google Play Store", viewModel) {
        if (activeSimulatedGame != null) {
            SimulatedGameScreen(game = activeSimulatedGame!!, onBack = { activeSimulatedGame = null })
        } else if (activeSimulatedBook != null) {
            SimulatedBookScreen(book = activeSimulatedBook!!, onBack = { activeSimulatedBook = null })
        } else if (selectedItem == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar with Google Profile Icon
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                if (playStoreQuery.isEmpty()) {
                                    Text(
                                        text = "Search games, apps, books",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = playStoreQuery,
                                    onValueChange = { viewModel.setPlayStoreQuery(it) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (playStoreQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setPlayStoreQuery("") }) {
                                    Icon(Icons.Default.Close, "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = {
                                viewModel.setPlayStoreQuery("Maps")
                            }) {
                                Icon(Icons.Default.Mic, "Voice search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // Profile Avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { showProfileDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Play Store Category Tabs: Games, Apps, Books
                val tabs = listOf("Games", "Apps", "Books")
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 12.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Filter items based on active tab category and search query
                val currentCategoryName = tabs[selectedTabIndex]
                val filteredItems = allItems.filter { item ->
                    val matchesQuery = item.name.contains(playStoreQuery, ignoreCase = true) ||
                            item.packageName.contains(playStoreQuery, ignoreCase = true) ||
                            item.subCategory.contains(playStoreQuery, ignoreCase = true)
                    
                    val matchesCategory = if (playStoreQuery.isEmpty()) {
                        item.category == currentCategoryName
                    } else {
                        item.category == currentCategoryName || allItems.none { it.category == currentCategoryName && it.name.contains(playStoreQuery, ignoreCase = true) }
                    }
                    matchesQuery && matchesCategory
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (playStoreQuery.isEmpty()) {
                        item {
                            val heroItem = when (currentCategoryName) {
                                "Games" -> gamesList.first()
                                "Books" -> booksList.first()
                                else -> appsListMapped.find { it.appReference == PhoneApp.MAPS } ?: appsListMapped.first()
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "RECOMMENDED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        heroItem.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        when (heroItem.id) {
                                            "game_runner" -> "Dandali tap-and-jump endless game mechanics. Extremely fun physics engine!"
                                            "book_alchemist" -> "Engaging Nigerian fictional chronicles set in Northern valleys. Multi-chapter fully readable story."
                                            else -> "Interactive maps featuring cellular network signal strengths and state geography."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { selectedItem = heroItem },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Learn More", fontSize = 12.sp)
                                        }
                                        Text(
                                            "${heroItem.rating} ★ • ${heroItem.subCategory}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (playStoreQuery.isNotEmpty()) "Search Results" else "Top Free $currentCategoryName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Safe 3-column Grid view of items
                    if (filteredItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No items found matching \"$playStoreQuery\"",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val chunkedItems = filteredItems.chunked(3)
                        items(chunkedItems) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    val isInstalled = if (item.appReference != null) {
                                        downloadedApks.any { it.id == item.appReference }
                                    } else {
                                        installedGamesAndBooks.contains(item.id)
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedItem = item }
                                                .padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(item.iconColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = item.iconVector,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )

                                            Text(
                                                text = "${item.rating} ★ • ${item.storageSize}",
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (isInstalled) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Installed",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // APP / GAME / BOOK DETAIL SCREEN
            val item = selectedItem!!
            val isInstalled = if (item.appReference != null) {
                downloadedApks.any { it.id == item.appReference }
            } else {
                installedGamesAndBooks.contains(item.id)
            }

            val activeTask = if (item.appReference != null) {
                activeDownloads.find { it.appName == item.appReference.appName }
            } else {
                null
            }

            val isLocalDownloading = localActiveDownloads.containsKey(item.id)
            val localProgress = localActiveDownloads[item.id] ?: 0f

            val reviews = if (item.appReference != null) {
                playStoreReviews[item.appReference] ?: emptyList()
            } else {
                localReviews[item.id] ?: emptyList()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (item.appReference != null) {
                                viewModel.selectPlayStoreApp(null)
                            } else {
                                selectedItem = null
                            }
                            reviewSuccessMessage = false
                            reviewAuthor = ""
                            reviewContent = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                        Text("App Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Box(modifier = Modifier.size(24.dp))
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(item.iconColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.iconVector, null, tint = Color.White, modifier = Modifier.size(38.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text(item.packageName, color = Color.Gray, fontSize = 12.sp)
                            Text("${item.developer} • Verified Publisher", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                item {
                    when {
                        activeTask != null -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = activeTask.progress,
                                    modifier = Modifier.fillMaxWidth().clip(CircleShape)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Simulating Install...", fontSize = 11.sp, color = Color.Gray)
                                    Text("${Math.round(activeTask.progress * 100)}% (${item.storageSize})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        isLocalDownloading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = localProgress,
                                    modifier = Modifier.fillMaxWidth().clip(CircleShape)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Simulating Install...", fontSize = 11.sp, color = Color.Gray)
                                    Text("${Math.round(localProgress * 100)}% (${item.storageSize})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        isInstalled -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (item.appReference != null) {
                                            viewModel.uninstallApp(item.appReference)
                                        } else {
                                            installedGamesAndBooks = installedGamesAndBooks - item.id
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Uninstall")
                                }
                                Button(
                                    onClick = {
                                        if (item.appReference != null) {
                                            viewModel.selectPlayStoreApp(null)
                                            viewModel.launchApp(item.appReference)
                                        } else {
                                            if (item.category == "Games") {
                                                activeSimulatedGame = item
                                            } else if (item.category == "Books") {
                                                activeSimulatedBook = item
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (item.appReference != null) "Open App" else if (item.category == "Games") "Play Game" else "Read Book")
                                }
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    if (item.appReference != null) {
                                        val realApp = viewModel.playStoreApps.find { it.id == item.appReference }
                                        if (realApp != null) {
                                            viewModel.triggerAppInstall(realApp)
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            localActiveDownloads = localActiveDownloads + (item.id to 0f)
                                            var prog = 0f
                                            while (prog < 1.0f) {
                                                delay(150)
                                                prog += 0.12f
                                                localActiveDownloads = localActiveDownloads + (item.id to prog.coerceAtMost(1.0f))
                                            }
                                            localActiveDownloads = localActiveDownloads - item.id
                                            installedGamesAndBooks = installedGamesAndBooks + item.id
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simulate Install (Get)")
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rating", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${item.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Size", fontSize = 11.sp, color = Color.Gray)
                                Text(item.storageSize, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Downloads", fontSize = 11.sp, color = Color.Gray)
                                Text("1M+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.3f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Version", fontSize = 11.sp, color = Color.Gray)
                                Text(item.version, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                item {
                    Text("Interactive Previews", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val previews = listOf(
                            "Primary Interface Dashboard",
                            "Real-Time Local State Logs",
                            "Advanced User Settings Panel",
                            "Responsive Performance Desk"
                        )
                        items(previews) { preview ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(4.dp)
                                                .background(Color.Gray.copy(alpha = 0.5f))
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            preview,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        )
                                        Text(
                                            "Simulated View",
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(18.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "The high-fidelity simulated '${item.name}' item utilizes Dandali operating rules. " +
                                "It integrates directly with the phone state subsystem, running fluidly within " +
                                "our custom visual mobile screen with Material Design 3 and complete localized states.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ratings and Reviews", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Rate this application", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    val isSelected = star <= reviewRating
                                    IconButton(
                                        onClick = { reviewRating = star },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star Stars",
                                            tint = if (isSelected) Color(0xFFFFC107) else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = reviewAuthor,
                                onValueChange = { reviewAuthor = it },
                                label = { Text("Your Name") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = reviewContent,
                                onValueChange = { reviewContent = it },
                                label = { Text("Write your review") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (reviewSuccessMessage) {
                                Text(
                                    "Your review has been successfully submitted!",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (reviewContent.isNotBlank()) {
                                        if (item.appReference != null) {
                                            viewModel.submitPlayStoreReview(item.appReference, reviewAuthor, reviewRating, reviewContent)
                                        } else {
                                            val current = localReviews[item.id] ?: emptyList()
                                            val newRev = PlayStoreReview(
                                                author = if (reviewAuthor.isBlank()) "Anonymous" else reviewAuthor,
                                                rating = reviewRating,
                                                date = "2026-07-07",
                                                content = reviewContent
                                            )
                                            localReviews = localReviews + (item.id to (listOf(newRev) + current))
                                        }
                                        reviewAuthor = ""
                                        reviewContent = ""
                                        reviewSuccessMessage = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Submit Review")
                            }
                        }
                    }
                }

                items(reviews) { review ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(review.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(review.date, fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            (1..5).forEach { index ->
                                Icon(
                                    imageVector = if (index <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index <= review.rating) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Text(review.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Google Account profile dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = {
                    showProfileDialog = false
                    isScanningPlayProtect = false
                    scanCompleted = false
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("Google Account", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Dandali User", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("dandali.user@gmail.com", color = Color.Gray, fontSize = 13.sp)
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Play Points", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Silver Level", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text("250 pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (scanCompleted) Icons.Default.CheckCircle else Icons.Default.Security,
                                        contentDescription = "Play Protect",
                                        tint = if (scanCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                    )
                                    Text("Google Play Protect", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                if (isScanningPlayProtect) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Text("Scanning apps for malware...", fontSize = 11.sp)
                                    }
                                } else if (scanCompleted) {
                                    Text("No harmful applications found. Scanned 22 visual packages successfully.", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    Text("Verify simulated integrity checks of downloaded files.", fontSize = 11.sp, color = Color.Gray)
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isScanningPlayProtect = true
                                                delay(2500)
                                                isScanningPlayProtect = false
                                                scanCompleted = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Scan Packages")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.triggerGoogleSignIn()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Account Relog")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showProfileDialog = false
                        isScanningPlayProtect = false
                        scanCompleted = false
                    }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

// ==========================================
// 17. APP MANAGER APP
// ==========================================
@Composable
fun AppManagerApp(viewModel: VisualPhoneViewModel) {
    val appsList = viewModel.appsList

    AppContainer("App Manager", viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Installed Packages", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(appsList) { app ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Column {
                                        Text(app.id.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(app.packageName, color = Color.Gray, fontSize = 11.sp)
                                    }
                                }

                                if (app.forceStopped) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Stopped", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Permissions: ${app.permissions.joinToString()}", fontSize = 10.sp, color = Color.Gray)

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (app.forceStopped) {
                                        TextButton(onClick = { viewModel.startStoppedApp(app.id) }) {
                                            Text("Enable", fontSize = 11.sp)
                                        }
                                    } else {
                                        TextButton(onClick = { viewModel.forceStopApp(app.id) }) {
                                            Text("Force Stop", fontSize = 11.sp, color = Color.Red)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.uninstallApp(app.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Uninstall", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 18. SETTINGS APP
// ==========================================
@Composable
fun SettingsApp(viewModel: VisualPhoneViewModel) {
    val passcode by viewModel.passcode.collectAsState()
    val isGestureNav by viewModel.isGestureNav.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var activeSubpage by remember { mutableStateOf<String?>(null) }

    AppContainer("Device Settings", viewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (activeSubpage == null) {
                // User info header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column {
                            Text("Dandali Device Owner", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("dandali.user@gmail.com", color = Color.DarkGray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Settings List Categories
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val settingsList = listOf(
                        Triple("Wi-Fi & Networks", Icons.Default.NetworkWifi, "Simulate cellular provider nodes"),
                        Triple("Display & Wallpaper", Icons.Default.DisplaySettings, "Adjust screen wallpaper gradient modes"),
                        Triple("Security & PIN Lock", Icons.Default.Security, "Configure default lockscreen PIN values"),
                        Triple("Developer Options", Icons.Default.DeveloperMode, "Verify live CPU telemetry logs")
                    )
                    items(settingsList) { setting ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeSubpage = setting.first },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(setting.second, setting.first, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(setting.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(setting.third, color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Settings details subpages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeSubpage = null }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                    Text(activeSubpage!!, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Divider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (activeSubpage) {
                        "Display & Wallpaper" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dark Mode Status")
                                Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode() })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Gesture Navigation Pill")
                                Switch(checked = isGestureNav, onCheckedChange = { viewModel.toggleNavigationStyle() })
                            }

                            Button(
                                onClick = { viewModel.changeWallpaper() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Switch Desktop Wallpaper")
                            }
                        }
                        "Security & PIN Lock" -> {
                            Text("Active PIN: $passcode", fontWeight = FontWeight.Bold)
                            Text("To lock your simulated Dandali phone, use power button on right.", color = Color.Gray, fontSize = 12.sp)

                            Button(onClick = { viewModel.lockDevice() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Lock Simulated Screen now")
                            }
                        }
                        "Wi-Fi & Networks" -> {
                            val wifiOn by viewModel.isWifiOn.collectAsState()
                            val mobileOn by viewModel.isMobileOn.collectAsState()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Wi-Fi")
                                Switch(checked = wifiOn, onCheckedChange = { viewModel.toggleWifi() })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Mobile Network")
                                Switch(checked = mobileOn, onCheckedChange = { viewModel.toggleMobileNetwork() })
                            }
                        }
                        "Developer Options" -> {
                            val cpuUsage by viewModel.cpuUsage.collectAsState()
                            val ramFree by viewModel.ramFree.collectAsState()

                            Text("Simulated Telemetry Metrics", fontWeight = FontWeight.Bold)
                            Text("CPU Usage Rate: $cpuUsage %", fontSize = 13.sp)
                            Text("RAM Free: ${String.format(Locale.US, "%.2f", ramFree)} GB / 8.00 GB", fontSize = 13.sp)

                            Divider()
                            Text("System Log Output Center", fontWeight = FontWeight.Bold)

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(8.dp)
                            ) {
                                items(viewModel.systemLogs) { log ->
                                    Text(
                                        text = log,
                                        color = Color(0xFF00FF00),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
