package com.gizemir.plantapp.data.local.entity.forum

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.gizemir.plantapp.domain.model.forum.Comment
import java.util.Date

@Entity(
    tableName = "forum_comments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["postId"])
    ]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userProfilePicUrl: String?,
    val content: String,
    val timestamp: Long,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toComment(): Comment {
        return Comment(
            id = id,
            postId = postId,
            userId = userId,
            userName = userName,
            userProfilePicUrl = userProfilePicUrl,
            content = content,
            timestamp = Date(timestamp)
        )
    }
    
    companion object {
        fun fromComment(comment: Comment): CommentEntity {
            return CommentEntity(
                id = comment.id,
                postId = comment.postId,
                userId = comment.userId,
                userName = comment.userName,
                userProfilePicUrl = comment.userProfilePicUrl,
                content = comment.content,
                timestamp = comment.timestamp.time
            )
        }
    }
} 