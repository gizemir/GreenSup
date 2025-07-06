package com.gizemir.plantapp.presentation.forum.state

import android.net.Uri

data class PostCreationState(
    val content: String = "",
    val imageUri: Uri? = null,
    val isPosting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
