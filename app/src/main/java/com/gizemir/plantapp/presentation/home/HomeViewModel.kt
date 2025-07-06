package com.gizemir.plantapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository
import com.gizemir.plantapp.domain.repository.weather.WeatherRepository
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.use_case.forum.ForumUseCases
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val forumUseCases: ForumUseCases,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _temperature = MutableStateFlow("--°C")
    val temperature: StateFlow<String> = _temperature

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _iconUrl = MutableStateFlow("")
    val iconUrl: StateFlow<String> = _iconUrl

    private val _humidity = MutableStateFlow("--")
    val humidity: StateFlow<String> = _humidity

    private val _windSpeed = MutableStateFlow("--")
    val windSpeed: StateFlow<String> = _windSpeed

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _currentUserId.value = currentUser?.id
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting current user: ${e.message}")
            }
        }
        

        val lastCity = preferencesRepository.getLastCity()
        if (lastCity.isNotBlank()) {
            Log.d("HomeViewModel", "Loading last saved city on init: $lastCity")
            getWeatherByCity(lastCity)
        }
        
        loadPosts()
    }

    fun getWeatherByCity(cityName: String?) {
        if (cityName.isNullOrBlank()) return
        
        Log.d("HomeViewModel", "Getting weather for city: $cityName")
        viewModelScope.launch {
            try {
                val result = weatherRepository.getCurrentWeather(cityName)
                _temperature.value = result.temperature
                _city.value = result.city
                _description.value = result.description
                _iconUrl.value = result.iconUrl
                _humidity.value = result.humidity
                _windSpeed.value = result.windSpeed
                
                if (result.city.isNotBlank()) {
                    preferencesRepository.saveLastCity(result.city)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading city weather: ${e.message}")
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            
            forumUseCases.getPosts().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _posts.value = result.data
                        _isLoading.value = false
                        _error.value = null
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                        _isLoading.value = false
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    fun likePost(post: Post) {
        viewModelScope.launch {
            val result = forumUseCases.likePost(post)
            
            if (result is Resource.Success) {
                val currentPosts = _posts.value.toMutableList()
                val index = currentPosts.indexOfFirst { it.id == post.id }
                if (index != -1) {
                    currentPosts[index] = result.data
                    _posts.value = currentPosts
                }
            }
        }
    }
    
    fun addComment(post: Post, commentText: String) {
        viewModelScope.launch {
            val result = forumUseCases.addComment(post.id, commentText)
            
            when (result) {
                is Resource.Success -> {
                    try {
                        val updatedPost = forumUseCases.getPostById(post.id)
                        val currentPosts = _posts.value.toMutableList()
                        val index = currentPosts.indexOfFirst { it.id == post.id }
                        if (index != -1) {
                            currentPosts[index] = updatedPost
                            _posts.value = currentPosts
                        }
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error updating post after comment: ${e.message}")
            loadPosts()
                    }
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUserId.value
                if (currentUser == null || comment.userId != currentUser) {
                    _error.value = "You can only delete your own comments"
                    return@launch
                }
                
                forumUseCases.deleteComment(comment)
                
                try {
                    val updatedPost = forumUseCases.getPostById(comment.postId)
                    val currentPosts = _posts.value.toMutableList()
                    val index = currentPosts.indexOfFirst { it.id == comment.postId }
                    if (index != -1) {
                        currentPosts[index] = updatedPost
                        _posts.value = currentPosts
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error updating post after comment deletion: ${e.message}")
                    loadPosts()
                }
                
                Log.d("HomeViewModel", "Comment deleted successfully: ${comment.id}")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error deleting comment: ${e.message}")
                _error.value = "Failed to delete comment: ${e.localizedMessage}"
            }
        }
    }

    fun onNewPostCreated() {
        Log.d("HomeViewModel", "New post created, refreshing posts")
        refreshPosts()
    }

    private fun refreshPosts() {
        Log.d("HomeViewModel", "Starting post data refresh")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                forumUseCases.getPosts().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("HomeViewModel", "Posts refreshed successfully: ${result.data.size} posts")
                            _posts.value = result.data
                            _isLoading.value = false
                            _error.value = null
                        }
                        is Resource.Error -> {
                            Log.e("HomeViewModel", "Post refresh error: ${result.message}")
                            _error.value = result.message
                            _isLoading.value = false
                        }
                        is Resource.Loading -> {
                            _isLoading.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Post refresh exception: ${e.message}", e)
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

