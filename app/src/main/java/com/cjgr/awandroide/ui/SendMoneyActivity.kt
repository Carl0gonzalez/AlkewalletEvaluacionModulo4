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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SendMoneyActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var authViewModel: AuthViewModel
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money)

        userId = intent.getIntExtra("userId", -1)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val txtNombre = findViewById<TextView>(R.id.txtNombreUsuario)
        val txtCorreo = findViewById<TextView>(R.id.txtCorreoUsuario)
        val edtDestinatario = findViewById<EditText>(R.id.edtDestinatario)
        val edtMonto = findViewById<EditText>(R.id.edtMonto)
        val edtNota = findViewById<EditText>(R.id.edtNota)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarConfirm)
        val imgBack = findViewById<ImageView>(R.id.imgBack)

        imgBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtNombre.text = it.nombre
                    txtCorreo.text = it.correo
                }
            }
        }

        if (userId != -1) authViewModel.cargarUsuario(userId)

        btnEnviar.setOnClickListener {
            val destinatario = edtDestinatario.text.toString().trim()
            val montoStr = edtMonto.text.toString().trim()

            if (destinatario.isEmpty() || montoStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull()
            if (monto == null || monto <= 0) {
                Toast.makeText(this, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            transactionViewModel.realizarTransferencia(
                userId = userId,
                destinatarioCorreo = destinatario,
                monto = monto,
                fecha = fecha,
                userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
            )
        }

        lifecycleScope.launch {
            transactionViewModel.transactionState.collect { state ->
                when (state) {
                    is TransactionState.Success -> {
                        Toast.makeText(this@SendMoneyActivity, state.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is TransactionState.Error -> {
                        Toast.makeText(this@SendMoneyActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}