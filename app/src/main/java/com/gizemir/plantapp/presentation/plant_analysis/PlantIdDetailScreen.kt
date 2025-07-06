package com.gizemir.plantapp.presentation.plant_analysis

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gizemir.plantapp.domain.model.plant_analysis.PlantSuggestion
import com.gizemir.plantapp.domain.model.plant_analysis.Taxonomy
import com.gizemir.plantapp.domain.model.plant_analysis.Watering
import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.model.favorite.FavoriteSource
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.gizemir.plantapp.presentation.garden.GardenViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantIdDetailScreen(
    navController: NavController,
    plantId: String,
    currentRoute: String = Screen.PlantIdDetail.route,
    viewModel: PlantAnalysisViewModel = hiltViewModel(),
    gardenViewModel: GardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }

    var plant by remember { mutableStateOf<PlantSuggestion?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(plantId, uiState.diseaseDetection) {
        val foundPlant = uiState.diseaseDetection?.plantSuggestions?.find { it.id == plantId }
        if (foundPlant != null) {
            plant = foundPlant
        } else {
            isLoading = true
            viewModel.loadPlantFromHistory(plantId) { loadedPlant: PlantSuggestion? ->
                plant = loadedPlant
                isLoading = false
            }
        }
    }

    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "Plant Details",
        isBackButtonVisible = true,
        drawerState = drawerState,
        drawerContent = { closeDrawer ->
            AppDrawer(
                currentRoute = currentRoute,
                navController = navController,
                closeDrawer = closeDrawer,
                themeManager = themeManager
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            plant != null -> {
                PlantDetailContent(
                    plant = plant!!,
                    navController = navController,
                    viewModel = viewModel,
                    gardenViewModel = gardenViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                PlantNotFoundContent(
                    onGoBack = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PlantDetailContent(
    plant: PlantSuggestion,
    navController: NavController,
    viewModel: PlantAnalysisViewModel,
    gardenViewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PlantImageCard(
                plant = plant,
                viewModel = viewModel,
                gardenViewModel = gardenViewModel
            )
        }

        item {
            ConfidenceCard(probability = plant.probability)
        }

        item {
            AiPlantCareCard(
                plantName = plant.commonNames.firstOrNull() ?: plant.name,
                scientificName = plant.nameAuthority ?: plant.name,
                navController = navController
            )
        }

        item {
            BasicInformationCard(plant = plant)
        }


        plant.taxonomy?.let { taxonomy ->
            item {
                TaxonomyCard(taxonomy = taxonomy)
            }
        }

        plant.watering?.let { watering ->
            item {
                WateringCard(watering = watering)
            }
        }

        if (plant.edibleParts.isNotEmpty()) {
            item {
                EdiblePartsCard(edibleParts = plant.edibleParts)
            }
        }

        if (plant.propagationMethods.isNotEmpty()) {
            item {
                PropagationCard(propagationMethods = plant.propagationMethods)
            }
        }

        plant.wikiDescription?.let { description ->
            item {
                DescriptionCard(description = description.value)
            }
        }
        
        if (plant.similarImages.isNotEmpty()) {
            item {
                PlantSimilarImagesGalleryCard(images = plant.similarImages)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantImageCard(
    plant: PlantSuggestion,
    viewModel: PlantAnalysisViewModel,
    gardenViewModel: GardenViewModel
) {
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    LaunchedEffect(plant.id) {
        isFavorite = viewModel.checkIfFavorite(plant.id)
    }
    
    val isInGarden = gardenViewModel.isPlantInGarden(plant.id)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val apiImageUrl = plant.similarImages.firstOrNull()?.url
            val userImageUri = viewModel.uiState.value.selectedImageUri?.toString()
            val imageToShow = apiImageUrl ?: userImageUri
            
            if (!imageToShow.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = plant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (apiImageUrl != null) "Plant Example" else "Your Plant",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (isInGarden) {
                                    gardenViewModel.removePlantFromGarden(plant.id)
                                } else {
                                    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    val wateringPeriod = plant.watering?.min ?: 3
                                    val plantedTime = System.currentTimeMillis()
                                    val gardenPlant = com.gizemir.plantapp.domain.model.garden.GardenPlant(
                                        userId = userId,
                                        plantId = plant.id,
                                        name = plant.commonNames.firstOrNull() ?: plant.name,
                                        imageUrl = plant.similarImages.firstOrNull()?.url,
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
                            onClick = {
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(plant.id)
                                    isFavorite = false
                                } else {
                                    val plantName = plant.commonNames.firstOrNull() ?: plant.name
                                    val scientificName = plant.nameAuthority ?: plant.name
                                    val imageUrl = imageToShow
                                    viewModel.addToFavorites(plant.id, plantName, scientificName, imageUrl)
                                    isFavorite = true
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = if (isFavorite) Color(0xFFE91E63) else Color.White.copy(alpha = 0.9f),
                            contentColor = if (isFavorite) Color.White else Color(0xFFE91E63)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = plant.commonNames.firstOrNull() ?: plant.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                if (plant.nameAuthority != null && plant.nameAuthority != plant.name) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plant.nameAuthority!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfidenceCard(
    probability: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(probability * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            LinearProgressIndicator(
                progress = probability.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiPlantCareCard(
    plantName: String,
    scientificName: String,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(16.dp)
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
                    val encodedPlantName = URLEncoder.encode(cleanPlantName, "UTF-8")
                    val encodedScientificName = URLEncoder.encode(cleanScientificName, "UTF-8")
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

@Composable
private fun PlantImagesGalleryCard(images: List<com.gizemir.plantapp.domain.model.plant_analysis.SimilarImage>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Plant Image Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { image ->
                    AsyncImage(
                        model = image.url,
                        contentDescription = "Plant example",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantSimilarImagesGalleryCard(images: List<com.gizemir.plantapp.domain.model.plant_analysis.SimilarImage>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Similar Plant Images",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { image ->
                    AsyncImage(
                        model = image.url,
                        contentDescription = "Similar plant example",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInformationCard(
    plant: PlantSuggestion
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (plant.commonNames.isNotEmpty()) {
                InfoRow(
                    icon = Icons.Default.Label,
                    title = "Common Names",
                    content = plant.commonNames.joinToString(", ")
                )
            }

            plant.nameAuthority?.let { authority ->
                InfoRow(
                    icon = Icons.Default.Person,
                    title = "Name Authority",
                    content = authority
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxonomyCard(
    taxonomy: Taxonomy
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Taxonomy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            taxonomy.kingdom?.let { 
                InfoRow(Icons.Default.Public, "Kingdom", it)
            }
            taxonomy.phylum?.let { 
                InfoRow(Icons.Default.Category, "Phylum", it)
            }
            taxonomy.className?.let { 
                InfoRow(Icons.Default.Class, "Class", it)
            }
            taxonomy.order?.let { 
                InfoRow(Icons.Default.Sort, "Order", it)
            }
            taxonomy.family?.let { 
                InfoRow(Icons.Default.Group, "Family", it)
            }
            taxonomy.genus?.let { 
                InfoRow(Icons.Default.LocalFlorist, "Genus", it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WateringCard(
    watering: Watering
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Watering Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            watering.min?.let { min ->
                InfoRow(
                    icon = Icons.Default.WaterDrop,
                    title = "Minimum Watering",
                    content = "$min days interval"
                )
            }

            watering.max?.let { max ->
                InfoRow(
                    icon = Icons.Default.WaterDrop,
                    title = "Maximum Watering", 
                    content = "$max days interval"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EdiblePartsCard(
    edibleParts: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Edible Parts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            InfoRow(
                icon = Icons.Default.Restaurant,
                title = "Parts",
                content = edibleParts.joinToString(", ")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropagationCard(
    propagationMethods: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Propagation Methods",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            InfoRow(
                icon = Icons.Default.Agriculture,
                title = "Methods",
                content = propagationMethods.joinToString(", ")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DescriptionCard(
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PlantNotFoundContent(
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Plant Not Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "The selected plant details could not be found.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(onClick = onGoBack) {
                Text("Go Back")
            }
        }
    }
} 