package com.example.fhj_student_app_part1.models

import android.content.Context
import com.example.fhj_student_app_part1.R

fun BookStatus.getLocalizedName(context: Context): String {
    return when (this) {
        BookStatus.READ -> context.getString(R.string.status_read)
        BookStatus.UNREAD -> context.getString(R.string.status_unread)
    }
} 