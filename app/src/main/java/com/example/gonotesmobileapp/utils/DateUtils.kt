package com.example.gonotesmobileapp.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    
    // Multiple date patterns to handle different API response formats
    private val datePatterns = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }, // For microseconds: 2025-07-06T11:03:45.618596Z
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }, // For milliseconds: 2025-07-06T11:03:45.618Z
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        } // For seconds only: 2025-07-06T11:03:45Z
    )
    
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    private fun parseDate(dateString: String): Date? {
        // Try each pattern until one works
        for (pattern in datePatterns) {
            try {
                return pattern.parse(dateString)
            } catch (e: Exception) {
                // Continue to next pattern
            }
        }
        
        // If all patterns fail, try to strip microseconds manually
        try {
            val cleanedDateString = if (dateString.contains('.')) {
                // Extract microseconds part and limit to 3 digits (milliseconds)
                val parts = dateString.split('.')
                if (parts.size == 2) {
                    val beforeDot = parts[0]
                    val afterDot = parts[1]
                    val milliseconds = afterDot.substring(0, minOf(3, afterDot.length))
                    val timezone = afterDot.substring(afterDot.length - 1) // Z
                    "$beforeDot.${milliseconds}$timezone"
                } else {
                    dateString
                }
            } else {
                dateString
            }
            
            return datePatterns[1].parse(cleanedDateString) // Try milliseconds pattern
        } catch (e: Exception) {
            Log.e("DateUtils", "Failed to parse date: $dateString", e)
            return null
        }
    }
    
    fun formatRelativeTime(isoDateString: String): String {
        return try {
            Log.d("DateUtils", "Parsing date: $isoDateString")
            val date = parseDate(isoDateString)
            
            if (date == null) {
                Log.e("DateUtils", "Could not parse date: $isoDateString")
                return "Just now" // Better fallback than "Unknown"
            }
            
            val now = Date()
            val diffInMillis = now.time - date.time
            
            val result = when {
                diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                    "${minutes}m ago"
                }
                diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                    "${hours}h ago"
                }
                diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                    "${days}d ago"
                }
                else -> displayDateFormat.format(date)
            }
            
            Log.d("DateUtils", "Formatted time: $result for date: $isoDateString")
            result
        } catch (e: Exception) {
            Log.e("DateUtils", "Error formatting relative time for: $isoDateString", e)
            "Just now" // Better fallback than "Unknown"
        }
    }
    
    fun formatDisplayDate(isoDateString: String): String {
        return try {
            val date = parseDate(isoDateString)
            if (date != null) {
                displayTimeFormat.format(date)
            } else {
                Log.e("DateUtils", "Could not parse date for display: $isoDateString")
                isoDateString
            }
        } catch (e: Exception) {
            Log.e("DateUtils", "Error formatting display date for: $isoDateString", e)
            isoDateString
        }
    }
    
    fun getCurrentISOString(): String {
        return datePatterns[2].format(Date()) // Use simple format for new dates
    }
} 