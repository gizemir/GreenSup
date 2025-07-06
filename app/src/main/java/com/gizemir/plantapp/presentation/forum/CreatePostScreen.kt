package com.gizemir.plantapp.presentation.forum

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.ui.theme.PlantAppTheme
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
private fun isNetworkAvailable(): Boolean {
    val context = LocalContext.current
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } else {
        @Suppress("DEPRECATION")
        connectivityManager.activeNetworkInfo?.isConnected == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    currentRoute: String = Screen.AddPost.route,
    viewModel: ForumViewModel = hiltViewModel()
) {
    val postCreationState by viewModel.postCreationState.collectAsState()
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val themeManager = remember { ThemeManager(context) }
    
    val tempImageFile = remember {
        File.createTempFile(
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_",
            ".jpg",
            context.cacheDir
        ).apply {
            deleteOnExit()
        }
    }
    
    val imageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onPostImageChange(imageUri)
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(imageUri)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onPostImageChange(it)
        }
    }
    
    if (showImagePickerDialog) {
        Dialog(onDismissRequest = { showImagePickerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Choose Image Source",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    showImagePickerDialog = false
                                    when (PackageManager.PERMISSION_GRANTED) {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) -> {
                                            cameraLauncher.launch(imageUri)
                                        }
                                        else -> {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Camera",
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .padding(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera")
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    showImagePickerDialog = false
                                    galleryLauncher.launch("image/*")
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Gallery",
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .padding(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gallery")
                        }
                    }
                }
            }
        }
    }
    
    val isOnline = isNetworkAvailable()
    
    MainScaffold(
        navController = navController,
        currentRoute = currentRoute,
        topBarTitle = "Create Post",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedButton(
                onClick = { showImagePickerDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = postCreationState.content,
                onValueChange = { viewModel.onPostContentChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                placeholder = { Text("What's on your mind?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            postCreationState.imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.onPostImageChange(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isPosting) {
                        if (!isOnline) {
                            android.widget.Toast.makeText(
                                context,
                                "You are offline. Please check your internet connection.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }
                        
                        isPosting = true
                        
                        val handler = android.os.Handler(android.os.Looper.getMainLooper())
                        val timeoutRunnable = Runnable {
                            if (isPosting) {
                                isPosting = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Operation timed out. Please try again.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        
                        handler.postDelayed(timeoutRunnable, 15000)
                        
                        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val network = connectivityManager.activeNetwork
                            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                        } else {
                            @Suppress("DEPRECATION")
                            connectivityManager.activeNetworkInfo?.isConnected == true
                        }
                        
                        if (!isConnected) {
                            handler.removeCallbacks(timeoutRunnable)
                            isPosting = false
                            android.widget.Toast.makeText(
                                context,
                                "No internet connection. Please check your network.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }
                        
                        val compressedImageUri = postCreationState.imageUri?.let {
                            try {
                                val inputStream = context.contentResolver.openInputStream(it)
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                
                                val maxSize = 1024
                                val width = bitmap.width
                                val height = bitmap.height
                                val scale = maxSize.toFloat() / Math.max(width, height)
                                
                                val matrix = android.graphics.Matrix()
                                matrix.postScale(scale, scale)
                                
                                val resizedBitmap = android.graphics.Bitmap.createBitmap(
                                    bitmap, 0, 0, width, height, matrix, true
                                )
                                
                                val compressedFile = java.io.File.createTempFile(
                                    "compressed_", ".jpg", context.cacheDir
                                )
                                val outputStream = java.io.FileOutputStream(compressedFile)
                                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                                outputStream.close()
                                
                                android.net.Uri.fromFile(compressedFile)
                            } catch (e: Exception) {
                                android.util.Log.e("CreatePostScreen", "Error compressing image: ${e.message}")
                                it
                            }
                        }
                        
                        if (compressedImageUri != null && compressedImageUri != postCreationState.imageUri) {
                            viewModel.onPostImageChange(compressedImageUri)
                        }
                        
                        viewModel.createPost(
                            onSuccess = {
                                handler.removeCallbacks(timeoutRunnable)
                                isPosting = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Post successfully shared!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                navController.previousBackStackEntry?.savedStateHandle?.set("newPostCreated", true)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.AddPost.route) { inclusive = true }
                                }
                            },
                            onError = { error ->
                                handler.removeCallbacks(timeoutRunnable)
                                isPosting = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to create post: $error",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = postCreationState.content.isNotBlank() && !isPosting,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Share Post",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}