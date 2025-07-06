package com.gizemir.plantapp.presentation.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gizemir.plantapp.presentation.forum.components.PostItem
import com.gizemir.plantapp.presentation.navigation.Screen

@Composable
fun ForumScreen(
    navController: NavController,
    viewModel: ForumViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val forumState by viewModel.forumState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    forumState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        
        when {
            forumState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            forumState.error != null -> {
                Text(
                    text = forumState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            forumState.posts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No posts yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to share something!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    
                    items(forumState.posts) { post ->
                        PostItem(
                            post = post,
                            onLikeClick = { viewModel.likePost(post) },
                            onCommentClick = { content -> viewModel.commentPost(post.id, content) },
                            onUserProfileClick = { navController.navigate("${Screen.Profile.route}/${post.userId}") },
                            showCommentActions = true,
                            currentUserId = currentUserId,
                            onDeleteCommentClick = { comment -> 
                                viewModel.deleteComment(comment)
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { navController.navigate(Screen.AddPost.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Post",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
