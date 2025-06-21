package com.example.fhj_student_app_part1

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.models.BookStatus
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CreateBookActivity : AppCompatActivity() {
    private val repository = BookRepository()
    private val TAG = "CreateBookActivity"
    private lateinit var toolbar: MaterialToolbar
    private lateinit var titleLayout: TextInputLayout
    private lateinit var authorLayout: TextInputLayout
    private lateinit var statusLayout: TextInputLayout
    private lateinit var titleEditText: TextInputEditText
    private lateinit var authorEditText: TextInputEditText
    private lateinit var statusAutoComplete: AutoCompleteTextView
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)
        
        setupWindowInsets()
        setupViews()
        setupToolbar()
        setupStatusDropdown()
        setupSaveButton()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        titleLayout = findViewById(R.id.til_title)
        authorLayout = findViewById(R.id.til_author)
        statusLayout = findViewById(R.id.til_status)
        titleEditText = findViewById(R.id.et_title)
        authorEditText = findViewById(R.id.et_author)
        statusAutoComplete = findViewById(R.id.et_status)
        saveButton = findViewById(R.id.btn_save_book)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.add_book)
        }
    }

    private fun setupStatusDropdown() {
        val statuses = BookStatus.values().map { it.name.lowercase().capitalize() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        statusAutoComplete.setAdapter(adapter)
        statusAutoComplete.setText(statuses.first(), false) // Set default to first status
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            if (validateForm()) {
                saveBook()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validate title
        val title = titleEditText.text.toString().trim()
        if (title.isEmpty()) {
            titleLayout.error = getString(R.string.error_title_required)
            isValid = false
        } else if (title.length < 2) {
            titleLayout.error = getString(R.string.error_title_too_short)
            isValid = false
        } else {
            titleLayout.error = null
        }

        // Validate author
        val author = authorEditText.text.toString().trim()
        if (author.isEmpty()) {
            authorLayout.error = getString(R.string.error_author_required)
            isValid = false
        } else if (author.length < 2) {
            authorLayout.error = getString(R.string.error_author_too_short)
            isValid = false
        } else {
            authorLayout.error = null
        }

        // Validate status
        val statusText = statusAutoComplete.text.toString()
        if (statusText.isEmpty()) {
            statusLayout.error = getString(R.string.error_status_required)
            isValid = false
        } else {
            statusLayout.error = null
        }

        return isValid
    }

    private fun saveBook() {
        val title = titleEditText.text.toString().trim()
        val author = authorEditText.text.toString().trim()
        val statusText = statusAutoComplete.text.toString()
        
        // Convert status text back to enum
        val status = when (statusText.lowercase()) {
            "read" -> BookStatus.READ
            "unread" -> BookStatus.UNREAD
            else -> BookStatus.UNREAD
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        
        val book = Book(
            author = author,
            title = title,
            status = status,
            ownerId = currentUserId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        Log.d(TAG, "Saving book: $book")
        
        // Show loading state
        saveButton.isEnabled = false
        saveButton.text = getString(R.string.saving_book)

        lifecycleScope.launch {
            try {
                val result = repository.addBook(book)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Book saved successfully")
                    Toast.makeText(this@CreateBookActivity, getString(R.string.book_saved_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Error saving book: ${result.exceptionOrNull()}")
                    Toast.makeText(this@CreateBookActivity, getString(R.string.error_saving_book), Toast.LENGTH_LONG).show()
                    resetSaveButton()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during save: ${e.message}", e)
                Toast.makeText(this@CreateBookActivity, getString(R.string.error_saving_book), Toast.LENGTH_LONG).show()
                resetSaveButton()
            }
        }
    }

    private fun resetSaveButton() {
        saveButton.isEnabled = true
        saveButton.text = getString(R.string.save_book)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun String.capitalize(): String {
        return if (isNotEmpty()) {
            this[0].uppercase() + substring(1)
        } else {
            this
        }
    }
} 