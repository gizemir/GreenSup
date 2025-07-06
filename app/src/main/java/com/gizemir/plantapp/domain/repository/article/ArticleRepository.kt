package com.gizemir.plantapp.domain.repository.article

import com.gizemir.plantapp.domain.model.article.Article

interface ArticleRepository {
    suspend fun searchArticles(
        query: String,
        language: String = "en",
        pageSize: Int = 20,
        page: Int = 1
    ): Result<List<Article>>
    
    suspend fun getTopHeadlines(
        category: String? = null,
        country: String = "us",
        language: String = "en",
        pageSize: Int = 20,
        page: Int = 1
    ): Result<List<Article>>
} 