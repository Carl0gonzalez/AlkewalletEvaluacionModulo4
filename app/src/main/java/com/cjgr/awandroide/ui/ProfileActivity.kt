package com.cjgr.awandroide.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getIntExtra("userId", -1)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val imgBack      = findViewById<ImageView>(R.id.imgBack)
        val txtNombre    = findViewById<TextView>(R.id.txtNombreUsuario)
        val edtNombre    = findViewById<EditText>(R.id.edtNombrePerfil)
        val edtCorreo    = findViewById<EditText>(R.id.edtCorreoPerfil)
        val btnEditar    = findViewById<ImageView>(R.id.imgEditarPerfil)
        val btnGuardar   = findViewById<Button>(R.id.btnGuardarPerfil)
        val layoutEditar = findViewById<View>(R.id.layoutEditarPerfil)

        imgBack.setOnClickListener { finish() }

        // ── Cargar datos actuales ──────────────────────────────────────────
        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtNombre.text = it.nombre
                    edtNombre.setText(it.nombre)
                    edtCorreo.setText(it.correo)
                }
            }
        }

        if (userId != -1) authViewModel.cargarUsuario(userId)

        // ── Mostrar / ocultar formulario de edición ────────────────────────
        btnEditar.setOnClickListener {
            layoutEditar.visibility =
                if (layoutEditar.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // ── Guardar cambios ────────────────────────────────────────────────
        btnGuardar.setOnClickListener {
            val nuevoNombre = edtNombre.text.toString()
            val nuevoCorreo = edtCorreo.text.toString()
            authViewModel.actualizarPerfil(userId, nuevoNombre, nuevoCorreo)
        }

        // ── Observar resultado ─────────────────────────────────────────────
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.ProfileUpdated -> {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Perfil actualizado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        layoutEditar.visibility = View.GONE
                        authViewModel.resetState()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                        authViewModel.resetState()
                    }
                    else -> Unit
                }
            }
        }
    }
}