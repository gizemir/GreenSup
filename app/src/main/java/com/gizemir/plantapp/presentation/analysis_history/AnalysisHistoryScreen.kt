package com.gizemir.plantapp.presentation.analysis_history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistoryScreen(
    navController: NavController,
    viewModel: AnalysisHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    MainScaffold(
        navController = navController,
        currentRoute = Screen.AnalysisHistory.route,
        topBarTitle = "Analysis History",
        isBackButtonVisible = true,
        drawerState = drawerState,
        drawerContent = { closeDrawer ->
            AppDrawer(
                currentRoute = Screen.AnalysisHistory.route,
                navController = navController,
                closeDrawer = closeDrawer,
                themeManager = themeManager
            )
        },
                                topBarActions = {
            if (uiState.currentAnalyses.isNotEmpty()) {
                IconButton(
                    onClick = { showDeleteAllDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete All",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "An error occurred",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { viewModel.refreshHistory() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
                
                uiState.allAnalyses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Empty history",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No analysis history yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Your plant analyses will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Tab Row
                        TabRow(
                            selectedTabIndex = when (uiState.selectedCategory) {
                                AnalysisCategory.HEALTHY -> 0
                                AnalysisCategory.DISEASED -> 1
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Tab(
                                selected = uiState.selectedCategory == AnalysisCategory.HEALTHY,
                                onClick = { viewModel.selectCategory(AnalysisCategory.HEALTHY) },
                                text = { 
                                    Text(
                                        text = "Healthy (${uiState.healthyAnalyses.size})",
                                        fontWeight = if (uiState.selectedCategory == AnalysisCategory.HEALTHY) 
                                            FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            )
                            Tab(
                                selected = uiState.selectedCategory == AnalysisCategory.DISEASED,
                                onClick = { viewModel.selectCategory(AnalysisCategory.DISEASED) },
                                text = { 
                                    Text(
                                        text = "Diseased (${uiState.diseasedAnalyses.size})",
                                        fontWeight = if (uiState.selectedCategory == AnalysisCategory.DISEASED) 
                                            FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                        
                        if (uiState.currentAnalyses.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = when (uiState.selectedCategory) {
                                            AnalysisCategory.HEALTHY -> Icons.Default.Check
                                            AnalysisCategory.DISEASED -> Icons.Default.Warning
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = when (uiState.selectedCategory) {
                                            AnalysisCategory.HEALTHY -> "No healthy plant analyses yet"
                                            AnalysisCategory.DISEASED -> "No diseased plant analyses yet"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = when (uiState.selectedCategory) {
                                            AnalysisCategory.HEALTHY -> "Analyses of healthy plants will appear here"
                                            AnalysisCategory.DISEASED -> "Analyses of diseased plants will appear here"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else {
                                                         LazyColumn(
                                 modifier = Modifier.fillMaxSize(),
                                 contentPadding = PaddingValues(16.dp),
                                 verticalArrangement = Arrangement.spacedBy(12.dp)
                             ) {
                                 items(uiState.currentAnalyses) { analysis ->
                                    AnalysisHistoryItem(
                                        analysis = analysis,
                                        onClick = {
                                            navController.navigate("${Screen.DetectResult.route}/${analysis.id}")
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteAnalysis(analysis.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Delete All History") },
                text = { 
                    val categoryText = when (uiState.selectedCategory) {
                        AnalysisCategory.HEALTHY -> "healthy plant analyses"
                        AnalysisCategory.DISEASED -> "diseased plant analyses"
                    }
                    Text("Are you sure you want to delete all $categoryText? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearCategoryHistory()
                            showDeleteAllDialog = false
                        }
                    ) {
                        Text("Delete", color = Color(0xFFF44336))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AnalysisHistoryItem(
    analysis: DiseaseDetection,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageToShow = remember(analysis.imageUri) {
                val imageUri = com.gizemir.plantapp.core.util.ImageUtils.filePathToUri(analysis.imageUri)
                imageUri ?: run {
                    analysis.plantSuggestions.firstOrNull()?.similarImages?.firstOrNull()?.url?.let {
                        it 
                    } ?: analysis.diseases.firstOrNull()?.similarImages?.firstOrNull()?.url
                }
            }
            
            AsyncImage(
                model = imageToShow,
                contentDescription = "Plant image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val plantName = if (analysis.plantSuggestions.isNotEmpty()) {
                    analysis.plantSuggestions.first().commonNames.firstOrNull() 
                        ?: analysis.plantSuggestions.first().name
                } else {
                    "Unknown Plant"
                }
                
                Text(
                    text = plantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                Text(
                    text = dateFormat.format(Date(analysis.analyzedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (analysis.isPlant) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = "Plant status",
                            tint = if (analysis.isPlant) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(3.dp))
                        
                        Text(
                            text = if (analysis.isPlant) "Plant" else "Not Plant",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (analysis.isPlant) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                    
                    if (analysis.isPlant) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (analysis.isHealthy) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = "Health status",
                                tint = if (analysis.isHealthy) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(3.dp))
                            
                            Text(
                                text = if (analysis.isHealthy) "Healthy" else "Disease",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (analysis.isHealthy) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (analysis.plantSuggestions.isNotEmpty()) {
                        FilterChip(
                            onClick = {  },
                            label = {
                                Text(
                                    text = "${analysis.plantSuggestions.size} Plant${if (analysis.plantSuggestions.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = false,
                            modifier = Modifier.height(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                labelColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                    
                    if (analysis.diseases.isNotEmpty()) {
                        FilterChip(
                            onClick = {  },
                            label = {
                                Text(
                                    text = "${analysis.diseases.size} Disease${if (analysis.diseases.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = false,
                            modifier = Modifier.height(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.15f),
                                labelColor = Color(0xFFFF9800)
                            )
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Analysis") },
            text = { Text("Are you sure you want to delete this analysis?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

 