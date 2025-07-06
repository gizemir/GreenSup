package com.gizemir.plantapp.presentation.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.auth.AuthResult
import com.gizemir.plantapp.domain.use_case.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    var state by mutableStateOf(LoginState())
        private set
    
    fun updateEmail(email: String) {
        state = state.copy(email = email)
    }
    
    fun updatePassword(password: String) {
        state = state.copy(password = password)
    }
    
    fun login() {
        if (state.email.isBlank() || state.password.isBlank()) {
            state = state.copy(errorMessage = "Email ve şifre boş olamaz")
            return
        }
        
        viewModelScope.launch {
            loginUseCase(state.email, state.password)
                .onEach { result ->
                    when (result) {
                        is AuthResult.Loading -> {
                            state = state.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                        is AuthResult.Success -> {
                            state = state.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                errorMessage = null
                            )
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

