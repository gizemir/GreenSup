package com.gizemir.plantapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.gizemir.plantapp.presentation.home.HomeScreen
import com.gizemir.plantapp.presentation.plantsearch.PlantSearchScreen
import com.gizemir.plantapp.presentation.plantsearch.PlantDetailScreen
import com.gizemir.plantapp.presentation.splash.SplashScreen
import com.gizemir.plantapp.presentation.auth.login.LoginScreen
import com.gizemir.plantapp.presentation.auth.signup.RegisterScreen
import com.gizemir.plantapp.presentation.auth.profile.SetUpProfileScreen

@Composable
fun PlantAppNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val themeManager = androidx.compose.runtime.remember { 
                com.gizemir.plantapp.presentation.ui.theme.ThemeManager(context) 
            }
            
            HomeScreen(
                navController = navController,
                currentRoute = currentRoute,
                onWeatherClick = { city ->
                    navController.navigate("${Screen.Weather.route}/$city")
                },
                viewModel = hiltViewModel(),
                themeManager = themeManager
            )
        }
        
        composable(Screen.PlantSearch.route) {
            PlantSearchScreen(
                navController = navController,
                currentRoute = currentRoute,
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
        


        

        
        composable(Screen.Article.route) {
            TempScreenPlaceholder("Makaleler Ekranı", currentRoute, navController)
        }
    }
}

@Composable
private fun TempScreenPlaceholder(
    text: String, 
    currentRoute: String, 
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}
