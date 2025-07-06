package com.gizemir.plantapp.data.remote.dto.article

import com.google.gson.annotations.SerializedName
import com.gizemir.plantapp.domain.model.article.Article
import com.gizemir.plantapp.domain.model.article.ArticleSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NewsApiResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("articles") val articles: List<ArticleDto>
)

data class ArticleDto(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("source") val source: ArticleSourceDto?,
    @SerializedName("author") val author: String?
) {
    fun toArticle(): Article {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val parsedDate = try {
            publishedAt?.let { dateFormat.parse(it) } ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return Article(
            title = title ?: "Title Not Found",
            description = description,
            content = content,
            url = url ?: "",
            urlToImage = urlToImage,
            publishedAt = parsedDate,
            source = source?.toArticleSource() ?: ArticleSource(null, "Unknown Source"),
            author = author
        )
    }
}

data class ArticleSourceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
) {
    fun toArticleSource(): ArticleSource {
        return ArticleSource(
            id = id,
            name = name ?: "Unknown Source"
        )
    }
} 