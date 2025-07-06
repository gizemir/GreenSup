package com.gizemir.plantapp.domain.use_case.auth

import com.gizemir.plantapp.domain.model.auth.AuthResult
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(displayName: String, bio: String): Flow<AuthResult<Unit>> {
        return flow {
            try {
                val user = authRepository.updateProfile(displayName, bio)
                emit(AuthResult.Success(Unit))
            } catch (e: Exception) {
                emit(AuthResult.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }
}

