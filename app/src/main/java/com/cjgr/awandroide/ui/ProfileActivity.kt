package com.cjgr.awandroide.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cjgr.awandroide.R
import com.cjgr.awandroide.data.local.AppDatabase
import com.cjgr.awandroide.data.repository.ContactRepository
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.network.RetrofitClient
import com.cjgr.awandroide.ui.viewmodel.AuthState
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
import com.cjgr.awandroide.ui.viewmodel.ViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var imgPerfilGrande: ImageView
    private var userId: Int = -1

    // Lanzador del selector de imagen de la galería
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    // Persistir permiso de lectura para que Picasso pueda releer la URI
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    cargarImagenConPicasso(it.toString())
                    authViewModel.actualizarFotoPerfil(userId, it.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getIntExtra("userId", -1)

        val db          = AppDatabase.getDatabase(this)
        val userRepo    = UserRepository(db.userDao())
        val txRepo      = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val contactRepo = ContactRepository(db.contactDao())
        val factory     = ViewModelFactory(userRepo, txRepo, contactRepo)
        authViewModel   = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val imgBack        = findViewById<ImageView>(R.id.imgBack)
        val txtNombre      = findViewById<TextView>(R.id.txtNombreUsuario)
        val edtNombre      = findViewById<EditText>(R.id.edtNombrePerfil)
        val edtCorreo      = findViewById<EditText>(R.id.edtCorreoPerfil)
        val btnEditar      = findViewById<ImageView>(R.id.imgEditarPerfil)
        val btnGuardar     = findViewById<Button>(R.id.btnGuardarPerfil)
        val layoutEditar   = findViewById<View>(R.id.layoutEditarPerfil)
        val btnCambiarFoto = findViewById<ImageView>(R.id.imgCambiarFoto)
        imgPerfilGrande    = findViewById(R.id.imgPerfilGrande)

        imgBack.setOnClickListener { finish() }

        // ── Cargar datos y foto desde Room ─────────────────────────────────
        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtNombre.text = it.nombre
                    edtNombre.setText(it.nombre)
                    edtCorreo.setText(it.correo)

                    // Cargar foto con Picasso (URI local o URL remota)
                    if (!it.fotoPerfil.isNullOrEmpty()) {
                        cargarImagenConPicasso(it.fotoPerfil)
                    }
                }
            }
        }

        if (userId != -1) authViewModel.cargarUsuario(userId)

        // ── Cambiar foto desde galería ─────────────────────────────────────
        btnCambiarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickImageLauncher.launch(intent)
        }

        // ── Mostrar / ocultar formulario de edición ────────────────────────
        btnEditar.setOnClickListener {
            layoutEditar.visibility =
                if (layoutEditar.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // ── Guardar cambios de nombre/correo ───────────────────────────────
        btnGuardar.setOnClickListener {
            val nuevoNombre = edtNombre.text.toString()
            val nuevoCorreo = edtCorreo.text.toString()
            authViewModel.actualizarPerfil(userId, nuevoNombre, nuevoCorreo)
        }

        // ── Observar resultados del ViewModel ──────────────────────────────
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.ProfileUpdated -> {
                        Toast.makeText(this@ProfileActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        layoutEditar.visibility = View.GONE
                        authViewModel.resetState()
                    }
                    is AuthState.PhotoUpdated -> {
                        Toast.makeText(this@ProfileActivity, "Foto actualizada", Toast.LENGTH_SHORT).show()
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

    /** Carga una imagen desde una URI local o URL remota usando Picasso. */
    private fun cargarImagenConPicasso(uriOUrl: String) {
        Picasso.get()
            .load(uriOUrl)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .fit()
            .centerCrop()
            .into(imgPerfilGrande)
    }
}
