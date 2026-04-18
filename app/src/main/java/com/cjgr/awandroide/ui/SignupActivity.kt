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

class SignupActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtEmail = findViewById<EditText>(R.id.edtEmailSignup)
        val edtPass1 = findViewById<EditText>(R.id.edtPass1)
        val edtPass2 = findViewById<EditText>(R.id.edtPass2)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnIrLogin = findViewById<TextView>(R.id.btnIrLogin)

        btnRegistrarse.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val correo = edtEmail.text.toString().trim()
            val pass1 = edtPass1.text.toString()
            val pass2 = edtPass2.text.toString()

            when {
                nombre.isEmpty() || correo.isEmpty() || pass1.isEmpty() -> {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
                pass1 != pass2 -> {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
                pass1.length < 6 -> {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
                else -> authViewModel.registrar(nombre, correo, pass1)
            }
        }

        btnIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.RegisterSuccess -> {
                        Toast.makeText(this@SignupActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}