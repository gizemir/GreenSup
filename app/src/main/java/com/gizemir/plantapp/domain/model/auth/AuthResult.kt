package com.gizemir.plantapp.domain.model.auth

sealed class AuthResult<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T? = null): AuthResult<T>(data)
    class Error<T>(message: String, data: T? = null): AuthResult<T>(data, message)
    class Loading<T>: AuthResult<T>()
}

