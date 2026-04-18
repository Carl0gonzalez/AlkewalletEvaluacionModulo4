package com.cjgr.awandroide.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cjgr.awandroide.R
import com.cjgr.awandroide.controller.WalletController
import com.cjgr.awandroide.controller.WalletSendResult

class RequestMoneyActivity : AppCompatActivity() {

    private lateinit var controller: WalletController
    private lateinit var edtMontoEnviar: EditText
    private lateinit var edtNotaEnviar: EditText
    private lateinit var spnUsuarioEnviar: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_money)

        controller = WalletController(this)

        spnUsuarioEnviar = findViewById(R.id.spnUsuarioEnviar)
        edtMontoEnviar = findViewById(R.id.edtMontoEnviar)
        edtNotaEnviar = findViewById(R.id.edtNotaEnviar)

        // Cargar lista de usuarios mock en el Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.usuarios_mock,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnUsuarioEnviar.adapter = adapter
        }

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnEnviarDineroConfirm).setOnClickListener {
            onConfirmarEnvio()
        }
    }

    private fun onConfirmarEnvio() {
        val montoTexto = edtMontoEnviar.text.toString().replace(",", ".").trim()
        val nota = edtNotaEnviar.text.toString().trim()

        // Validar selección de usuario
        val usuarioDestino = spnUsuarioEnviar.selectedItem?.toString() ?: ""
        if (usuarioDestino.isEmpty() || usuarioDestino == "Selecciona un usuario") {
            Toast.makeText(this, "Selecciona un usuario destino", Toast.LENGTH_SHORT).show()
            return
        }

        if (montoTexto.isEmpty()) {
            Toast.makeText(this, "Ingresa un monto", Toast.LENGTH_SHORT).show()
            return
        }

        val monto = montoTexto.toDoubleOrNull()
        if (monto == null || monto <= 0.0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Incluir el usuario en la nota para dejar trazabilidad
        val notaFinal = if (nota.isEmpty()) {
            "Envío a $usuarioDestino"
        } else {
            "[$usuarioDestino] $nota"
        }

        try {
            when (controller.send(monto, notaFinal)) {
                WalletSendResult.SUCCESS -> {
                    Toast.makeText(this, "Dinero enviado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                WalletSendResult.INSUFFICIENT_FUNDS -> {
                    Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
                }
                WalletSendResult.INVALID_AMOUNT -> {
                    Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.message ?: "Error al enviar dinero", Toast.LENGTH_SHORT).show()
        }
    }
}