package com.cjgr.awandroide.data.repository

import com.cjgr.awandroide.data.local.UserDao
import com.cjgr.awandroide.data.local.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun registrar(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun login(correo: String, password: String): UserEntity? =
        userDao.login(correo, password)

    suspend fun buscarPorCorreo(correo: String): UserEntity? =
        userDao.getUserByEmail(correo)

    suspend fun buscarPorId(userId: Int): UserEntity? =
        userDao.getUserById(userId)

    suspend fun listarUsuarios(): List<UserEntity> =
        userDao.getAllUsers()

    suspend fun actualizarSaldo(userId: Int, nuevoSaldo: Double) =
        userDao.updateSaldo(userId, nuevoSaldo)

    suspend fun actualizarToken(userId: Int, token: String?) =
        userDao.updateToken(userId, token)

    suspend fun actualizarUsuario(user: UserEntity) =
        userDao.updateUser(user)

    /** Actualiza sólo nombre y correo (sin tocar contraseña ni saldo). */
    suspend fun actualizarPerfil(userId: Int, nombre: String, correo: String) =
        userDao.updatePerfil(userId, nombre, correo)

    /** Guarda la URI local de la foto de perfil elegida desde galería. */
    suspend fun actualizarFotoPerfil(userId: Int, url: String) =
        userDao.updateFotoPerfil(userId, url)

    suspend fun limpiarUsuarios() =
        userDao.deleteAllUsers()
}
