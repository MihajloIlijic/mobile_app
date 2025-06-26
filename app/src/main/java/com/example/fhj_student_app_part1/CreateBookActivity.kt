package com.example.fhj_student_app_part1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.models.BookStatus
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateBookActivity : AppCompatActivity() {
    private val repository = BookRepository()
    private val TAG = "CreateBookActivity"
    
    private var isEditMode = false
    private var existingBook: Book? = null
    
    // Camera functionality
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
        }
    }
    
    // Camera result launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            displayPhoto()
        } else {
            Toast.makeText(this, "Failed to take photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        setupToolbar()

        val authorEditText = findViewById<TextInputEditText>(R.id.et_author)
        val titleEditText = findViewById<TextInputEditText>(R.id.et_title)
        val thoughtsEditText = findViewById<TextInputEditText>(R.id.et_thoughts)
        val saveButton = findViewById<Button>(R.id.btn_save_book)

        // Check if we're in edit mode
        val bookId = intent.getStringExtra("book_id")
        if (bookId != null) {
            isEditMode = true
            loadExistingBook(bookId, authorEditText, titleEditText, thoughtsEditText, saveButton)
        } else {
            // Create mode - set default title
            setToolbarTitle(getString(R.string.add_book))
        }

        // Setup camera functionality
        setupCameraButtons()

        saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            
            val author = authorEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val thoughts = thoughtsEditText.text.toString().trim()

            Log.d(TAG, "Author: $author, Title: $title, Thoughts: $thoughts")

            if (author.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Validation failed - empty fields")
                return@setOnClickListener
            }

            if (isEditMode && existingBook != null) {
                // Update existing book
                updateBook(author, title, thoughts)
            } else {
                // Create new book
                createBook(author, title, thoughts)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Center the title
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setToolbarTitle(title: String) {
        findViewById<android.widget.TextView>(R.id.tv_toolbar_title).text = title
    }

    private fun loadExistingBook(bookId: String, authorEditText: TextInputEditText, titleEditText: TextInputEditText, thoughtsEditText: TextInputEditText, saveButton: Button) {
        lifecycleScope.launch {
            try {
                val result = repository.getBook(bookId)
                if (result.isSuccess) {
                    existingBook = result.getOrNull()
                    existingBook?.let { book ->
                        // Fill the fields with existing data
                        authorEditText.setText(book.author)
                        titleEditText.setText(book.title)
                        thoughtsEditText.setText(book.thoughts)
                        
                        // Update UI for edit mode
                        setToolbarTitle(getString(R.string.edit_book))
                        saveButton.text = getString(R.string.update)
                        
                        Log.d(TAG, "Loaded existing book: $book")
                    }
                } else {
                    Toast.makeText(this@CreateBookActivity, "Error loading book", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading book: ${e.message}", e)
                Toast.makeText(this@CreateBookActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun createBook(author: String, title: String, thoughts: String) {
        // Save photo to internal storage if exists
        val photoPath = savePhotoToInternalStorage()
        
        val book = Book(
            author = author, 
            title = title, 
            thoughts = thoughts,
            coverImageUrl = photoPath ?: "",
            status = BookStatus.UNREAD,
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

    private fun updateBook(author: String, title: String, thoughts: String) {
        existingBook?.let { book ->
            // Save photo to internal storage if exists
            val photoPath = savePhotoToInternalStorage()
            
            val updatedBook = book.copy(
                author = author,
                title = title,
                thoughts = thoughts,
                coverImageUrl = photoPath ?: book.coverImageUrl,
                updatedAt = System.currentTimeMillis()
            )
            Log.d(TAG, "Updated book: $updatedBook")
            
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting to update book...")
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
    
    // Camera functionality methods
    private fun setupCameraButtons() {
        val takePhotoButton = findViewById<MaterialButton>(R.id.btn_take_photo)
        val removePhotoButton = findViewById<MaterialButton>(R.id.btn_remove_photo)
        
        takePhotoButton.setOnClickListener {
            checkCameraPermission()
        }
        
        removePhotoButton.setOnClickListener {
            removePhoto()
        }
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun takePhoto() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    
    private fun displayPhoto() {
        val imageView = findViewById<ImageView>(R.id.iv_book_cover)
        val noImageLayout = findViewById<LinearLayout>(R.id.ll_no_image)
        val removeButton = findViewById<MaterialButton>(R.id.btn_remove_photo)
        
        try {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
            noImageLayout.visibility = View.GONE
            removeButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying photo: ${e.message}", e)
            Toast.makeText(this, "Error displaying photo", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removePhoto() {
        val imageView = findViewById<ImageView>(R.id.iv_book_cover)
        val noImageLayout = findViewById<LinearLayout>(R.id.ll_no_image)
        val removeButton = findViewById<MaterialButton>(R.id.btn_remove_photo)
        
        imageView.visibility = View.GONE
        noImageLayout.visibility = View.VISIBLE
        removeButton.visibility = View.GONE
        
        // Delete the photo file
        currentPhotoPath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting photo file: ${e.message}", e)
            }
        }
        
        currentPhotoPath = null
        photoUri = null
    }
    
    private fun savePhotoToInternalStorage(): String? {
        if (currentPhotoPath == null) return null
        
        return try {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            val fileName = "book_cover_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo: ${e.message}", e)
            null
        }
    }
} 