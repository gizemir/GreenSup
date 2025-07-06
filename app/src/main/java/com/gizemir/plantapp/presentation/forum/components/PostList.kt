package com.gizemir.plantapp.presentation.forum.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gizemir.plantapp.domain.model.forum.Post

@Composable
fun PostList(
    posts: List<Post>,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post, String) -> Unit,
    onUserProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Forum",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            posts.forEach { post ->
                PostItem(
                    post = post,
                    onLikeClick = { onLikeClick(post) },
                    onCommentClick = { comment -> onCommentClick(post, comment) },
                    onUserProfileClick = { onUserProfileClick(post.userId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

