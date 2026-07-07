package com.example.ui.phone

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PhoneApp
import com.example.ui.viewmodel.PlayStoreReview
import kotlin.random.Random
import kotlinx.coroutines.delay

data class PlayStoreItem(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String, // "Games", "Apps", "Books"
    val subCategory: String,
    val rating: Double,
    val storageSize: String,
    val developer: String,
    val version: String,
    val iconVector: ImageVector,
    val iconColor: Color,
    val appReference: PhoneApp? = null
)

@Composable
fun SimulatedGameScreen(game: PlayStoreItem, onBack: () -> Unit) {
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Game Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back to Play Store", tint = Color.White)
            }
            Text(game.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Score: $score", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (!gameOver) {
            when (game.id) {
                "game_runner" -> {
                    var isJumping by remember { mutableStateOf(false) }
                    var obstacleX by remember { mutableStateOf(300f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(50)
                            obstacleX -= 15f
                            if (obstacleX < -50f) {
                                obstacleX = 350f
                                score += 10
                            }
                            if (obstacleX > 40f && obstacleX < 80f && !isJumping) {
                                gameOver = true
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Dandali Runner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Tap anywhere to Jump!", color = Color.Gray, fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFF1E1E1E))
                                .clickable {
                                    if (!isJumping) {
                                        isJumping = true
                                    }
                                }
                        ) {
                            // Ground
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.White)
                            )
                            
                            val playerY = if (isJumping) 40.dp else 110.dp
                            LaunchedEffect(isJumping) {
                                if (isJumping) {
                                    delay(450)
                                    isJumping = false
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .padding(start = 50.dp, top = playerY)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(game.iconColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsRun, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(x = obstacleX.dp, y = 110.dp)
                                    .size(36.dp)
                                    .background(Color.Red, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                            }
                        }
                    }
                }
                "game_quiz" -> {
                    var num1 by remember { mutableStateOf(Random.nextInt(2, 12)) }
                    var num2 by remember { mutableStateOf(Random.nextInt(2, 12)) }
                    var op by remember { mutableStateOf("+") }
                    val correctAnswer = remember(num1, num2, op) {
                        when (op) {
                            "+" -> num1 + num2
                            "-" -> num1 - num2
                            else -> num1 * num2
                        }
                    }
                    val options = remember(correctAnswer) {
                        val list = mutableListOf(correctAnswer)
                        while (list.size < 4) {
                            val wrong = correctAnswer + Random.nextInt(-10, 10)
                            if (wrong != correctAnswer && !list.contains(wrong)) {
                                list.add(wrong)
                            }
                        }
                        list.shuffled()
                    }

                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Math Challenge", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Solve:", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("$num1 $op $num2 = ?", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            options.chunked(2).forEach { rowList ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                ) {
                                    rowList.forEach { opt ->
                                        Button(
                                            onClick = {
                                                if (opt == correctAnswer) {
                                                    score += 1
                                                    num1 = Random.nextInt(2, 15)
                                                    num2 = Random.nextInt(2, 15)
                                                    op = listOf("+", "-", "*").random()
                                                } else {
                                                    gameOver = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("$opt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Click the button to score!", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(game.iconColor)
                                .clickable { score += 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("TAP!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Simulating ${game.name} gameplay", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔥 GAME OVER 🔥", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your Score: $score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        score = 0
                        gameOver = false
                    }
                ) {
                    Text("Restart Game")
                }
            }
        }

        Text("Playable Game Simulation", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
fun SimulatedBookScreen(book: PlayStoreItem, onBack: () -> Unit) {
    var fontSize by remember { mutableStateOf(14) }
    var sepiaTheme by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    
    val bookText = remember(book.id) {
        when (book.id) {
            "book_alchemist" -> listOf(
                "Chapter 1: Golden Hills of Dandali\n\nFar in the northern reaches of the ancient Dandali state, there lived a quiet scholar named Ibrahim. Ibrahim did not seek gold or silver; instead, he searched for the secrets of harmony between technology and the soil.",
                "Chapter 2: The Whispering Towers\n\nOne day, Ibrahim discovered a series of massive iron towers. They hummed with a strange energy, matching the heartbeat of the land. It was through these towers that the modern networks of connection were born, linking the 36 states of the nation."
            )
            "book_kotlin" -> listOf(
                "Chapter 1: The Magic of Functions\n\nKotlin is a modern, concise language that brings joy back to developers. With elegant structures like higher-order functions and null safety, Kotlin allows us to write expressive code with zero boilerplates.",
                "Chapter 2: Concurrency with Coroutines\n\nCoroutines simplify asynchronous programming by writing non-blocking code that looks synchronous. By using launch, async, and flow, you can orchestrate complex parallel tasks effortlessly."
            )
            "book_compose" -> listOf(
                "Chapter 1: Thinking in Compose\n\nJetpack Compose is a modern, declarative UI toolkit. Instead of modifying XML layouts, you define how your screen should look based on the current application state. When state changes, Compose recomposes the UI automatically.",
                "Chapter 2: Material 3 Elegance\n\nWith Material 3 design, your app gains dynamic color schemes, rich typography, and beautiful spatial layouts. It encourages generous negative space and accessible touch targets."
            )
            else -> listOf(
                "Chapter 1: An Amazing Journey\n\nWelcome to this beautiful simulated e-book. Here you will find stories of connection, code, and growth. Reading expands the horizons of the mind and inspires creation.",
                "Chapter 2: The Future Awaits\n\nAs the virtual sun sets on the simulated visual phone, we realize that technology is just a canvas. The true art is what you decide to create on it."
            )
        }
    }

    val themeBg = if (sepiaTheme) Color(0xFFF4ECD8) else Color(0xFF1E1E1E)
    val themeText = if (sepiaTheme) Color(0xFF5B4636) else Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back to Play Store", tint = themeText)
            }
            Text(book.name, color = themeText, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            IconButton(onClick = { sepiaTheme = !sepiaTheme }) {
                Icon(
                    imageVector = if (sepiaTheme) Icons.Default.DarkMode else Icons.Default.WbSunny,
                    contentDescription = "Toggle Theme",
                    tint = themeText
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = bookText.getOrElse(currentPage - 1) { "No page found." },
                color = themeText,
                fontSize = fontSize.sp,
                lineHeight = (fontSize + 6).sp,
                textAlign = TextAlign.Justify
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = if (sepiaTheme) Color(0xEED9CBAC) else Color(0xEE2A2A2A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { if (fontSize > 10) fontSize -= 2 }) {
                        Text("A-", color = themeText, fontWeight = FontWeight.Bold)
                    }
                    Text("${fontSize}sp", color = themeText, fontSize = 11.sp)
                    TextButton(onClick = { if (fontSize < 24) fontSize += 2 }) {
                        Text("A+", color = themeText, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = { if (currentPage > 1) currentPage -= 1 },
                        enabled = currentPage > 1
                    ) {
                        Text("Prev", color = if (currentPage > 1) themeText else Color.Gray)
                    }
                    Text("$currentPage / ${bookText.size}", color = themeText, fontSize = 12.sp)
                    TextButton(
                        onClick = { if (currentPage < bookText.size) currentPage += 1 },
                        enabled = currentPage < bookText.size
                    ) {
                        Text("Next", color = if (currentPage < bookText.size) themeText else Color.Gray)
                    }
                }
            }
        }
    }
}
