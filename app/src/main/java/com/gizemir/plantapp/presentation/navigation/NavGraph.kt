package com.gizemir.plantapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gizemir.plantapp.presentation.auth.login.LoginScreen
import com.gizemir.plantapp.presentation.profile.UserProfileScreen
import com.gizemir.plantapp.presentation.profile.UserProfileViewModel
import com.gizemir.plantapp.presentation.auth.signup.RegisterScreen
import com.gizemir.plantapp.presentation.splash.SplashScreen
import com.gizemir.plantapp.presentation.home.HomeScreen
import com.gizemir.plantapp.presentation.home.HomeViewModel
import com.gizemir.plantapp.presentation.weather.WeatherDetailScreen
import com.gizemir.plantapp.presentation.weather.WeatherViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerValue // Import DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gizemir.plantapp.presentation.forum.ForumScreen
import com.gizemir.plantapp.presentation.forum.ForumViewModel
import com.gizemir.plantapp.presentation.forum.CreatePostScreen
import com.gizemir.plantapp.presentation.auth.profile.SetUpProfileScreen
import java.util.Locale // Import Locale for string capitalization
import com.gizemir.plantapp.presentation.plantsearch.PlantSearchScreen
import com.gizemir.plantapp.presentation.plantsearch.PlantDetailScreen
import com.gizemir.plantapp.presentation.favorites.FavoritesScreen
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.gizemir.plantapp.presentation.article.ArticleScreen
import com.gizemir.plantapp.presentation.article.ArticleDetailScreen
import com.gizemir.plantapp.presentation.garden.GardenScreen
import com.gizemir.plantapp.presentation.garden.GardenPlantDetailScreen
import com.gizemir.plantapp.presentation.garden.GardenViewModel

@OptIn(ExperimentalMaterial3Api::class) // Added for TopAppBar in CreatePostScreen
@Composable
fun PlantAppNavHost(
    navController: NavHostController,
    themeManager: ThemeManager,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userProfileViewModel: UserProfileViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Profile.route

            UserProfileScreen(
                navController = navController,
                currentRoute = currentRoute,
                viewModel = userProfileViewModel,
                userId = userId
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val selectedCity = navController.currentBackStackEntry?.savedStateHandle?.get<String>("selectedCity")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selectedCity") // Consume the city
            
            val newPostCreated = navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("newPostCreated")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("newPostCreated") // Consume the flag

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route


            LaunchedEffect(selectedCity) {
                selectedCity?.let {
                    if (it.isNotBlank()) {
                        Log.d("Navigation", "Home screen loading city from navigation: $it")
                        homeViewModel.getWeatherByCity(it)
                    }
                }
            }
            
            LaunchedEffect(newPostCreated) {
                if (newPostCreated == true) {
                    Log.d("Navigation", "New post created, refreshing posts in Home")
                    homeViewModel.onNewPostCreated()
                }
            }
            
            HomeScreen(
                onWeatherClick = { city ->
                    navController.navigate(Screen.Weather.withCity(city))
                },
                viewModel = homeViewModel,
                navController = navController,
                currentRoute = currentRoute,
                themeManager = themeManager
            )
        }

        composable(Screen.Forum.route) {
            val forumViewModel: ForumViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Forum.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)


            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Forum",
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
            ) { _ ->
                ForumScreen(
                    navController = navController,
                    viewModel = forumViewModel
                )
            }
        }

        composable(Screen.AddPost.route) {
            val forumViewModel: ForumViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.AddPost.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            
            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Create Post",
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
            ) { _ ->
                CreatePostScreen(
                    navController = navController,
                    viewModel = forumViewModel
                )
            }
        }

        composable(
            route = Screen.Weather.route + "/{city}",
            arguments = listOf(navArgument("city") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityArg = backStackEntry.arguments?.getString("city") ?: "Istanbul"
            val weatherViewModel: WeatherViewModel = hiltViewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: (Screen.Weather.route + "/{city}")
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Weather Forecast",
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
            ) { _ ->
                WeatherDetailScreen(
                    viewModel = weatherViewModel,
                    initialCity = cityArg,
                    onCityChanged = { newCity ->
                        Log.d("Navigation", "Updating city before navigation: $newCity")
                        try {
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedCity", newCity)
                        } catch (e: Exception) {
                            Log.e("Navigation", "Error setting selectedCity: ${e.message}")
                        }
                    },
                    navController = navController
                )
            }
        }
        
        composable(Screen.Weather.route) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Weather.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        
            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Weather",
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
                WeatherDetailScreen(
                    viewModel = hiltViewModel(),
                    initialCity = "Istanbul",
                    onCityChanged = { newCity ->
                        try {
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedCity", newCity)
                        } catch (e: Exception) {
                            Log.e("Navigation", "Error setting selectedCity: ${e.message}")
                        }
                    },
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
        }

        composable(Screen.PlantSearch.route) {
            PlantSearchScreen(
                navController = navController,
                currentRoute = Screen.PlantSearch.route,
                viewModel = hiltViewModel()
            )
        }
        
        composable(
            route = "${Screen.PlantDetail.route}/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            PlantDetailScreen(navController = navController, plantId = plantId)
        }

        composable(
            route = "${Screen.PlantCare.route}/{plantName}/{scientificName}",
            arguments = listOf(
                navArgument("plantName") { type = NavType.StringType },
                navArgument("scientificName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val plantName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("plantName") ?: "", 
                "UTF-8"
            )
            val scientificName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("scientificName") ?: "", 
                "UTF-8"
            )
            com.gizemir.plantapp.presentation.plant_care.PlantCareScreen(
                plantName = plantName,
                scientificName = scientificName,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                navController = navController,
                currentRoute = Screen.Favorites.route,
                viewModel = hiltViewModel(),
                themeManager = themeManager
            )
        }

        composable(Screen.AnalysisHistory.route) {
            com.gizemir.plantapp.presentation.analysis_history.AnalysisHistoryScreen(
                navController = navController
            )
        }

        composable(Screen.Forum.route) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Forum.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Forum",
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
                ForumScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(Screen.AddPost.route) {
            CreatePostScreen(
                navController = navController,
                currentRoute = Screen.AddPost.route,
                viewModel = hiltViewModel()
            )
        }


        composable(Screen.Article.route) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Article.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            MainScaffold(
                navController = navController,
                currentRoute = currentRoute,
                topBarTitle = "Agriculture Articles",
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
                ArticleScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(
            route = "article_detail/{articleUrl}",
            arguments = listOf(navArgument("articleUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            ArticleDetailScreen(
                navController = navController,
                articleUrl = articleUrl
            )
        }

        composable(Screen.SetUpProfile.route) {
            SetUpProfileScreen(navController = navController)
        }

        composable(Screen.Garden.route) {
            GardenScreen(navController = navController)
        }

        composable(Screen.DetectDisease.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.DetectDisease.route)
            }
            com.gizemir.plantapp.presentation.plant_analysis.PlantAnalysisScreen(
                navController = navController,
                currentRoute = Screen.DetectDisease.route,
                viewModel = hiltViewModel(parentEntry)
            )
        }
        
        composable(Screen.DetectResult.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.DetectDisease.route)
            }
            com.gizemir.plantapp.presentation.plant_analysis.DetectResultScreen(
                navController = navController,
                currentRoute = Screen.DetectResult.route,
                viewModel = hiltViewModel(parentEntry)
            )
        }
        
        composable(
            route = "${Screen.DetectResult.route}/{detectionId}",
            arguments = listOf(navArgument("detectionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val detectionId = backStackEntry.arguments?.getString("detectionId") ?: ""
            val viewModel = hiltViewModel<com.gizemir.plantapp.presentation.plant_analysis.PlantAnalysisViewModel>()
            
            LaunchedEffect(detectionId) {
                if (detectionId.isNotEmpty()) {
                    viewModel.loadDetectionFromHistory(detectionId)
                }
            }
            
            com.gizemir.plantapp.presentation.plant_analysis.DetectResultScreen(
                navController = navController,
                currentRoute = Screen.DetectResult.route,
                viewModel = viewModel
            )
        }

        composable(
            route = "${Screen.PlantIdDetail.route}/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            
            val viewModel = hiltViewModel<com.gizemir.plantapp.presentation.plant_analysis.PlantAnalysisViewModel>()
            
            com.gizemir.plantapp.presentation.plant_analysis.PlantIdDetailScreen(
                navController = navController,
                plantId = plantId,
                currentRoute = Screen.PlantIdDetail.route,
                viewModel = viewModel
            )
        }

        composable(
            route = "${Screen.DiseaseDetailPage.route}/{diseaseId}",
            arguments = listOf(navArgument("diseaseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getString("diseaseId") ?: ""
            
            val viewModel = hiltViewModel<com.gizemir.plantapp.presentation.plant_analysis.PlantAnalysisViewModel>()
            
            com.gizemir.plantapp.presentation.plant_analysis.DiseaseDetailPageScreen(
                navController = navController,
                diseaseId = diseaseId,
                currentRoute = Screen.DiseaseDetailPage.route,
                viewModel = viewModel
            )
        }

        composable(
            route = "garden_plant_detail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId")
            if (plantId != null) {
                GardenPlantDetailScreen(plantId = plantId, navController = navController)
            }
        }
    }
}

@Composable
fun TempScreen(text: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        Text(text)
    }
}

