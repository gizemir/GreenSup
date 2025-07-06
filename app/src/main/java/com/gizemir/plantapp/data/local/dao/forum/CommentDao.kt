package com.gizemir.plantapp.data.local.dao.forum

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.forum.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    
    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY timestamp ASC")
    suspend fun getCommentsByPostId(postId: String): List<CommentEntity>
    
    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsByPostIdFlow(postId: String): Flow<List<CommentEntity>>
    
    @Query("SELECT * FROM forum_comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): CommentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)
    
    @Delete
    suspend fun deleteComment(comment: CommentEntity)
    
    @Query("DELETE FROM forum_comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)
    
    @Query("DELETE FROM forum_comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: String)
    
    @Query("DELETE FROM forum_comments WHERE cached_at < :expireTime")
    suspend fun deleteExpiredComments(expireTime: Long)
    
    @Query("DELETE FROM forum_comments")
    suspend fun deleteAllComments()
    
    @Query("SELECT COUNT(*) FROM forum_comments WHERE postId = :postId")
    suspend fun getCommentCountByPostId(postId: String): Int
} 