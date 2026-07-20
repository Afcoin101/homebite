package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

fun getMealImageModel(imageUrl: String, mealName: String = ""): Any {
    return when {
        imageUrl.contains("jollof", ignoreCase = true) || mealName.contains("jollof", ignoreCase = true) -> {
            com.example.R.drawable.img_jollof_rice_1782163924128
        }
        imageUrl.contains("egusi", ignoreCase = true) || mealName.contains("egusi", ignoreCase = true) -> {
            com.example.R.drawable.img_egusi_pounded_yam_1782163995182
        }
        imageUrl.contains("amala", ignoreCase = true) || mealName.contains("amala", ignoreCase = true) || 
        imageUrl.contains("abula", ignoreCase = true) || mealName.contains("abula", ignoreCase = true) -> {
            com.example.R.drawable.img_amala_abula_1782164562868
        }
        imageUrl.contains("puff puff", ignoreCase = true) || mealName.contains("puff puff", ignoreCase = true) -> {
            com.example.R.drawable.img_nigerian_puff_puff_1784429983181
        }
        imageUrl.contains("moi moi", ignoreCase = true) || mealName.contains("moi moi", ignoreCase = true) ||
        imageUrl.contains("moin moin", ignoreCase = true) || mealName.contains("moin moin", ignoreCase = true) -> {
            com.example.R.drawable.img_moi_moi_1784456040852
        }
        imageUrl.contains("chin chin", ignoreCase = true) || mealName.contains("chin chin", ignoreCase = true) ||
        imageUrl.contains("chinchin", ignoreCase = true) || mealName.contains("chinchin", ignoreCase = true) -> {
            com.example.R.drawable.img_chin_chin_snack_1784456350762
        }
        imageUrl.contains("ewa agoyin", ignoreCase = true) || mealName.contains("ewa agoyin", ignoreCase = true) ||
        imageUrl.contains("agoyin", ignoreCase = true) || mealName.contains("agoyin", ignoreCase = true) -> {
            com.example.R.drawable.ewa_agoyin_plate_1784456541832
        }
        else -> imageUrl
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: HomeChefViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val unreadCount = remember(alerts) { alerts.count { !it.isRead } }

    // Real-time floating toast notifications state management
    var activeToasts by remember { mutableStateOf<List<AlertEntity>>(emptyList()) }
    val seenAlertIds = remember { mutableStateListOf<Int>() }
    var isFirstAlertCollection by remember { mutableStateOf(true) }

    LaunchedEffect(alerts) {
        if (alerts.isNotEmpty()) {
            if (isFirstAlertCollection) {
                // Initialize seen alerts with history to prevent startup toast storm
                alerts.forEach { seenAlertIds.add(it.id) }
                isFirstAlertCollection = false
            } else {
                // Safely identify and queue only brand-new alerts triggered in this session
                val newAlerts = alerts.filter { it.id !in seenAlertIds }
                if (newAlerts.isNotEmpty()) {
                    newAlerts.forEach { alert ->
                        seenAlertIds.add(alert.id)
                        activeToasts = activeToasts + alert
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Citch",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    if (currentScreen is Screen.ChefDetail) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Explore) }) {
                            Icon(Icons.Default.Close, contentDescription = "Back to Explore")
                        }
                    } else if (currentScreen is Screen.GoLiveConfig) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Explore) }) {
                            Icon(Icons.Default.Close, contentDescription = "Back to Explore")
                        }
                    } else if (currentScreen is Screen.AICulinaryHub) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Explore) }) {
                            Icon(Icons.Default.Close, contentDescription = "Back to Explore")
                        }
                    } else {
                        IconButton(onClick = { viewModel.navigateTo(Screen.AICulinaryHub) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Culinary Lab", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.navigateTo(Screen.GoLiveConfig) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Go Live Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen is Screen.Explore,
                    onClick = { viewModel.navigateTo(Screen.Explore) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Explore") },
                    label = { Text("Kitchens") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Showcase,
                    onClick = { viewModel.navigateTo(Screen.Showcase) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Showcase") },
                    label = { Text("Showcase") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.MapSearch,
                    onClick = { viewModel.navigateTo(Screen.MapSearch) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Map Search") },
                    label = { Text("Map search") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Orders,
                    onClick = { viewModel.navigateTo(Screen.Orders) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Orders") },
                    label = { Text("My Orders") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Notifications,
                    onClick = { viewModel.navigateTo(Screen.Notifications) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    is Screen.Explore -> ExploreScreen(viewModel)
                    is Screen.Showcase -> ShowcaseScreen(viewModel)
                    is Screen.MapSearch -> MapSearchScreen(viewModel)
                    is Screen.Orders -> OrdersScreen(viewModel)
                    is Screen.Notifications -> NotificationsScreen(viewModel)
                    is Screen.GoLiveConfig -> GoLiveConfigScreen(viewModel)
                    is Screen.AICulinaryHub -> AICulinaryHubScreen(viewModel)
                    is Screen.ChefDetail -> ChefDetailScreen(screen.chefId, viewModel)
                }
            }

            // Real-time floating toast notifications hud overlaid above active screens
            if (activeToasts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    activeToasts.take(3).forEach { alert ->
                        key(alert.id) {
                            ToastNotificationItem(
                                alert = alert,
                                onDismiss = {
                                    activeToasts = activeToasts.filter { it.id != alert.id }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToastNotificationItem(
    alert: AlertEntity,
    onDismiss: () -> Unit
) {
    // Standard timeout to auto-dismiss: 6 seconds
    LaunchedEffect(alert.id) {
        delay(6000)
        onDismiss()
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        val (icon, tintColor) = remember(alert.title) {
            when {
                alert.title.contains("Paid", ignoreCase = true) || alert.title.contains("Securely", ignoreCase = true) -> {
                    Icons.AutoMirrored.Filled.ReceiptLong to Color(0xFF2E7D32) // Success Green
                }
                alert.title.contains("Preparing", ignoreCase = true) || alert.title.contains("Kitchen Preparing", ignoreCase = true) -> {
                    Icons.Default.RestaurantMenu to Color(0xFFE65100) // Warm Orange for active prep
                }
                alert.title.contains("Delivery", ignoreCase = true) || alert.title.contains("Out for Delivery", ignoreCase = true) -> {
                    Icons.Default.LocationOn to Color(0xFF1976D2) // Courier Blue
                }
                alert.title.contains("Arrived", ignoreCase = true) || alert.title.contains("Served", ignoreCase = true) || alert.title.contains("Delivered", ignoreCase = true) -> {
                    Icons.Default.Verified to Color(0xFF8E24AA) // Celebratory Purple/Gold
                }
                alert.title.contains("Kitchen Alert", ignoreCase = true) -> {
                    Icons.Default.Campaign to Color(0xFFC62828) // Promotion/Alert Red
                }
                else -> {
                    Icons.Default.Notifications to Color(0xFF00796B) // Default teal
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("toast_alert_${alert.id}"),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
            ),
            border = BorderStroke(1.dp, tintColor.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon with subtle circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(tintColor.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Alert description Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Dismiss Button
                IconButton(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("dismiss_toast_button_${alert.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

data class LeaderboardChefData(
    val chef: ChefEntity,
    val averageRating: Double,
    val reviewCount: Int,
    val orderVolume: Int,
    val score: Double
)

@Composable
fun ChefLeaderboardSection(
    leaderboardChefs: List<LeaderboardChefData>,
    onChefClick: (Int) -> Unit
) {
    var showFormulaDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_container_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆 Top Rated Chefs",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showFormulaDialog = true },
                        modifier = Modifier.size(24.dp).testTag("leaderboard_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Score Formula Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_leaderboard_expand")
                ) {
                    Text(
                        text = if (isExpanded) "Hide" else "Show Ranks",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Display Top 5
                val topChefs = leaderboardChefs.take(5)

                if (topChefs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No chef rankings available yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topChefs.forEachIndexed { index, leaderboardItem ->
                            val rank = index + 1
                            val rankColor = when (rank) {
                                1 -> Color(0xFFFBC02D) // Gold
                                2 -> Color(0xFFB0BEC5) // Silver
                                3 -> Color(0xFFD84315) // Bronze
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }

                            val rankIcon = when (rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "•"
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChefClick(leaderboardItem.chef.id) }
                                    .testTag("leaderboard_item_rank_$rank"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank Number / Badge
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = rankColor.copy(alpha = 0.15f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (rank <= 3) {
                                            Text(
                                                text = rankIcon,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Text(
                                                text = "#$rank",
                                                color = rankColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Chef image and name info
                                    AsyncImage(
                                        model = leaderboardItem.chef.avatarUrl,
                                        contentDescription = "Chef Avatar",
                                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = leaderboardItem.chef.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = leaderboardItem.chef.cuisineType,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Dynamic score and volume metrics
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${String.format("%.1f", leaderboardItem.score)} pts",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = String.format("%.1f", leaderboardItem.averageRating),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.ShoppingBag,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${leaderboardItem.orderVolume}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
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

    if (showFormulaDialog) {
        AlertDialog(
            onDismissRequest = { showFormulaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("How we rank our Chefs")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Our Chef Leaderboard is calculated dynamically in real-time based on actual user reviews and kitchen order volume:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Leaderboard Score =\n(Average Rating × 15) + (Order Volume × 5)",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "⭐ Average Rating (Weight: 15):\nRepresents the quality of cooking. Higher ratings from reviews give the biggest points boost.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "📦 Order Volume (Weight: 5):\nRepresents local popularity. Every order placed dynamically increases the chef's rank standing!",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFormulaDialog = false },
                    modifier = Modifier.testTag("dismiss_formula_dialog")
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

// EXPLORE MAIN SCREEN
@Composable
fun ExploreScreen(viewModel: HomeChefViewModel) {
    val chefs by viewModel.chefs.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val leaderboardChefs = remember(chefs, reviews, orders) {
        chefs.map { chef ->
            val chefReviews = reviews.filter { it.chefId == chef.id }
            val chefOrders = orders.filter { it.chefId == chef.id }
            
            val avgRating = if (chefReviews.isNotEmpty()) {
                chefReviews.map { it.rating }.average()
            } else {
                chef.rating.toDouble()
            }
            
            val orderVolume = chefOrders.size
            val score = (avgRating * 15) + (orderVolume * 5)
            
            LeaderboardChefData(
                chef = chef,
                averageRating = avgRating,
                reviewCount = chefReviews.size,
                orderVolume = orderVolume,
                score = score
            )
        }.sortedByDescending { it.score }
    }
    
    var showRegisterDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Mains", "Starters", "Desserts", "Drinks")

    val filteredChefs = remember(chefs, meals, searchQuery) {
        if (searchQuery.isEmpty()) chefs else {
            chefs.filter { chef ->
                val chefMeals = meals.filter { it.chefId == chef.id }
                chef.name.contains(searchQuery, ignoreCase = true) ||
                chef.cuisineType.contains(searchQuery, ignoreCase = true) ||
                chef.address.contains(searchQuery, ignoreCase = true) ||
                chefMeals.any { meal ->
                    meal.name.contains(searchQuery, ignoreCase = true) ||
                    meal.description.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRegisterDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("register_chef_button")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Post")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Host Kitchen", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Search Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search dishes, cuisines, chefs near you...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Category Badges
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = CircleShape
                        )
                    }
                }
            }

            // Screen Content
            if (filteredChefs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Not found",
                            tint = Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No kitchens found", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text("Try clearing your search query filter.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured African Specialties horizontal list
                    val africanMeals = meals.filter { meal ->
                        meal.chefId == 5 ||
                        meal.name.contains("Jollof", true) ||
                        meal.name.contains("Egusi", true) ||
                        meal.name.contains("Fufu", true) ||
                        meal.name.contains("Yam", true) ||
                        meal.name.contains("Amala", true) ||
                        meal.name.contains("Suya", true) ||
                        meal.name.contains("Asun", true)
                    }
                    val activeAfricanMeals = africanMeals.filter { selectedCategory == "All" || it.category == selectedCategory }

                    if (activeAfricanMeals.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Featured African Specialties 🌶️",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "View Chef",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            viewModel.navigateTo(Screen.ChefDetail(5))
                                        }
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(end = 4.dp)
                                ) {
                                    items(activeAfricanMeals) { meal ->
                                        val chefForMeal = chefs.find { it.id == meal.chefId }
                                        val chefName = chefForMeal?.name ?: "Chef Chinelo"
                                        FeaturedAfricanMealCard(
                                            meal = meal,
                                            chefName = chefName,
                                            onClick = {
                                                viewModel.navigateTo(Screen.ChefDetail(meal.chefId))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ChefLeaderboardSection(
                            leaderboardChefs = leaderboardChefs,
                            onChefClick = { chefId ->
                                viewModel.navigateTo(Screen.ChefDetail(chefId))
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text(
                            text = "Popular Kitchens Nearby",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(filteredChefs) { chef ->
                        val chefMeals = meals.filter { it.chefId == chef.id && (selectedCategory == "All" || it.category == selectedCategory) }
                        
                        // Avoid rendering empty chef slots when category filter does not apply
                        if (chefMeals.isNotEmpty() || selectedCategory == "All") {
                            ChefCard(chef = chef, meals = chefMeals, searchQuery = searchQuery, onClick = {
                                viewModel.navigateTo(Screen.ChefDetail(chef.id))
                            })
                        }
                    }
                }
            }
        }

        if (showRegisterDialog) {
            RegisterKitchenDialog(viewModel = viewModel, onDismiss = { showRegisterDialog = false })
        }
    }
}

// COMPOSABLE: FEATURED WEST AFRICAN SPECIALTIES CARD
@Composable
fun FeaturedAfricanMealCard(
    meal: MealEntity,
    chefName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick)
            .testTag("featured_african_meal_card_${meal.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = getMealImageModel(meal.imageUrl, meal.name),
                    contentDescription = meal.name,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE65100).copy(alpha = 0.9f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "West African 🌶️",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = meal.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", meal.price)}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = meal.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "By $chefName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// COMPOSABLE: INDIVIDUAL CHEF CARD
@Composable
fun ChefCard(chef: ChefEntity, meals: List<MealEntity>, searchQuery: String = "", onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chef_card_${chef.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Cover Header image (simulate beautiful kitchen background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                // Profile overlay
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AsyncImage(
                        model = chef.avatarUrl,
                        contentDescription = "Chef Avatar",
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = chef.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${chef.rating} • ${chef.cuisineType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Display dishes overview briefly
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = chef.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meals.size} Signature Dishes",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chef.address.substringBefore(" -"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }

                val matchedMeals = if (searchQuery.isNotEmpty()) {
                    meals.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }
                } else emptyList()

                if (matchedMeals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✨ Matches for \"$searchQuery\":",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        matchedMeals.forEach { meal ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = meal.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (meal.description.contains(searchQuery, ignoreCase = true) && 
                                        !meal.name.contains(searchQuery, ignoreCase = true)) {
                                        // Find snippet of description around matched query
                                        val desc = meal.description
                                        val index = desc.indexOf(searchQuery, ignoreCase = true)
                                        val start = (index - 20).coerceAtLeast(0)
                                        val end = (index + searchQuery.length + 30).coerceAtMost(desc.length)
                                        val prefix = if (start > 0) "..." else ""
                                        val suffix = if (end < desc.length) "..." else ""
                                        val snippet = prefix + desc.substring(start, end).trim() + suffix
                                        Text(
                                            text = snippet,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontStyle = FontStyle.Italic
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

// SHOWCASE SCREEN
@Composable
fun ShowcaseScreen(viewModel: HomeChefViewModel) {
    val chefs by viewModel.chefs.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val likedMealIds by viewModel.likedMealIds.collectAsState()
    val mealLikesCount by viewModel.mealLikesCount.collectAsState()
    val mealComments by viewModel.mealComments.collectAsState()
    
    val isDark = isSystemInDarkTheme()
    
    val cuisineFilters = listOf("All", "West African", "Indian", "Mexican", "Italian", "Ghanian", "Arab")
    var selectedCuisine by remember { mutableStateOf("All") }
    
    val filteredMeals = remember(meals, chefs, selectedCuisine) {
        if (selectedCuisine == "All") {
            meals
        } else {
            meals.filter { meal ->
                val chef = chefs.find { it.id == meal.chefId }
                if (chef == null) false else {
                    val cuisineTypeLower = chef.cuisineType.lowercase()
                    when (selectedCuisine) {
                        "West African" -> {
                            cuisineTypeLower.contains("nigerian") || 
                            cuisineTypeLower.contains("ghanaian") || 
                            cuisineTypeLower.contains("ghanian") || 
                            cuisineTypeLower.contains("west african")
                        }
                        "Ghanian" -> {
                            cuisineTypeLower.contains("ghanaian") || 
                            cuisineTypeLower.contains("ghanian")
                        }
                        "Indian" -> cuisineTypeLower.contains("indian")
                        "Mexican" -> cuisineTypeLower.contains("mexican")
                        "Italian" -> cuisineTypeLower.contains("italian")
                        "Arab" -> {
                            cuisineTypeLower.contains("arab") || 
                            cuisineTypeLower.contains("middle eastern")
                        }
                        else -> true
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top decorative header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF231B15), MaterialTheme.colorScheme.background)
                        } else {
                            listOf(Color(0xFFFFF4F0), MaterialTheme.colorScheme.background)
                        }
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Culinary Showcase ✨",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Discover hot daily creations from certified home kitchens. Like, comment, and order in real-time!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFFA5928E) else Color(0xFF6B5854)
                )
            }
        }

        // Cuisine Filter Bar (Horizontal Scrollable Chips)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 1.dp
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(cuisineFilters) { cuisine ->
                    FilterChip(
                        selected = selectedCuisine == cuisine,
                        onClick = { selectedCuisine = cuisine },
                        label = { 
                            Text(
                                text = cuisine,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCuisine == cuisine) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = CircleShape
                    )
                }
            }
        }

        if (filteredMeals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Empty",
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No dishes to display for this cuisine",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            var checkoutMeal by remember { mutableStateOf<MealEntity?>(null) }
            var checkoutChefName by remember { mutableStateOf("") }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredMeals) { meal ->
                        val chef = chefs.find { it.id == meal.chefId }
                        if (chef != null) {
                            val isLiked = likedMealIds.contains(meal.id)
                            val likesCount = mealLikesCount[meal.id] ?: ((meal.id * 17 + 23) % 150 + 12)
                            val commentsList = mealComments[meal.id] ?: emptyList()
                            
                            SocialDishPostCard(
                                meal = meal,
                                chef = chef,
                                isLiked = isLiked,
                                likesCount = likesCount,
                                comments = commentsList,
                                onLikeToggle = { viewModel.toggleLikeMeal(meal.id) },
                                onAddComment = { user, txt -> viewModel.addCommentToMeal(meal.id, user, txt) },
                                onViewChef = { viewModel.navigateTo(Screen.ChefDetail(chef.id)) },
                                onOrderNow = {
                                    checkoutMeal = meal
                                    checkoutChefName = chef.name
                                }
                            )
                        }
                    }
                }

                if (checkoutMeal != null) {
                    OrderCheckoutDialog(
                        meal = checkoutMeal!!,
                        chefName = checkoutChefName,
                        viewModel = viewModel,
                        onDismiss = { checkoutMeal = null }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocialDishPostCard(
    meal: MealEntity,
    chef: ChefEntity,
    isLiked: Boolean,
    likesCount: Int,
    comments: List<Pair<String, String>>,
    onLikeToggle: () -> Unit,
    onAddComment: (String, String) -> Unit,
    onViewChef: () -> Unit,
    onOrderNow: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showComments by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var reviewerName by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    // Animate heart scale on state change
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "HeartScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("social_dish_card_${meal.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Post Header (Chef Identity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = chef.avatarUrl,
                    contentDescription = chef.name,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chef.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = chef.cuisineType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Button(
                    onClick = onViewChef,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        "Visit Kitchen",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Post Visual Image (with double-tap to like)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .combinedClickable(
                        onDoubleClick = {
                            if (!isLiked) {
                                onLikeToggle()
                                Toast.makeText(context, "Liked ${meal.name}! ❤️", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClick = { /* Just select / read */ }
                    )
            ) {
                AsyncImage(
                    model = getMealImageModel(meal.imageUrl, meal.name),
                    contentDescription = meal.name,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay Category Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = meal.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Overlay Sells Price Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", meal.price)}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Likes and Interactions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeToggle,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Button",
                            tint = if (isLiked) Color(0xFFFF3D00) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$likesCount likes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(
                        onClick = { showComments = !showComments },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${comments.size} comments",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Link copied to share this gourmet masterpiece! 🔗", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Post Content Body / Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFFC7B1AC) else Color(0xFF5D4A46)
                )

                // Dynamic Cooking Tutorial Section
                if (meal.tutorialVideoUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    var isPlaying by remember { mutableStateOf(false) }

                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                        ) {
                            VideoPlayer(
                                youtubeVideoUrl = meal.tutorialVideoUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            IconButton(
                                onClick = { isPlaying = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Playback banner button
                        Card(
                            onClick = { isPlaying = true },
                            modifier = Modifier.fillMaxWidth().testTag("watch_tutorial_card_${meal.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Tutorial",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Watch Cooking Tutorial 📺",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Learn secret preparation techniques directly from ${chef.name}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOrderNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("social_order_button_${meal.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Order Dish",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Order Now • $${String.format("%.2f", meal.price)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Real-Time Comments Board
            if (showComments) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recent Feedback 💬",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    if (comments.isEmpty()) {
                        Text(
                            text = "No comments yet. Be the first to cheer them on!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        comments.forEach { (user, comment) ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Just now",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFAFA09C) else Color(0xFF534846)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Write Comment Row Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = reviewerName,
                                onValueChange = { reviewerName = it },
                                placeholder = { Text("Your Name", fontSize = 11.sp) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Write a supportive comment...", fontSize = 11.sp) },
                                singleLine = false,
                                maxLines = 3,
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    val nameToPost = if (reviewerName.isBlank()) "Guest Foodie" else reviewerName
                                    onAddComment(nameToPost, newCommentText)
                                    newCommentText = ""
                                    Toast.makeText(context, "Comment posted! 💬", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = newCommentText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (newCommentText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)
                                )
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Comment",
                                tint = if (newCommentText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// MAP SEARCH SCREEN
@Composable
fun MapSearchScreen(viewModel: HomeChefViewModel) {
    val chefs by viewModel.chefs.collectAsState()
    val mapRangeKm by viewModel.mapRangeKm.collectAsState()

    val closeChefs = remember(chefs, mapRangeKm) {
        viewModel.getChefsWithinRange(chefs, mapRangeKm)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Map search control card inside screen top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Search Proximity Radius",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Find kitchens cooking freshly within absolute local perimeter ranges.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Radius Limit:",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${String.format("%.1f", mapRangeKm)} km",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Slider(
                    value = mapRangeKm,
                    onValueChange = { viewModel.setMapRange(it) },
                    valueRange = 1f..15f,
                    steps = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Custom Dynamic Leaflet Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
        ) {
            LeafletMapView(
                closeChefs = closeChefs,
                userLat = viewModel.userLat,
                userLng = viewModel.userLng,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // Legend / User Marker Info Overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(
                        color = if (isSystemInDarkTheme()) Color(0xFF1C1816).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (isSystemInDarkTheme()) Color(0xFF2E2724) else Color(0x1F000000),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007AFF))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "You",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isSystemInDarkTheme()) Color(0xFFFF6E4A) else Color(0xFFFF4B2B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Kitchens: ${closeChefs.size}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun LeafletMapView(
    closeChefs: List<Pair<ChefEntity, Double>>,
    userLat: Double,
    userLng: Double,
    viewModel: HomeChefViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                
                webViewClient = WebViewClient()
                
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun openChefDetails(chefId: Int) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            viewModel.navigateTo(Screen.ChefDetail(chefId))
                        }
                    }
                }, "Android")
                
                val html = generateLeafletHtml(closeChefs, userLat, userLng, isDark)
                loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier,
        update = { webView ->
            val html = generateLeafletHtml(closeChefs, userLat, userLng, isDark)
            webView.loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
        }
    )
}

fun generateLeafletHtml(
    closeChefs: List<Pair<ChefEntity, Double>>,
    userLat: Double,
    userLng: Double,
    isDark: Boolean
): String {
    val markersCode = StringBuilder()
    closeChefs.forEach { (chef, distance) ->
        val escapedName = chef.name.replace("'", "\\'")
        val escapedCuisine = chef.cuisineType.replace("'", "\\'")
        val escapedAddress = chef.address.replace("'", "\\'")
        markersCode.append("""
            L.marker([${chef.latitude}, ${chef.longitude}], {icon: kitchenIcon})
                .addTo(map)
                .bindPopup(`
                    <div style="font-family: system-ui, -apple-system, sans-serif; line-height: 1.4; min-width: 150px;">
                        <div class="popup-title">${escapedName}</div>
                        <div class="popup-cuisine">${escapedCuisine}</div>
                        <div class="popup-address">${escapedAddress}</div>
                        <div class="popup-distance">🍳 ${String.format("%.2f", distance)} km away</div>
                        <button class="popup-button" onclick="Android.openChefDetails(${chef.id})">View Menu</button>
                    </div>
                `);
        """.trimIndent())
    }

    val mapClass = if (isDark) "dark-map" else ""

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                }
                
                /* Dark Mode Tile inversion filter */
                .dark-map .leaflet-tile {
                    filter: invert(100%) hue-rotate(180deg) brightness(95%) contrast(90%);
                }
                .dark-map .leaflet-container {
                    background: #120F0E;
                }
                
                /* Pulse animation for user dot */
                .user-location-icon {
                    position: relative;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .user-dot {
                    width: 12px;
                    height: 12px;
                    background-color: #007AFF;
                    border: 2px solid white;
                    border-radius: 50%;
                    box-shadow: 0 0 6px rgba(0,0,0,0.4);
                }
                .pulse-ring {
                    position: absolute;
                    width: 32px;
                    height: 32px;
                    border: 2px solid #007AFF;
                    border-radius: 50%;
                    animation: pulse 1.8s infinite ease-out;
                    opacity: 0;
                }
                @keyframes pulse {
                    0% { transform: scale(0.5); opacity: 0.8; }
                    100% { transform: scale(1.6); opacity: 0; }
                }

                /* Custom kitchen pin styling */
                .kitchen-location-icon {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .pin-marker {
                    width: 30px;
                    height: 30px;
                    background-color: ${if (isDark) "#FF6E4A" else "#FF4B2B"};
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    box-shadow: -2px 2px 5px rgba(0,0,0,0.4);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    animation: bounce 0.4s ease-out;
                }
                .pin-marker::after {
                    content: "🍳";
                    font-size: 14px;
                    transform: rotate(45deg);
                }
                
                @keyframes bounce {
                    0% { transform: translateY(-10px) rotate(-45deg); }
                    100% { transform: translateY(0) rotate(-45deg); }
                }
                
                /* Leaflet popup styling override */
                .leaflet-popup-content-wrapper {
                    background: ${if (isDark) "#1C1816" else "#FFFFFF"};
                    color: ${if (isDark) "#F5EFEB" else "#1E1B1A"};
                    border-radius: 14px;
                    padding: 8px;
                    box-shadow: 0px 4px 20px rgba(0,0,0,0.3);
                    border: 1px solid ${if (isDark) "#2E2724" else "rgba(0,0,0,0.05)"};
                }
                .leaflet-popup-tip {
                    background: ${if (isDark) "#1C1816" else "#FFFFFF"};
                }
                .popup-title {
                    font-weight: 800;
                    font-size: 14px;
                    margin-bottom: 3px;
                }
                .popup-cuisine {
                    font-size: 11px;
                    color: ${if (isDark) "#10B981" else "#00B074"};
                    font-weight: bold;
                    text-transform: uppercase;
                    letter-spacing: 0.8px;
                    margin-bottom: 6px;
                }
                .popup-address {
                    font-size: 12px;
                    color: ${if (isDark) "#A5928E" else "#4D3F3C"};
                    margin-bottom: 8px;
                }
                .popup-distance {
                    font-size: 11px;
                    font-weight: 700;
                    color: ${if (isDark) "#FF6E4A" else "#FF4B2B"};
                }
                .popup-button {
                    display: block;
                    width: 100%;
                    text-align: center;
                    background: ${if (isDark) "#FF6E4A" else "#FF4B2B"};
                    color: white !important;
                    font-weight: bold;
                    border: none;
                    border-radius: 8px;
                    padding: 8px 12px;
                    margin-top: 10px;
                    text-decoration: none;
                    font-size: 12px;
                    cursor: pointer;
                    box-sizing: border-box;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.15);
                    transition: background 0.2s;
                }
                .popup-button:active {
                    background: ${if (isDark) "#E05533" else "#D0351B"};
                }
            </style>
        </head>
        <body class="${mapClass}">
            <div id="map"></div>
            <script>
                // Initialize the map centered at user location
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                }).setView([$userLat, $userLng], 14);

                // Add OpenStreetMap tiles
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);
                
                // Add scale/zoom controls beautifully
                L.control.zoom({ position: 'topright' }).addTo(map);

                // User location pulsing marker
                var userIcon = L.divIcon({
                    className: 'user-location-icon',
                    html: '<div class="pulse-ring"></div><div class="user-dot"></div>',
                    iconSize: [30, 30],
                    iconAnchor: [15, 15]
                });
                
                L.marker([$userLat, $userLng], {icon: userIcon})
                    .addTo(map)
                    .bindPopup('<div style="font-family: system-ui, -apple-system, sans-serif; font-weight: bold; font-size: 13px; text-align: center;">📍 You are here</div>');

                // Kitchen markers setup
                var kitchenIcon = L.divIcon({
                    className: 'kitchen-location-icon',
                    html: '<div class="pin-marker"></div>',
                    iconSize: [30, 30],
                    iconAnchor: [15, 30]
                });

                $markersCode
            </script>
        </body>
        </html>
    """.trimIndent()
}

private fun vLines(scope: androidx.compose.ui.graphics.drawscope.DrawScope, stroke: Float) {
    var x = 0f
    with(scope) {
        while (x < scope.size.width) {
            scope.drawLine(
                color = Color(0x1F2196F3),
                start = Offset(x, 0f),
                end = Offset(x, scope.size.height),
                strokeWidth = stroke
            )
            x += 60.dp.toPx()
        }
    }
}

private fun hLines(scope: androidx.compose.ui.graphics.drawscope.DrawScope, stroke: Float) {
    var y = 0f
    with(scope) {
        while (y < scope.size.height) {
            scope.drawLine(
                color = Color(0x1F2196F3),
                start = Offset(0f, y),
                end = Offset(scope.size.width, y),
                strokeWidth = stroke
            )
            y += 60.dp.toPx()
        }
    }
}

// MY ORDERS SCREEN + TRACKING TIMELINE
@Composable
fun MockDeliveryMap(currentStep: Int) {
    val isDark = isSystemInDarkTheme()
    val routeColor = MaterialTheme.colorScheme.primary
    val trackBgColor = if (isDark) Color(0xFF1E1C1B) else Color(0xFFF9F6F5)
    val pathProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            0 -> 0.0f
            1 -> 0.15f
            2 -> 0.65f
            3 -> 1.0f
            else -> 0.0f
        },
        animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "riderProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .testTag("mock_delivery_map"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = trackBgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
                val width = size.width
                val height = size.height

                // Define coordinates
                val kitchenX = width * 0.15f
                val kitchenY = height * 0.5f
                val homeX = width * 0.85f
                val homeY = height * 0.5f

                // Draw decorative background map grid lines
                val gridColor = if (isDark) Color(0xFF33302F) else Color(0xFFEDE5E3)
                for (i in 1..4) {
                    val lineX = width * (i * 0.2f)
                    drawLine(
                        color = gridColor.copy(alpha = 0.25f),
                        start = Offset(lineX, 0f),
                        end = Offset(lineX, height),
                        strokeWidth = 2f
                    )
                }
                for (i in 1..3) {
                    val lineY = height * (i * 0.25f)
                    drawLine(
                        color = gridColor.copy(alpha = 0.25f),
                        start = Offset(0f, lineY),
                        end = Offset(width, lineY),
                        strokeWidth = 2f
                    )
                }

                // Draw path connecting them with a curvy bezier path
                val controlX1 = width * 0.4f
                val controlY1 = height * 0.2f
                val controlX2 = width * 0.6f
                val controlY2 = height * 0.8f

                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(kitchenX, kitchenY)
                    cubicTo(controlX1, controlY1, controlX2, controlY2, homeX, homeY)
                }

                // Draw background road path
                drawPath(
                    path = path,
                    color = (if (isDark) Color(0xFF4A4543) else Color(0xFFE5DDD9)).copy(alpha = 0.6f),
                    style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // Draw completed path in primary color
                val t = pathProgress
                val u = 1 - t
                val riderX = u * u * u * kitchenX + 3 * u * u * t * controlX1 + 3 * u * t * t * controlX2 + t * t * t * homeX
                val riderY = u * u * u * kitchenY + 3 * u * u * t * controlY1 + 3 * u * t * t * controlY2 + t * t * t * homeY

                // Draw dotted road details
                drawPath(
                    path = path,
                    color = routeColor,
                    style = Stroke(
                        width = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )

                // Draw Kitchen Pin Circle
                drawCircle(
                    color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F),
                    radius = 16f,
                    center = Offset(kitchenX, kitchenY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(kitchenX, kitchenY)
                )

                // Draw Home Pin Circle
                drawCircle(
                    color = if (isDark) Color(0xFF81C784) else Color(0xFF388E3C),
                    radius = 16f,
                    center = Offset(homeX, homeY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(homeX, homeY)
                )

                // Draw Rider current location circle with pulse
                drawCircle(
                    color = routeColor.copy(alpha = 0.25f),
                    radius = 28f,
                    center = Offset(riderX, riderY)
                )
                drawCircle(
                    color = routeColor,
                    radius = 14f,
                    center = Offset(riderX, riderY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(riderX, riderY)
                )
            }

            // Text labels overlay on Canvas coordinates
            Text(
                text = "🍳 Kitchen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFFFA7A7) else Color(0xFFC62828),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .offset(y = (-30).dp)
            )

            Text(
                text = "🏠 You",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1FDF1) else Color(0xFF2E7D32),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .offset(y = (-30).dp)
            )

            val statusText = when (currentStep) {
                0 -> "Awaiting Confirmation ⏰"
                1 -> "Cooking Recipe 🍳"
                2 -> "Courier En Route 🚴"
                else -> "Arrived safely 🎉"
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun OrdersScreen(viewModel: HomeChefViewModel) {
    val orders by viewModel.orders.collectAsState()
    val trackedOrderId by viewModel.trackedOrderId.collectAsState()
    val meals by viewModel.meals.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val isDark = isSystemInDarkTheme()

    // State for reorder checkout
    var checkoutMeal by remember { mutableStateOf<MealEntity?>(null) }
    var checkoutChefName by remember { mutableStateOf("") }
    var checkoutQuantity by remember { mutableStateOf(1) }
    var checkoutName by remember { mutableStateOf("") }
    var checkoutAddress by remember { mutableStateOf("") }
    var checkoutPhone by remember { mutableStateOf("") }

    // State for past orders dashboard search & sort
    var historySearchQuery by remember { mutableStateOf("") }
    var historySortOption by remember { mutableStateOf(0) } // 0: Newest, 1: Oldest, 2: Price High-to-Low, 3: Price Low-to-High

    // State for leaving a review
    var reviewChefId by remember { mutableStateOf<Int?>(null) }
    var reviewMealId by remember { mutableStateOf<Int?>(null) }
    var reviewMealName by remember { mutableStateOf("") }

    val activeTrackedOrder = remember(orders, trackedOrderId) {
        orders.find { it.id == trackedOrderId }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (trackedOrderId != null && activeTrackedOrder != null) {
            // RENDER FULL LIVE TRACKING DASHBOARD FOR A SINGLE ORDER
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.setTrackedOrder(null) },
                        modifier = Modifier.testTag("back_to_orders_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Live Tracking Details",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful custom drawn dynamic map representing live coordinate transitions
                MockDeliveryMap(currentStep = activeTrackedOrder.step)

                Spacer(modifier = Modifier.height(16.dp))

                // Estimated time arrival card with progress circle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val etaText = when (activeTrackedOrder.step) {
                                0 -> "Estimated Arrival: 25-30 Mins"
                                1 -> "Estimated Arrival: 15-20 Mins"
                                2 -> "Estimated Arrival: 5-8 Mins"
                                else -> "Arrived Successfully"
                            }
                            val subtext = when (activeTrackedOrder.step) {
                                0 -> "Your payment was processed securely. Waiting for chef acceptance."
                                1 -> "The chef is hand-crafting your fresh meal with premium ingredients."
                                2 -> "Rider Tobi is speeding down local roads with your hot package!"
                                else -> "Arrived safely and still steaming hot. Hope you love it!"
                            }

                            Text(
                                text = etaText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtext,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Pulse radial loader
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                            CircularProgressIndicator(
                                progress = {
                                    when (activeTrackedOrder.step) {
                                        0 -> 0.1f
                                        1 -> 0.4f
                                        2 -> 0.75f
                                        else -> 1.0f
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp
                            )
                            Icon(
                                imageVector = when (activeTrackedOrder.step) {
                                    0 -> Icons.Default.AccessTime
                                    1 -> Icons.Default.Restaurant
                                    2 -> Icons.Default.Moped
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Courier details & mock actions (Call/Message)
                if (activeTrackedOrder.step >= 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mock avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "T",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tobi (Specialist Rider)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF9A825),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "4.9 • Electric Cargo Bike",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Calling Tobi... 📞 (Standard VoIP simulation)", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Opening direct secure courier chat... 💬", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Active order full timeline stepper
                Text(
                    text = "Live Tracking Milestones",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TimelineStepper(currentStep = activeTrackedOrder.step)

                Spacer(modifier = Modifier.height(16.dp))

                // Accordion of summary
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Order Summary Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dish ordered", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("${activeTrackedOrder.quantity}x ${activeTrackedOrder.mealName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Chef kitchen", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(activeTrackedOrder.chefName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Stripe Secure Tx", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(activeTrackedOrder.paymentId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Destination Address", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(activeTrackedOrder.buyerAddress, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Amount Paid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format("%.2f", activeTrackedOrder.totalAmount)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.setTrackedOrder(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("de_focus_tracker_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to All Tracker Console", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        } else {
            // RENDER ACTIVE VS PAST TABS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        val activeCount = orders.count { it.step < 3 }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Tracker", fontWeight = FontWeight.Bold)
                            if (activeCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(activeCount.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_active_orders")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Past Orders", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_past_orders")
                )
            }

            if (selectedTab == 0) {
                // ACTIVE TRACKING TAB
                val activeOrders = remember(orders) { orders.filter { it.step < 3 } }

                if (activeOrders.isEmpty()) {
                    // EMPTY STATE FOR ACTIVE TRACKING
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.img_delivery_courier_1784427764589),
                                contentDescription = "Active Delivery Courier",
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "No Active Orders Underway",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Pick a mouth-watering meal, finalize Stripe Sandbox payment, and view direct live-tracking GPS countdown updates in real time!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.navigateTo(Screen.Showcase) },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(48.dp)
                                    .testTag("empty_active_go_showcase"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Browse Kitchen Dishes Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Active Shipments En Route",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(activeOrders) { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setTrackedOrder(order.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Order #${order.id}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Chef: ${order.chefName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }

                                        // Status indicator chip
                                        val (color, text) = when (order.step) {
                                            0 -> Color(0xFFF9A825) to "Awaiting Cook"
                                            1 -> Color(0xFF1565C0) to "Preparing Food"
                                            else -> Color(0xFFE65100) to "En Route 🚴"
                                        }

                                        Surface(
                                            color = color.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = text,
                                                color = color,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${order.quantity}x ${order.mealName}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Estimated arrival: " + when (order.step) {
                                                    0 -> "30 mins"
                                                    1 -> "20 mins"
                                                    else -> "6 mins"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }

                                        Text(
                                            text = "$${String.format("%.2f", order.totalAmount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Direct visual progress slider bar
                                    val progressFraction = when (order.step) {
                                        0 -> 0.15f
                                        1 -> 0.5f
                                        else -> 0.8f
                                    }
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { viewModel.setTrackedOrder(order.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("track_active_order_${order.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Interactive Live GPS Tracker", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // PAST ORDERS TAB
                val pastOrders = remember(orders) { orders.filter { it.step >= 3 } }

                val totalSpent = remember(pastOrders) { pastOrders.sumOf { it.totalAmount } }
                val averageOrderValue = remember(pastOrders) { if (pastOrders.isNotEmpty()) totalSpent / pastOrders.size else 0.0 }

                val filteredPastOrders = remember(pastOrders, historySearchQuery, historySortOption) {
                    val filtered = if (historySearchQuery.isBlank()) {
                        pastOrders
                    } else {
                        pastOrders.filter {
                            it.mealName.contains(historySearchQuery, ignoreCase = true) ||
                            it.chefName.contains(historySearchQuery, ignoreCase = true)
                        }
                    }

                    when (historySortOption) {
                        0 -> filtered.sortedByDescending { it.timestamp }
                        1 -> filtered.sortedBy { it.timestamp }
                        2 -> filtered.sortedByDescending { it.totalAmount }
                        3 -> filtered.sortedBy { it.totalAmount }
                        else -> filtered
                    }
                }

                if (pastOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Empty Past",
                                tint = Color.LightGray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Completed Orders Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "After meals arrive successfully, they appear in history for instant reordering or to write trust reviews.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.LightGray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Stat Summary Section (True Dashboard visual highlights)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "History Dashboard Summary",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Total Spent
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    Icons.Default.TrendingUp,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "$${String.format("%.2f", totalSpent)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Total Spent",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Total Orders
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    Icons.Default.Restaurant,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${pastOrders.size}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Total Orders",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Avg Value
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    Icons.Default.ShoppingBag,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "$${String.format("%.2f", averageOrderValue)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Avg Value",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search & Sorting controls section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Search Input
                                OutlinedTextField(
                                    value = historySearchQuery,
                                    onValueChange = { historySearchQuery = it },
                                    placeholder = { Text("Search meals or chefs...", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        if (historySearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { historySearchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("history_search_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Sorting Filter Chips Scroll
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sort:", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    val options = listOf("Newest First", "Oldest First", "Price: High to Low", "Price: Low to High")
                                    options.forEachIndexed { index, label ->
                                        val isSelected = historySortOption == index
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { historySortOption = index },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("sort_chip_$index")
                                        )
                                    }
                                }
                            }
                        }

                        // Orders Log items
                        if (filteredPastOrders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No matching orders found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            items(filteredPastOrders, key = { it.id }) { order ->
                                PastOrderCard(
                                    order = order,
                                    meals = meals,
                                    onReorderClick = { targetMeal, chefName, quantity, buyerName, buyerAddress, buyerPhone ->
                                        checkoutMeal = targetMeal
                                        checkoutChefName = chefName
                                        checkoutQuantity = quantity
                                        checkoutName = buyerName
                                        checkoutAddress = buyerAddress
                                        checkoutPhone = buyerPhone
                                    },
                                    onReviewClick = { chefId, mealId, mealName ->
                                        reviewChefId = chefId
                                        reviewMealId = mealId
                                        reviewMealName = mealName
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog overlays inside OrdersScreen context
    if (checkoutMeal != null) {
        OrderCheckoutDialog(
            meal = checkoutMeal!!,
            chefName = checkoutChefName,
            viewModel = viewModel,
            onDismiss = { checkoutMeal = null },
            initialQuantity = checkoutQuantity,
            initialName = checkoutName,
            initialAddress = checkoutAddress,
            initialPhone = checkoutPhone
        )
    }

    if (reviewChefId != null) {
        ReviewDialog(
            chefId = reviewChefId!!,
            mealId = reviewMealId ?: 0,
            mealName = reviewMealName,
            viewModel = viewModel,
            onDismiss = { reviewChefId = null }
        )
    }
}

@Composable
fun PastOrderCard(
    order: OrderEntity,
    meals: List<MealEntity>,
    onReorderClick: (MealEntity, String, Int, String, String, String) -> Unit,
    onReviewClick: (Int, Int, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("past_order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Chef: ${order.chefName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Served",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.quantity}x ${order.mealName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val orderDate = remember(order.timestamp) {
                        try {
                            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                            sdf.format(Date(order.timestamp))
                        } catch (e: Exception) {
                            "Recently Delivered"
                        }
                    }
                    Text(
                        text = orderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Collapsible details accordion
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stripe Secure Tx ID", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(order.paymentId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Recipient Name", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(order.buyerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Contact Phone", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(order.buyerPhone, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Destination", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = order.buyerAddress,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle expand button (Icon)
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                        .testTag("toggle_details_${order.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reorder instant chip
                Button(
                    onClick = {
                        val targetMeal = meals.find { it.id == order.mealId } ?: MealEntity(
                            id = order.mealId,
                            chefId = order.chefId,
                            name = order.mealName,
                            description = "Classic meal specially pre-cooked for your preferences.",
                            price = order.totalAmount / order.quantity,
                            imageUrl = "",
                            category = "Classic"
                        )
                        onReorderClick(targetMeal, order.chefName, order.quantity, order.buyerName, order.buyerAddress, order.buyerPhone)
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("reorder_button_${order.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instant Reorder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Leave rating review chip
                OutlinedButton(
                    onClick = {
                        onReviewClick(order.chefId, order.mealId, order.mealName)
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("rate_past_order_${order.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// REALTIME TRACKING CARD VIEW
@Composable
fun ActiveTrackingCard(order: OrderEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "DELIVERY TO:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                order.buyerName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                order.buyerAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Dish Name:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        order.mealName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Paid SECURE:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Secured",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Yes",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// DYNAMIC STEPPER VIEW COMPONENT
@Composable
fun TimelineStepper(currentStep: Int) {
    val steps = listOf(
        "Order Confirmed ✓" to "Cashier processed secure transactions.",
        "Prep stage 🍳" to "Chef raw crafting premium fresh recipe.",
        "Out for Delivery 🚴" to "Courier navigating surrounding local roads.",
        "Arrived & Served 🎉" to "Meal served perfectly hot! Enjoy!"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        steps.forEachIndexed { index, (title, desc) ->
            val isActive = index <= currentStep
            val isCurrent = index == currentStep
            val color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Stepper index bullet bubble
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isCurrent) 3.dp else 0.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stepper line connecting to next step
                    if (index < steps.size - 1) {
                        Spacer(
                            modifier = Modifier
                                .width(3.dp)
                                .height(38.dp)
                                .background(if (index < currentStep) MaterialTheme.colorScheme.primary else Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) MaterialTheme.colorScheme.onBackground else Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = desc,
                        color = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ALERT NOTIFICATION LIST VIEW
@Composable
fun NotificationsScreen(viewModel: HomeChefViewModel) {
    val alerts by viewModel.alerts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearAlerts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Community Meal Alerts 🔔",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No notification alerts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(alerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (alert.title.contains("Paid")) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    alert.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    alert.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(alert.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// DETAIL: KITCHEN PROFILE, SOCIAL SHOWCASE, TUTORIAL WEB-VIEWS, & REVIEWS
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChefDetailScreen(chefId: Int, viewModel: HomeChefViewModel) {
    val chef by viewModel.activeChef.collectAsState()
    val meals by viewModel.activeChefMeals.collectAsState()
    val reviews by viewModel.activeChefReviews.collectAsState()
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var selectedMealForRating by remember { mutableStateOf<MealEntity?>(null) }
    var activeOrderMeal by remember { mutableStateOf<MealEntity?>(null) }
    var activeTutorialUrl by remember { mutableStateOf<String?>(null) }

    // State for dedicated tabs
    var selectedTab by remember { mutableStateOf(0) }

    if (chef == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeChefNonNull = chef!!

    // Dynamic calculations for aggregate ratings
    val totalReviews = reviews.size
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else activeChefNonNull.rating.toDouble()
    
    val ratingDistribution = remember(reviews) {
        val dist = IntArray(6) { 0 }
        reviews.forEach {
            if (it.rating in 1..5) {
                dist[it.rating]++
            }
        }
        dist
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
        // Upper banner graphic
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Explore) },
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                // Chef identity profile chip overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AsyncImage(
                        model = activeChefNonNull.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            activeChefNonNull.name,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${String.format("%.1f", avgRating)} ⭐ • ${activeChefNonNull.cuisineType}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Dedicated profile Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Dishes (${meals.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Active Dishes") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("About Chef", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Chef Bio") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Ratings (${reviews.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Ratings") }
                )
            }
        }

        // Content switching based on selected tab
        when (selectedTab) {
            0 -> { // TAB 0: ACTIVE DISHES MENU
                if (meals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No dishes currently listed.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(meals) { meal ->
                        val mealReviews = remember(reviews, meal.id) { reviews.filter { it.mealId == meal.id } }
                        val hasRatings = mealReviews.isNotEmpty()
                        val dishAvgRating = remember(mealReviews) { if (hasRatings) mealReviews.map { it.rating }.average() else 5.0 }
                        val ratingCount = mealReviews.size

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    AsyncImage(
                                        model = getMealImageModel(meal.imageUrl, meal.name),
                                        contentDescription = "Dish Cover",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                meal.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "$${String.format("%.2f", meal.price)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        
                                        // DISH STAR RATING OVERVIEW
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            repeat(5) { index ->
                                                val isFilled = index < dishAvgRating.toInt()
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (isFilled) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (hasRatings) {
                                                    "${String.format("%.1f", dishAvgRating)} ($ratingCount ${if (ratingCount == 1) "review" else "reviews"})"
                                                } else {
                                                    "New ⭐"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        Text(
                                            meal.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    meal.category,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    onClick = { selectedMealForRating = meal },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .testTag("rate_dish_button_${meal.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.RateReview,
                                                        contentDescription = "Rate Dish",
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                }

                                                Button(
                                                    onClick = { activeOrderMeal = meal },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                    modifier = Modifier
                                                        .height(32.dp)
                                                        .testTag("buy_and_pay_button_${meal.id}")
                                                ) {
                                                    Text("Buy & Pay Sec", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                // DYNAMIC COOKING TUTORIAL INLINE
                                if (meal.tutorialVideoUrl.isNotEmpty()) {
                                    var isPlaying by remember { mutableStateOf(false) }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    
                                    if (isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.Black)
                                        ) {
                                            VideoPlayer(
                                                youtubeVideoUrl = meal.tutorialVideoUrl,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                            )
                                            IconButton(
                                                onClick = { isPlaying = false },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close Video",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isPlaying = true }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Watch preparation masterclass 📺",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> { // TAB 1: CHEF BIOGRAPHY & CONTACT
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${activeChefNonNull.followersCount} Local loyal fans",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                var isFollowing by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { isFollowing = !isFollowing },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color.LightGray else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Favorite,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isFollowing) "Following" else "Follow Chef", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "About the Chef",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeChefNonNull.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Kitchen Location & Contact",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = activeChefNonNull.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = activeChefNonNull.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showChatDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chef_contact_chat_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chat & Customize Ingredients 💬",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // YouTube Playlist Masterclass integration (from original layout)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "YouTube logo",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chef Recipe Tutorials 📺",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Learn secret preparation techniques directly from chef channel: ${activeChefNonNull.youtubeChannelName}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (activeTutorialUrl != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                VideoPlayer(youtubeVideoUrl = activeTutorialUrl!!)
                                IconButton(
                                    onClick = { activeTutorialUrl = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close player")
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeTutorialUrl = activeChefNonNull.youtubeChannelUrl },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp, 50.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Watch: Signature masterclass tutorials from channel",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Channel: ${activeChefNonNull.youtubeChannelName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> { // TAB 2: AGGREGATE STAR RATINGS & TRUST REVIEWS
                // 1. Dynamic Aggregate Statistics Panel
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Aggregate Customer Ratings 📊",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Column: Large Average Rating
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = String.format("%.1f", avgRating),
                                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) { index ->
                                            val isFilled = index < avgRating.toInt()
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (isFilled) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Based on $totalReviews reviews",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Right Column: Linear Star Progress Bars
                                Column(
                                    modifier = Modifier.weight(1.5f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (star in 5 downTo 1) {
                                        val count = ratingDistribution[star]
                                        val fraction = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "$star ★",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.width(28.dp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            LinearProgressIndicator(
                                                progress = { fraction },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFFFFB300),
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.width(24.dp),
                                                textAlign = TextAlign.End,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Add Review header row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customer Testimonials",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        TextButton(onClick = { showReviewDialog = true }) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Review")
                        }
                    }
                }

                // 3. Customer Reviews List
                if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No reviews yet. Be the first to try!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(reviews) { review ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        review.reviewerName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row {
                                        repeat(5) { starIndex ->
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < review.rating) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.5f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    review.comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(review.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Real-time Chat Floating Action Button
    ExtendedFloatingActionButton(
            onClick = { showChatDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("chef_chat_fab"),
            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat icon") },
            text = { Text("Ask Chef 💬") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        )
    }

    if (showChatDialog) {
        ChefChatDialog(
            chef = activeChefNonNull,
            viewModel = viewModel,
            onDismiss = { showChatDialog = false }
        )
    }

    if (showReviewDialog) {
        ReviewDialog(
            chefId = activeChefNonNull.id,
            mealId = 0,
            mealName = "",
            viewModel = viewModel,
            onDismiss = { showReviewDialog = false }
        )
    }

    if (selectedMealForRating != null) {
        ReviewDialog(
            chefId = activeChefNonNull.id,
            mealId = selectedMealForRating!!.id,
            mealName = selectedMealForRating!!.name,
            viewModel = viewModel,
            onDismiss = { selectedMealForRating = null }
        )
    }

    if (activeOrderMeal != null) {
        OrderCheckoutDialog(
            meal = activeOrderMeal!!,
            chefName = activeChefNonNull.name,
            viewModel = viewModel,
            onDismiss = { activeOrderMeal = null }
        )
    }
}

// SECURE EMBEDDED WEB-VIEW YOUTUBE PLAYER
@Composable
fun VideoPlayer(youtubeVideoUrl: String, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val videoId = remember(youtubeVideoUrl) {
        val pattern = "(?<=watch\\?v=|/videos/|embed/)[^#&?]*".toRegex()
        pattern.find(youtubeVideoUrl)?.value ?: "FLeSREbZ7Rk"
    }

    val embedUrl = "https://www.youtube.com/embed/$videoId?autoplay=1"
    var hasWebViewError by remember { mutableStateOf(false) }

    if (hasWebViewError) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(youtubeVideoUrl)
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // ignore
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play Tutorial",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Watch Tutorial Recipe",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Click to play recipe tutorial on YouTube.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        AndroidView(
            factory = { ctx ->
                try {
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        webViewClient = WebViewClient()
                        loadUrl(embedUrl)
                    }
                } catch (e: Throwable) {
                    hasWebViewError = true
                    android.view.View(ctx)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            update = { webView ->
                try {
                    if (webView is WebView) {
                        webView.loadUrl(embedUrl)
                    }
                } catch (e: Throwable) {
                    hasWebViewError = true
                }
            }
        )
    }
}

// DIALOG: CREATE NEW IN-APP CHEF / DISH HOSTING POST
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterKitchenDialog(viewModel: HomeChefViewModel, onDismiss: () -> Unit) {
    var chefName by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("Local Homestyle Cooking") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var youtubeName by remember { mutableStateOf("") }

    var dishName by remember { mutableStateOf("") }
    var dishDesc by remember { mutableStateOf("") }
    var dishPrice by remember { mutableStateOf("") }
    var dishCategory by remember { mutableStateOf("Mains") }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Gourmet Kitchen Post", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Chef Social Profile Info", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                OutlinedTextField(
                    value = chefName,
                    onValueChange = { chefName = it },
                    label = { Text("Your Chef Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_chef_name")
                )
                OutlinedTextField(
                    value = cuisine,
                    onValueChange = { cuisine = it },
                    label = { Text("Cuisine Specialties Tag") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio / Passion Statement") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Kitchen Physical Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube Channel url (Mocked or Real)") },
                    placeholder = { Text("https://www.youtube.com/watch?v=FLeSREbZ7Rk") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = youtubeName,
                    onValueChange = { youtubeName = it },
                    label = { Text("YouTube Tutorial Channel Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Signature Dish Details", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Dish Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dishDesc,
                    onValueChange = { dishDesc = it },
                    label = { Text("Short Dish Culinary Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dishPrice,
                    onValueChange = { dishPrice = it },
                    label = { Text("Dish Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category options Box / Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Mains", "Starters", "Desserts", "Drinks").forEach { cat ->
                        FilterChip(
                            selected = dishCategory == cat,
                            onClick = { dishCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chefName.isEmpty() || address.isEmpty() || dishName.isEmpty() || dishPrice.isEmpty()) {
                        Toast.makeText(context, "Please complete all mandatory parameters.", Toast.LENGTH_SHORT).show()
                    } else {
                        val parsedPrice = dishPrice.toDoubleOrNull() ?: 10.0
                        viewModel.createPostListing(
                            chefName = chefName,
                            cuisine = cuisine,
                            bio = bio,
                            phone = phone,
                            address = address,
                            youtubeUrl = youtubeUrl,
                            youtubeName = youtubeName,
                            mealName = dishName,
                            mealDesc = dishDesc,
                            mealPrice = parsedPrice,
                            category = dishCategory
                        )
                        Toast.makeText(context, "Kitchen post has been listed near neighborhood!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("publish_post_confirm")
            ) {
                Text("Publish Listing & Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChefChatDialog(
    chef: ChefEntity,
    viewModel: HomeChefViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.activeChefChatMessages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 8.dp)
                .testTag("chef_chat_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Chef Avatar & Name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = chef.avatarUrl,
                        contentDescription = chef.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chef.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Online • Instant Reply",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QuestionAnswer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Ask about ingredients, spices, allergen info, or customizable options!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(messages) { message ->
                            val isUser = message.sender == "User"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.widthIn(max = 260.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = message.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = (if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.6f),
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                val suggestions = listOf(
                    "Is it spicy?",
                    "Any nut allergies?",
                    "Vegetarian options?",
                    "Can I customize portion?"
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { query ->
                        SuggestionChip(
                            onClick = {
                                viewModel.sendChefChatMessage(chef.id, query)
                            },
                            label = { Text(query, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.testTag("suggestion_chip_${query.replace(" ", "_").replace("?", "")}")
                        )
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask about ingredients...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendChefChatMessage(chef.id, textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier
                            .background(
                                if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .testTag("send_chat_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// DIALOG: ADD DISH COGNITIVE Star-rating TRUST REVIEW
@Composable
fun ReviewDialog(
    chefId: Int,
    mealId: Int = 0,
    mealName: String = "",
    viewModel: HomeChefViewModel,
    onDismiss: () -> Unit
) {
    var reviewerName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (mealId != 0) "Rate Dish: $mealName" else "Publish Trust Review",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Your Screen Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_reviewer_name")
                )

                Text("Rating Core Score:", fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val itemRate = index + 1
                        IconButton(onClick = { rating = itemRate }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$itemRate Stars",
                                tint = if (itemRate <= rating) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(if (mealId != 0) "How was the dish?" else "Product / Culinary Feedback comment") },
                    modifier = Modifier.fillMaxWidth().testTag("add_review_comment")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reviewerName.isEmpty() || comment.isEmpty()) {
                        Toast.makeText(context, "Please write feedback parameters fully.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitReview(chefId, mealId, reviewerName, rating, comment)
                        val isLive = viewModel.isLiveMode.value
                        val msg = if (isLive) "Review submitted & synced with backend!" else "Trust rating saved in local base!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("add_review_submit_button")
            ) {
                Text("Publish Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// DIALOG: CHECKOUT SECURE PAYMENTS POWERED BY STRIPE
@Composable
fun OrderCheckoutDialog(
    meal: MealEntity,
    chefName: String,
    viewModel: HomeChefViewModel,
    onDismiss: () -> Unit,
    initialQuantity: Int = 1,
    initialName: String = "",
    initialAddress: String = "",
    initialPhone: String = ""
) {
    var quantity by remember { mutableStateOf(initialQuantity) }
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var phone by remember { mutableStateOf(initialPhone) }

    // Card Details checkout simulation parameters
    var cardNum by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val totalCost = remember(quantity) { meal.price * quantity }
    val context = LocalContext.current
    val isLiveMode by viewModel.isLiveMode.collectAsState()
    val scope = rememberCoroutineScope()

    // Stripe process states: "INPUT", "PROCESSING", "SUCCESS", "FAILURE"
    var paymentStage by remember { mutableStateOf("INPUT") }
    var stripeStatusMessage by remember { mutableStateOf("") }
    var stripeErrorMessage by remember { mutableStateOf("") }
    var stripeTxId by remember { mutableStateOf("") }

    // Detected Brand Icon
    val cardBrand = remember(cardNum) {
        val clean = cardNum.replace(" ", "")
        when {
            clean.startsWith("4") -> "Visa ⭐"
            clean.startsWith("5") -> "Mastercard 🌟"
            clean.startsWith("3") -> "American Express"
            else -> "Credit/Debit Card"
        }
    }

    AlertDialog(
        onDismissRequest = { if (paymentStage != "PROCESSING") onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isLiveMode) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (isLiveMode) "Stripe Live Checkout" else "Stripe Sandbox Checkout",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (paymentStage == "INPUT") {
                    Text("Order Item: ${meal.name} by $chefName", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Unit Price: $${String.format("%.2f", meal.price)}", style = MaterialTheme.typography.bodyMedium)

                    // Quantity counter selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Select Order Quantity:", fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Desc")
                            }
                            Text(quantity.toString(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { quantity++ }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Inc")
                            }
                        }
                    }

                    // ENVIRONMENT SELECTOR TOGGLE INSIDE DIALOG
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLiveMode) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, if (isLiveMode) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isLiveMode) "Stripe Live Mode Active 🌐" else "Stripe Sandbox Simulation 🛠️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isLiveMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = if (isLiveMode) "Requires active endpoint: ${viewModel.liveBackendUrl.value}" else "Processes offline cards instantly",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isLiveMode,
                                onCheckedChange = { viewModel.toggleLiveMode(it) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Text("Delivery Context Information", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("checkout_name_input")
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Destination Street Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("checkout_address_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Contact Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Card Details ($cardBrand)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }

                    // AUTOFILL CHIPS FOR TEST CARDS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = {
                                cardNum = "4242 4242 4242 4242"
                                cardExpiry = "12/28"
                                cardCvv = "123"
                                if (isLiveMode) {
                                    viewModel.toggleLiveMode(false)
                                    Toast.makeText(context, "Switched to Sandbox Mode for simulation!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("Autofill Pass ⭐", fontSize = 10.sp) }
                        )
                        AssistChip(
                            onClick = {
                                cardNum = "4000 0000 0000 0002"
                                cardExpiry = "11/27"
                                cardCvv = "999"
                                if (isLiveMode) {
                                    viewModel.toggleLiveMode(false)
                                    Toast.makeText(context, "Switched to Sandbox Mode for simulation!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("Autofill Decline ❌", fontSize = 10.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = cardNum,
                        onValueChange = { if (it.length <= 19) cardNum = it },
                        label = { Text("Card Number (15-16 digits with spaces)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("checkout_card_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                            label = { Text("MM/YY Expiry") },
                            placeholder = { Text("12/28") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("checkout_expiry_input")
                        )
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = { if (it.length <= 4) cardCvv = it },
                            label = { Text("CVV Security") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("checkout_cvv_input")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLiveMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else Color(0xFFE8F5E9),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = "SSL Verified",
                            tint = if (isLiveMode) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveMode) {
                                "Stripe production environment: End-to-end encrypted under PCI-DSS standards."
                            } else {
                                "Operative sandbox mode. Valid test credentials (e.g., 4242 4242...) pass safely."
                            },
                            fontSize = 10.sp,
                            color = if (isLiveMode) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (paymentStage == "PROCESSING") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = if (isLiveMode) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Processing Stripe Payout Gateway...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stripeStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (paymentStage == "SUCCESS") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            "Stripe Payment Received!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            "Transaction authorized and resolved successfully. Your chef has been credited.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Transaction Target: $stripeTxId", style = MaterialTheme.typography.labelSmall)
                                Text("Method: Credit card ending in (${cardNum.takeLast(4)})", style = MaterialTheme.typography.labelSmall)
                                Text("Total processed: $${String.format("%.2f", totalCost)}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else if (paymentStage == "FAILURE") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            "Stripe Verification Failed",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            stripeErrorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isLiveMode) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Sandbox Testing Tip 💡",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Live Mode requires a custom, running Stripe API backend. For a local, instant, zero-setup connection experience, switch to Sandbox Mode to simulate actual PCI-DSS billing.",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.toggleLiveMode(false)
                                            paymentStage = "INPUT"
                                            Toast.makeText(context, "Developer Sandbox Mode active!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Toggle Sandbox & Try Autofill")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { paymentStage = "INPUT" },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                        ) {
                            Text("Retry Billing Options")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (paymentStage == "INPUT") {
                Button(
                    onClick = {
                        val cleanCard = cardNum.replace(" ", "")
                        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || cleanCard.length < 15) {
                            Toast.makeText(context, "Please complete fields and credit credentials.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cardExpiry.length < 5 || cardCvv.length < 3) {
                            Toast.makeText(context, "Verify your Expiry and CVV codes.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            paymentStage = "PROCESSING"
                            stripeStatusMessage = "1. Creating dynamic secure PaymentIntent sessions..."
                            
                            // Call processStripePayment through the view model
                            val billingRes = viewModel.processStripePayment(
                                amount = totalCost,
                                cardNum = cardNum,
                                expiry = cardExpiry,
                                cvv = cardCvv,
                                description = "Payment for ${meal.name} by ${name}"
                            )

                            when (billingRes) {
                                is StripePaymentResult.Success -> {
                                    stripeTxId = billingRes.transactionId
                                    stripeStatusMessage = "2. Finalizing billing record sync with local Room DB..."
                                    
                                    viewModel.requestOrder(
                                        meal = meal,
                                        chefName = chefName,
                                        quantity = quantity,
                                        buyerName = name,
                                        buyerAddress = address,
                                        buyerPhone = phone,
                                        onSuccess = { orderId ->
                                            paymentStage = "SUCCESS"
                                        }
                                    )
                                }
                                is StripePaymentResult.Failure -> {
                                    stripeErrorMessage = billingRes.errorMessage
                                    paymentStage = "FAILURE"
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("submit_checkout_pay"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLiveMode) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                    )
                ) {
                    Text("Pay $${String.format("%.2f", totalCost)}")
                }
            } else if (paymentStage == "SUCCESS") {
                Button(
                    onClick = {
                        onDismiss()
                        Toast.makeText(context, "Stripe Charge finalized! Order is active.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("close_invoice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Close Invoice & Track Order")
                }
            }
        },
        dismissButton = {
            if (paymentStage == "INPUT") {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_checkout")
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun GoLiveConfigScreen(viewModel: HomeChefViewModel) {
    val isLiveByState by viewModel.isLiveMode.collectAsState()
    val liveBackendUrl by viewModel.liveBackendUrl.collectAsState()
    val stripePublishableKey by viewModel.stripePublishableKey.collectAsState()
    val googleMapsApiKey by viewModel.googleMapsApiKey.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var tempUrl by remember(liveBackendUrl) { mutableStateOf(liveBackendUrl) }
    var tempStripe by remember(stripePublishableKey) { mutableStateOf(stripePublishableKey) }
    var tempMapKey by remember(googleMapsApiKey) { mutableStateOf(googleMapsApiKey) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Production Go-Live Dashboard",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Switch from Simulated Sandbox mode to real production API endpoints, Stripe checkout integrations, and launch services for Citch live customers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Live Toggle Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Live Production Mode",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        if (isLiveByState) "Serving real requests via custom backend API" else "Operating offline database & localized sandboxes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isLiveByState,
                    onCheckedChange = {
                        viewModel.toggleLiveMode(it)
                        val msg = if (it) "Enabled Live Production routing mode!" else "Reverted to Local Simulated Sandbox mode."
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Configuration Inputs
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Production Integration Targets",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("Base API Endpoint URL") },
                    placeholder = { Text("https://your-api.com/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Preset Autofills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            tempUrl = "http://localhost:3000"
                            Toast.makeText(context, "Filled Cloud Localhost Preset!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Cloud Localhost (3000)", style = MaterialTheme.typography.labelSmall)
                    }

                    ElevatedButton(
                        onClick = {
                            tempUrl = "http://10.0.2.2:3000"
                            Toast.makeText(context, "Filled Emulator Bridge Preset!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Emulator Bridge (10.0.2.2)", style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedTextField(
                    value = tempStripe,
                    onValueChange = { tempStripe = it },
                    label = { Text("Stripe Publishable Token Key") },
                    placeholder = { Text("pk_live_...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempMapKey,
                    onValueChange = { tempMapKey = it },
                    label = { Text("Google Maps SDK API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dynamic Sync Status HUD
                AnimatedVisibility(visible = syncStatus != null) {
                    val status = syncStatus ?: ""
                    val isError = status.startsWith("Sync Failed")
                    val isSuccess = status.startsWith("Sync Succeeded")
                    
                    val bgColor = when {
                        isSuccess -> Color(0xFFE8F5E9)
                        isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    }
                    val contentColor = when {
                        isSuccess -> Color(0xFF2E7D32)
                        isError -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                    val icon = when {
                        isSuccess -> Icons.Default.Check
                        isError -> Icons.Default.Warning
                        else -> Icons.Default.Sync
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = bgColor,
                        contentColor = contentColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (status == "Syncing...") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp).padding(top = 2.dp),
                                    strokeWidth = 2.dp,
                                    color = contentColor
                                )
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = contentColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isError) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "How to fix:\n1. Ensure the Node.js backend is active (it's running in your workspace container!).\n2. In the AI Studio streaming emulator, use 'http://10.0.2.2:3000' as the endpoint.\n3. Make sure 'usesCleartextTraffic' is enabled in the Manifest.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor.copy(alpha = 0.8f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            viewModel.updateLiveBackendUrl(tempUrl)
                            viewModel.updateStripeKey(tempStripe)
                            viewModel.updateGoogleMapsKey(tempMapKey)
                            Toast.makeText(context, "Attempting database synchronization...", Toast.LENGTH_SHORT).show()
                            viewModel.syncDataFromBackend()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection & Sync")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateLiveBackendUrl(tempUrl)
                            viewModel.updateStripeKey(tempStripe)
                            viewModel.updateGoogleMapsKey(tempMapKey)
                            Toast.makeText(context, "Production configurations verified & synced!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Keys")
                    }
                }
            }
        }

        // Interactive Launch Guides & Step checklist (Expandable)
        Text(
            "Complete Go-Live Checklist",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )

        StepItem(
            stepNumber = "1",
            title = "Launch Production Web Server & Room Sync",
            description = "1. Replace the simulated Room initial datasets with live HTTP Retrofit/Ktor networking. \n2. Host a Node.js Express/Spring Boot server securely on platform clouds. \n3. Point 'Base API Endpoint URL' above to connect dynamic chef search streams natively."
        )

        StepItem(
            stepNumber = "2",
            title = "Secure Live Stripe Merchants",
            description = "1. Replace 'OrderCheckoutDialog' with real Stripe Mobile SDK library targets. \n2. Configure a modern Customer/PaymentIntent session token server-side. \n3. Turn on Stripe webhooks to trigger notifications instantly to customers when orders are confirmed."
        )

        StepItem(
            stepNumber = "3",
            title = "Setup Google Maps API Keys",
            description = "1. Generate an API Key under the Google Cloud Console with Maps SDK enabled. \n2. Insert the key in AndroidManifest.xml within <meta-data android:name=\"com.google.android.geo.API_KEY\" ... /> tags. \n3. The MapSearchScreen's WebView will automatically load real interactive locations securely."
        )

        StepItem(
            stepNumber = "4",
            title = "Deploy on Google Play Console",
            description = "1. Go to build.gradle.kts and update your unique Application ID. \n2. Sign your production release package with the Upload Keystore tool. \n3. Create a store listing under developer.android.com/distribute to publish Citch completely."
        )

        // Bottom Spacer to prevent overlapping by Bottom Navigation Bar
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun StepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stepNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// --- AI CULINARY HUB COMPOSABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICulinaryHubScreen(viewModel: HomeChefViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "AI Chatbot" to Icons.Default.Chat,
        "Sourcing Maps" to Icons.Default.Map,
        "Creative Studio" to Icons.Default.AutoAwesome,
        "Vision Scanner" to Icons.Default.PhotoCamera,
        "Live Voice" to Icons.Default.GraphicEq
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("ai_tabs")
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                    icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("ai_tab_$index")
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> ChatbotTabContent(viewModel)
                1 -> SourcingMapsTabContent(viewModel)
                2 -> CreativeStudioTabContent(viewModel)
                3 -> VisionScannerTabContent(viewModel)
                4 -> LiveVoiceTabContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotTabContent(viewModel: HomeChefViewModel) {
    val aiChatHistory by viewModel.aiChatHistory.collectAsState()
    val aiChatIsLoading by viewModel.aiChatIsLoading.collectAsState()
    val aiChatModel by viewModel.aiChatModel.collectAsState()
    val aiChatThinkingMode by viewModel.aiChatThinkingMode.collectAsState()
    val aiChatGoogleSearch by viewModel.aiChatGoogleSearch.collectAsState()
    val aiChatGoogleMaps by viewModel.aiChatGoogleMaps.collectAsState()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val transcriptionResult by viewModel.transcriptionResult.collectAsState()

    var userText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(aiChatHistory.size) {
        if (aiChatHistory.isNotEmpty()) {
            listState.animateScrollToItem(aiChatHistory.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Culinara Configuration (Gemini API)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Model:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val models = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        models.forEach { m ->
                            val isSelected = aiChatModel == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateAiChatModel(m) },
                                label = { Text(m.substringAfter("gemini-"), fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp).testTag("chip_$m")
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = aiChatThinkingMode,
                            onCheckedChange = { viewModel.toggleAiChatThinking(it) },
                            modifier = Modifier.scale(0.7f).testTag("thinking_switch")
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Thinking", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = aiChatGoogleSearch,
                            onCheckedChange = { viewModel.toggleAiChatGoogleSearch(it) },
                            modifier = Modifier.scale(0.7f).testTag("search_grounding_switch")
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Search", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = aiChatGoogleMaps,
                            onCheckedChange = { viewModel.toggleAiChatGoogleMaps(it) },
                            modifier = Modifier.scale(0.7f).testTag("maps_grounding_switch")
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Maps", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (transcriptionResult.isNotEmpty() || isRecordingAudio) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isRecordingAudio) Icons.Default.Mic else Icons.Default.Receipt,
                        contentDescription = "Dictation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRecordingAudio) "Listening..." else "Speech Transcript (Gemini 3.5 Flash)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = transcriptionResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (!isRecordingAudio && transcriptionResult.isNotEmpty() && transcriptionResult != "Transcribing...") {
                        TextButton(
                            onClick = {
                                userText = transcriptionResult
                            }
                        ) {
                            Text("Use Text", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(aiChatHistory) { (sender, text) ->
                val isUser = sender == "User"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column(
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = sender,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
            if (aiChatIsLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Culinara AI is thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleAudioRecording() },
                modifier = Modifier.testTag("ai_mic_button")
            ) {
                Icon(
                    imageVector = if (isRecordingAudio) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Dictation",
                    tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("Ask Culinara a recipe challenge...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                shape = CircleShape,
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (userText.isNotBlank()) {
                        viewModel.sendAiChatMessage(userText)
                        userText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_chat_send_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcingMapsTabContent(viewModel: HomeChefViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val pastSearches = remember {
        mutableStateListOf(
            "Organic Farmers Market Downtown SF",
            "Premium African Spice Importers",
            "Fresh Sourdough Flour Millers"
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Google Maps Sourcing Grounding (Gemini 3.5 Flash)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Verify premium local supply chains, wholesale distributors, and food markets using real-world Google Maps API locations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search sourcing e.g. organic avocado suppliers, SF spice markets") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().testTag("maps_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        isLoading = true
                        resultText = null
                        if (!pastSearches.contains(searchQuery)) {
                            pastSearches.add(0, searchQuery)
                        }
                        coroutineScope.launch {
                            val response = GeminiService.generateContent(
                                model = "gemini-3.5-flash",
                                prompt = "Find real, actual business locations for: \"$searchQuery\" in the San Francisco Bay Area. List business names, real street addresses, phone numbers, and proximity notes based on Google Maps. Format clearly with bullets.",
                                useMaps = true
                            )
                            resultText = response
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.weight(1f).testTag("maps_search_btn")
            ) {
                Text("Scan Google Maps Sourcing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Querying Google Maps with Grounding...", style = MaterialTheme.typography.bodySmall)
            }
        } else if (resultText != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = "Map Pin", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grounding Citations Verified ✓", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = resultText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text("Suggested Sourcing Queries:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                pastSearches.forEach { search ->
                    OutlinedCard(
                        onClick = {
                            searchQuery = search
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(search, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeStudioTabContent(viewModel: HomeChefViewModel) {
    var activeSubStudio by remember { mutableStateOf(0) }
    
    val imagePrompt by viewModel.imagePrompt.collectAsState()
    val imageQuality by viewModel.imageQuality.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()
    val imageAspectRatio by viewModel.imageAspectRatio.collectAsState()
    val generatedImageUrl by viewModel.generatedImageUrl.collectAsState()
    val imageIsGenerating by viewModel.imageIsGenerating.collectAsState()

    val videoPrompt by viewModel.videoPrompt.collectAsState()
    val videoAspectRatio by viewModel.videoAspectRatio.collectAsState()
    val generatedVideoUrl by viewModel.generatedVideoUrl.collectAsState()
    val videoIsGenerating by viewModel.videoIsGenerating.collectAsState()

    val musicPrompt by viewModel.musicPrompt.collectAsState()
    val musicDurationSec by viewModel.musicDurationSec.collectAsState()
    val generatedMusicUrl by viewModel.generatedMusicUrl.collectAsState()
    val musicIsGenerating by viewModel.musicIsGenerating.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = activeSubStudio,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(selected = activeSubStudio == 0, onClick = { activeSubStudio = 0 }, text = { Text("Imagen 3") })
            Tab(selected = activeSubStudio == 1, onClick = { activeSubStudio = 1 }, text = { Text("Veo Sizzle") })
            Tab(selected = activeSubStudio == 2, onClick = { activeSubStudio = 2 }, text = { Text("Lyria Beats") })
        }

        when (activeSubStudio) {
            0 -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("High-Quality Menu Designer (Imagen 3)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { viewModel.updateImagePrompt(it) },
                        placeholder = { Text("e.g. ultra realistic wood fire gourmet pizza with bubbles on crust and fresh basil") },
                        modifier = Modifier.fillMaxWidth().testTag("image_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Model Selection:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("gemini-3.1-flash-image-preview" to "Standard", "gemini-3-pro-image-preview" to "Studio Pro").forEach { (qualityId, label) ->
                            FilterChip(
                                selected = imageQuality == qualityId,
                                onClick = { viewModel.updateImageQuality(qualityId) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("quality_chip_$qualityId")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Select Target Definition:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1K", "2K", "4K").forEach { size ->
                            FilterChip(
                                selected = imageSize == size,
                                onClick = { viewModel.updateImageSize(size) },
                                label = { Text(size) },
                                modifier = Modifier.testTag("size_chip_$size")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Choose Aspect Ratio (Imagen Standard):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    val ratios = listOf("1:1", "16:9", "9:16", "3:2", "2:3", "4:3", "21:9")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ratios.forEach { ratio ->
                            FilterChip(
                                selected = imageAspectRatio == ratio,
                                onClick = { viewModel.updateImageAspectRatio(ratio) },
                                label = { Text(ratio) },
                                modifier = Modifier.testTag("ratio_chip_$ratio")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateImage() },
                        enabled = !imageIsGenerating,
                        modifier = Modifier.fillMaxWidth().testTag("image_generate_btn")
                    ) {
                        if (imageIsGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Illustration...")
                        } else {
                            Text("Generate High-Fidelity Menu Asset")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedImageUrl != null) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = generatedImageUrl,
                                    contentDescription = "Generated Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(
                                            when (imageAspectRatio) {
                                                "16:9" -> 1.77f
                                                "9:16" -> 0.56f
                                                "3:2" -> 1.5f
                                                "2:3" -> 0.67f
                                                "4:3" -> 1.33f
                                                "21:9" -> 2.33f
                                                else -> 1f
                                            }
                                        )
                                        .background(Color.Black),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Render: Ready ($imageSize - $imageAspectRatio)", style = MaterialTheme.typography.labelSmall)
                                        Text("Model: $imageQuality", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Veo Cinematic Sizzle Creator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Generate sizzling cooking video reels using veo-3.1-fast-generate-preview.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = videoPrompt,
                        onValueChange = { viewModel.updateVideoPrompt(it) },
                        placeholder = { Text("e.g. delicious garlic sauce pouring over grilled salmon steak, slow motion, steam rising, high contrast") },
                        modifier = Modifier.fillMaxWidth().testTag("video_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Aspect Ratio:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("16:9" to "Landscape (Cinematic)", "9:16" to "Portrait (Reel)").forEach { (aspect, label) ->
                            FilterChip(
                                selected = videoAspectRatio == aspect,
                                onClick = { viewModel.updateVideoAspectRatio(aspect) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("video_aspect_$aspect")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateVideo() },
                        enabled = !videoIsGenerating,
                        modifier = Modifier.fillMaxWidth().testTag("video_generate_btn")
                    ) {
                        if (videoIsGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Animating Video on Veo...")
                        } else {
                            Text("Animate Video from Text")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedVideoUrl != null) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Veo 3.1 Promo Output (1080p, $videoAspectRatio)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(if (videoAspectRatio == "16:9") 1.77f else 0.56f)
                                        .background(Color.Black, shape = MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", modifier = Modifier.size(60.dp), tint = Color.White.copy(alpha = 0.8f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Cinematic Sizzle Simulation Loop", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        Text("Double tap to play simulated Veo feed", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Lyria Culinary Ambient Beat Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Create background cooking tracks, sizzling soundscapes, or coffee shop vibes using lyria-3-clip-preview.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = musicPrompt,
                        onValueChange = { viewModel.updateMusicPrompt(it) },
                        placeholder = { Text("e.g. warm lo-fi kitchen hiphop with background sizzle of frying oil and clinking cups") },
                        modifier = Modifier.fillMaxWidth().testTag("music_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Generate Mode:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("30s" to "Short Clip (lyria-3-clip-preview)", "3m" to "Full Track (lyria-3-pro-preview)").forEach { (dur, label) ->
                            FilterChip(
                                selected = musicDurationSec == dur,
                                onClick = { viewModel.updateMusicDuration(dur) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("music_dur_$dur")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateMusic() },
                        enabled = !musicIsGenerating,
                        modifier = Modifier.fillMaxWidth().testTag("music_generate_btn")
                    ) {
                        if (musicIsGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Composing Ambient Track...")
                        } else {
                            Text("Compose Custom Kitchen Beat")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedMusicUrl != null) {
                        var isPlayingMusic by remember { mutableStateOf(false) }
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = { isPlayingMusic = !isPlayingMusic },
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingMusic) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Playback Control"
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ambient Kitchen Rhythm", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (isPlayingMusic) "Now Playing Lyria Track..." else "Audio File Ready (Format: MP3, $musicDurationSec)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = if (isPlayingMusic) 0.45f else 0f,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionScannerTabContent(viewModel: HomeChefViewModel) {
    var selectedVisionType by remember { mutableStateOf(0) }
    
    val selectedImageAnalysisResult by viewModel.selectedImageAnalysisResult.collectAsState()
    val imageAnalysisIsLoading by viewModel.imageAnalysisIsLoading.collectAsState()

    val videoAnalysisResult by viewModel.videoAnalysisResult.collectAsState()
    val videoAnalysisIsLoading by viewModel.videoAnalysisIsLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = selectedVisionType,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(selected = selectedVisionType == 0, onClick = { selectedVisionType = 0 }, text = { Text("Fridge Scanner") })
            Tab(selected = selectedVisionType == 1, onClick = { selectedVisionType = 1 }, text = { Text("Video Analyzer") })
        }

        when (selectedVisionType) {
            0 -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("What's in my Fridge? (Gemini 3.1 Pro)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Take a picture or choose a sample fridge section to scan for fresh ingredients and draft a tailored recipe instantly.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select a Fridge Compartment Scan:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val fridgeSamples = listOf(
                        "Crisper Drawer: Spinach, Bell Peppers & Ginger" to "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=200",
                        "Middle Shelf: Salmon Steak, Garlic & Lemon" to "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=200",
                        "Door Rack: Eggs, Truffle Oil, Parmesan & Butter" to "https://images.unsplash.com/photo-1506084868230-bb9d95c24759?w=200"
                    )

                    fridgeSamples.forEach { (title, url) ->
                        OutlinedCard(
                            onClick = { viewModel.analyzeFridgeImage(title) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("fridge_scan_$title")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).background(Color.Gray, shape = MaterialTheme.shapes.small),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(title.substringBefore(":"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(title.substringAfter(": "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (imageAnalysisIsLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gemini Pro is identifying ingredients...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (selectedImageAnalysisResult != null) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Chef's Draft Recipe Recommendations:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(selectedImageAnalysisResult ?: "", style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Culinary Video Analyzer (Gemini 3.1 Pro)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Provide a cooking video recipe to extract exact proportions, chef techniques, and temperatures.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    val videoSamples = listOf(
                        "Kenji's Deep-Bowl Black Garlic Tonkotsu Ramen" to "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=200",
                        "Nigerian Sizzling Jollof Rice Masterclass" to "https://images.unsplash.com/photo-1626861300079-7844b27f17f6?w=200",
                        "The Secrets of Perfect French Truffle Soufflé" to "https://images.unsplash.com/photo-1579372786545-d24232daf58c?w=200"
                    )

                    videoSamples.forEach { (title, url) ->
                        OutlinedCard(
                            onClick = { viewModel.analyzeRecipeVideo(title) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("video_scan_$title")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp).background(Color.Gray, shape = MaterialTheme.shapes.small),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(modifier = Modifier.size(24.dp).background(Color.Black.copy(alpha = 0.5f), shape = CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (videoAnalysisIsLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gemini Pro is analyzing video frames...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (videoAnalysisResult != null) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pro Recipe Transcript & Analysis Metrics:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(videoAnalysisResult ?: "", style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveVoiceTabContent(viewModel: HomeChefViewModel) {
    val isLiveVoiceSessionActive by viewModel.isLiveVoiceSessionActive.collectAsState()
    val voiceSessionLog by viewModel.voiceSessionLog.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(voiceSessionLog.size) {
        if (voiceSessionLog.isNotEmpty()) {
            listState.animateScrollToItem(voiceSessionLog.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Voice Sous-Chef Companion",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Initiate a real-time voice session using the Live API (gemini-3.1-flash-live-preview) to get step-by-step guidance hands-free.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    if (isLiveVoiceSessionActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .testTag("voice_pulsator_box"),
            contentAlignment = Alignment.Center
        ) {
            if (isLiveVoiceSessionActive) {
                var pulseState by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    while (true) {
                        pulseState = !pulseState
                        delay(800)
                    }
                }
                val scale by animateFloatAsState(if (pulseState) 1.2f else 0.95f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape)
                )
            }

            FloatingActionButton(
                onClick = { viewModel.toggleLiveVoiceSession() },
                shape = CircleShape,
                containerColor = if (isLiveVoiceSessionActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isLiveVoiceSessionActive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(80.dp).testTag("live_voice_fab")
            ) {
                Icon(
                    imageVector = if (isLiveVoiceSessionActive) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Control",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isLiveVoiceSessionActive) "Voice Session Active (Listening...)" else "Tap to connect Voice Sous-Chef",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isLiveVoiceSessionActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Conversation Stream:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (voiceSessionLog.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active session stream logs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(voiceSessionLog) { log ->
                            val isSystem = log.startsWith("[")
                            val isChef = log.startsWith("AI Sous-Chef")
                            Text(
                                text = log,
                                style = if (isSystem) MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic) else MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSystem -> MaterialTheme.colorScheme.outline
                                    isChef -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isChef) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
