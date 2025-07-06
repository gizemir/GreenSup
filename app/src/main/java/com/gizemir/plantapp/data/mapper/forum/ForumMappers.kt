package com.gizemir.plantapp.data.mapper.forum

import com.gizemir.plantapp.data.remote.dto.forum.CommentDto
import com.gizemir.plantapp.data.remote.dto.forum.PostDto
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.model.forum.Post

fun PostDto.toDomain(isLiked: Boolean = false, comments: List<Comment> = emptyList()): Post {
    return Post(
        id = id,
        userId = userId,
        userName = userName,
        userProfilePicUrl = userProfilePicUrl,
        content = content,
        imageUrl = imageUrl,
        timestamp = timestamp,
        likeCount = likeCount,
        commentCount = commentCount,
        isLikedByCurrentUser = isLiked,
        comments = comments
    )
}

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        userName = userName,
        userProfilePicUrl = userProfilePicUrl,
        content = content,
        timestamp = timestamp
    )
}
