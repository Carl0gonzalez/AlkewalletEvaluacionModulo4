package com.cjgr.awandroide.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE correo = :correo LIMIT 1")
    suspend fun getUserByEmail(correo: String): UserEntity?

    @Query("SELECT * FROM users WHERE correo = :correo AND password = :password LIMIT 1")
    suspend fun login(correo: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET saldo = :nuevoSaldo WHERE id = :userId")
    suspend fun updateSaldo(userId: Int, nuevoSaldo: Double)

    @Query("UPDATE users SET token = :token WHERE id = :userId")
    suspend fun updateToken(userId: Int, token: String?)

    @Query("UPDATE users SET nombre = :nombre, correo = :correo WHERE id = :userId")
    suspend fun updatePerfil(userId: Int, nombre: String, correo: String)

    @Query("UPDATE users SET fotoPerfil = :url WHERE id = :userId")
    suspend fun updateFotoPerfil(userId: Int, url: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
