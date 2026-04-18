package com.cjgr.awandroide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cjgr.awandroide.data.local.TransactionEntity
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class TransactionState {
    object Idle : TransactionState()
    object Loading : TransactionState()
    data class Success(val message: String) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Idle)
    val transactionState: StateFlow<TransactionState> = _transactionState

    private val _transacciones = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transacciones: StateFlow<List<TransactionEntity>> = _transacciones

    private val _balanceCalculado = MutableStateFlow<Double?>(null)
    val balanceCalculado: StateFlow<Double?> = _balanceCalculado

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun cargarTransacciones(userId: Int) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            try {
                val lista = transactionRepository.obtenerTransaccionesPorUsuario(userId)

                val listaOrdenada = lista.sortedByDescending { tx ->
                    try { dateFormat.parse(tx.fecha) } catch (e: Exception) { null }
                }
                _transacciones.value = listaOrdenada

                if (lista.isNotEmpty()) {
                    val balanceApi = lista.sumOf { it.monto }
                    _balanceCalculado.value = balanceApi
                    userRepository.actualizarSaldo(userId, balanceApi)
                }

                _transactionState.value = TransactionState.Idle
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(
                    e.message ?: "Error al cargar transacciones"
                )
            }
        }
    }

    fun ingresarDinero(userId: Int, monto: Double, fecha: String, descripcion: String) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            try {
                val usuario = userRepository.buscarPorId(userId)
                if (usuario == null) {
                    _transactionState.value = TransactionState.Error("Usuario no encontrado")
                    return@launch
                }
                val transaccion = TransactionEntity(
                    userId = userId,
                    fecha = fecha,
                    monto = monto,
                    descripcion = descripcion,
                    tipo = "ingreso"
                )
                transactionRepository.enviarTransaccion(transaccion)
                userRepository.actualizarSaldo(userId, usuario.saldo + monto)
                cargarTransacciones(userId)
                _transactionState.value = TransactionState.Success("Dinero ingresado con éxito")
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(
                    e.message ?: "Error al ingresar dinero"
                )
            }
        }
    }

    /**
     * Valida que el destinatario exista en Room (usuarios previamente registrados)
     * antes de ejecutar la transferencia. Acepta descripcion como parámetro.
     */
    fun realizarTransferencia(
        userId: Int,
        destinatarioCorreo: String,
        monto: Double,
        fecha: String,
        descripcion: String = "Transferencia",
        userRepository: UserRepository
    ) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            try {
                val remitente    = userRepository.buscarPorId(userId)
                val destinatario = userRepository.buscarPorCorreo(destinatarioCorreo)

                if (remitente == null) {
                    _transactionState.value = TransactionState.Error("Usuario no encontrado")
                    return@launch
                }
                // Destinatario debe existir en la BD local (usuario registrado previamente)
                if (destinatario == null) {
                    _transactionState.value = TransactionState.Error(
                        "El destinatario no está registrado en la aplicación"
                    )
                    return@launch
                }
                if (remitente.saldo < monto) {
                    _transactionState.value = TransactionState.Error("Saldo insuficiente")
                    return@launch
                }

                val transaccionEgreso = TransactionEntity(
                    userId = userId,
                    fecha = fecha,
                    monto = -monto,
                    descripcion = "Para: ${destinatario.nombre} — $descripcion",
                    tipo = "egreso"
                )
                val transaccionIngreso = TransactionEntity(
                    userId = destinatario.id,
                    fecha = fecha,
                    monto = monto,
                    descripcion = "De: ${remitente.nombre} — $descripcion",
                    tipo = "ingreso"
                )

                transactionRepository.enviarTransaccion(transaccionEgreso)
                transactionRepository.enviarTransaccion(transaccionIngreso)
                userRepository.actualizarSaldo(userId, remitente.saldo - monto)
                userRepository.actualizarSaldo(destinatario.id, destinatario.saldo + monto)

                cargarTransacciones(userId)
                _transactionState.value = TransactionState.Success(
                    "Transferencia a ${destinatario.nombre} realizada con éxito"
                )
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error(
                    e.message ?: "Error al realizar transferencia"
                )
            }
        }
    }

    fun resetState() {
        _transactionState.value = TransactionState.Idle
    }
}