package com.gizemir.plantapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.gizemir.plantapp.presentation.navigation.PlantAppNavHost
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.PlantAppTheme
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themeManager.isDarkModeFlow.collectAsState(initial = false)
            
            LaunchedEffect(isDarkMode) {
                themeManager.updateCurrentMode(isDarkMode)
            }
            
            PlantAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(themeManager = themeManager)
                }

            }
        }
    }
}

@Composable
fun AppNavigation(themeManager: ThemeManager) {
    val navController = rememberNavController()
    
    PlantAppNavHost(
        navController = navController,
        themeManager = themeManager,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Splash.route
        }
    )
}
