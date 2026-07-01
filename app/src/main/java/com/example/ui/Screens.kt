package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
                            text = "D-KITCN Connect",
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
                    } else {
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
                    is Screen.MapSearch -> MapSearchScreen(viewModel)
                    is Screen.Orders -> OrdersScreen(viewModel)
                    is Screen.Notifications -> NotificationsScreen(viewModel)
                    is Screen.GoLiveConfig -> GoLiveConfigScreen(viewModel)
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

// EXPLORE MAIN SCREEN
@Composable
fun ExploreScreen(viewModel: HomeChefViewModel) {
    val chefs by viewModel.chefs.collectAsState()
    val meals by viewModel.meals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    var showRegisterDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Mains", "Starters", "Desserts")

    val filteredChefs = remember(chefs, searchQuery) {
        if (searchQuery.isEmpty()) chefs else {
            chefs.filter { chef ->
                chef.name.contains(searchQuery, ignoreCase = true) ||
                chef.cuisineType.contains(searchQuery, ignoreCase = true) ||
                chef.address.contains(searchQuery, ignoreCase = true)
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
                            ChefCard(chef = chef, meals = chefMeals, onClick = {
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
fun ChefCard(chef: ChefEntity, meals: List<MealEntity>, onClick: () -> Unit) {
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
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${chef.rating} • ${chef.cuisineType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
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

        // Custom Simulated Stylized Kitchen Map Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE3F2FD)) // Beautiful light blue map background
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
        ) {
            // Grid draw lines representing map roads / streets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val strokeWidth = 3.dp.toPx()
                        vLines(this, strokeWidth)
                        hLines(this, strokeWidth)
                        
                        // Draw central user locator beacon
                        drawCircle(
                            color = Color(0x332196F3),
                            radius = 90.dp.toPx(),
                            center = Offset(size.width / 2, size.height / 2)
                        )
                        drawCircle(
                            color = Color(0xFF1976D2),
                            radius = 12.dp.toPx(),
                            center = Offset(size.width / 2, size.height / 2)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
            )

            // Dynamic pin calculations: Place kitchens according to their latitude and longitude difference from user
            closeChefs.forEach { (chef, distance) ->
                // Math offsets based on coordinate translation to map limits
                val xOffset = remember(chef.id) { (chef.longitude - viewModel.userLng) * 12000 }
                val yOffset = remember(chef.id) { (chef.latitude - viewModel.userLat) * 12000 }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = xOffset.dp, y = -yOffset.dp)
                ) {
                    var showCardDetails by remember { mutableStateOf(false) }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        IconButton(
                            onClick = { showCardDetails = !showCardDetails },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = chef.name,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = chef.name.substringBefore(" "),
                                color = Color.White,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showCardDetails) {
                            Dialog(onDismissRequest = { showCardDetails = false }) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            chef.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            chef.cuisineType,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            chef.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Proximity Distance: ${String.format("%.2f", distance)} km",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { showCardDetails = false }) {
                                                Text("Close")
                                            }
                                            Button(onClick = {
                                                showCardDetails = false
                                                viewModel.navigateTo(Screen.ChefDetail(chef.id))
                                            }) {
                                                Text("Open Menu")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legend / User Marker Info Overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Your Location", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kitchens found: ${closeChefs.size}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
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
fun OrdersScreen(viewModel: HomeChefViewModel) {
    val orders by viewModel.orders.collectAsState()
    val trackedOrderId by viewModel.trackedOrderId.collectAsState()

    val activeTrackedOrder = remember(orders, trackedOrderId) {
        orders.find { it.id == trackedOrderId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (trackedOrderId != null && activeTrackedOrder != null) {
            // Render Real-Time Tracker View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.setTrackedOrder(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Track Order #${activeTrackedOrder.id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ActiveTrackingCard(order = activeTrackedOrder)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Real-time timeline steps
                TimelineStepper(currentStep = activeTrackedOrder.step)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.setTrackedOrder(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("De-focus Tracker")
                }
            }
        } else {
            // Render lists of All Orders placed
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Empty",
                            tint = Color.LightGray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Orders Placed Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Browse kitchens nearby, pick a mouth-watering meal, pay securely, and start tracking live!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "History & Live Tracking",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(orders) { order ->
                        OrderHistoryItem(order = order, onClickTracking = {
                            viewModel.setTrackedOrder(order.id)
                        })
                    }
                }
            }
        }
    }
}

// ORDER HISTORY LIST COMPONENT
@Composable
fun OrderHistoryItem(order: OrderEntity, onClickTracking: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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

                // Tracking Quick Status badge
                val (color, text) = when (order.step) {
                    0 -> Color(0xFFF9A825) to "Pending"
                    1 -> Color(0xFF1565C0) to "Preparing"
                    2 -> Color(0xFFE65100) to "On the road"
                    else -> Color(0xFF2E7D32) to "Delivered"
                }

                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.quantity}x ${order.mealName}",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Paid securely • ${order.paymentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClickTracking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Track Order Blueprint Live")
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
    var selectedMealForRating by remember { mutableStateOf<MealEntity?>(null) }
    var activeOrderMeal by remember { mutableStateOf<MealEntity?>(null) }
    var activeTutorialUrl by remember { mutableStateOf<String?>(null) }

    if (chef == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeChefNonNull = chef!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
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
                                "${activeChefNonNull.rating} ⭐ • ${activeChefNonNull.cuisineType}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // About Description & Social Followers
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${activeChefNonNull.followersCount} Local loyal fans",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    var isFollowing by remember { mutableStateOf(false) }
                    Button(
                        onClick = { isFollowing = !isFollowing },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.LightGray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(if (isFollowing) Icons.Default.Check else Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFollowing) "Following" else "Follow Chef")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    activeChefNonNull.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        activeChefNonNull.address,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        activeChefNonNull.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        // TUTORIALS CHANNEL INTEGRATION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.PlayCircleFilled,
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
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Inline Player frame if active
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

                // Show dynamic playlist banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeTutorialUrl = activeChefNonNull.youtubeChannelUrl },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // DESIGN SIGNATURE MEALS LIST
        item {
            Text(
                "Chef Signature Food Menu",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        if (meals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No dishes currently listed.", color = Color.Gray)
                }
            }
        } else {
            items(meals) { meal ->
                val mealReviews = remember(reviews, meal.id) { reviews.filter { it.mealId == meal.id } }
                val hasRatings = mealReviews.isNotEmpty()
                val avgRating = remember(mealReviews) { if (hasRatings) mealReviews.map { it.rating }.average() else 5.0 }
                val ratingCount = mealReviews.size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = getMealImageModel(meal.imageUrl, meal.name),
                            contentDescription = "Dish Cover",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(10.dp))
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$${String.format("%.2f", meal.price)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // DISH STAR RATING OVERVIEW
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                repeat(5) { index ->
                                    val isFilled = index < avgRating.toInt()
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
                                        "${String.format("%.1f", avgRating)} ($ratingCount ${if (ratingCount == 1) "review" else "reviews"})"
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
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            meal.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    TextButton(
                                        onClick = { selectedMealForRating = meal },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("rate_dish_button_${meal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RateReview,
                                            contentDescription = "Rate Dish",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rate Dish", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
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
            }
        }

        // TRUST REVIEWS FEEDBACK SYSTEM
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Community Trust Reviews",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(onClick = { showReviewDialog = true }) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Review")
                }
            }
        }

        if (reviews.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No community reviews yet. Be the first to try!", color = Color.Gray)
                }
            }
        } else {
            items(reviews) { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                review.reviewerName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row {
                                repeat(5) { starIndex ->
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (starIndex < review.rating) Color(0xFFFFB300) else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            review.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
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
                    listOf("Mains", "Starters", "Desserts").forEach { cat ->
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
                        Toast.makeText(context, "Trust rating saved in local base!", Toast.LENGTH_SHORT).show()
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
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

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
                    "Switch from Simulated Sandbox mode to real production API endpoints, Stripe checkout integrations, and launch services for D-KITCN live customers.",
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
            description = "1. Go to build.gradle.kts and update your unique Application ID. \n2. Sign your production release package with the Upload Keystore tool. \n3. Create an store listing under developer.android.com/distribute to publish D-KITCN completely."
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
