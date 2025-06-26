package com.example.fhj_student_app_part1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.models.BookStatus
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookDetailsActivity : AppCompatActivity() {
    private val repository = BookRepository()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "BookDetailsActivity"
    
    private lateinit var book: Book
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get book data from intent
        val bookId = intent.getStringExtra("book_id")
        if (bookId == null) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUserId = auth.currentUser?.uid ?: ""
        
        // Setup toolbar
        setupToolbar()
        
        // Load book details
        loadBookDetails(bookId)
        
        // Setup button listeners
        setupButtonListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload book details in case they were updated
        val bookId = intent.getStringExtra("book_id")
        if (bookId != null) {
            loadBookDetails(bookId)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.book_details)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadBookDetails(bookId: String) {
        lifecycleScope.launch {
            try {
                val result = repository.getBook(bookId)
                if (result.isSuccess) {
                    book = result.getOrNull() ?: return@launch
                    displayBookDetails()
                } else {
                    Toast.makeText(this@BookDetailsActivity, "Error loading book", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading book: ${e.message}", e)
                Toast.makeText(this@BookDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayBookDetails() {
        // Set book title and author
        findViewById<TextView>(R.id.tv_book_title).text = book.title
        findViewById<TextView>(R.id.tv_book_author).text = book.author
        
        // Set current status
        findViewById<TextView>(R.id.tv_current_status).text = book.status.name

        
        // Set thoughts
        val thoughtsTextView = findViewById<TextView>(R.id.tv_thoughts)
        if (book.thoughts.isNotEmpty()) {
            thoughtsTextView.text = book.thoughts
            thoughtsTextView.visibility = View.VISIBLE
        } else {
            thoughtsTextView.text = "No thoughts added yet"
            thoughtsTextView.visibility = View.VISIBLE
        }
        
        // Format dates
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        findViewById<TextView>(R.id.tv_created_date).text = dateFormat.format(Date(book.createdAt))
        findViewById<TextView>(R.id.tv_updated_date).text = dateFormat.format(Date(book.updatedAt))
        
        // Show/hide action buttons based on ownership
        val isOwner = book.ownerId == currentUserId
        findViewById<MaterialButton>(R.id.btn_toggle_status).isEnabled = isOwner
        findViewById<MaterialButton>(R.id.btn_edit_book).isEnabled = isOwner
        findViewById<MaterialButton>(R.id.btn_delete_book).isEnabled = isOwner
        
        if (!isOwner) {
            findViewById<MaterialButton>(R.id.btn_toggle_status).alpha = 0.5f
            findViewById<MaterialButton>(R.id.btn_edit_book).alpha = 0.5f
            findViewById<MaterialButton>(R.id.btn_delete_book).alpha = 0.5f
        }
    }

    private fun getOwnerEmail(ownerId: String): String {
        return try {
            // Try to get the current user's email if it matches the owner
            val currentUser = auth.currentUser
            if (currentUser?.uid == ownerId) {
                currentUser.email ?: "${ownerId.take(8)}..."
            } else {
                // For other users, we can't get their email from Firebase Auth
                // So we'll show a placeholder or truncated ID
                "${ownerId.take(8)}..."
            }
        } catch (e: Exception) {
            "${ownerId.take(8)}..."
        }
    }

    private fun setupButtonListeners() {
        // Toggle status button
        findViewById<MaterialButton>(R.id.btn_toggle_status).setOnClickListener {
            toggleBookStatus()
        }
        
        // Edit book button
        findViewById<MaterialButton>(R.id.btn_edit_book).setOnClickListener {
            // Navigate to CreateBookActivity with book data for editing
            val intent = Intent(this, CreateBookActivity::class.java)
            intent.putExtra("book_id", book.id)
            startActivity(intent)
        }
        
        // Delete book button
        findViewById<MaterialButton>(R.id.btn_delete_book).setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun toggleBookStatus() {
        if (book.ownerId != currentUserId) {
            Toast.makeText(this, "You can only change status of your own books", Toast.LENGTH_SHORT).show()
            return
        }

        val newStatus = if (book.status == BookStatus.READ) BookStatus.UNREAD else BookStatus.READ
        val updatedBook = book.copy(status = newStatus)

        lifecycleScope.launch {
            try {
                val result = repository.updateBook(updatedBook)
                if (result.isSuccess) {
                    book = updatedBook
                    findViewById<TextView>(R.id.tv_current_status).text = book.status.name
                    findViewById<TextView>(R.id.tv_updated_date).text = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(book.updatedAt))
                    Toast.makeText(this@BookDetailsActivity, "Status updated to ${book.status.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BookDetailsActivity, "Error updating status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating book status: ${e.message}", e)
                Toast.makeText(this@BookDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete '${book.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBook() {
        lifecycleScope.launch {
            try {
                val result = repository.deleteBook(book.id)
                if (result.isSuccess) {
                    Toast.makeText(this@BookDetailsActivity, "Book deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@BookDetailsActivity, "Error deleting book", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting book: ${e.message}", e)
                Toast.makeText(this@BookDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 