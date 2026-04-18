package com.cjgr.awandroide.data.repository

import com.cjgr.awandroide.data.local.UserDao
import com.cjgr.awandroide.data.local.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun registrar(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun login(correo: String, password: String): UserEntity? {
        return userDao.login(correo, password)
    }

    suspend fun buscarPorCorreo(correo: String): UserEntity? {
        return userDao.getUserByEmail(correo)
    }

    suspend fun buscarPorId(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun listarUsuarios(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun actualizarSaldo(userId: Int, nuevoSaldo: Double) {
        userDao.updateSaldo(userId, nuevoSaldo)
    }

    suspend fun actualizarToken(userId: Int, token: String?) {
        userDao.updateToken(userId, token)
    }

    suspend fun actualizarUsuario(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun limpiarUsuarios() {
        userDao.deleteAllUsers()
    }
}