package com.cjgr.awandroide.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cjgr.awandroide.R
import com.cjgr.awandroide.data.local.AppDatabase
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.network.RetrofitClient
import com.cjgr.awandroide.ui.viewmodel.AuthState
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
import com.cjgr.awandroide.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnIrCrearCuenta = findViewById<TextView>(R.id.btnIrCrearCuenta)

        btnLogin.setOnClickListener {
            val correo = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.login(correo, password)
        }

        btnIrCrearCuenta.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.LoginSuccess -> {
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.putExtra("userId", state.user.id)
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}