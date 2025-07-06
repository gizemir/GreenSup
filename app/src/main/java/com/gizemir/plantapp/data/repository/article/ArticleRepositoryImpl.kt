package com.gizemir.plantapp.data.repository.article

import android.util.Log
import com.gizemir.plantapp.data.remote.api.ArticleApiService
import com.gizemir.plantapp.domain.model.article.Article
import com.gizemir.plantapp.domain.repository.article.ArticleRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val articleApiService: ArticleApiService
) : ArticleRepository {

    companion object {
        private const val TAG = "ArticleRepository"
    }

    override suspend fun searchArticles(
        query: String,
        language: String,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        return try {
            Log.d(TAG, "Searching articles for query: $query, page: $page")
            
            val response = articleApiService.searchArticles(
                query = query,
                language = language,
                pageSize = pageSize,
                page = page,
                apiKey = com.gizemir.plantapp.core.util.ApiConfig.NEWS_API_KEY
            )

            if (response.isSuccessful) {
                val articles = response.body()?.articles?.map { it.toArticle() } ?: emptyList()
                Log.d(TAG, "Successfully fetched ${articles.size} articles")
                Result.success(articles)
            } else {
                val errorMessage = when (response.code()) {
                    429 -> "Rate limit exceeded. Please try again later. NewsAPI free plan allows 100 requests per day."
                    401 -> "Invalid API key. Please check your NewsAPI configuration."
                    else -> "API Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}")
            Result.failure(Exception("Network error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}")
            Result.failure(Exception("Connection error: Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Exception: ${e.message}")
            Result.failure(Exception("An unexpected error occurred: ${e.localizedMessage}"))
        }
    }

    override suspend fun getTopHeadlines(
        category: String?,
        country: String,
        language: String,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        return try {
            Log.d(TAG, "Getting top headlines for category: $category, page: $page")
            
            val response = articleApiService.getTopHeadlines(
                category = category,
                country = country,
                language = language,
                pageSize = pageSize,
                page = page,
                apiKey = com.gizemir.plantapp.core.util.ApiConfig.NEWS_API_KEY
            )

            if (response.isSuccessful) {
                val articles = response.body()?.articles?.map { it.toArticle() } ?: emptyList()
                Log.d(TAG, "Successfully fetched ${articles.size} top headlines")
                Result.success(articles)
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Exception: ${e.message()}")
            Result.failure(Exception("Network error: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}")
            Result.failure(Exception("Connection error: Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Exception: ${e.message}")
            Result.failure(Exception("An unexpected error occurred: ${e.localizedMessage}"))
        }
    }
} 