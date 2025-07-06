package com.gizemir.plantapp.domain.model.user

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val plantCount: Int = 0
)

