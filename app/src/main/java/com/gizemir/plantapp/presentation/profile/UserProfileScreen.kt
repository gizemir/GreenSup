package com.gizemir.plantapp.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.model.forum.Comment
import com.gizemir.plantapp.domain.model.user.User
import com.gizemir.plantapp.presentation.forum.components.PostItem
import com.gizemir.plantapp.presentation.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    currentRoute: String,
    viewModel: UserProfileViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val posts by viewModel.userPosts.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    
    val isOwnProfile = remember(userId, currentUserId) { 
        currentUserId != null && userId == currentUserId 
    }

    LaunchedEffect(userId, currentFirebaseUser?.photoUrl, currentFirebaseUser?.displayName) {
        viewModel.loadUserProfile(userId)
        viewModel.loadUserPosts(userId)
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {

            viewModel.clearError()
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    
    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "Profile",
        isBackButtonVisible = true,
        drawerState = drawerState,
        drawerContent = { closeDrawer ->
            AppDrawer(
                currentRoute = currentRoute,
                navController = navController,
                closeDrawer = closeDrawer,
                themeManager = themeManager
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { 
                        viewModel.clearError()
                        viewModel.loadUserProfile(userId)
                        viewModel.loadUserPosts(userId)
                    }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // User profile header
                    item {
                        UserProfileHeader(
                            user = user, 
                            postsCount = posts.size,
                            isOwnProfile = isOwnProfile,
                            currentFirebaseUser = currentFirebaseUser
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isOwnProfile) "You haven't posted anything yet" else "No posts yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(posts) { post ->
                            UserPostItem(
                                post = post,
                                isOwnProfile = isOwnProfile,
                                currentUserId = currentUserId,
                                onLikeClick = { viewModel.likePost(post) },
                                onCommentClick = { commentText -> 
                                    viewModel.addComment(post, commentText) 
                                },
                                onUserProfileClick = { 
                                    if (post.userId != userId) {
                                        navController.navigate("${Screen.Profile.route}/${post.userId}")
                                    }
                                },
                                onEditClick = { postId, newContent ->
                                    viewModel.editPost(postId, newContent)
                                },
                                onDeleteClick = { 
                                    viewModel.deletePost(post)
                                },
                                onDeleteCommentClick = { comment ->
                                    viewModel.deleteComment(comment, post.id)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    user: User,
    postsCount: Int,
    isOwnProfile: Boolean,
    currentFirebaseUser: com.google.firebase.auth.FirebaseUser?
) {
    val displayName = if (isOwnProfile && currentFirebaseUser != null) {
        currentFirebaseUser.displayName ?: user.name
    } else {
        user.name
    }
    
    val profileImageUrl = if (isOwnProfile && currentFirebaseUser != null) {
        currentFirebaseUser.photoUrl?.toString() ?: user.profilePictureUrl
    } else {
        user.profilePictureUrl
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            if (profileImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = displayName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            user.bio?.let { userBio ->
                Text(
                    text = userBio,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("$postsCount posts")
            }
        }
    }
}

@Composable
private fun UserPostItem(
    post: Post,
    isOwnProfile: Boolean,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onCommentClick: (String) -> Unit,
    onUserProfileClick: () -> Unit,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteCommentClick: (Comment) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            PostItem(
                post = post,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onUserProfileClick = onUserProfileClick,
                showCommentActions = true,
                currentUserId = currentUserId,
                onDeleteCommentClick = onDeleteCommentClick
            )
            
            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Post",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        PostEditDialog(
            currentContent = post.content,
            onConfirm = { newContent ->
                onEditClick(post.id, newContent)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PostEditDialog(
    currentContent: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedContent by remember { mutableStateOf(currentContent) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Post") },
        text = {
            Column {
                Text(
                    text = "Edit your post content:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    placeholder = { Text("What's on your mind?") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(editedContent) },
                enabled = editedContent.isNotBlank() && editedContent != currentContent
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
