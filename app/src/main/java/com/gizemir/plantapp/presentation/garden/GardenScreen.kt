package com.gizemir.plantapp.presentation.garden

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gizemir.plantapp.domain.model.garden.GardenPlant
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    navController: NavController,
    viewModel: GardenViewModel = hiltViewModel(),
    currentRoute: String = Screen.Garden.route
) {
    val plants by viewModel.gardenPlants.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var plantToDelete by remember { mutableStateOf<GardenPlant?>(null) }

    if (showDeleteDialog && plantToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to remove '${plantToDelete?.name}' from your garden?") },
            confirmButton = {
                Button(
                    onClick = {
                        plantToDelete?.let { viewModel.removePlantFromGarden(it.plantId) }
                        showDeleteDialog = false
                        plantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "My Garden",
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
    ) { padding ->
        if (plants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌱",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Garden is Empty",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start your plant journey by searching and adding plants to your personal garden. Monitor their growth, set watering reminders, and watch them flourish!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { navController.navigate(Screen.PlantSearch.route) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Search Plants", color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants) { plant ->
                    GardenPlantCard(
                        plant = plant,
                        dateFormat = dateFormat,
                        onWater = { viewModel.waterPlant(plant) },
                        onClick = {
                            navController.navigate("garden_plant_detail/${plant.id}")
                        },
                        onDelete = { 
                            plantToDelete = plant
                            showDeleteDialog = true 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GardenPlantCard(
    plant: GardenPlant,
    dateFormat: SimpleDateFormat,
    onWater: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()

    val useMinutesForTesting = false
    
    val nextWaterDate = if (useMinutesForTesting) {
        plant.lastWateredDate + plant.wateringPeriodDays * 60 * 1000L
    } else {
        plant.lastWateredDate + plant.wateringPeriodDays * 24 * 60 * 60 * 1000L
    }
    
    val hasBeenWatered = plant.lastWateredDate > plant.plantedDate
    val needsWater = !hasBeenWatered || now >= nextWaterDate

    val buttonText = if (needsWater) "Water Now" else "Watered"
    val buttonColor = if (needsWater) Color(0xFF4CAF50) else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = plant.imageUrl,
                    contentDescription = plant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (plant.lastWateredDate > plant.plantedDate) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Last Watered: ")
                                }
                                append(dateFormat.format(Date(plant.lastWateredDate)))
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Awaiting first watering",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        val timeUnit = if (useMinutesForTesting) "minutes" else "days"
                        Text(
                            text = "Water in ${plant.wateringPeriodDays} $timeUnit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { if (needsWater) onWater() },
                        enabled = needsWater,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text(buttonText, color = Color.White)
                    }
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from Garden",
                    tint = Color.Red
                )
            }
        }
    }
} 