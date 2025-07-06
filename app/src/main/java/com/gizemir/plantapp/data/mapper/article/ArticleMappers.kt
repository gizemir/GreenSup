package com.gizemir.plantapp.data.mapper.article

import com.gizemir.plantapp.data.remote.dto.article.ArticleDto
import com.gizemir.plantapp.data.remote.dto.article.ArticleSourceDto
import com.gizemir.plantapp.domain.model.article.Article
import com.gizemir.plantapp.domain.model.article.ArticleSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun ArticleDto.toDomain(): Article {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val parsedDate = try {
        publishedAt?.let { dateFormat.parse(it) } ?: Date()
    } catch (e: Exception) {
        Date()
    }

    return Article(
        title = title ?: "Başlık Bulunamadı",
        description = description,
        content = content,
        url = url ?: "",
        urlToImage = urlToImage,
        publishedAt = parsedDate,
        source = source?.toDomain() ?: ArticleSource(null, "Bilinmeyen Kaynak"),
        author = author
    )
}

fun ArticleSourceDto.toDomain(): ArticleSource {
    return ArticleSource(
        id = id,
        name = name ?: "Bilinmeyen Kaynak"
    )
} 