package com.cjgr.awandroide.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cjgr.awandroide.R
import com.cjgr.awandroide.controller.WalletController
import com.cjgr.awandroide.model.UserPreferences
import java.text.NumberFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var controller: WalletController
    private lateinit var txtBalanceAmount: TextView
    private lateinit var txtEmpty: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var txtSaludo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        controller = WalletController(this)

        txtBalanceAmount = findViewById(R.id.txtBalanceAmount)
        txtEmpty = findViewById(R.id.txtEmptyTransactions)
        rvTransactions = findViewById(R.id.rvTransactions)
        txtSaludo = findViewById(R.id.txtSaludo)

        adapter = TransactionAdapter(emptyList())
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter

        findViewById<Button>(R.id.btnEnviarDinero).setOnClickListener {
            startActivity(Intent(this, RequestMoneyActivity::class.java))
        }

        findViewById<Button>(R.id.btnIngresarDinero).setOnClickListener {
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }

        updateUserName()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        updateUserName()
        loadData()
    }

    private fun updateUserName() {
        val nombre = UserPreferences.getName(this)
        txtSaludo.text = "Hola, $nombre!"
    }

    private fun loadData() {
        val balance = controller.getBalance()
        txtBalanceAmount.text = formatCurrency(balance)

        val transactions = controller.getTransactions()
        if (transactions.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            rvTransactions.visibility = View.GONE
        } else {
            txtEmpty.visibility = View.GONE
            rvTransactions.visibility = View.VISIBLE
            adapter.update(transactions)
        }
    }

    private fun formatCurrency(value: Double): String {
        val clLocale = Locale("es", "CL")
        val formatter = NumberFormat.getCurrencyInstance(clLocale)
        return formatter.format(value)
    }
}