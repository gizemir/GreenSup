package com.gizemir.plantapp.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gizemir.plantapp.presentation.weather.WeatherCard
import com.gizemir.plantapp.presentation.forum.components.PostItem
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWeatherClick: (String) -> Unit,
    viewModel: HomeViewModel,
    navController: NavController,
    currentRoute: String,
    themeManager: ThemeManager
) {
    val temperature by viewModel.temperature.collectAsState()
    val city by viewModel.city.collectAsState()
    val description by viewModel.description.collectAsState()
    val iconUrl by viewModel.iconUrl.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val windSpeed by viewModel.windSpeed.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val date = remember {
        SimpleDateFormat("dd MMMM", Locale.getDefault()).format(Date())
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "GreenSup",
        topBarActions = {
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddPost.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Post",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            WeatherCard(
                city = city,
                date = date,
                temperature = temperature,
                description = description,
                iconUrl = iconUrl,
                humidity = humidity,
                windSpeed = windSpeed,
                onClick = { onWeatherClick(city.ifBlank { "Istanbul" }) }
            )

            Spacer(modifier = Modifier.height(16.dp))


            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                posts.isEmpty() -> {
                    Text("No posts yet. Be the first to share something!")
                }
                else -> {
                    Column {
                        posts.take(2).forEach { post ->
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.likePost(post) },
                                onCommentClick = { comment -> viewModel.addComment(post, comment) },
                                onUserProfileClick = { navController.navigate("${Screen.Profile.route}/${post.userId}") },
                                showCommentActions = true,
                                currentUserId = currentUserId,
                                onDeleteCommentClick = { comment -> 
                                    viewModel.deleteComment(comment)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (posts.size > 2) {
                            TextButton(
                                onClick = { navController.navigate(Screen.Forum.route) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("See all posts")
                            }
                        }
                    }
                }
            }
        }
    }


    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {

            viewModel.clearError()
        }
    }
}

