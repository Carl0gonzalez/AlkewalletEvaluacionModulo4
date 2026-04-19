package com.cjgr.awandroide

import com.cjgr.awandroide.data.local.UserDao
import com.cjgr.awandroide.data.local.UserEntity
import com.cjgr.awandroide.data.repository.UserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var repo: UserRepository

    private val usuario = UserEntity(
        id = 1, nombre = "Carlo", correo = "carlo@test.com",
        password = "1234", saldo = 100.0
    )

    @Before
    fun setUp() {
        userDao = mock()
        repo = UserRepository(userDao)
    }

    @Test
    fun `registrar llama insertUser y retorna id`() = runTest {
        whenever(userDao.insertUser(usuario)).thenReturn(1L)
        val id = repo.registrar(usuario)
        assertEquals(1L, id)
        verify(userDao).insertUser(usuario)
    }

    @Test
    fun `buscarPorCorreo retorna usuario existente`() = runTest {
        whenever(userDao.getUserByEmail("carlo@test.com")).thenReturn(usuario)
        val result = repo.buscarPorCorreo("carlo@test.com")
        assertNotNull(result)
        assertEquals("Carlo", result!!.nombre)
    }

    @Test
    fun `buscarPorCorreo retorna null si no existe`() = runTest {
        whenever(userDao.getUserByEmail("no@existe.com")).thenReturn(null)
        val result = repo.buscarPorCorreo("no@existe.com")
        assertNull(result)
    }

    @Test
    fun `actualizarSaldo llama updateSaldo en DAO`() = runTest {
        repo.actualizarSaldo(1, 500.0)
        verify(userDao).updateSaldo(1, 500.0)
    }

    @Test
    fun `actualizarPerfil llama updatePerfil en DAO`() = runTest {
        repo.actualizarPerfil(1, "Nuevo Nombre", "nuevo@mail.com")
        verify(userDao).updatePerfil(1, "Nuevo Nombre", "nuevo@mail.com")
    }

    @Test
    fun `actualizarFotoPerfil llama updateFotoPerfil en DAO`() = runTest {
        repo.actualizarFotoPerfil(1, "content://img/foto")
        verify(userDao).updateFotoPerfil(1, "content://img/foto")
    }

    @Test
    fun `login retorna usuario con credenciales correctas`() = runTest {
        whenever(userDao.login("carlo@test.com", "1234")).thenReturn(usuario)
        val result = repo.login("carlo@test.com", "1234")
        assertNotNull(result)
    }

    @Test
    fun `login retorna null con credenciales incorrectas`() = runTest {
        whenever(userDao.login(any(), any())).thenReturn(null)
        val result = repo.login("x@x.com", "wrong")
        assertNull(result)
    }
}
