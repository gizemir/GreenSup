package com.gizemir.plantapp.presentation.auth.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.gizemir.plantapp.presentation.navigation.Screen
import com.gizemir.plantapp.presentation.common.MainScaffold
import com.gizemir.plantapp.presentation.common.AppDrawer
import com.gizemir.plantapp.presentation.ui.theme.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetUpProfileScreen(
    navController: NavController
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(currentUser) {
        currentUser?.photoUrl?.let { uri ->
            profileImageUri = uri
        }
    }
    
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
            profileImageUri = imageUri
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
            profileImageUri = it
        }
    }
    
    suspend fun uploadProfileImage(uri: Uri): String? {
        return try {
            val storage = FirebaseStorage.getInstance()
            val userId = currentUser?.uid ?: return null
            
            val imageRef = storage.reference.child("profile_images/$userId/profile.jpg")
            
            android.util.Log.d("SetUpProfile", "Starting image upload for user: $userId")
            android.util.Log.d("SetUpProfile", "Storage path: profile_images/$userId/profile.jpg")
            
            val uploadTask = imageRef.putFile(uri)
            uploadTask.await()
            
            android.util.Log.d("SetUpProfile", "Image uploaded successfully")
            
            val downloadUrl = imageRef.downloadUrl.await().toString()
            android.util.Log.d("SetUpProfile", "Download URL obtained: $downloadUrl")
            
            return downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("SetUpProfile", "Image upload failed: ${e.message}", e)
            
            if (e.message?.contains("403") == true || e.message?.contains("Permission denied") == true) {
                android.util.Log.e("SetUpProfile", "Firebase Storage permission denied. Check Storage Rules.")
            }
            
            return null
        }
    }
    
    if (showImagePickerDialog) {
        Dialog(onDismissRequest = { showImagePickerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Profile Picture",
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
    
    MainScaffold(
        navController = navController,
        currentRoute = Screen.SetUpProfile.route,
        topBarTitle = "Profile Settings",
        isBackButtonVisible = true,
        drawerState = drawerState,
        drawerContent = { closeDrawer ->
            AppDrawer(
                currentRoute = Screen.SetUpProfile.route,
                navController = navController,
                closeDrawer = closeDrawer,
                themeManager = themeManager
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    if (profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { showImagePickerDialog = true },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { showImagePickerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (displayName.isNotEmpty()) displayName.first().toString().uppercase() else "U",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Picture",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(4.dp)
                            .clickable { showImagePickerDialog = true },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = if (displayName.isNotEmpty()) displayName else "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 100.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (displayName.isBlank()) {
                                errorMessage = "Full name cannot be empty"
                                return@Button
                            }
                            
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            
                            coroutineScope.launch {
                                try {
                                    val photoUrl = profileImageUri?.let { uri ->
                                        if (uri.toString().startsWith("https://")) {
                                            uri.toString()
                                        } else {
                                            android.util.Log.d("SetUpProfile", "Attempting to upload new profile image")
                                            val uploadResult = uploadProfileImage(uri)
                                            
                                            if (uploadResult == null) {
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    errorMessage = "Failed to upload image. Please check your internet connection and Firebase Storage permissions."
                                                }
                                                return@launch
                                            }
                                            
                                            uploadResult
                                        }
                                    }
                                    
                                    withContext(Dispatchers.Main) {
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName)
                                            .apply {
                                                photoUrl?.let { setPhotoUri(Uri.parse(it)) }
                                            }
                                            .build()
                                        
                                        currentUser?.updateProfile(profileUpdates)
                                            ?.addOnSuccessListener {
                                                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                val userUpdates = mutableMapOf<String, Any>(
                                                    "name" to displayName
                                                )
                                                photoUrl?.let { userUpdates["profilePictureUrl"] = it }
                                                
                                                firestore.collection("users").document(currentUser.uid)
                                                    .update(userUpdates)
                                                    .addOnSuccessListener {
                                                        isLoading = false
                                                        successMessage = "Profile updated successfully"
                                                        android.util.Log.d("SetUpProfile", "Profile and Firestore updated successfully")
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        isLoading = false
                                                        successMessage = "Profile updated, but some data sync failed"
                                                        android.util.Log.e("SetUpProfile", "Firestore update failed: ${exception.message}")
                                                    }
                                            }
                                            ?.addOnFailureListener { exception ->
                                                isLoading = false
                                                errorMessage = "Failed to update profile: ${exception.localizedMessage}"
                                                android.util.Log.e("SetUpProfile", "Profile update failed: ${exception.message}")
                                            }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        
                                        errorMessage = when {
                                            e.message?.contains("403") == true || 
                                            e.message?.contains("Permission denied") == true -> {
                                                "Storage permission denied. Please check Firebase Storage rules."
                                            }
                                            e.message?.contains("network") == true -> {
                                                "Network error. Please check your internet connection."
                                            }
                                            else -> {
                                                "Failed to upload image: ${e.localizedMessage}"
                                            }
                                        }
                                        
                                        android.util.Log.e("SetUpProfile", "Profile update error: ${e.message}", e)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Update Profile")
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Password fields cannot be empty"
                                return@Button
                            }
                            
                            if (newPassword != confirmPassword) {
                                errorMessage = "New passwords do not match"
                                return@Button
                            }
                            
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            
                            val credential = com.google.firebase.auth.EmailAuthProvider
                                .getCredential(currentUser?.email ?: "", currentPassword)
                            
                            currentUser?.reauthenticate(credential)
                                ?.addOnSuccessListener {
                                    currentUser.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            successMessage = "Password changed successfully"
                                        }
                                        .addOnFailureListener { exception ->
                                            isLoading = false
                                            errorMessage = "Failed to change password: ${exception.localizedMessage}"
                                        }
                                }
                                ?.addOnFailureListener { exception ->
                                    isLoading = false
                                    errorMessage = "Current password is incorrect: ${exception.localizedMessage}"
                                }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Change Password")
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Log Out")
                }
            }
        }
    }
}

