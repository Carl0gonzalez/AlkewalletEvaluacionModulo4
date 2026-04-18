package com.cjgr.awandroide.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cjgr.awandroide.R
import com.cjgr.awandroide.data.local.AppDatabase
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.network.RetrofitClient
import com.cjgr.awandroide.ui.adapter.TransactionAdapter
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
import com.cjgr.awandroide.ui.viewmodel.TransactionViewModel
import com.cjgr.awandroide.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userId = intent.getIntExtra("userId", -1)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        val txtSaludo = findViewById<TextView>(R.id.txtSaludo)
        val txtBalance = findViewById<TextView>(R.id.txtBalanceAmount)
        val rvTransacciones = findViewById<RecyclerView>(R.id.rvTransacciones)

        adapter = TransactionAdapter()
        rvTransacciones.layoutManager = LinearLayoutManager(this)
        rvTransacciones.adapter = adapter

        findViewById<Button>(R.id.btnEnviarDinero).setOnClickListener {
            val intent = Intent(this, SendMoneyActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnIngresarDinero).setOnClickListener {
            val intent = Intent(this, RequestMoneyActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Datos del usuario (nombre y saldo guardado en Room)
        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtSaludo.text = "Hola, ${it.nombre}!"
                    txtBalance.text = "$ %.2f".format(it.saldo)
                }
            }
        }

        // Lista de transacciones (ya ordenada desc por fecha en el ViewModel)
        lifecycleScope.launch {
            transactionViewModel.transacciones.collect { lista ->
                adapter.submitList(lista)
            }
        }

        // Balance recalculado desde el historial de la API (sobreescribe el de Room)
        lifecycleScope.launch {
            transactionViewModel.balanceCalculado.collect { balance ->
                balance?.let {
                    txtBalance.text = "$ %.2f".format(it)
                }
            }
        }

        if (userId != -1) {
            authViewModel.cargarUsuario(userId)
            transactionViewModel.cargarTransacciones(userId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1) {
            authViewModel.cargarUsuario(userId)
            transactionViewModel.cargarTransacciones(userId)
        }
    }
}
