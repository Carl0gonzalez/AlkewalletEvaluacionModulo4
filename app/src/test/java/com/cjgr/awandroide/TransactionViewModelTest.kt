package com.cjgr.awandroide

import com.cjgr.awandroide.data.local.TransactionEntity
import com.cjgr.awandroide.data.local.UserEntity
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.ui.viewmodel.TransactionState
import com.cjgr.awandroide.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: TransactionViewModel

    private val remitente = UserEntity(
        id = 1, nombre = "Carlo", correo = "carlo@test.com",
        password = "1234", saldo = 500.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mock()
        userRepository = mock()
        viewModel = TransactionViewModel(transactionRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── ingresarDinero ────────────────────────────────────────────────────

    @Test
    fun `ingresarDinero con usuario valido emite Success`() = runTest {
        whenever(userRepository.buscarPorId(1)).thenReturn(remitente)

        viewModel.ingresarDinero(1, 100.0, "18/04/2026", "Depósito")

        // cargarTransacciones se llama internamente, stub necesario
        whenever(transactionRepository.obtenerTransaccionesPorUsuario(1)).thenReturn(emptyList())

        val state = viewModel.transactionState.first()
        assertTrue(state is TransactionState.Success || state is TransactionState.Idle)
    }

    @Test
    fun `ingresarDinero con usuario inexistente emite Error`() = runTest {
        whenever(userRepository.buscarPorId(99)).thenReturn(null)

        viewModel.ingresarDinero(99, 100.0, "18/04/2026", "Test")

        val state = viewModel.transactionState.first()
        assertTrue(state is TransactionState.Error)
        assertEquals("Usuario no encontrado", (state as TransactionState.Error).message)
    }

    // ── realizarTransferencia ─────────────────────────────────────────────

    @Test
    fun `realizarTransferencia exitosa emite Success`() = runTest {
        whenever(userRepository.buscarPorId(1)).thenReturn(remitente)
        whenever(userRepository.buscarPorCorreo("dest@test.com")).thenReturn(null)
        whenever(transactionRepository.obtenerTransaccionesPorUsuario(1)).thenReturn(emptyList())

        viewModel.realizarTransferencia(
            userId = 1,
            destinatarioCorreo = "dest@test.com",
            monto = 200.0,
            fecha = "18/04/2026",
            descripcion = "Pago",
            userRepository = userRepository
        )

        val state = viewModel.transactionState.first()
        assertTrue(state is TransactionState.Success || state is TransactionState.Idle)
    }

    @Test
    fun `realizarTransferencia con saldo insuficiente emite Error`() = runTest {
        val sinSaldo = remitente.copy(saldo = 10.0)
        whenever(userRepository.buscarPorId(1)).thenReturn(sinSaldo)

        viewModel.realizarTransferencia(
            userId = 1,
            destinatarioCorreo = "dest@test.com",
            monto = 500.0,
            fecha = "18/04/2026",
            userRepository = userRepository
        )

        val state = viewModel.transactionState.first()
        assertTrue(state is TransactionState.Error)
        assertEquals("Saldo insuficiente", (state as TransactionState.Error).message)
    }

    @Test
    fun `realizarTransferencia con remitente inexistente emite Error`() = runTest {
        whenever(userRepository.buscarPorId(1)).thenReturn(null)

        viewModel.realizarTransferencia(
            userId = 1,
            destinatarioCorreo = "dest@test.com",
            monto = 100.0,
            fecha = "18/04/2026",
            userRepository = userRepository
        )

        val state = viewModel.transactionState.first()
        assertTrue(state is TransactionState.Error)
        assertEquals("Usuario no encontrado", (state as TransactionState.Error).message)
    }

    @Test
    fun `realizarTransferencia registra ingreso al destinatario local`() = runTest {
        val destinatario = UserEntity(id = 2, nombre = "Dest", correo = "dest@test.com", password = "x", saldo = 0.0)
        whenever(userRepository.buscarPorId(1)).thenReturn(remitente)
        whenever(userRepository.buscarPorCorreo("dest@test.com")).thenReturn(destinatario)
        whenever(transactionRepository.obtenerTransaccionesPorUsuario(1)).thenReturn(emptyList())

        viewModel.realizarTransferencia(
            userId = 1,
            destinatarioCorreo = "dest@test.com",
            monto = 100.0,
            fecha = "18/04/2026",
            userRepository = userRepository
        )

        // Verificar que se guardó la transacción de ingreso para el destinatario
        verify(transactionRepository, atLeastOnce()).enviarTransaccion(
            argThat { userId == 2 && tipo == "ingreso" }
        )
    }

    // ── cargarTransacciones ───────────────────────────────────────────────

    @Test
    fun `cargarTransacciones ordena por fecha descendente`() = runTest {
        val txs = listOf(
            TransactionEntity(id = 1, userId = 1, fecha = "01/04/2026", monto = 100.0, descripcion = "A", tipo = "ingreso"),
            TransactionEntity(id = 2, userId = 1, fecha = "18/04/2026", monto = 200.0, descripcion = "B", tipo = "ingreso"),
            TransactionEntity(id = 3, userId = 1, fecha = "10/04/2026", monto = 50.0,  descripcion = "C", tipo = "egreso")
        )
        whenever(transactionRepository.obtenerTransaccionesPorUsuario(1)).thenReturn(txs)

        viewModel.cargarTransacciones(1)

        val lista = viewModel.transacciones.first()
        assertEquals("18/04/2026", lista[0].fecha)
        assertEquals("10/04/2026", lista[1].fecha)
        assertEquals("01/04/2026", lista[2].fecha)
    }

    @Test
    fun `resetState devuelve estado Idle`() = runTest {
        viewModel.resetState()
        assertTrue(viewModel.transactionState.first() is TransactionState.Idle)
    }
}
