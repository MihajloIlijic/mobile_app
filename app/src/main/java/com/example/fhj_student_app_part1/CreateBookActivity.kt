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
    private var isEditMode = false
    private var bookId: String? = null

    companion object {
        const val EXTRA_BOOK_ID = "book_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if we're in edit mode
        bookId = intent.getStringExtra(EXTRA_BOOK_ID)
        isEditMode = bookId != null

        val authorEditText = findViewById<TextInputEditText>(R.id.et_author)
        val titleEditText = findViewById<TextInputEditText>(R.id.et_title)
        val saveButton = findViewById<Button>(R.id.btn_save_book)

        // Update button text based on mode
        saveButton.text = if (isEditMode) "Update Book" else "Save Book"

        // If in edit mode, load existing book data
        if (isEditMode) {
            loadBookData()
        }

        saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            
            val author = authorEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val status = BookStatus.UNREAD

            Log.d(TAG, "Author: $author, Title: $title, Status: $status")

            if (author.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Validation failed - empty fields")
                return@setOnClickListener
            }

            if (isEditMode) {
                updateBook(author, title, status)
            } else {
                createBook(author, title, status)
            }
        }
    }

    private fun loadBookData() {
        bookId?.let { id ->
            lifecycleScope.launch {
                val result = repository.getBook(id)
                if (result.isSuccess) {
                    val book = result.getOrNull()
                    book?.let {
                        findViewById<TextInputEditText>(R.id.et_author).setText(it.author)
                        findViewById<TextInputEditText>(R.id.et_title).setText(it.title)
                    }
                } else {
                    Toast.makeText(this@CreateBookActivity, "Error loading book", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun createBook(author: String, title: String, status: BookStatus) {
        val book = Book(author = author, title = title, status = status)
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

    private fun updateBook(author: String, title: String, status: BookStatus) {
        bookId?.let { id ->
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting to update book...")
                    
                    // First get the existing book
                    val existingBookResult = repository.getBook(id)
                    if (existingBookResult.isFailure) {
                        Toast.makeText(this@CreateBookActivity, "Error loading book", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    
                    val existingBook = existingBookResult.getOrNull()
                    if (existingBook == null) {
                        Toast.makeText(this@CreateBookActivity, "Book not found", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    
                    // Create updated book with new values
                    val updatedBook = existingBook.copy(
                        author = author,
                        title = title,
                        status = status,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    Log.d(TAG, "Updating book: $updatedBook")
                    val result = repository.updateBook(updatedBook)
                    Log.d(TAG, "Update result: $result")
                    
                    if (result.isSuccess) {
                        Log.d(TAG, "Book updated successfully")
                        Toast.makeText(this@CreateBookActivity, "Book updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e(TAG, "Error updating book: ${result.exceptionOrNull()}")
                        Toast.makeText(this@CreateBookActivity, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during update: ${e.message}", e)
                    Toast.makeText(this@CreateBookActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 