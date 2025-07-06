package com.gizemir.plantapp.presentation.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.gizemir.plantapp.presentation.notification.NotificationViewModel
import com.gizemir.plantapp.presentation.notification.NotificationPanel
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.gizemir.plantapp.presentation.ui.theme.NavBarGreen
import com.gizemir.plantapp.presentation.ui.theme.NavBarYellowGreen
import com.gizemir.plantapp.presentation.ui.theme.NavBarLimeYellow
import com.gizemir.plantapp.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    currentRoute: String?,
    topBarTitle: String,
    topBarActions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    drawerState: DrawerState,
    drawerContent: @Composable (closeDrawer: () -> Unit) -> Unit,
    isBackButtonVisible: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    
    val showNotificationPanel by notificationViewModel.showPanel.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        drawerContent { scope.launch { drawerState.close() } }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { 
                                    Text(
                                        topBarTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier.background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            NavBarGreen,
                                            NavBarYellowGreen,
                                            NavBarLimeYellow
                                        )
                                    )
                                ),
                                navigationIcon = {
                                    if (isBackButtonVisible) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                Icons.Filled.ArrowBack, 
                                                contentDescription = "Geri",
                                                tint = Color.White
                                            )
                                        }
                                    } else {
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser != null) {
                                            IconButton(onClick = { 
                                                val currentUserId = currentUser.uid
                                                navController.navigate("profile/$currentUserId")
                                            }) {
                                                Icon(
                                                    Icons.Filled.AccountCircle, 
                                                    contentDescription = "Profil",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    topBarActions()
                                    
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    if (currentUser != null) {
                                        Box {
                                            IconButton(onClick = { 
                                                notificationViewModel.togglePanel() 
                                            }) {
                                                Icon(
                                                    Icons.Filled.Notifications, 
                                                    contentDescription = "Notifications",
                                                    tint = Color.White
                                                )
                                                
                                                val unreadCount by notificationViewModel.unreadNotificationCount.collectAsState()
                                                if (unreadCount > 0) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .offset(x = 8.dp, y = (-8).dp)
                                                            .clip(CircleShape),
                                                        color = MaterialTheme.colorScheme.error
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    }) {
                                        Icon(
                                            Icons.Filled.Menu, 
                                            contentDescription = "Menüyü Aç",
                                            tint = Color.White
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            BottomNavBar(navController = navController, currentRoute = currentRoute ?: "")
                        },
                        floatingActionButton = floatingActionButton
                    ) { innerPadding ->
                        content(innerPadding)
                    }
                    
                    if (showNotificationPanel) {
                        val notifications by notificationViewModel.notifications.collectAsState()
                        val unreadCount by notificationViewModel.unreadCount.collectAsState()
                        val isLoading by notificationViewModel.isLoading.collectAsState()
                        
                        NotificationPanel(
                            notifications = notifications,
                            unreadCount = unreadCount,
                            isLoading = isLoading,
                            onMarkAsRead = { notificationId ->
                                notificationViewModel.markAsRead(notificationId)
                            },
                            onMarkAllAsRead = {
                                notificationViewModel.markAllAsRead()
                            },
                            onDeleteNotification = { notificationId ->
                                notificationViewModel.deleteNotification(notificationId)
                            },
                            onNotificationClick = { _ ->
                            },
                            onClose = {
                                notificationViewModel.closePanel()
                            },
                            navController = navController,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}
