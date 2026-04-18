package com.cjgr.awandroide.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cjgr.awandroide.R
import com.cjgr.awandroide.model.UserPreferences

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val txtNombreUsuario = findViewById<TextView>(R.id.txtNombreUsuario)
        val edtNombre = findViewById<EditText>(R.id.edtNombrePerfil)
        val edtEmail = findViewById<EditText>(R.id.edtEmailPerfil)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPerfil)

        // Cargar datos actuales
        val nombreActual = UserPreferences.getName(this)
        val emailActual = UserPreferences.getEmail(this)

        txtNombreUsuario.text = nombreActual
        edtNombre.setText(nombreActual)
        edtEmail.setText(emailActual)

        btnGuardar.setOnClickListener {
            val nuevoNombre = edtNombre.text.toString().trim()
            val nuevoEmail = edtEmail.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches()) {
                Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserPreferences.save(this, nuevoNombre, nuevoEmail)
            txtNombreUsuario.text = nuevoNombre

            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
        }
    }
}