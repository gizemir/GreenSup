package com.gizemir.plantapp.presentation.forum

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.use_case.forum.ForumUseCases
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.util.Resource
import com.gizemir.plantapp.presentation.forum.state.ForumState
import com.gizemir.plantapp.presentation.forum.state.PostCreationState
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumUseCases: ForumUseCases,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _forumState = MutableStateFlow(ForumState())
    val forumState: StateFlow<ForumState> = _forumState.asStateFlow()

    private val _postCreationState = MutableStateFlow(PostCreationState())
    val postCreationState: StateFlow<PostCreationState> = _postCreationState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _currentUserId.value = currentUser?.id
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Error getting current user: ${e.message}")
            }
        }
        
        getPosts()
    }

    fun getPosts() {
        viewModelScope.launch {
            _forumState.update { it.copy(isLoading = true, error = null) }
            
            forumUseCases.getPosts().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _forumState.update { 
                            it.copy(
                                posts = result.data,
                                isLoading = false,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _forumState.update { 
                            it.copy(
                                error = result.message,
                                isLoading = false
                            ) 
                        }
                    }
                    is Resource.Loading -> {
                        _forumState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onPostContentChange(content: String) {
        _postCreationState.update { it.copy(content = content) }
    }

    fun onPostImageChange(imageUri: Uri?) {
        _postCreationState.update { it.copy(imageUri = imageUri) }
    }

    fun createPost() {
        viewModelScope.launch {
            try {
                val originalImageUri = postCreationState.value.imageUri
                

                if (postCreationState.value.content.isNotBlank() || originalImageUri != null) {
                    val result = forumUseCases.createPost(
                        content = postCreationState.value.content,
                        imageUri = originalImageUri
                    )
                    
                    when (result) {
                        is Resource.Success -> {
                            resetPostCreationState()
                            _postCreationState.update { it.copy(isSuccess = true) }
                        }
                        is Resource.Error -> {
                            _postCreationState.update { it.copy(error = result.message) }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _postCreationState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun createPost(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("ForumViewModel", "Starting post creation...")
                
                val currentUser = Firebase.auth.currentUser
                if (currentUser == null) {
                    Log.e("ForumViewModel", "User is not authenticated")
                    onError("Please log in to create a post")
                    return@launch
                }
                
                Log.d("ForumViewModel", "User authenticated: ${currentUser.uid}")
                
                try {
                    currentUser.reload().await()
                    Log.d("ForumViewModel", "Firebase connection verified")
                } catch (e: Exception) {
                    Log.e("ForumViewModel", "Firebase connectivity issue: ${e.message}")
                    onError("Connection problem. Please check your internet and try again.")
                    return@launch
                }
                
                val originalImageUri = postCreationState.value.imageUri
                
                if (postCreationState.value.content.isNotBlank() || originalImageUri != null) {
                    Log.d("ForumViewModel", "Creating post with content: '${postCreationState.value.content}', hasImage: ${originalImageUri != null}")
                    
                    val result = forumUseCases.createPost(
                        content = postCreationState.value.content,
                        imageUri = originalImageUri
                    )
                    
                    when (result) {
                        is Resource.Success -> {
                            Log.d("ForumViewModel", "Post created successfully: ${result.data?.id}")
                            clearPostCreationState()
                            refreshPosts()
                            onSuccess()
                        }
                        is Resource.Error -> {
                            Log.e("ForumViewModel", "Post creation failed: ${result.message}")
                            onError(result.message ?: "Failed to create post")
                        }
                        is Resource.Loading -> {
                            Log.w("ForumViewModel", "Unexpected loading state")
                        }
                    }
                } else {
                    Log.w("ForumViewModel", "Post content is empty")
                    onError("Please add some content to your post")
                }
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Unexpected error in createPost: ${e.message}", e)
                
                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> {
                        "Permission denied. Please check your account settings and try again."
                    }
                    e.message?.contains("UNAUTHENTICATED") == true -> {
                        "Authentication failed. Please log in again."
                    }
                    e.message?.contains("UNAVAILABLE") == true -> {
                        "Service temporarily unavailable. Please try again later."
                    }
                    e.message?.contains("network") == true -> {
                        "Network error. Please check your internet connection."
                    }
                    else -> {
                        "Failed to create post: ${e.message ?: "Unknown error"}"
                    }
                }
                
                onError(errorMessage)
            }
        }
    }

    private suspend fun uploadImageToFirebase(uri: Uri): String? {
        return try {
            val storageRef = Firebase.storage.reference
            val userId = Firebase.auth.currentUser?.uid ?: return null
            
            val timestamp = System.currentTimeMillis()
            val uuid = UUID.randomUUID().toString()
            val fileName = "post_images/${userId}/${timestamp}_${uuid}.jpg"
            
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(uri).await()
            
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("ForumViewModel", "Image upload error: ${e.message}", e)
            null
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            val result = forumUseCases.likePost(post)
            
            when (result) {
                is Resource.Success -> {
                    val updatedPosts = _forumState.value.posts.map {
                        if (it.id == post.id) result.data else it
                    }
                    _forumState.update { it.copy(posts = updatedPosts) }
                }
                is Resource.Error -> {
                    _forumState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun commentPost(postId: String, content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            val result = forumUseCases.addComment(postId, content)
            
            when (result) {
                is Resource.Success -> {
                    try {
                        val updatedPost = forumUseCases.getPostById(postId)
                        val currentPosts = _forumState.value.posts.toMutableList()
                        val index = currentPosts.indexOfFirst { it.id == postId }
                        if (index != -1) {
                            currentPosts[index] = updatedPost
                            _forumState.update { it.copy(posts = currentPosts) }
                        }
                    } catch (e: Exception) {
                        Log.e("ForumViewModel", "Error updating post after comment: ${e.message}")
                        getPosts()
                    }
                }
                is Resource.Error -> {
                    _forumState.update { it.copy(error = result.message) }
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
                    _forumState.update { it.copy(error = "You can only delete your own comments") }
                    return@launch
                }
                
                // Delete comment using use case
                forumUseCases.deleteComment(comment)
                
                try {
                    val updatedPost = forumUseCases.getPostById(comment.postId)
                    val currentPosts = _forumState.value.posts.toMutableList()
                    val index = currentPosts.indexOfFirst { it.id == comment.postId }
                    if (index != -1) {
                        currentPosts[index] = updatedPost
                        _forumState.update { it.copy(posts = currentPosts) }
                    }
                } catch (e: Exception) {
                    Log.e("ForumViewModel", "Error updating post after comment deletion: ${e.message}")
                    getPosts()
                }
                
                Log.d("ForumViewModel", "Comment deleted successfully: ${comment.id}")
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Error deleting comment: ${e.message}")
                _forumState.update { it.copy(error = "Failed to delete comment: ${e.localizedMessage}") }
            }
        }
    }

    fun resetPostCreationState() {
        _postCreationState.update { PostCreationState() }
    }
    
    fun clearPostCreationState() {
        _postCreationState.update { PostCreationState() }
    }
    
    fun refreshPosts() {
        getPosts()
    }

    fun clearError() {
        _forumState.update { it.copy(error = null) }
    }

    private suspend fun isFirebaseAvailable(): Boolean {
        return try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                Log.e("ForumViewModel", "User not authenticated")
                return false
            }
            
            currentUser.reload().await()
            Log.d("ForumViewModel", "Firebase authentication verified")
            true
        } catch (e: Exception) {
            Log.e("ForumViewModel", "Firebase connectivity check failed: ${e.message}")
            false
        }
    }
}
