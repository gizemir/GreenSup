package com.gizemir.plantapp.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.favorites.components.FavoritePlantItem
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    currentRoute: String = Screen.Favorites.route,
    viewModel: FavoritesViewModel = hiltViewModel(),
    themeManager: ThemeManager
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showClearDialog by remember { mutableStateOf(false) }

    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "My Favorites",
        isBackButtonVisible = true,
        drawerState = drawerState,
        topBarActions = {
            if (uiState.currentFavorites.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = Color.White
                    )
                }
            }
        },
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
                            text = "Loading favorites...",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = viewModel::clearError
                            ) { 
                                Text("OK")
                            }
                        }
                    }
                }
            }
            
            uiState.allFavorites.isEmpty() -> {
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
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No favorites yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can add plants to your favorites from Plant Search or add diseases from Analysis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Screen.PlantSearch.route) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Search Plants")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(Screen.DetectDisease.route) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze Disease")
                            }
                        }
                    }
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabRow(
                        selectedTabIndex = when (uiState.selectedCategory) {
                            FavoriteCategory.PLANTS -> 0
                            FavoriteCategory.DISEASES -> 1
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Tab(
                            selected = uiState.selectedCategory == FavoriteCategory.PLANTS,
                            onClick = { viewModel.selectCategory(FavoriteCategory.PLANTS) },
                            text = { 
                                Text(
                                    text = "Plants (${uiState.plantFavorites.size})",
                                    fontWeight = if (uiState.selectedCategory == FavoriteCategory.PLANTS) 
                                        FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LocalFlorist,
                                    contentDescription = null
                                )
                            }
                        )
                        Tab(
                            selected = uiState.selectedCategory == FavoriteCategory.DISEASES,
                            onClick = { viewModel.selectCategory(FavoriteCategory.DISEASES) },
                            text = { 
                                Text(
                                    text = "Diseases (${uiState.diseaseFavorites.size})",
                                    fontWeight = if (uiState.selectedCategory == FavoriteCategory.DISEASES) 
                                        FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    
                    if (uiState.currentFavorites.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (uiState.selectedCategory) {
                                        FavoriteCategory.PLANTS -> Icons.Default.LocalFlorist
                                        FavoriteCategory.DISEASES -> Icons.Default.BugReport
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (uiState.selectedCategory) {
                                        FavoriteCategory.PLANTS -> "No favorite plants yet"
                                        FavoriteCategory.DISEASES -> "No favorite diseases yet"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (uiState.selectedCategory) {
                                        FavoriteCategory.PLANTS -> "Add plants from Plant Search or Identify Plant"
                                        FavoriteCategory.DISEASES -> "Add diseases from Disease Analysis"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        when (uiState.selectedCategory) {
                                            FavoriteCategory.PLANTS -> navController.navigate(Screen.PlantSearch.route)
                                            FavoriteCategory.DISEASES -> navController.navigate(Screen.DetectDisease.route)
                                        }
                                    }
                                ) {
                                    Text(
                                        when (uiState.selectedCategory) {
                                            FavoriteCategory.PLANTS -> "Search Plants"
                                            FavoriteCategory.DISEASES -> "Analyze Disease"
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(uiState.currentFavorites) { favoritePlant ->
                                FavoritePlantItem(
                                    favoritePlant = favoritePlant,
                                    onItemClick = {
                                        when (favoritePlant.source) {
                                            com.gizemir.plantapp.domain.model.favorite.FavoriteSource.PLANT_SEARCH -> {
                                                navController.navigate("${Screen.PlantDetail.route}/${favoritePlant.plantId}")
                                            }
                                            com.gizemir.plantapp.domain.model.favorite.FavoriteSource.PLANT_ID_DETAIL -> {
                                                // Plant ID Detail sayfasından eklenen favoriler için
                                                navController.navigate("${Screen.PlantIdDetail.route}/${favoritePlant.plantId}")
                                            }
                                            com.gizemir.plantapp.domain.model.favorite.FavoriteSource.DISEASE_ANALYSIS -> {
                                                // Disease analysis sonuçlarına git
                                                navController.navigate("${Screen.DetectResult.route}/${favoritePlant.scientificName}")
                                            }
                                        }
                                    },
                                    onRemoveClick = {
                                        viewModel.removeFromFavorites(favoritePlant.plantId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear All Favorites")
            },
            text = {
                val categoryText = when (uiState.selectedCategory) {
                    FavoriteCategory.PLANTS -> "plants"
                    FavoriteCategory.DISEASES -> "diseases"
                }
                Text("Are you sure you want to remove all favorite $categoryText? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearDialog = false
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

