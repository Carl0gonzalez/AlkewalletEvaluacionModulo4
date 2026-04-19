package com.cjgr.awandroide

import com.cjgr.awandroide.data.local.UserEntity
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.ui.viewmodel.AuthState
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
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
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock()
        viewModel = AuthViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    fun `login correcto emite LoginSuccess`() = runTest {
        val user = UserEntity(id = 1, nombre = "Carlo", correo = "carlo@test.com", password = "1234")
        whenever(userRepository.login("carlo@test.com", "1234")).thenReturn(user)

        viewModel.login("carlo@test.com", "1234")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.LoginSuccess)
        assertEquals(user, (state as AuthState.LoginSuccess).user)
    }

    @Test
    fun `login con credenciales incorrectas emite Error`() = runTest {
        whenever(userRepository.login(any(), any())).thenReturn(null)

        viewModel.login("no@existe.com", "wrong")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
    }

    // ── registrar ─────────────────────────────────────────────────────────

    @Test
    fun `registrar usuario nuevo emite RegisterSuccess`() = runTest {
        whenever(userRepository.buscarPorCorreo("nuevo@test.com")).thenReturn(null)
        whenever(userRepository.registrar(any())).thenReturn(5L)

        viewModel.registrar("Nuevo", "nuevo@test.com", "pass123")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.RegisterSuccess)
        assertEquals(5L, (state as AuthState.RegisterSuccess).userId)
    }

    @Test
    fun `registrar correo duplicado emite Error`() = runTest {
        val existente = UserEntity(id = 2, nombre = "Otro", correo = "dup@test.com", password = "abc")
        whenever(userRepository.buscarPorCorreo("dup@test.com")).thenReturn(existente)

        viewModel.registrar("Otro", "dup@test.com", "abc")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
        assertTrue((state as AuthState.Error).message.contains("registrado", ignoreCase = true))
    }

    // ── actualizarPerfil ──────────────────────────────────────────────────

    @Test
    fun `actualizarPerfil con datos validos emite ProfileUpdated`() = runTest {
        val actualizado = UserEntity(id = 1, nombre = "Carlo New", correo = "nuevo@mail.com", password = "1234")
        whenever(userRepository.buscarPorCorreo("nuevo@mail.com")).thenReturn(null)
        whenever(userRepository.buscarPorId(1)).thenReturn(actualizado)

        viewModel.actualizarPerfil(1, "Carlo New", "nuevo@mail.com")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.ProfileUpdated)
        assertEquals("Carlo New", (state as AuthState.ProfileUpdated).user.nombre)
    }

    @Test
    fun `actualizarPerfil con nombre vacio emite Error`() = runTest {
        viewModel.actualizarPerfil(1, "", "correo@test.com")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
    }

    @Test
    fun `actualizarPerfil con correo invalido emite Error`() = runTest {
        viewModel.actualizarPerfil(1, "Carlo", "correo-invalido")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
    }

    @Test
    fun `actualizarPerfil con correo en uso por otro usuario emite Error`() = runTest {
        val otro = UserEntity(id = 99, nombre = "Otro", correo = "ocupado@mail.com", password = "x")
        whenever(userRepository.buscarPorCorreo("ocupado@mail.com")).thenReturn(otro)

        viewModel.actualizarPerfil(1, "Carlo", "ocupado@mail.com")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.Error)
        assertTrue((state as AuthState.Error).message.contains("uso", ignoreCase = true))
    }

    // ── actualizarFotoPerfil ──────────────────────────────────────────────

    @Test
    fun `actualizarFotoPerfil guarda URI y emite PhotoUpdated`() = runTest {
        val userConFoto = UserEntity(id = 1, nombre = "Carlo", correo = "c@c.com", password = "1", fotoPerfil = "content://img/1")
        whenever(userRepository.buscarPorId(1)).thenReturn(userConFoto)

        viewModel.actualizarFotoPerfil(1, "content://img/1")

        val state = viewModel.authState.first()
        assertTrue(state is AuthState.PhotoUpdated)
        assertEquals("content://img/1", (state as AuthState.PhotoUpdated).user.fotoPerfil)
    }

    // ── cerrarSesion ──────────────────────────────────────────────────────

    @Test
    fun `cerrarSesion limpia currentUser y estado`() = runTest {
        viewModel.cerrarSesion()

        assertNull(viewModel.currentUser.first())
        assertTrue(viewModel.authState.first() is AuthState.Idle)
    }
}
