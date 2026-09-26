package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
  private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  private val displayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
  private val shortDisplayFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
  private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
  private val isoDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

  fun today(): String {
    return isoFormat.format(Date())
  }

  fun formatDisplay(dateStr: String): String {
    return try {
      val parsed = isoFormat.parse(dateStr)
      if (parsed != null) displayFormat.format(parsed) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun formatShort(dateStr: String): String {
    return try {
      val parsed = isoFormat.parse(dateStr)
      if (parsed != null) shortDisplayFormat.format(parsed) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun formatTime(timestamp: Long): String {
    return timeFormat.format(Date(timestamp))
  }

  fun formatIsoDateTime(timestamp: Long): String {
    return isoDateTimeFormat.format(Date(timestamp))
  }

  fun offsetDate(baseDateStr: String, days: Int): String {
    return try {
      val date = isoFormat.parse(baseDateStr) ?: Date()
      val cal = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_YEAR, days)
      }
      isoFormat.format(cal.time)
    } catch (_: Exception) {
      today()
    }
  }
}
