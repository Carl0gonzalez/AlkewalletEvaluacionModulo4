package com.cjgr.awandroide.ui

import android.os.Bundle
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
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
import com.cjgr.awandroide.ui.viewmodel.TransactionState
import com.cjgr.awandroide.ui.viewmodel.TransactionViewModel
import com.cjgr.awandroide.ui.viewmodel.ViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestMoneyActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_money)

        val userId = intent.getIntExtra("userId", -1)

        val db       = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo   = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory  = ViewModelFactory(userRepo, txRepo)

        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        authViewModel        = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val imgBack         = findViewById<ImageView>(R.id.imgBack)
        val imgFoto         = findViewById<ImageView>(R.id.imgFotoUsuario)
        val txtNombre       = findViewById<TextView>(R.id.txtNombreUsuario)
        val txtCorreo       = findViewById<TextView>(R.id.txtCorreoUsuario)
        val edtMonto        = findViewById<EditText>(R.id.edtMontoEnviar)
        val edtNota         = findViewById<EditText>(R.id.edtNotaEnviar)
        val btnConfirm      = findViewById<Button>(R.id.btnEnviarDineroConfirm)

        imgBack.setOnClickListener { finish() }

        // Cargar datos del usuario logueado
        if (userId != -1) authViewModel.cargarUsuario(userId)

        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtNombre.text = it.nombre
                    txtCorreo.text = it.correo
                    if (!it.fotoPerfil.isNullOrEmpty()) {
                        Picasso.get()
                            .load(it.fotoPerfil)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .fit()
                            .centerCrop()
                            .into(imgFoto)
                    }
                }
            }
        }

        btnConfirm.setOnClickListener {
            val montoStr = edtMonto.text.toString().trim()
            val nota     = edtNota.text.toString().trim()

            if (montoStr.isEmpty()) {
                Toast.makeText(this, "Ingresa un monto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val monto = montoStr.toDoubleOrNull()
            if (monto == null || monto <= 0) {
                Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha       = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val descripcion = if (nota.isNotEmpty()) nota else "Ingreso de dinero"

            transactionViewModel.ingresarDinero(
                userId      = userId,
                monto       = monto,
                fecha       = fecha,
                descripcion = descripcion
            )
        }

        lifecycleScope.launch {
            transactionViewModel.transactionState.collect { state ->
                when (state) {
                    is TransactionState.Success -> {
                        Toast.makeText(this@RequestMoneyActivity, state.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is TransactionState.Error -> {
                        Toast.makeText(this@RequestMoneyActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}
