package com.gizemir.plantapp.data.source.forum

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.data.remote.dto.forum.CommentDto
import com.gizemir.plantapp.data.remote.dto.forum.LikeDto
import com.gizemir.plantapp.data.remote.dto.forum.PostDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseForumDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ForumRemoteDataSource {

    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")
    private val likesCollection = firestore.collection("likes")
    private val usersCollection = firestore.collection("users")

    override fun getAllPosts(): Flow<List<PostDto>> = callbackFlow {
        val subscription = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("FirebaseForumDataSource", "Error fetching posts", exception)
                    return@addSnapshotListener
                }
                
                val posts = snapshot?.documents?.mapNotNull { it.toObject(PostDto::class.java) } ?: emptyList()
                trySend(posts)
            }
            
        awaitClose { subscription.remove() }
    }

    override suspend fun getPostById(postId: String): PostDto {
        val document = postsCollection.document(postId).get().await()
        return document.toObject(PostDto::class.java) ?: throw Exception("Post not found")
    }

    override suspend fun createPost(userId: String, content: String, imageUri: Uri?): PostDto {
        val userDoc = usersCollection.document(userId).get().await()
        
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: userDoc.getString("name") ?: "Unknown User"
        val userProfilePicUrl = currentUser?.photoUrl?.toString() ?: userDoc.getString("profilePictureUrl")
        
        val imageUrl = if (imageUri != null) {
            Log.d("ForumDataSource", "Uploading image...")
            uploadImage(imageUri, "post_images")
        } else null
        
        val post = PostDto(
            userId = userId,
            userName = userName,
            userProfilePicUrl = userProfilePicUrl,
            content = content,
            imageUrl = imageUrl
        )
        
        val postRef = postsCollection.add(post).await()
        
        return post.copy(id = postRef.id)
    }

    override suspend fun likePost(postId: String, userId: String): Int {
        try {
            Log.d("FirebaseForumDataSource", "=== LIKE POST DEBUG START ===")
            Log.d("FirebaseForumDataSource", "Post ID: $postId")
            Log.d("FirebaseForumDataSource", "User ID: $userId")
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("FirebaseForumDataSource", "Current user is null")
                throw Exception("User not authenticated")
            }
            
            Log.d("FirebaseForumDataSource", "Current user ID: ${currentUser.uid}")
            Log.d("FirebaseForumDataSource", "Current user email: ${currentUser.email}")
            Log.d("FirebaseForumDataSource", "User authenticated: ${currentUser.uid == userId}")
            Log.d("FirebaseForumDataSource", "User email verified: ${currentUser.isEmailVerified}")
            Log.d("FirebaseForumDataSource", "User provider data: ${currentUser.providerData}")
            
            if (currentUser.uid != userId) {
                Log.e("FirebaseForumDataSource", "User ID mismatch: authenticated=${currentUser.uid}, provided=$userId")
                throw Exception("User ID mismatch: authenticated=${currentUser.uid}, provided=$userId")
            }
            
            Log.d("FirebaseForumDataSource", "Testing Firestore connection...")
            try {
                currentUser.reload().await()
                Log.d("FirebaseForumDataSource", "User auth refreshed successfully")
                Log.d("FirebaseForumDataSource", "ID Token: ${currentUser.getIdToken(false).await().token?.take(20)}...")
            } catch (e: Exception) {
                Log.e("FirebaseForumDataSource", "Auth refresh failed: ${e.message}")
                throw Exception("Authentication verification failed: ${e.message}")
            }
            

            try {
                val testRead = firestore.collection("posts").limit(1).get().await()
                Log.d("FirebaseForumDataSource", "Basic Firestore read successful: ${testRead.size()} documents")
            } catch (readError: Exception) {
                Log.e("FirebaseForumDataSource", "Basic Firestore read failed: ${readError.message}")
                throw Exception("Firestore access denied: ${readError.message}")
            }
            
            val existingLikeQuery = likesCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            
            if (!existingLikeQuery.isEmpty) {
                Log.w("FirebaseForumDataSource", "Post already liked by user")
                val postDoc = postsCollection.document(postId).get().await()
                return postDoc.getLong("likeCount")?.toInt() ?: 0
            }
            
            Log.d("FirebaseForumDataSource", "Post not yet liked, creating like document...")
            
            val like = hashMapOf(
                "postId" to postId,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )
            
            Log.d("FirebaseForumDataSource", "Like document data: $like")
            
            Log.d("FirebaseForumDataSource", "Testing likes collection access...")
            try {
                val testQuery = likesCollection.limit(1).get().await()
                Log.d("FirebaseForumDataSource", "Likes collection read test successful: ${testQuery.size()} documents")
            } catch (readException: Exception) {
                Log.e("FirebaseForumDataSource", "Likes collection read test failed: ${readException.message}")
                throw Exception("Cannot access likes collection: ${readException.message}")
            }
            
            Log.d("FirebaseForumDataSource", "Attempting to create like document...")
            try {
                val likeDocRef = likesCollection.add(like).await()
                Log.d("FirebaseForumDataSource", "Like document created with ID: ${likeDocRef.id}")
            } catch (createException: Exception) {
                Log.e("FirebaseForumDataSource", "Failed to create like document: ${createException.message}")
                Log.e("FirebaseForumDataSource", "Create exception type: ${createException.javaClass.simpleName}")
                Log.e("FirebaseForumDataSource", "Create exception cause: ${createException.cause}")
                
                if (createException.message?.contains("PERMISSION_DENIED") == true) {
                    Log.e("FirebaseForumDataSource", "FIRESTORE RULES ARE BLOCKING THE CREATE OPERATION!")
                    Log.e("FirebaseForumDataSource", "Current user: ${currentUser.uid}")
                    Log.e("FirebaseForumDataSource", "Attempting to create like with userId: $userId")
                    Log.e("FirebaseForumDataSource", "Please check Firebase Console > Firestore > Rules")
                    Log.e("FirebaseForumDataSource", "Current rules may not be published or incorrect")
                    throw Exception("❌ FIRESTORE RULES ERROR: Check Firebase Console rules are published correctly!")
                }
                throw createException
            }
            
            val postRef = postsCollection.document(postId)
            Log.d("FirebaseForumDataSource", "Updating post like count...")
            
            val postDoc = postRef.get().await()
            val currentLikes = postDoc.getLong("likeCount")?.toInt() ?: 0
            val newLikeCount = currentLikes + 1
            
            postRef.update("likeCount", newLikeCount).await()
            Log.d("FirebaseForumDataSource", "Post like count updated from $currentLikes to $newLikeCount")
            Log.d("FirebaseForumDataSource", "=== LIKE POST DEBUG END ===")
            
            return newLikeCount
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "=== LIKE POST ERROR ===")
            Log.e("FirebaseForumDataSource", "Error type: ${e.javaClass.simpleName}")
            Log.e("FirebaseForumDataSource", "Error message: ${e.message}")
            Log.e("FirebaseForumDataSource", "Error cause: ${e.cause}")
            Log.e("FirebaseForumDataSource", "Stack trace:")
            e.printStackTrace()
            
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    throw Exception("Permission denied: Cannot like this post. Check Firestore rules for 'likes' collection.")
                }
                e.message?.contains("UNAUTHENTICATED") == true -> {
                    throw Exception("Authentication required: Please log in to like posts.")
                }
                e.message?.contains("NETWORK") == true -> {
                    throw Exception("Network error: Check your internet connection.")
                }
                else -> {
                    throw Exception("Failed to like post: ${e.message}")
                }
            }
        }
    }

    override suspend fun unlikePost(postId: String, userId: String): Int {
        try {
            Log.d("FirebaseForumDataSource", "=== UNLIKE POST DEBUG START ===")
            Log.d("FirebaseForumDataSource", "Post ID: $postId")
            Log.d("FirebaseForumDataSource", "User ID: $userId")
            
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("FirebaseForumDataSource", "Current user is null")
                throw Exception("User not authenticated")
            }
            
            Log.d("FirebaseForumDataSource", "Current user ID: ${currentUser.uid}")
            Log.d("FirebaseForumDataSource", "Current user email: ${currentUser.email}")
            Log.d("FirebaseForumDataSource", "User authenticated: ${currentUser.uid == userId}")
            
            if (currentUser.uid != userId) {
                Log.e("FirebaseForumDataSource", "User ID mismatch: authenticated=${currentUser.uid}, provided=$userId")
                throw Exception("User ID mismatch: authenticated=${currentUser.uid}, provided=$userId")
            }
            
            Log.d("FirebaseForumDataSource", "Authentication verified for user: ${currentUser.uid}")
            
            Log.d("FirebaseForumDataSource", "Searching for like document...")
            val query = likesCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Log.w("FirebaseForumDataSource", "Like not found for unlike operation")
                val postDoc = postsCollection.document(postId).get().await()
                return postDoc.getLong("likeCount")?.toInt() ?: 0
            }
            
            val likeDoc = query.documents[0]
            val likeData = likeDoc.data
            Log.d("FirebaseForumDataSource", "Found like document: ID=${likeDoc.id}, data=$likeData")
            
            val likeUserId = likeDoc.getString("userId")
            Log.d("FirebaseForumDataSource", "Like userId: $likeUserId, Current userId: $userId")
            
            if (likeUserId != userId) {
                Log.e("FirebaseForumDataSource", "Cannot delete like: User mismatch (like owner: $likeUserId, current user: $userId)")
                throw Exception("Cannot delete like: User mismatch (like owner: $likeUserId, current user: $userId)")
            }
            
            Log.d("FirebaseForumDataSource", "Testing Firestore connection...")
            try {
                currentUser.reload().await()
                Log.d("FirebaseForumDataSource", "User auth refreshed successfully")
            } catch (e: Exception) {
                Log.e("FirebaseForumDataSource", "Auth refresh failed: ${e.message}")
                throw Exception("Authentication verification failed: ${e.message}")
            }
            
            Log.d("FirebaseForumDataSource", "Attempting to delete like document: ${likeDoc.id}")
            
            try {
                likesCollection.document(likeDoc.id).delete().await()
                Log.d("FirebaseForumDataSource", "Like document deleted successfully: ${likeDoc.id}")
            } catch (deleteException: Exception) {
                Log.e("FirebaseForumDataSource", "Failed to delete like document: ${deleteException.message}")
                Log.e("FirebaseForumDataSource", "Delete exception type: ${deleteException.javaClass.simpleName}")
                
                if (deleteException.message?.contains("PERMISSION_DENIED") == true) {
                    Log.e("FirebaseForumDataSource", "Firestore rules are blocking the delete operation")
                    Log.e("FirebaseForumDataSource", "Current user: ${currentUser.uid}")
                    Log.e("FirebaseForumDataSource", "Like document owner: $likeUserId")
                    Log.e("FirebaseForumDataSource", "Document ID: ${likeDoc.id}")
                    throw Exception("Firestore permission denied for like deletion. Check rules for 'likes' collection.")
                }
                throw deleteException
            }
            
            Log.d("FirebaseForumDataSource", "Updating post like count...")
            val postRef = postsCollection.document(postId)
            val postDoc = postRef.get().await()
            val currentLikes = postDoc.getLong("likeCount")?.toInt() ?: 0
            val newLikeCount = (currentLikes - 1).coerceAtLeast(0)
            
            postRef.update("likeCount", newLikeCount).await()
            Log.d("FirebaseForumDataSource", "Post like count updated from $currentLikes to $newLikeCount")
            Log.d("FirebaseForumDataSource", "=== UNLIKE POST DEBUG END ===")
            
            return newLikeCount
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "=== UNLIKE POST ERROR ===")
            Log.e("FirebaseForumDataSource", "Error type: ${e.javaClass.simpleName}")
            Log.e("FirebaseForumDataSource", "Error message: ${e.message}")
            Log.e("FirebaseForumDataSource", "Error cause: ${e.cause}")
            Log.e("FirebaseForumDataSource", "Stack trace:")
            e.printStackTrace()
            
            when {
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    throw Exception("Permission denied: Cannot unlike this post. Check your authentication and Firestore rules.")
                }
                e.message?.contains("UNAUTHENTICATED") == true -> {
                    throw Exception("Authentication required: Please log in to unlike posts.")
                }
                e.message?.contains("NETWORK") == true -> {
                    throw Exception("Network error: Check your internet connection.")
                }
                else -> {
                    throw Exception("Failed to unlike post: ${e.message}")
                }
            }
        }
    }

    override suspend fun isPostLikedByUser(postId: String, userId: String): Boolean {
        val query = likesCollection
            .whereEqualTo("postId", postId)
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()
            
        return !query.isEmpty
    }

    override suspend fun addComment(postId: String, userId: String, content: String): CommentDto {
        val userDoc = usersCollection.document(userId).get().await()
        
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: userDoc.getString("name") ?: "Unknown User"
        val userProfilePicUrl = currentUser?.photoUrl?.toString() ?: userDoc.getString("profilePictureUrl")
        
        val comment = CommentDto(
            postId = postId,
            userId = userId,
            userName = userName,
            userProfilePicUrl = userProfilePicUrl,
            content = content
        )
        
        val commentRef = commentsCollection.add(comment).await()
        
        val postRef = postsCollection.document(postId)
        val postDoc = postRef.get().await()
        val currentComments = postDoc.getLong("commentCount")?.toInt() ?: 0
        val newCommentCount = currentComments + 1
        
        postRef.update("commentCount", newCommentCount).await()
        
        return comment.copy(id = commentRef.id)
    }

    override suspend fun getCommentsForPost(postId: String): List<CommentDto> {
        val query = commentsCollection
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
            
        return query.documents.mapNotNull { it.toObject(CommentDto::class.java) }
    }

    override suspend fun getUserPosts(userId: String): List<PostDto> {
        val query = postsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            
        return query.documents.mapNotNull { it.toObject(PostDto::class.java) }
    }

    override suspend fun deletePost(postId: String): Boolean {
        return try {
            postsCollection.document(postId).delete().await()
            
            val commentsQuery = commentsCollection.whereEqualTo("postId", postId).get().await()
            val batch = firestore.batch()
            commentsQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            val likesQuery = likesCollection.whereEqualTo("postId", postId).get().await()
            val likesBatch = firestore.batch()
            likesQuery.documents.forEach { doc ->
                likesBatch.delete(doc.reference)
            }
            likesBatch.commit().await()
            
            true
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "Error deleting post: ${e.message}")
            false
        }
    }

    override suspend fun updatePost(postId: String, content: String, imageUrl: String?): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "content" to content,
                "updatedAt" to System.currentTimeMillis()
            )
            
            imageUrl?.let { updates["imageUrl"] = it }
            
            postsCollection.document(postId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "Error updating post: ${e.message}")
            false
        }
    }

    override suspend fun deleteComment(commentId: String, postId: String): Boolean {
        return try {
            commentsCollection.document(commentId).delete().await()
            
            val postRef = postsCollection.document(postId)
            val remainingComments = commentsCollection.whereEqualTo("postId", postId).get().await()
            postRef.update("commentCount", remainingComments.size()).await()
            
            true
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "Error deleting comment: ${e.message}")
            false
        }
    }

    override suspend fun uploadImage(imageUri: Uri, path: String): String {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        val userId = currentUser.uid
        
        val filename = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        
        val storagePath = if (path.startsWith("post_images")) {
            "post_images/$userId/$filename"
        } else {
            "$path/$userId/$filename"
        }
        
        Log.d("FirebaseForumDataSource", "Uploading image to path: $storagePath")
        
        val ref = storage.reference.child(storagePath)
        
        try {
            val uploadTask = ref.putFile(imageUri).await()
            Log.d("FirebaseForumDataSource", "Image uploaded successfully")
            
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("FirebaseForumDataSource", "Download URL obtained: $downloadUrl")
            
            return downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseForumDataSource", "Image upload failed: ${e.message}", e)
            
            when {
                e.message?.contains("403") == true || 
                e.message?.contains("Permission denied") == true -> {
                    throw Exception("Storage permission denied. Please check Firebase Storage rules.")
                }
                e.message?.contains("network") == true -> {
                    throw Exception("Network error during image upload. Please check your connection.")
                }
                else -> {
                    throw Exception("Failed to upload image: ${e.message}")
                }
            }
        }
    }
}
