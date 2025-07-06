package com.gizemir.plantapp.domain.model.article

import java.util.Date

data class Article(
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: Date,
    val source: ArticleSource,
    val author: String?
)

data class ArticleSource(
    val id: String?,
    val name: String
) 