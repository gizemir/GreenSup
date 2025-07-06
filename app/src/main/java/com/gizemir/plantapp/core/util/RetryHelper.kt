package com.gizemir.plantapp.core.util

import android.util.Log
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object RetryHelper {
    
    private const val TAG = "RetryHelper"
    
    suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        backoffMultiplier: Double = 2.0,
        retryOn: (Throwable) -> Boolean = ::defaultRetryCondition,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Throwable) {
                lastException = e
                
                if (!retryOn(e)) {
                    Log.d(TAG, "Not retrying for exception: ${e.javaClass.simpleName}")
                    throw e
                }
                
                if (attempt == maxRetries - 1) {
                    Log.e(TAG, "Max retries ($maxRetries) reached for ${e.javaClass.simpleName}")
                    throw e
                }
                
                Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms: ${e.message}")
                delay(currentDelay)
                
                currentDelay = (currentDelay * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
            }
        }
        
        throw lastException ?: RuntimeException("Unexpected retry failure")
    }
    
    private fun defaultRetryCondition(throwable: Throwable): Boolean {
        return when (throwable) {
            is IOException,
            is SocketTimeoutException,
            is UnknownHostException -> true
            
            // HTTP errors
            is HttpException -> {
                when (throwable.code()) {
                    429 -> true
                    // Server errors - retry
                    in 500..599 -> true
                    // Client errors - don't retry
                    in 400..499 -> false
                    else -> false
                }
            }
            
            else -> false
        }
    }
    
    fun isRateLimitError(throwable: Throwable): Boolean {
        return throwable is HttpException && throwable.code() == 429
    }
    
    fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is IOException || 
               throwable is SocketTimeoutException || 
               throwable is UnknownHostException
    }
    
    fun isServerError(throwable: Throwable): Boolean {
        return throwable is HttpException && throwable.code() in 500..599
    }
    
    fun getErrorMessage(throwable: Throwable): String {
        return when {
            isRateLimitError(throwable) -> "Rate limit exceeded. Please wait before making more requests."
            isNetworkError(throwable) -> "Network connection error. Please check your internet connection."
            isServerError(throwable) -> "Server error. Please try again later."
            throwable is HttpException -> "API error: ${throwable.code()} - ${throwable.message()}"
            else -> throwable.message ?: "Unknown error occurred"
        }
    }
} 