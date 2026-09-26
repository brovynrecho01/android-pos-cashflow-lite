package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileSharer {

  fun shareCsvFile(context: Context, csvFile: File, summaryText: String) {
    try {
      val authority = "${context.packageName}.fileprovider"
      val uri = FileProvider.getUriForFile(context, authority, csvFile)

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Arus Kas Kelontong")
        putExtra(Intent.EXTRA_TEXT, summaryText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan CSV ke:")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      // Fallback to text sharing if file uri fails
      shareText(context, summaryText)
    }
  }

  fun shareText(context: Context, text: String) {
    try {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Arus Kas Kelontong")
        putExtra(Intent.EXTRA_TEXT, text)
      }
      val chooser = Intent.createChooser(intent, "Bagikan Ringkasan:")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Tidak dapat membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }
}
