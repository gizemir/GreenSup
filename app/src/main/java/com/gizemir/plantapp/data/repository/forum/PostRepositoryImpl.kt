package com.gizemir.plantapp.data.repository.forum

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.model.notification.Notification
import com.gizemir.plantapp.domain.model.notification.NotificationType
import com.gizemir.plantapp.domain.repository.forum.PostRepository
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageRepository: StorageRepository,
    private val notificationRepository: NotificationRepository
) : PostRepository {

    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")
    private val likesCollection = firestore.collection("likes")
    private val usersCollection = firestore.collection("users")
    
    override suspend fun getAllPosts(): List<Post> {
        try {
            val postsSnapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val currentUserId = auth.currentUser?.uid ?: ""
            
            return postsSnapshot.documents.mapNotNull { document ->
                try {
                    val post = document.toObject(Post::class.java) ?: return@mapNotNull null
                    
                    val comments = getCommentsForPost(document.id)
                    val isLiked = isPostLikedByCurrentUser(document.id, currentUserId)
                    
                    post.copy(
                        id = document.id,
                        comments = comments,
                        isLikedByCurrentUser = isLiked
                    )
                } catch (e: Exception) {
                    Log.e("PostRepository", "Error parsing post: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error getting posts: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getPostById(postId: String): Post {
        try {
            val postDoc = postsCollection.document(postId).get().await()
            val post = postDoc.toObject(Post::class.java) 
                ?: throw Exception("Post not found")
                
            val comments = getCommentsForPost(postId)
            val currentUserId = auth.currentUser?.uid ?: ""
            val isLiked = isPostLikedByCurrentUser(postId, currentUserId)
            
            return post.copy(
                id = postId,
                comments = comments,
                isLikedByCurrentUser = isLiked
            )
        } catch (e: Exception) {
            Log.e("PostRepository", "Error getting post: ${e.message}")
            throw e
        }
    }
    
    override suspend fun createPost(content: String, imageUri: Uri?): Post {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        
        try {
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userName = userDoc.getString("name") ?: currentUser.displayName ?: "Unknown User"
            val userProfilePicUrl = userDoc.getString("profilePictureUrl")
            
            val imageUrl = if (imageUri != null) {
                storageRepository.uploadImage(imageUri, "post_images")
            } else {
                null
            }
            
            val newPost = Post(
                userId = currentUser.uid,
                userName = userName,
                userProfilePicUrl = userProfilePicUrl,
                content = content,
                imageUrl = imageUrl,
                timestamp = Date()
            )
            
            val postRef = postsCollection.add(newPost).await()
            
            return newPost.copy(id = postRef.id)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error creating post: ${e.message}")
            throw e
        }
    }
    
    override suspend fun likePost(postId: String): Post {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        
        try {
            val postRef = postsCollection.document(postId)
            val likesCollection = firestore.collection("likes")
            
            val likeRef = likesCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                
            val isAlreadyLiked = !likeRef.isEmpty
            
            val postDoc = postRef.get().await()
            val post = postDoc.toObject(Post::class.java) ?: throw Exception("Post not found")
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userName = userDoc.getString("name") ?: currentUser.displayName ?: "Bilinmeyen Kullanıcı"
            val userProfilePicUrl = userDoc.getString("profilePictureUrl")
            
            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                val currentPost = postSnapshot.toObject(Post::class.java)
                    ?: throw Exception("Post not found")
                
                if (isAlreadyLiked) {
                    val likeId = likeRef.documents[0].id
                    transaction.delete(likesCollection.document(likeId))
                    
                    transaction.update(postRef, "likeCount", currentPost.likeCount - 1)
                } else {
                    val like = mapOf(
                        "postId" to postId,
                        "userId" to currentUser.uid,
                        "timestamp" to Date()
                    )
                    transaction.set(likesCollection.document(), like)
                    
                    transaction.update(postRef, "likeCount", currentPost.likeCount + 1)
                }
            }.await()
            
            if (!isAlreadyLiked && post.userId != currentUser.uid) {
                val notification = Notification(
                    userId = post.userId,
                    type = NotificationType.LIKE,
                    title = "Yeni Beğeni",
                    message = "$userName gönderinizi beğendi",
                    postId = postId,
                    fromUserId = currentUser.uid,
                    fromUserName = userName,
                    fromUserProfilePic = userProfilePicUrl,
                    timestamp = Date(),
                    isRead = false
                )
                
                try {
                    notificationRepository.createNotification(notification)
                    Log.d("PostRepository", "Notification created for like on post: $postId")
                } catch (e: Exception) {
                    Log.e("PostRepository", "Failed to create notification: ${e.message}")
                    // Don't fail the like operation if notification fails
                }
            }
            
            return getPostById(postId)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error liking post: ${e.message}")
            throw e
        }
    }
    
    override suspend fun addComment(postId: String, commentText: String): Post {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        
        try {
            val postDoc = postsCollection.document(postId).get().await()
            val post = postDoc.toObject(Post::class.java) ?: throw Exception("Post not found")
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val userName = userDoc.getString("name") ?: currentUser.displayName ?: "Unknown User"
            val userProfilePicUrl = userDoc.getString("profilePictureUrl")
            
            val newComment = Comment(
                postId = postId,
                userId = currentUser.uid,
                userName = userName,
                userProfilePicUrl = userProfilePicUrl,
                content = commentText,
                timestamp = Date()
            )
            
            commentsCollection.add(newComment).await()
            
            val postRef = postsCollection.document(postId)
            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                val currentPost = postSnapshot.toObject(Post::class.java)
                    ?: throw Exception("Post not found")
                
                transaction.update(postRef, "commentCount", currentPost.commentCount + 1)
            }.await()
            
            if (post.userId != currentUser.uid) {
                val notification = Notification(
                    userId = post.userId,
                    type = NotificationType.COMMENT,
                    title = "Yeni Yorum",
                    message = "$userName gönderinize yorum yaptı: \"${commentText.take(50)}${if (commentText.length > 50) "..." else ""}\"",
                    postId = postId,
                    fromUserId = currentUser.uid,
                    fromUserName = userName,
                    fromUserProfilePic = userProfilePicUrl,
                    timestamp = Date(),
                    isRead = false
                )
                
                try {
                    notificationRepository.createNotification(notification)
                    Log.d("PostRepository", "Notification created for comment on post: $postId")
                } catch (e: Exception) {
                    Log.e("PostRepository", "Failed to create notification: ${e.message}")
                }
            }
            
            return getPostById(postId)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error adding comment: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getUserPosts(userId: String): List<Post> {
        try {
            val postsSnapshot = try {
                postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (indexException: Exception) {
                Log.w("PostRepository", "Index not available, using fallback query: ${indexException.message}")
                postsCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }
                
            val currentUserId = auth.currentUser?.uid ?: ""
            
            val posts = postsSnapshot.documents.mapNotNull { document ->
                try {
                    val post = document.toObject(Post::class.java) ?: return@mapNotNull null
                    
                    val comments = getCommentsForPost(document.id)
                    val isLiked = isPostLikedByCurrentUser(document.id, currentUserId)
                    
                    post.copy(
                        id = document.id,
                        comments = comments,
                        isLikedByCurrentUser = isLiked
                    )
                } catch (e: Exception) {
                    Log.e("PostRepository", "Error parsing user post: ${e.message}")
                    null
                }
            }
            
            return posts.sortedByDescending { it.timestamp }
            
        } catch (e: Exception) {
            Log.e("PostRepository", "Error getting user posts: ${e.message}")
            throw e
        }
    }
    

    private suspend fun getCommentsForPost(postId: String): List<Comment> {
        try {
            val commentsSnapshot = commentsCollection
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
                
            return commentsSnapshot.documents.mapNotNull { document ->
                try {
                    val comment = document.toObject(Comment::class.java) ?: return@mapNotNull null
                    comment.copy(id = document.id)
                } catch (e: Exception) {
                    Log.e("PostRepository", "Error parsing comment: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error getting comments: ${e.message}")
            return emptyList()
        }
    }
    
    private suspend fun isPostLikedByCurrentUser(postId: String, userId: String): Boolean {
        if (userId.isBlank()) return false
        
        try {
            val likeDoc = likesCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
                
            return !likeDoc.isEmpty
        } catch (e: Exception) {
            Log.e("PostRepository", "Error checking if post is liked: ${e.message}")
            return false
        }
    }
}

