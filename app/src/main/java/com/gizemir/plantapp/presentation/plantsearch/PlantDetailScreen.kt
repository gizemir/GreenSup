package com.gizemir.plantapp.presentation.plantsearch

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail
import com.gizemir.plantapp.domain.model.plant_search.PlantDistribution
import com.gizemir.plantapp.presentation.plantsearch.components.PlantInfoCard
import com.gizemir.plantapp.presentation.plantsearch.PlantDetailViewModel
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.gizemir.plantapp.presentation.garden.GardenViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: Int,
    viewModel: PlantDetailViewModel = hiltViewModel(),
    gardenViewModel: GardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    
    LaunchedEffect(plantId) {
        viewModel.loadPlantDetail(plantId)
    }
    
    MainScaffold(
        navController = navController,
        currentRoute = "plant_detail/$plantId",
        topBarTitle = "Plant Details",
        isBackButtonVisible = true,
        drawerState = drawerState,
        drawerContent = { closeDrawer ->
            AppDrawer(
                currentRoute = "plant_detail/$plantId",
                navController = navController,
                closeDrawer = closeDrawer,
                themeManager = themeManager
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading plant information...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CreativeErrorCard(
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.loadPlantDetail(plantId) },
                        onDismiss = viewModel::clearError
                    )
                }
            }
            
            uiState.plantDetail != null -> {
                CreativePlantDetailContent(
                    plantDetail = uiState.plantDetail!!,
                    isFavorite = uiState.isFavorite,
                    onFavoriteClick = viewModel::toggleFavorite,
                    navController = navController,
                    viewModel = viewModel,
                    gardenViewModel = gardenViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun CreativeErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun CreativePlantDetailContent(
    plantDetail: PlantDetail,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    navController: NavController,
    viewModel: PlantDetailViewModel,
    gardenViewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        CreativeHeroSection(
            plantDetail = plantDetail,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick,
            viewModel = viewModel,
            gardenViewModel = gardenViewModel
        )
        
        PlantCareButtonSection(
            plantName = plantDetail.commonName ?: "Unknown Plant",
            scientificName = plantDetail.scientificName,
            navController = navController
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CreativeInfoCard(
                title = "Botanical Information",
                icon = Icons.Filled.Science,
                iconColor = Color(0xFF4CAF50)
            ) {
                PlantBotanicalInfo(plantDetail = plantDetail)
            }
            
            if (plantDetail.distribution != null &&
                (!plantDetail.distribution!!.native.isNullOrEmpty() || 
                 !plantDetail.distribution!!.introduced.isNullOrEmpty() ||
                 !plantDetail.distribution!!.doubtful.isNullOrEmpty() ||
                 !plantDetail.distribution!!.extinct.isNullOrEmpty())) {
                CreativeInfoCard(
                    title = "Geographic Distribution",
                    icon = Icons.Filled.Public,
                    iconColor = Color(0xFF2196F3)
                ) {
                    PlantDistributionInfo(distribution = plantDetail.distribution!!)
                }
            }
            
            if (!plantDetail.hardinessMapUrl.isNullOrEmpty()) {
                CreativeHardinessMapCard(hardinessMapUrl = plantDetail.hardinessMapUrl!!)
            }
            
            if (plantDetail.careInfo != null &&
                (!plantDetail.careInfo!!.sunlight.isNullOrEmpty() || 
                 !plantDetail.careInfo!!.watering.isNullOrBlank() ||
                 !plantDetail.careInfo!!.wateringDays.isNullOrBlank() ||
                 !plantDetail.careInfo!!.pruningMonths.isNullOrEmpty() ||
                 !plantDetail.careInfo!!.pruningFrequency.isNullOrBlank())) {
                CreativeInfoCard(
                    title = "Care Information",
                    icon = Icons.Filled.Eco,
                    iconColor = Color(0xFF4CAF50)
                ) {
                    PlantCareInfo(careInfo = plantDetail.careInfo!!)
                }
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlantBotanicalInfo(plantDetail: PlantDetail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CreativeInfoRow(
            icon = Icons.Filled.Science,
            label = "Scientific Name",
            value = plantDetail.scientificName,
            iconColor = Color(0xFF4CAF50)
        )
        
        if (plantDetail.genus != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.Category,
                label = "Genus",
                value = plantDetail.genus!!,
                iconColor = Color(0xFF8BC34A)
            )
        }
        
        if (plantDetail.family != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.AccountTree,
                label = "Family",
                value = plantDetail.family!!,
                iconColor = Color(0xFF66BB6A)
            )
        }
        
        if (plantDetail.familyCommonName != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.LocalFlorist,
                label = "Family Common Name",
                value = plantDetail.familyCommonName!!,
                iconColor = Color(0xFF81C784)
            )
        }
        
        if (plantDetail.author != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.Person,
                label = "Author",
                value = plantDetail.author!!,
                iconColor = Color(0xFF7E57C2)
            )
        }
        
        if (plantDetail.year != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.DateRange,
                label = "Year",
                value = plantDetail.year.toString(),
                iconColor = Color(0xFF9C27B0)
            )
        }
        
        if (plantDetail.status != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.Verified,
                label = "Status",
                value = plantDetail.status!!,
                iconColor = Color(0xFF2196F3)
            )
        }
        
        if (plantDetail.rank != null) {
            CreativeInfoRow(
                icon = Icons.Outlined.EmojiEvents,
                label = "Taxonomic Rank",
                value = plantDetail.rank!!,
                iconColor = Color(0xFFFF9800)
            )
        }
        
        if (plantDetail.synonyms != null && plantDetail.synonyms!!.isNotEmpty()) {
            CreativeInfoRow(
                icon = Icons.Outlined.List,
                label = "Synonyms",
                value = plantDetail.synonyms!!.take(3).joinToString(", ") + 
                       if (plantDetail.synonyms!!.size > 3) " ..." else "",
                iconColor = Color(0xFF607D8B)
            )
        }
    }
}

@Composable
private fun PlantDistributionInfo(distribution: PlantDistribution) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (distribution.native != null && distribution.native!!.isNotEmpty()) {
            val nativeRegions = distribution.native!!.take(5).joinToString(", ")
            CreativeInfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Native Distribution",
                value = nativeRegions + if (distribution.native!!.size > 5) " ..." else "",
                iconColor = Color(0xFF4CAF50)
            )
        }
        
        if (distribution.introduced != null && distribution.introduced!!.isNotEmpty()) {
            val introducedRegions = distribution.introduced!!.take(3).joinToString(", ")
            CreativeInfoRow(
                icon = Icons.Filled.Flight,
                label = "Introduced Regions",
                value = introducedRegions + if (distribution.introduced!!.size > 3) " ..." else "",
                iconColor = Color(0xFF2196F3)
            )
        }
        
        if (distribution.doubtful != null && distribution.doubtful!!.isNotEmpty()) {
            val doubtfulRegions = distribution.doubtful!!.take(3).joinToString(", ")
            CreativeInfoRow(
                icon = Icons.Filled.Help,
                label = "Doubtful Regions",
                value = doubtfulRegions,
                iconColor = Color(0xFFFF9800)
            )
        }
        
        if (distribution.extinct != null && distribution.extinct!!.isNotEmpty()) {
            val extinctRegions = distribution.extinct!!.take(3).joinToString(", ")
            CreativeInfoRow(
                icon = Icons.Filled.Cancel,
                label = "Extinct Regions",
                value = extinctRegions,
                iconColor = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun PlantCareInfo(careInfo: com.gizemir.plantapp.domain.model.plant_search.PlantCareInfo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sunlight requirements
        if (careInfo.sunlight != null && careInfo.sunlight!!.isNotEmpty()) {
            CreativeInfoRow(
                icon = Icons.Filled.WbSunny,
                label = "Sunlight",
                value = careInfo.sunlight!!.joinToString(", "),
                iconColor = Color(0xFFFFB74D)
            )
        }
        
        if (careInfo.watering != null) {
            CreativeInfoRow(
                icon = Icons.Filled.Water,
                label = "Watering",
                value = careInfo.watering!!,
                iconColor = Color(0xFF42A5F5)
            )
        }
        
        if (careInfo.wateringDays != null) {
            CreativeInfoRow(
                icon = Icons.Filled.Schedule,
                label = "Watering Frequency",
                value = "Every ${careInfo.wateringDays} days",
                iconColor = Color(0xFF26C6DA)
            )
        }
        
        if (careInfo.pruningMonths != null && careInfo.pruningMonths!!.isNotEmpty()) {
            CreativeInfoRow(
                icon = Icons.Filled.ContentCut,
                label = "Pruning Months",
                value = careInfo.pruningMonths!!.joinToString(", "),
                iconColor = Color(0xFF66BB6A)
            )
        }
        
        if (careInfo.pruningFrequency != null) {
            CreativeInfoRow(
                icon = Icons.Filled.Repeat,
                label = "Pruning Frequency",
                value = careInfo.pruningFrequency!!,
                iconColor = Color(0xFF8BC34A)
            )
        }
    }
}

@Composable
private fun CreativeInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreativeHeroSection(
    plantDetail: PlantDetail,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    viewModel: PlantDetailViewModel,
    gardenViewModel: GardenViewModel
) {
    val isInGarden = gardenViewModel.isPlantInGarden(plantDetail.id.toString())
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // Background Image
        AsyncImage(
            model = plantDetail.imageUrl,
            contentDescription = plantDetail.commonName ?: plantDetail.scientificName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = plantDetail.commonName ?: "Unknown Name",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = plantDetail.scientificName,
                style = MaterialTheme.typography.titleLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            if (plantDetail.family != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = plantDetail.family!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (isInGarden) {
                        gardenViewModel.removePlantFromGarden(plantDetail.id.toString())
                    } else {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        
                        val wateringDaysString = plantDetail.careInfo?.wateringDays
                        val wateringPeriod = wateringDaysString?.split(Regex("\\D+"))?.firstOrNull { it.isNotBlank() }?.toIntOrNull() ?: 3
                        
                        val plantedTime = System.currentTimeMillis()
                        val gardenPlant = com.gizemir.plantapp.domain.model.garden.GardenPlant(
                            userId = userId,
                            plantId = plantDetail.id.toString(),
                            name = plantDetail.commonName ?: plantDetail.scientificName,
                            imageUrl = plantDetail.imageUrl,
                            plantedDate = plantedTime,
                            lastWateredDate = plantedTime,
                            wateringPeriodDays = wateringPeriod
                        )
                        gardenViewModel.addPlantToGarden(gardenPlant)
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (isInGarden) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.9f),
                contentColor = if (isInGarden) Color.White else Color(0xFF4CAF50)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = if (isInGarden) "Remove from Garden" else "Add to Garden",
                    modifier = Modifier.size(24.dp)
                )
            }
            FloatingActionButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(48.dp),
                containerColor = if (isFavorite) Color(0xFFE91E63) else Color.White.copy(alpha = 0.9f),
                contentColor = if (isFavorite) Color.White else Color(0xFFE91E63)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CreativeInfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun CreativeHardinessMapCard(hardinessMapUrl: String) {
    val uriHandler = LocalUriHandler.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF795548).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = Color(0xFF795548),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Hardiness Map",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "View the hardiness map to see which climate zones this plant can thrive in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { uriHandler.openUri(hardinessMapUrl) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF795548)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Hardiness Map")
            }
        }
    }
}

@Composable
private fun PlantCareButtonSection(
    plantName: String,
    scientificName: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = "🤖 AI-Powered Care Guide",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Learn the plant's specific care needs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val cleanPlantName = plantName.replace("+", "")
                    val cleanScientificName = scientificName.replace("+", "")
                    val encodedPlantName = java.net.URLEncoder.encode(cleanPlantName, "UTF-8")
                    val encodedScientificName = java.net.URLEncoder.encode(cleanScientificName, "UTF-8")
                    navController.navigate("plant_care/$encodedPlantName/$encodedScientificName")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                                            "Learn Plant Care",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
