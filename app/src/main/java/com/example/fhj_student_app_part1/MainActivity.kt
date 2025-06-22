package com.example.fhj_student_app_part1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.repository.BookRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val repository = BookRepository()
    private val books = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter
    private val currentUserId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        loadBooks()
    }

    private fun setupUI() {
        // Toolbar Logout
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_books)
        adapter = BookAdapter(books, currentUserId) { book, action ->
            when (action) {
                BookAction.VIEW -> openBookDetails(book)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // FAB
        findViewById<FloatingActionButton>(R.id.fab_add_book).setOnClickListener {
            startActivity(Intent(this, CreateBookActivity::class.java))
        }
    }

    private fun openBookDetails(book: Book) {
        val intent = Intent(this, BookDetailsActivity::class.java)
        intent.putExtra("book_id", book.id)
        startActivity(intent)
    }

    private fun loadBooks() {
        lifecycleScope.launch {
            val result = repository.getAllBooks()
            if (result.isSuccess) {
                books.clear()
                books.addAll(result.getOrDefault(emptyList()))
                adapter.notifyDataSetChanged()
                updateEmptyState()
            } else {
                Toast.makeText(this@MainActivity, "Error loading books", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState() {
        val emptyState = findViewById<View>(R.id.empty_state)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_books)
        if (books.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            loadBooks()
        }
    }
}

enum class BookAction { VIEW }

class BookAdapter(
    private val books: List<Book>,
    private val currentUserId: String?,
    private val onAction: (Book, BookAction) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount() = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position], currentUserId, onAction)
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(book: Book, currentUserId: String?, onAction: (Book, BookAction) -> Unit) {
            itemView.findViewById<TextView>(R.id.tv_title).text = book.title
            itemView.findViewById<TextView>(R.id.tv_author).text = book.author
            itemView.findViewById<TextView>(R.id.tv_status).text = book.status.name
            
            // Display owner email instead of ID
            val ownerEmail = getOwnerEmail(book.ownerId)
            itemView.findViewById<TextView>(R.id.tv_owner).text = "Owner: $ownerEmail"

            // Apply light green background if book is owned by current user
            val isOwner = book.ownerId == currentUserId
            if (isOwner) {
                itemView.setBackgroundResource(R.drawable.owned_book_background)
            } else {
                itemView.setBackgroundResource(0) // Reset to default background
            }

            // Details button click
            itemView.findViewById<MaterialButton>(R.id.btn_details).setOnClickListener {
                onAction(book, BookAction.VIEW)
            }
        }
        
        private fun getOwnerEmail(ownerId: String): String {
            return try {
                // Try to get the current user's email if it matches the owner
                val currentUser = FirebaseAuth.getInstance().currentUser
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
    }
}