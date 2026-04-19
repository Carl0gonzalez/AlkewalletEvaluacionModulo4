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
    data class ProfileUpdated(val user: UserEntity) : AuthState()
    data class PhotoUpdated(val user: UserEntity) : AuthState()
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
                    nombre   = nombre,
                    correo   = correo,
                    password = password,
                    saldo    = 0.0
                )
                val id = userRepository.registrar(nuevoUsuario)
                _authState.value = AuthState.RegisterSuccess(id)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar usuario")
            }
        }
    }

    fun cargarUsuario(userId: Int) {
        viewModelScope.launch {
            try {
                val user = userRepository.buscarPorId(userId)
                _currentUser.value = user
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al cargar usuario")
            }
        }
    }

    /**
     * Actualiza nombre y correo del usuario en Room.
     * Valida campos vacíos y que el nuevo correo no esté ocupado por otro usuario.
     */
    fun actualizarPerfil(userId: Int, nuevoNombre: String, nuevoCorreo: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val nombre = nuevoNombre.trim()
                val correo = nuevoCorreo.trim()

                if (nombre.isEmpty() || correo.isEmpty()) {
                    _authState.value = AuthState.Error("El nombre y correo no pueden estar vacíos")
                    return@launch
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    _authState.value = AuthState.Error("Ingresa un correo válido")
                    return@launch
                }
                val existente = userRepository.buscarPorCorreo(correo)
                if (existente != null && existente.id != userId) {
                    _authState.value = AuthState.Error("Ese correo ya está en uso por otro usuario")
                    return@launch
                }

                userRepository.actualizarPerfil(userId, nombre, correo)
                val actualizado = userRepository.buscarPorId(userId)!!
                _currentUser.value = actualizado
                _authState.value = AuthState.ProfileUpdated(actualizado)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al actualizar perfil")
            }
        }
    }

    /**
     * Guarda la URI de la imagen seleccionada desde la galería en Room
     * y actualiza el StateFlow para que la UI reaccione sin necesidad de
     * recargar la actividad.
     */
    fun actualizarFotoPerfil(userId: Int, uriString: String) {
        viewModelScope.launch {
            try {
                userRepository.actualizarFotoPerfil(userId, uriString)
                val actualizado = userRepository.buscarPorId(userId)!!
                _currentUser.value = actualizado
                _authState.value = AuthState.PhotoUpdated(actualizado)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al actualizar foto")
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
