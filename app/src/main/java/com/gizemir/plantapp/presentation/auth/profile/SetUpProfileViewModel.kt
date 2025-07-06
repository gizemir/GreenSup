package com.gizemir.plantapp.presentation.auth.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.auth.AuthResult
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.use_case.auth.RegisterUseCase
import com.gizemir.plantapp.domain.use_case.auth.UpdateProfileUseCase
import com.gizemir.plantapp.domain.use_case.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val bio: String = "",  // Added bio field
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    var state by mutableStateOf(ProfileState())
        private set
    
    fun updateDisplayName(displayName: String) {
        state = state.copy(displayName = displayName)
    }
    
    fun updateEmail(email: String) {
        state = state.copy(email = email)
    }
    
    fun updatePassword(password: String) {
        state = state.copy(password = password)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        state = state.copy(confirmPassword = confirmPassword)
    }
    
    fun updateBio(bio: String) {
        state = state.copy(bio = bio)
    }
    
    fun register() {
        if (state.displayName.isBlank() || state.email.isBlank() ||
            state.password.isBlank() || state.confirmPassword.isBlank()) {
            state = state.copy(errorMessage = "Tüm alanların doldurulması gerekiyor")
            return
        }
        
        if (state.password != state.confirmPassword) {
            state = state.copy(errorMessage = "Şifreler eşleşmiyor")
            return
        }
        
        viewModelScope.launch {
            registerUseCase(state.email, state.password, state.displayName)
                .onEach { result -> 
                    when (result) {
                        is AuthResult.Loading -> {
                            state = state.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                        is AuthResult.Success -> {
                            updateProfileUseCase(state.displayName, state.bio)
                                .onEach { profileResult -> 
                                    when (profileResult) {
                                        is AuthResult.Loading -> {
                                        }
                                        is AuthResult.Success -> {
                                            state = state.copy(
                                                isLoading = false,
                                                isSuccess = true,
                                                errorMessage = null
                                            )
                                        }
                                        is AuthResult.Error -> {
                                            state = state.copy(
                                                isLoading = false,
                                                errorMessage = "Profil güncellenirken bir hata oluştu: ${profileResult.message}"
                                            )
                                        }
                                    }
                                }.launchIn(viewModelScope)
                        }
                        is AuthResult.Error -> {
                            state = state.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }
}

