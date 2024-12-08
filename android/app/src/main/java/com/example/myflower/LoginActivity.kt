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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Firebase Auth örneği
        firebaseAuth = FirebaseAuth.getInstance()
        editTextEmail=findViewById<EditText>(R.id.email)
        editTextPassword=findViewById<EditText>(R.id.sifre)

        // Kullanıcı Girişi
        val LoginButton=findViewById<Button>(R.id.giris_buton)
        LoginButton.setOnClickListener{
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Giriş Başarılı
                            Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, FlowersActivity::class.java)
                            startActivity(intent)
                            finish() // LoginActivity'yi kapat
                        } else {
                            // Giriş Hatası
                            Toast.makeText(this, "Hatalı e-posta veya şifre.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        // Kayıt Ol
        val kayitButton=findViewById<Button>(R.id.kayit_button)
        kayitButton.setOnClickListener{
            val intent=Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Şifremi Unuttum
        val forgotPasswordButton=findViewById<TextView>(R.id.txtSifreUnuttum)
        forgotPasswordButton.setOnClickListener{
            val intent=Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}