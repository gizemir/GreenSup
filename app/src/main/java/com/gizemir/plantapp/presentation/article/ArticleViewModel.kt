package com.gizemir.plantapp.presentation.article

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.article.Article
import com.gizemir.plantapp.domain.model.article.ArticleCategories
import com.gizemir.plantapp.domain.model.article.ArticleCategory
import com.gizemir.plantapp.domain.repository.article.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private val allCategories = ArticleCategories.categories
    
    private val _articleCache = mutableMapOf<String, Article>()
    val articleCache: Map<String, Article> get() = _articleCache.toMap()
    
    private var lastLoadTime = 0L
    private val CACHE_DURATION = 1000 * 60 * 10

    init {
        loadArticlesByCategory()
    }
    
    private fun cacheArticles(articles: List<Article>) {
        articles.forEach { article ->
            _articleCache[article.url] = article
        }
    }
    
    fun getArticleByUrl(url: String): Article? {
        return _articleCache[url]
    }

    fun loadArticlesByCategory(selectedCategory: ArticleCategory? = null) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLoadTime < CACHE_DURATION && _articleCache.isNotEmpty()) {
                Log.d("ArticleViewModel", "Using cached articles, skipping API call")
                val cachedArticles = _articleCache.values.toList()
                _uiState.value = _uiState.value.copy(
                    articles = cachedArticles,
                    selectedCategory = selectedCategory,
                    isLoading = false
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = selectedCategory)
            
            try {
                val allArticles = mutableListOf<Article>()
                
                if (selectedCategory != null) {
                    selectedCategory.keywords.forEach { keyword ->
                        val plantSpecificQuery = "$keyword plants OR $keyword agriculture OR $keyword farming"
                        val result1 = articleRepository.searchArticles(
                            query = plantSpecificQuery,
                            language = "en",
                            pageSize = 8,
                            page = 1
                        )
                        result1.getOrNull()?.let { articles ->
                            allArticles.addAll(articles)
                        }
                        
                        val result2 = articleRepository.searchArticles(
                            query = plantSpecificQuery,
                            language = "en",
                            pageSize = 8,
                            page = 2
                        )
                        result2.getOrNull()?.let { articles ->
                            allArticles.addAll(articles)
                        }
                    }
                    
                    if (allArticles.size < 10) {
                        selectedCategory.keywords.take(3).forEach { keyword ->
                            val result = articleRepository.searchArticles(
                                query = keyword,
                                language = "en",
                                pageSize = 12,
                                page = 1
                            )
                            result.getOrNull()?.let { articles ->
                                val filteredArticles = articles.filter { article ->
                                    val content = "${article.title} ${article.description} ${article.content}".lowercase()
                                    val plantTerms = listOf("plant", "agriculture", "farming", "crop", "garden", "soil", 
                                                          "greenhouse", "irrigation", "fertilizer", "compost", "seed", 
                                                          "harvest", "cultivation", "botanical", "horticulture")
                                    plantTerms.any { term -> content.contains(term) }
                                }
                                allArticles.addAll(filteredArticles)
                            }
                        }
                    }
                } else {
                    val plantTopics = listOf(
                        "agriculture farming",
                        "plant diseases", 
                        "greenhouse farming",
                        "organic agriculture",
                        "sustainable farming",
                        "crop management",
                        "soil health",
                        "irrigation systems",
                        
                        "vegetable gardening",
                        "fruit cultivation",
                        "tomato growing",
                        "herb gardening",
                        
                        "hydroponic farming",
                        "vertical farming",
                        "smart agriculture",
                        "precision farming",
                        
                        "permaculture",
                        "composting methods",
                        "natural fertilizers",
                        "pest control organic"
                    )
                    
                    plantTopics.forEach { topic ->
                        val result = articleRepository.searchArticles(
                            query = topic,
                            language = "en",
                            pageSize = 8,
                            page = 1
                        )
                        result.getOrNull()?.let { articles ->

                            val filteredArticles = articles.filter { article ->
                                val content = "${article.title} ${article.description}".lowercase()
                                val plantKeywords = listOf("plant", "agriculture", "farming", "crop", "garden", 
                                                         "botanical", "horticulture", "cultivation", "greenhouse")
                                val irrelevantKeywords = listOf("politics", "sports", "entertainment", "celebrity", 
                                                               "movie", "music", "fashion", "cryptocurrency", "stock")
                                
                                plantKeywords.any { keyword -> content.contains(keyword) } &&
                                !irrelevantKeywords.any { keyword -> content.contains(keyword) }
                            }
                            allArticles.addAll(filteredArticles)
                        }
                    }
                }
                

                val uniqueArticles = allArticles
                    .distinctBy { it.url }
                    .filter { article ->
                        val content = "${article.title} ${article.description} ${article.content}".lowercase()
                        val plantTerms = listOf("plant", "agriculture", "farming", "crop", "garden", "soil",
                                              "greenhouse", "irrigation", "fertilizer", "compost", "botanical",
                                              "horticulture", "cultivation", "harvest", "seed")
                        val exclusionTerms = listOf("politics", "sports", "entertainment", "celebrity", "movie", 
                                                   "music", "fashion", "cryptocurrency", "bitcoin", "stock")
                        
                        article.title.isNotEmpty() && 
                        article.description?.isNotEmpty() == true &&
                        plantTerms.any { term -> content.contains(term) } &&
                        !exclusionTerms.any { term -> content.contains(term) }
                    }
                    .sortedByDescending { it.publishedAt }
                    .take(if (selectedCategory != null) 40 else 50)
                
                _uiState.value = _uiState.value.copy(
                    articles = uniqueArticles,
                    isLoading = false
                )
                
                cacheArticles(uniqueArticles)
                
                lastLoadTime = System.currentTimeMillis()
            } catch (e: Exception) {
                if (e.message?.contains("Rate limit") == true) {
                    Log.w("ArticleViewModel", "Rate limit reached, using demo articles")
                    val demoArticles = getDemoArticles()
                    Log.d("ArticleViewModel", "Generated ${demoArticles.size} demo articles")
                    demoArticles.forEach { article ->
                        Log.d("ArticleViewModel", "Demo article: ${article.title}")
                    }
                    _uiState.value = _uiState.value.copy(
                        articles = demoArticles,
                        isLoading = false,
                        error = "Demo articles loaded due to rate limit. Real articles will be available after 24 hours."
                    )
                    cacheArticles(demoArticles)
                    lastLoadTime = System.currentTimeMillis()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun searchArticles(query: String) {
        if (query.isBlank()) {
            loadArticlesByCategory()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val searchResults = mutableListOf<Article>()
                
                val primaryResult1 = articleRepository.searchArticles(
                    query = query,
                    language = "en",
                    pageSize = 10,
                    page = 1
                )
                primaryResult1.getOrNull()?.let { articles ->
                    searchResults.addAll(articles)
                }
                
                val primaryResult2 = articleRepository.searchArticles(
                    query = query,
                    language = "en",
                    pageSize = 10,
                    page = 2
                )
                primaryResult2.getOrNull()?.let { articles ->
                    searchResults.addAll(articles)
                }
                
                if (searchResults.size < 15) { // 10'dan 15'e çıkardık
                    val plantRelatedTerms = listOf("plant", "agriculture", "farming", "garden", "cultivation")
                    val hasPlantTerms = plantRelatedTerms.any { term -> 
                        query.lowercase().contains(term) 
                    }
                    
                    if (!hasPlantTerms) {
                        val enhancedQueries = listOf(
                            "$query plants",
                            "$query agriculture",
                            "$query farming",
                            "plant $query",
                            "agriculture $query",
                            "$query cultivation",
                            "organic $query",
                            "sustainable $query"
                        )
                        
                        enhancedQueries.take(4).forEach { enhancedQuery ->
                            val result = articleRepository.searchArticles(
                                query = enhancedQuery,
                                language = "en",
                                pageSize = 6,
                                page = 1
                            )
                            result.getOrNull()?.let { articles ->
                                searchResults.addAll(articles)
                            }
                        }
                    }
                }
                
                val filteredArticles = searchResults
                    .distinctBy { it.url }
                    .filter { article ->
                        val content = "${article.title} ${article.description} ${article.content}".lowercase()
                        val searchTerm = query.lowercase()
                        val plantTerms = listOf("plant", "agriculture", "farming", "crop", "garden", "soil", 
                                              "greenhouse", "irrigation", "fertilizer", "compost", "botanical",
                                              "horticulture", "cultivation", "harvest", "seed")
                        val exclusionTerms = listOf("politics", "sports", "entertainment", "celebrity", "movie", 
                                                   "music", "fashion", "cryptocurrency", "bitcoin", "stock market")
                        
                        val containsSearchTerm = content.contains(searchTerm) ||
                                               article.title.lowercase().contains(searchTerm)
                        val isPlantRelated = plantTerms.any { term -> content.contains(term) }
                        val isNotExcluded = !exclusionTerms.any { term -> content.contains(term) }
                        
                        article.title.isNotEmpty() && 
                        article.description?.isNotEmpty() == true &&
                        (containsSearchTerm || isPlantRelated) &&
                        isNotExcluded
                    }
                    .sortedByDescending { article ->
                        when {
                            article.title.lowercase().contains(query.lowercase()) -> 3
                            article.description?.lowercase()?.contains(query.lowercase()) == true -> 2
                            else -> 1
                        }
                    }
                    .take(40)
                
                _uiState.value = _uiState.value.copy(
                    articles = filteredArticles,
                    isLoading = false
                )
                
                cacheArticles(filteredArticles)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error occurred during search",
                    isLoading = false
                )
            }
        }
    }

    fun selectCategory(category: ArticleCategory?) {
        loadArticlesByCategory(category)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getCategories(): List<ArticleCategory> = allCategories
    
    private fun getDemoArticles(): List<Article> {
        return listOf(
            Article(
                source = com.gizemir.plantapp.domain.model.article.ArticleSource(
                    id = "demo1",
                    name = "Agriculture Today"
                ),
                author = "Dr. Plant Expert",
                title = "Revolutionary Greenhouse Farming Techniques Transform Agriculture",
                description = "New sustainable greenhouse methods increase crop yield by 40% while reducing water consumption. Learn about the latest innovations in controlled environment agriculture.",
                url = "https://demo.agriculture-today.com/greenhouse-revolution",
                urlToImage = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=800",
                publishedAt = java.util.Date(System.currentTimeMillis() - 86400000), // 1 gün önce
                content = "Revolutionary greenhouse farming techniques are transforming modern agriculture. These new methods combine traditional farming wisdom with cutting-edge technology to create optimal growing conditions..."
            ),
            Article(
                source = com.gizemir.plantapp.domain.model.article.ArticleSource(
                    id = "demo2", 
                    name = "Sustainable Farming News"
                ),
                author = "Green Agriculture Team",
                title = "Organic Farming: The Future of Sustainable Food Production",
                description = "Organic farming practices not only benefit the environment but also improve soil health and biodiversity. Discover how organic methods are shaping the future of agriculture.",
                url = "https://demo.sustainable-farming.com/organic-future",
                urlToImage = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800",
                publishedAt = java.util.Date(System.currentTimeMillis() - 172800000),
                content = "Organic farming represents a holistic approach to agriculture that works in harmony with nature. By avoiding synthetic pesticides and fertilizers, organic farmers create healthier ecosystems..."
            ),
            Article(
                source = com.gizemir.plantapp.domain.model.article.ArticleSource(
                    id = "demo3",
                    name = "Plant Science Weekly"
                ),
                author = "Dr. Sarah Plant",
                title = "Smart Irrigation Systems: Saving Water While Maximizing Crop Yield",
                description = "Modern smart irrigation technology uses sensors and AI to optimize water usage, resulting in 30% water savings and improved crop quality.",
                url = "https://demo.plant-science.com/smart-irrigation",
                urlToImage = "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=800",
                publishedAt = java.util.Date(System.currentTimeMillis() - 259200000),
                content = "Smart irrigation systems represent the next evolution in agricultural water management. These systems use advanced sensors to monitor soil moisture, weather conditions, and plant needs..."
            ),
            Article(
                source = com.gizemir.plantapp.domain.model.article.ArticleSource(
                    id = "demo4",
                    name = "Garden & Farm Magazine"
                ),
                author = "Expert Gardener",
                title = "Composting 101: Turn Your Kitchen Waste into Garden Gold",
                description = "Learn the basics of composting and how to create nutrient-rich soil amendment from organic waste. Perfect for both home gardeners and small-scale farmers.",
                url = "https://demo.garden-farm.com/composting-guide",
                urlToImage = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=800",
                publishedAt = java.util.Date(System.currentTimeMillis() - 345600000),
                content = "Composting is one of the most rewarding and environmentally friendly practices you can adopt in your garden. This natural process transforms organic waste into valuable soil amendment..."
            )
        )
    }
}

data class ArticleUiState(
    val articles: List<Article> = emptyList(),
    val selectedCategory: ArticleCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

