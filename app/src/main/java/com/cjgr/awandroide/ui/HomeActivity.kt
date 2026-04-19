package com.cjgr.awandroide.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
import com.squareup.picasso.Picasso
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
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val rvTransacciones = findViewById<RecyclerView>(R.id.rvTransacciones)

        adapter = TransactionAdapter()
        rvTransacciones.layoutManager = LinearLayoutManager(this)
        rvTransacciones.adapter = adapter

        // ── Navegar a perfil desde saludo o foto ──────────────────────────
        val irAPerfil = { _: android.view.View ->
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        txtSaludo.setOnClickListener(irAPerfil)
        imgProfile.setOnClickListener(irAPerfil)

        // Hacer el saludo visualmente clickeable
        txtSaludo.isClickable = true
        txtSaludo.isFocusable = true

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

        // Datos del usuario
        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    txtSaludo.text = "Hola, ${it.nombre}!"
                    txtBalance.text = "$ %.2f".format(it.saldo)
                    // Cargar foto de perfil en el header
                    if (!it.fotoPerfil.isNullOrEmpty()) {
                        Picasso.get()
                            .load(it.fotoPerfil)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .fit()
                            .centerCrop()
                            .into(imgProfile)
                    }
                }
            }
        }

        lifecycleScope.launch {
            transactionViewModel.transacciones.collect { lista ->
                adapter.submitList(lista)
            }
        }

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
