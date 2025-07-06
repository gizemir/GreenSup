package com.gizemir.plantapp.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.user.User
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.repository.user.UserRepository
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.use_case.forum.ForumUseCases
import com.gizemir.plantapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val forumUseCases: ForumUseCases,
    private val forumRepository: ForumRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user
    
    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _currentUserId.value = currentUser?.id
                Log.d("UserProfileViewModel", "Current user ID set to: ${currentUser?.id}")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error getting current user: ${e.message}")
            }
        }
    }
    
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            Log.d("UserProfileViewModel", "Loading user profile for userId: $userId")
            
            try {
                val userProfile = userRepository.getUserById(userId)
                _user.value = userProfile
                Log.d("UserProfileViewModel", "User profile loaded successfully: ${userProfile.name}")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error loading user profile: ${e.message}")
                _error.value = "Could not load user profile: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            Log.d("UserProfileViewModel", "Loading posts for userId: $userId")
            
            try {
                val posts = forumRepository.getUserPosts(userId)
                // Sort posts by creation date (newest first)
                val sortedPosts = posts.sortedByDescending { it.timestamp }
                _userPosts.value = sortedPosts
                Log.d("UserProfileViewModel", "Posts loaded successfully: ${posts.size} posts found")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error loading user posts: ${e.message}")
                _error.value = "Could not load posts"
            }
        }
    }
    
    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                val result = forumUseCases.likePost(post)
                
                if (result is Resource.Success) {
                    // Update in the current posts list
                    val currentPosts = _userPosts.value.toMutableList()
                    val index = currentPosts.indexOfFirst { it.id == post.id }
                    if (index != -1) {
                        currentPosts[index] = result.data
                        _userPosts.value = currentPosts
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error liking post: ${e.message}")
            }
        }
    }
    
    fun addComment(post: Post, commentText: String) {
        viewModelScope.launch {
            try {
                forumUseCases.addComment(post.id, commentText)
                
                loadUserPosts(_user.value.id)
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error adding comment: ${e.message}")
            }
        }
    }
    
    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUserId.value
                if (currentUser == null || post.userId != currentUser) {
                    _error.value = "You can only delete your own posts"
                    return@launch
                }
                
                forumRepository.deletePost(post.id)
                
                val currentPosts = _userPosts.value.toMutableList()
                currentPosts.removeAll { it.id == post.id }
                _userPosts.value = currentPosts
                
                Log.d("UserProfileViewModel", "Post deleted successfully: ${post.id}")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error deleting post: ${e.message}")
                _error.value = "Failed to delete post: ${e.localizedMessage}"
            }
        }
    }
    
    fun editPost(postId: String, newContent: String, newImageUrl: String? = null) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUserId.value
                val post = _userPosts.value.find { it.id == postId }
                
                if (currentUser == null || post?.userId != currentUser) {
                    _error.value = "You can only edit your own posts"
                    return@launch
                }
                
                forumRepository.updatePost(postId, newContent, newImageUrl)
                
                loadUserPosts(_user.value.id)
                
                Log.d("UserProfileViewModel", "Post updated successfully: $postId")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error updating post: ${e.message}")
                _error.value = "Failed to update post: ${e.localizedMessage}"
            }
        }
    }
    
    fun deleteComment(comment: Comment, postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUserId.value
                if (currentUser == null || comment.userId != currentUser) {
                    _error.value = "You can only delete your own comments"
                    return@launch
                }
                
                forumRepository.deleteComment(comment.id, postId)
                
                loadUserPosts(_user.value.id)
                
                Log.d("UserProfileViewModel", "Comment deleted successfully: ${comment.id}")
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error deleting comment: ${e.message}")
                _error.value = "Failed to delete comment: ${e.localizedMessage}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun isOwnPost(post: Post): Boolean {
        return _currentUserId.value == post.userId
    }
    
    fun isOwnComment(comment: Comment): Boolean {
        return _currentUserId.value == comment.userId
    }
}
