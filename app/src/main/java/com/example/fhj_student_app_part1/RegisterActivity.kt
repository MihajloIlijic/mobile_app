package com.example.fhj_student_app_part1

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<TextInputEditText>(R.id.et_register_email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.et_register_password)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.et_register_confirm_password)
        val registerButton = findViewById<Button>(R.id.btn_register)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Send email verification
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(this, "Registration successful. Please check your email for verification.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Registration successful but failed to send verification email.", Toast.LENGTH_LONG).show()
                                }
                                finish()
                            }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}