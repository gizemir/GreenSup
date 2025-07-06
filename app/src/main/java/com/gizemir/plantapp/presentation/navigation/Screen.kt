package com.gizemir.plantapp.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Weather : Screen("weather") {
        fun withCity(city: String) = "weather/$city"
    }
    object Forum : Screen("forum")
    object AddPost : Screen("add_post")
    object SetUpProfile : Screen("set_up_profile")
    object Favorites : Screen("favorites")
    object AnalysisHistory : Screen("analysis_history")
    object PlantSearch : Screen("plant_search")
    object PlantDetail : Screen("plant_detail")
    object PlantCare : Screen("plant_care")

    object DetectDisease : Screen("plant_analysis")
    object DetectResult : Screen("detect_result")
    object PlantIdDetail : Screen("plant_id_detail")
    object DiseaseDetailPage : Screen("disease_detail_page")

    object Article : Screen("article")

    object Garden : Screen("garden_screen")

}