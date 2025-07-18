package com.example.fhj_student_app_part1

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.fhj_student_app_part1.models.getLocalizedName
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
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
                    Toast.makeText(this@BookDetailsActivity, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show()
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
        findViewById<TextView>(R.id.tv_current_status).text = book.status.getLocalizedName(this)

        // Set owner ID
        findViewById<TextView>(R.id.tv_owner_id).text = getOwnerEmail(book.ownerId)
        
        // Display book cover if available
        displayBookCover()
        
        // Set thoughts
        val thoughtsTextView = findViewById<TextView>(R.id.tv_thoughts)
        if (book.thoughts.isNotEmpty()) {
            thoughtsTextView.text = book.thoughts
            thoughtsTextView.visibility = View.VISIBLE
        } else {
            thoughtsTextView.text = getString(R.string.no_thoughts_added)
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

    private fun displayBookCover() {
        val imageView = findViewById<ImageView>(R.id.iv_book_cover)
        val noCoverLayout = findViewById<LinearLayout>(R.id.ll_no_cover)
        
        if (book.coverImageUrl.isNotEmpty()) {
            try {
                val file = File(book.coverImageUrl)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                    noCoverLayout.visibility = View.GONE
                } else {
                    imageView.visibility = View.GONE
                    noCoverLayout.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading book cover: ${e.message}", e)
                imageView.visibility = View.GONE
                noCoverLayout.visibility = View.VISIBLE
            }
        } else {
            imageView.visibility = View.GONE
            noCoverLayout.visibility = View.VISIBLE
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
            Toast.makeText(this, getString(R.string.only_own_books_status), Toast.LENGTH_SHORT).show()
            return
        }

        val newStatus = if (book.status == BookStatus.READ) BookStatus.UNREAD else BookStatus.READ
        val updatedBook = book.copy(status = newStatus)

        lifecycleScope.launch {
            try {
                val result = repository.updateBook(updatedBook)
                if (result.isSuccess) {
                    book = updatedBook
                    findViewById<TextView>(R.id.tv_current_status).text = book.status.getLocalizedName(this@BookDetailsActivity)
                    findViewById<TextView>(R.id.tv_updated_date).text = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(book.updatedAt))
                    Toast.makeText(this@BookDetailsActivity, getString(R.string.status_updated_to, book.status.getLocalizedName(this@BookDetailsActivity)), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BookDetailsActivity, getString(R.string.error_updating_status), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating book status: ${e.message}", e)
                Toast.makeText(this@BookDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message, book.title))
            .setPositiveButton(getString(R.string.delete_book)) { _, _ ->
                deleteBook()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteBook() {
        lifecycleScope.launch {
            try {
                val result = repository.deleteBook(book.id)
                if (result.isSuccess) {
                    Toast.makeText(this@BookDetailsActivity, getString(R.string.book_deleted_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@BookDetailsActivity, getString(R.string.error_deleting_book), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting book: ${e.message}", e)
                Toast.makeText(this@BookDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 