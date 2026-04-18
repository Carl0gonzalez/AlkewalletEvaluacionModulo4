package com.cjgr.awandroide.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cjgr.awandroide.R
import com.cjgr.awandroide.data.local.AppDatabase
import com.cjgr.awandroide.data.repository.TransactionRepository
import com.cjgr.awandroide.data.repository.UserRepository
import com.cjgr.awandroide.network.RetrofitClient
import com.cjgr.awandroide.ui.viewmodel.AuthViewModel
import com.cjgr.awandroide.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userId = intent.getIntExtra("userId", -1)

        val db = AppDatabase.getDatabase(this)
        val userRepo = UserRepository(db.userDao())
        val txRepo = TransactionRepository(db.transactionDao(), RetrofitClient.api)
        val factory = ViewModelFactory(userRepo, txRepo)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        val txtNombre = findViewById<TextView>(R.id.txtNombreUsuario)

        lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let { txtNombre.text = it.nombre }
            }
        }

        if (userId != -1) authViewModel.cargarUsuario(userId)
    }
}