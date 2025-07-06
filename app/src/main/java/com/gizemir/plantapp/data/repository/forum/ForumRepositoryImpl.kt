package com.gizemir.plantapp.data.repository.forum

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.data.local.dao.forum.PostDao
import com.gizemir.plantapp.data.local.dao.forum.CommentDao
import com.gizemir.plantapp.data.local.entity.forum.PostEntity
import com.gizemir.plantapp.data.local.entity.forum.CommentEntity
import com.gizemir.plantapp.data.mapper.forum.toDomain
import com.gizemir.plantapp.data.source.forum.ForumRemoteDataSource
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val remoteDataSource: ForumRemoteDataSource,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val postDao: PostDao,
    private val commentDao: CommentDao
) : ForumRepository {

    companion object {
        private const val TAG = "ForumRepository"
        private const val CACHE_EXPIRY_TIME = 30 * 60 * 1000L // 30 dakika
    }


    override fun getAllPosts(): Flow<List<Post>> {
        return combine(
            getCachedPostsFlow(),
            getFirebasePostsFlow()
        ) { cachedPosts, firebasePosts ->
            if (firebasePosts.isNotEmpty()) {
                savePosts(firebasePosts)
                firebasePosts
            } else {
                cachedPosts
            }
        }.onStart {
            val cachedPosts = getCachedPosts()
            if (cachedPosts.isNotEmpty()) {
                emit(cachedPosts)
            }
        }
    }

    private fun getCachedPostsFlow(): Flow<List<Post>> {
        return postDao.getAllPostsFlow().map { postEntities ->
            postEntities.map { postEntity ->
                val comments = commentDao.getCommentsByPostId(postEntity.id).map { it.toComment() }
                postEntity.toPost(comments)
            }
        }
    }

    private fun getFirebasePostsFlow(): Flow<List<Post>> {
        return remoteDataSource.getAllPosts().map { postDtos ->
            postDtos.map { postDto ->
                val currentUserId = auth.currentUser?.uid ?: ""
                val isLiked = try {
                    remoteDataSource.isPostLikedByUser(postDto.id, currentUserId)
                } catch (e: Exception) {
                    Log.e(TAG, "Like status kontrol hatası: ${e.message}")
                    false
                }
                
                val comments = try {
                    remoteDataSource.getCommentsForPost(postDto.id).map { it.toDomain() }
                } catch (e: Exception) {
                    Log.e(TAG, "Comment alma hatası: ${e.message}")
                    commentDao.getCommentsByPostId(postDto.id).map { it.toComment() }
                }
                
                postDto.toDomain(isLiked, comments)
            }
        }
    }

    private suspend fun getCachedPosts(): List<Post> {
        return try {
            val postEntities = postDao.getAllPosts()
            postEntities.map { postEntity ->
                val comments = commentDao.getCommentsByPostId(postEntity.id).map { it.toComment() }
                postEntity.toPost(comments)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cache'den post alma hatası: ${e.message}")
            emptyList()
        }
    }

    private suspend fun savePosts(posts: List<Post>) {
        try {
            val postEntities = posts.map { PostEntity.fromPost(it) }
            postDao.insertPosts(postEntities)
            
            posts.forEach { post ->
                val commentEntities = post.comments.map { CommentEntity.fromComment(it) }
                commentDao.insertComments(commentEntities)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cache kaydetme hatası: ${e.message}")
        }
    }

    override suspend fun getPostById(postId: String): Post {
        return try {
            val cachedPost = postDao.getPostById(postId)
            if (cachedPost != null && !isExpired(cachedPost.cached_at)) {
                val comments = commentDao.getCommentsByPostId(postId).map { it.toComment() }
                return cachedPost.toPost(comments)
            }
            
            val postDto = remoteDataSource.getPostById(postId)
            val currentUserId = auth.currentUser?.uid ?: ""
            val isLiked = remoteDataSource.isPostLikedByUser(postId, currentUserId)
            val comments = remoteDataSource.getCommentsForPost(postId).map { it.toDomain() }
            
            val post = postDto.toDomain(isLiked, comments)
            
            postDao.insertPost(PostEntity.fromPost(post))
            val commentEntities = comments.map { CommentEntity.fromComment(it) }
            commentDao.insertComments(commentEntities)
            
            post
        } catch (e: Exception) {
            Log.e(TAG, "getPostById hatası: ${e.message}")
            val cachedPost = postDao.getPostById(postId)
            if (cachedPost != null) {
                val comments = commentDao.getCommentsByPostId(postId).map { it.toComment() }
                cachedPost.toPost(comments)
            } else {
                throw e
            }
        }
    }

    override suspend fun createPost(content: String, imageUri: Uri?): Post {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val postDto = remoteDataSource.createPost(currentUserId, content, imageUri)
            val post = postDto.toDomain()
            
            postDao.insertPost(PostEntity.fromPost(post))
            
            post
        } catch (e: Exception) {
            Log.e(TAG, "createPost hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun likePost(postId: String): Post {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val newLikeCount = remoteDataSource.likePost(postId, currentUserId)
            
            postDao.updatePostLike(postId, newLikeCount, true)
            
            getPostById(postId)
        } catch (e: Exception) {
            Log.e(TAG, "likePost hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun unlikePost(postId: String): Post {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val newLikeCount = remoteDataSource.unlikePost(postId, currentUserId)
            
            postDao.updatePostLike(postId, newLikeCount, false)
            
            getPostById(postId)
        } catch (e: Exception) {
            Log.e(TAG, "unlikePost hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun addComment(postId: String, content: String): Comment {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val commentDto = remoteDataSource.addComment(postId, currentUserId, content)
            val comment = commentDto.toDomain()
            
            commentDao.insertComment(CommentEntity.fromComment(comment))
            
            val commentCount = commentDao.getCommentCountByPostId(postId)
            postDao.updatePostCommentCount(postId, commentCount)
            
            comment
        } catch (e: Exception) {
            Log.e(TAG, "addComment hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun getUserPosts(userId: String): List<Post> {
        return try {
            val cachedPosts = postDao.getPostsByUserId(userId)
            if (cachedPosts.isNotEmpty() && cachedPosts.all { !isExpired(it.cached_at) }) {
                return cachedPosts.map { postEntity ->
                    val comments = commentDao.getCommentsByPostId(postEntity.id).map { it.toComment() }
                    postEntity.toPost(comments)
                }
            }
            
            val postDtos = remoteDataSource.getUserPosts(userId)
            val posts = postDtos.map { it.toDomain() }
            
            savePosts(posts)
            
            posts
        } catch (e: Exception) {
            Log.e(TAG, "getUserPosts hatası: ${e.message}")
            val cachedPosts = postDao.getPostsByUserId(userId)
            cachedPosts.map { postEntity ->
                val comments = commentDao.getCommentsByPostId(postEntity.id).map { it.toComment() }
                postEntity.toPost(comments)
            }
        }
    }

    override suspend fun deletePost(postId: String): Boolean {
        return try {
            val success = remoteDataSource.deletePost(postId)
            if (success) {

                postDao.deletePostById(postId)
                commentDao.deleteCommentsByPostId(postId)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "deletePost hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun updatePost(postId: String, content: String, imageUrl: String?): Boolean {
        return try {
            val success = remoteDataSource.updatePost(postId, content, imageUrl)
            if (success) {
                val cachedPost = postDao.getPostById(postId)
                cachedPost?.let { post ->
                    val updatedPost = post.copy(
                        content = content,
                        imageUrl = imageUrl ?: post.imageUrl
                    )
                    postDao.updatePost(updatedPost)
                }
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "updatePost hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteComment(commentId: String, postId: String): Boolean {
        return try {
            val success = remoteDataSource.deleteComment(commentId, postId)
            if (success) {
                commentDao.deleteCommentById(commentId)
                
                val remainingComments = commentDao.getCommentsByPostId(postId)
                postDao.updatePostCommentCount(postId, remainingComments.size)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "deleteComment hatası: ${e.message}")
            throw e
        }
    }

    override suspend fun getPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        
        try {
            val cachedPosts = getCachedPosts()
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            }
            
            getAllPosts().collect { posts ->
                emit(Resource.Success(posts))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPosts hatası: ${e.message}")
            val cachedPosts = getCachedPosts()
            if (cachedPosts.isNotEmpty()) {
                emit(Resource.Success(cachedPosts))
            } else {
                emit(Resource.Error(e.message ?: "Forum yükleme hatası"))
            }
        }
    }

    suspend fun clearExpiredCache() {
        try {
            val expireTime = System.currentTimeMillis() - CACHE_EXPIRY_TIME
            postDao.deleteExpiredPosts(expireTime)
            commentDao.deleteExpiredComments(expireTime)
        } catch (e: Exception) {
            Log.e(TAG, "Forum cache temizleme hatası: ${e.message}")
        }
    }

    suspend fun clearAllCache() {
        try {
            postDao.deleteAllPosts()
            commentDao.deleteAllComments()
        } catch (e: Exception) {
            Log.e(TAG, "Forum cache temizleme hatası: ${e.message}")
        }
    }

    private fun isExpired(cachedTime: Long): Boolean {
        return (System.currentTimeMillis() - cachedTime) > CACHE_EXPIRY_TIME
    }
}
