package com.gizemir.plantapp.data.local.entity.forum

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.forum.Post
import java.util.Date

@Entity(tableName = "forum_posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userProfilePicUrl: String?,
    val content: String,
    val imageUrl: String?,
    val timestamp: Long,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean = false,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toPost(comments: List<com.gizemir.plantapp.domain.model.forum.Comment> = emptyList()): Post {
        return Post(
            id = id,
            userId = userId,
            userName = userName,
            userProfilePicUrl = userProfilePicUrl,
            content = content,
            imageUrl = imageUrl,
            timestamp = Date(timestamp),
            likeCount = likeCount,
            commentCount = commentCount,
            isLikedByCurrentUser = isLikedByCurrentUser,
            comments = comments
        )
    }
    
    companion object {
        fun fromPost(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                userId = post.userId,
                userName = post.userName,
                userProfilePicUrl = post.userProfilePicUrl,
                content = post.content,
                imageUrl = post.imageUrl,
                timestamp = post.timestamp.time,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                isLikedByCurrentUser = post.isLikedByCurrentUser
            )
        }
    }
} 