package com.example.fhj_student_app_part1

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.models.BookStatus
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateBookActivity : AppCompatActivity() {
    private val repository = BookRepository()
    private val TAG = "CreateBookActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val authorEditText = findViewById<TextInputEditText>(R.id.et_author)
        val titleEditText = findViewById<TextInputEditText>(R.id.et_title)
        val thoughtsEditText = findViewById<TextInputEditText>(R.id.et_thoughts)
        val saveButton = findViewById<Button>(R.id.btn_save_book)

        saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            
            val author = authorEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val thoughts = thoughtsEditText.text.toString().trim()
            val status = BookStatus.UNREAD

            Log.d(TAG, "Author: $author, Title: $title, Thoughts: $thoughts, Status: $status")

            if (author.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Validation failed - empty fields")
                return@setOnClickListener
            }

            val book = Book(
                author = author, 
                title = title, 
                thoughts = thoughts,
                status = status,
                createdAt = System.currentTimeMillis()
            )
            Log.d(TAG, "Created book: $book")
            
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting to save book...")
                    val result = repository.addBook(book)
                    Log.d(TAG, "Save result: $result")
                    
                    if (result.isSuccess) {
                        Log.d(TAG, "Book saved successfully")
                        Toast.makeText(this@CreateBookActivity, "Book saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e(TAG, "Error saving book: ${result.exceptionOrNull()}")
                        Toast.makeText(this@CreateBookActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during save: ${e.message}", e)
                    Toast.makeText(this@CreateBookActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 