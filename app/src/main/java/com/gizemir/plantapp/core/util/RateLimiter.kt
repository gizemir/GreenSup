package com.gizemir.plantapp.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimiter @Inject constructor() {
    
    private val mutex = Mutex()
    private val requestTimes = ConcurrentLinkedQueue<Long>()
    
    private val maxRequestsPerMinute = 15
    private val timeWindowMs = 60_000L // 1 minute
    
    suspend fun waitForRateLimit() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            
            while (requestTimes.isNotEmpty()) {
                val oldestTime = requestTimes.peek()
                if (oldestTime != null && currentTime - oldestTime > timeWindowMs) {
                    requestTimes.poll()
                } else {
                    break
                }
            }
            
            if (requestTimes.size >= maxRequestsPerMinute) {
                val oldestRequest = requestTimes.peek()
                if (oldestRequest != null) {
                    val waitTime = timeWindowMs - (currentTime - oldestRequest) + 1000 // +1s buffer
                    if (waitTime > 0) {
                        delay(waitTime)
                    }
                }
                val newCurrentTime = System.currentTimeMillis()
                while (requestTimes.isNotEmpty()) {
                    val oldestTime = requestTimes.peek()
                    if (oldestTime != null && newCurrentTime - oldestTime > timeWindowMs) {
                        requestTimes.poll()
                    } else {
                        break
                    }
                }
            }
            

            requestTimes.offer(currentTime)
        }
    }
    
    fun getRemainingRequests(): Int {
        val currentTime = System.currentTimeMillis()

        while (requestTimes.isNotEmpty()) {
            val oldestTime = requestTimes.peek()
            if (oldestTime != null && currentTime - oldestTime > timeWindowMs) {
                requestTimes.poll()
            } else {
                break
            }
        }
        return maxOf(0, maxRequestsPerMinute - requestTimes.size)
    }
    
    fun getTimeUntilNextRequest(): Long {
        if (requestTimes.size < maxRequestsPerMinute) return 0
        
        val currentTime = System.currentTimeMillis()
        val oldestRequest = requestTimes.peek() ?: return 0
        return maxOf(0, timeWindowMs - (currentTime - oldestRequest))
    }
} 