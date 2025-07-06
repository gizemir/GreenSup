package com.gizemir.plantapp.data.local.dao.forum

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.forum.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>
    
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    suspend fun getAllPosts(): List<PostEntity>
    
    @Query("SELECT * FROM forum_posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?
    
    @Query("SELECT * FROM forum_posts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getPostsByUserId(userId: String): List<PostEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)
    
    @Update
    suspend fun updatePost(post: PostEntity)
    
    @Query("UPDATE forum_posts SET likeCount = :likeCount, isLikedByCurrentUser = :isLiked WHERE id = :postId")
    suspend fun updatePostLike(postId: String, likeCount: Int, isLiked: Boolean)
    
    @Query("UPDATE forum_posts SET commentCount = :commentCount WHERE id = :postId")
    suspend fun updatePostCommentCount(postId: String, commentCount: Int)
    
    @Delete
    suspend fun deletePost(post: PostEntity)
    
    @Query("DELETE FROM forum_posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
    
    @Query("DELETE FROM forum_posts WHERE cached_at < :expireTime")
    suspend fun deleteExpiredPosts(expireTime: Long)
    
    @Query("DELETE FROM forum_posts")
    suspend fun deleteAllPosts()
    
    @Query("SELECT COUNT(*) FROM forum_posts")
    suspend fun getPostCount(): Int
    
    @Query("SELECT * FROM forum_posts ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentPosts(limit: Int): List<PostEntity>
} 