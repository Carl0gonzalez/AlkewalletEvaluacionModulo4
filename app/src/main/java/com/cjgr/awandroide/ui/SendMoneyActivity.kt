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

class SendMoneyActivity : AppCompatActivity() {

    private lateinit var controller: WalletController
    private lateinit var edtMontoIngresar: EditText
    private lateinit var edtNota: EditText
    private lateinit var spnUsuarioIngresar: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money)

        controller = WalletController(this)

        spnUsuarioIngresar = findViewById(R.id.spnUsuarioIngresar)
        edtMontoIngresar = findViewById(R.id.edtMontoIngresar)
        edtNota = findViewById(R.id.edtNota)

        // Cargar lista de usuarios mock en el Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.usuarios_mock,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnUsuarioIngresar.adapter = adapter
        }

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnIngresarDineroConfirm).setOnClickListener {
            onConfirmarIngreso()
        }
    }

    private fun onConfirmarIngreso() {
        val montoTexto = edtMontoIngresar.text.toString().replace(",", ".").trim()
        val nota = edtNota.text.toString().trim()

        // Validar selección de usuario
        val usuarioDestino = spnUsuarioIngresar.selectedItem?.toString() ?: ""
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
            "Ingreso para $usuarioDestino"
        } else {
            "[$usuarioDestino] $nota"
        }

        try {
            val ok = controller.deposit(monto, notaFinal)
            if (ok) {
                Toast.makeText(this, "Dinero ingresado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "No se pudo ingresar el dinero", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.message ?: "Error al ingresar dinero", Toast.LENGTH_SHORT).show()
        }
    }
}