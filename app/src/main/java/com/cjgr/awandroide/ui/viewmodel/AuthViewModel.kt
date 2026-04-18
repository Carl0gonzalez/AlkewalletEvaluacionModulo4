package com.cjgr.awandroide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cjgr.awandroide.data.local.UserEntity
import com.cjgr.awandroide.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val user: UserEntity) : AuthState()
    data class RegisterSuccess(val userId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    fun login(correo: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = userRepository.login(correo, password)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.LoginSuccess(user)
                } else {
                    _authState.value = AuthState.Error("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun registrar(nombre: String, correo: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val existente = userRepository.buscarPorCorreo(correo)
                if (existente != null) {
                    _authState.value = AuthState.Error("El correo ya está registrado")
                    return@launch
                }
                val nuevoUsuario = UserEntity(
                    nombre = nombre,
                    correo = correo,
                    password = password,
                    saldo = 0.0
                )
                val id = userRepository.registrar(nuevoUsuario)
                _authState.value = AuthState.RegisterSuccess(id)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar usuario")
            }
        }
    }

    fun cerrarSesion() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}