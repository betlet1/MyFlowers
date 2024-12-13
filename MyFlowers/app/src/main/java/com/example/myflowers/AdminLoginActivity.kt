package com.example.myflowers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Admin")
        editTextEmail = findViewById(R.id.mailInput)
        editTextPassword = findViewById(R.id.passwordInput)

        val adminLoginButton = findViewById<MaterialButton>(R.id.btnLogin)
        adminLoginButton.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "E-posta ve şifre boş olamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Firebase Authentication ile giriş yap
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Authentication başarılı, admin kontrolü yap
                            checkAdminRole(firebaseAuth.currentUser?.uid ?: "")
                        } else {
                            Toast.makeText(this, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAdminRole(userId: String) {
        // Veritabanından admin kontrolü
        databaseReference.child(userId).child("role").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val role = snapshot.getValue(String::class.java)
                    if (role == "admin") {
                        val intent = Intent(this@AdminLoginActivity, AdminFlowersActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@AdminLoginActivity, "Bu kullanıcı admin değil.", Toast.LENGTH_SHORT).show()
                        firebaseAuth.signOut()
                    }
                } else {
                    Toast.makeText(this@AdminLoginActivity, "Admin bilgisi bulunamadı.", Toast.LENGTH_SHORT).show()
                    firebaseAuth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminLoginActivity, "Veritabanı hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
