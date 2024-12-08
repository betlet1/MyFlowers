package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var editTextEmail: TextInputEditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase Auth örneği
        firebaseAuth = FirebaseAuth.getInstance()
        editTextEmail=findViewById<TextInputEditText>(R.id.emailEditText)

        // Şifrem sıfırla
        val sifirlaButton=findViewById<MaterialButton>(R.id.submitButton)
        sifirlaButton.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Şifre sıfırlama e-postası gönderildi.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "E-posta gönderilirken hata oluştu.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else {
                Toast.makeText(this, "Lütfen email girin.", Toast.LENGTH_SHORT).show()
            }
        }



            // Geri Dön butonu
        val backButton=findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }
    }
}