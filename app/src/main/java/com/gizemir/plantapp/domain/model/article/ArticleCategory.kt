package com.gizemir.plantapp.domain.model.article

data class ArticleCategory(
    val id: String,
    val name: String,
    val displayName: String,
    val keywords: List<String> = emptyList()
)

object ArticleCategories {
    val categories = listOf(
        ArticleCategory(
            id = "agriculture",
            name = "agriculture",
            displayName = "Agriculture",
            keywords = listOf("agriculture", "farming techniques", "crop production", "agricultural innovation", "farm management")
        ),
        ArticleCategory(
            id = "organic_farming",
            name = "organic farming",
            displayName = "Organic Farming",
            keywords = listOf("organic farming", "organic agriculture", "pesticide-free", "organic certification", "natural farming")
        ),
        ArticleCategory(
            id = "plant_diseases",
            name = "plant diseases",
            displayName = "Plant Diseases",
            keywords = listOf("plant disease", "crop disease", "plant pathogen", "fungal disease", "plant protection")
        ),
        ArticleCategory(
            id = "soil_health",
            name = "soil health",
            displayName = "Soil Health",
            keywords = listOf("soil health", "soil fertility", "soil nutrition", "soil management", "soil conservation")
        ),
        ArticleCategory(
            id = "composting",
            name = "composting",
            displayName = "Composting",
            keywords = listOf("composting", "organic compost", "compost making", "vermicomposting", "organic fertilizer")
        ),
        ArticleCategory(
            id = "greenhouse_farming",
            name = "greenhouse farming",
            displayName = "Greenhouse Farming",
            keywords = listOf("greenhouse", "controlled environment", "greenhouse cultivation", "indoor farming", "hydroponic")
        ),
        ArticleCategory(
            id = "permaculture",
            name = "permaculture",
            displayName = "Permaculture",
            keywords = listOf("permaculture", "sustainable farming", "regenerative agriculture", "ecological farming", "agroecology")
        ),
        ArticleCategory(
            id = "gardening",
            name = "gardening",
            displayName = "Gardening",
            keywords = listOf("gardening", "home garden", "vegetable garden", "garden care", "plant care")
        ),
        ArticleCategory(
            id = "irrigation",
            name = "irrigation",
            displayName = "Irrigation",
            keywords = listOf("irrigation", "drip irrigation", "water management", "irrigation system", "agricultural water")
        ),
        ArticleCategory(
            id = "plant_nutrition",
            name = "plant nutrition",
            displayName = "Plant Nutrition",
            keywords = listOf("plant nutrition", "fertilizer", "nutrient management", "plant feeding", "crop nutrition")
        )
    )
} 