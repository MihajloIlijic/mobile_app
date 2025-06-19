package com.example.fhj_student_app_part1.repository

import com.example.fhj_student_app_part1.models.Book
import com.example.fhj_student_app_part1.models.BookStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private fun getBooksCollection() = firestore.collection("books")
    
    suspend fun addBook(book: Book): Result<Book> {
        return try {
            val bookWithOwner = book.copy(
                ownerId = getCurrentUserId() ?: "",
                updatedAt = System.currentTimeMillis()
            )
            getBooksCollection()
                .document(bookWithOwner.id)
                .set(bookWithOwner)
                .await()
            Result.success(bookWithOwner)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBook(book: Book): Result<Book> {
        return try {
            val bookWithTimestamp = book.copy(
                updatedAt = System.currentTimeMillis()
            )
            getBooksCollection()
                .document(book.id)
                .set(bookWithTimestamp)
                .await()
            Result.success(bookWithTimestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            getBooksCollection()
                .document(bookId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBook(bookId: String): Result<Book?> {
        return try {
            val document = getBooksCollection()
                .document(bookId)
                .get()
                .await()
            
            if (document.exists()) {
                Result.success(document.toObject(Book::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllBooks(): Result<List<Book>> {
        return try {
            val querySnapshot = getBooksCollection()
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val books = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Book::class.java)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBooksByStatus(status: BookStatus): Result<List<Book>> {
        return try {
            val querySnapshot = getBooksCollection()
                .whereEqualTo("status", status)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val books = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Book::class.java)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val querySnapshot = getBooksCollection()
                .get()
                .await()
            
            val books = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Book::class.java)
            }.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                book.author.contains(query, ignoreCase = true)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 