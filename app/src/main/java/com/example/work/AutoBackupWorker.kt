package com.example.work

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.util.DatabaseBackupManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  companion object {
    private const val TAG = "AutoBackupWorker"
    const val PREFS_NAME = "auto_backup_prefs"
    const val KEY_LAST_BACKUP_TIME = "last_auto_backup_time"
    const val KEY_LAST_BACKUP_FILE = "last_auto_backup_file"
    const val KEY_LAST_BACKUP_COUNT = "last_auto_backup_count"
    const val KEY_LAST_BACKUP_STATUS = "last_auto_backup_status"
    const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
  }

  override suspend fun doWork(): Result {
    val prefs: SharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val isEnabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)

    if (!isEnabled) {
      Log.i(TAG, "Auto-backup is disabled by user settings. Skipping.")
      return Result.success()
    }

    return try {
      Log.i(TAG, "Starting recurring 24-hour database auto-backup...")
      val database = AppDatabase.getDatabase(applicationContext)
      val posDao = database.posDao()

      // Fetch all records from SQLite Room database
      val summaries = posDao.getAllDailySummaries()
      val transactions = posDao.getAllTransactions()
      val items = posDao.getAllTransactionItems()
      val expenses = posDao.getAllExpenses()

      // Store name
      val posPrefs = applicationContext.getSharedPreferences("pos_kelontong_prefs", Context.MODE_PRIVATE)
      val storeName = posPrefs.getString("store_name", "Toko Pak Kadi Cash System") ?: "Toko Pak Kadi Cash System"

      // Generate structured JSON
      val backupContent = DatabaseBackupManager.generateBackupJson(
        storeName = storeName,
        summaries = summaries,
        transactions = transactions,
        items = items,
        expenses = expenses
      )

      // Save to external storage directory
      val savedFile = DatabaseBackupManager.saveToExternalStorage(applicationContext, backupContent)

      // Cleanup older backup files (keep most recent 20 files to save storage)
      cleanupOldBackups(savedFile.parentFile, maxFiles = 20)

      val now = System.currentTimeMillis()
      prefs.edit()
        .putLong(KEY_LAST_BACKUP_TIME, now)
        .putString(KEY_LAST_BACKUP_FILE, savedFile.name)
        .putInt(KEY_LAST_BACKUP_COUNT, backupContent.totalRecords)
        .putString(KEY_LAST_BACKUP_STATUS, "Sukses")
        .apply()

      Log.i(TAG, "Auto-backup completed successfully: ${savedFile.absolutePath}, records: ${backupContent.totalRecords}")

      Result.success(
        workDataOf(
          "file_name" to savedFile.name,
          "records_count" to backupContent.totalRecords,
          "backup_time" to now
        )
      )
    } catch (e: Exception) {
      Log.e(TAG, "Auto-backup failed with exception: ${e.message}", e)
      prefs.edit()
        .putString(KEY_LAST_BACKUP_STATUS, "Gagal: ${e.localizedMessage}")
        .apply()

      if (runAttemptCount < 3) {
        Result.retry()
      } else {
        Result.failure()
      }
    }
  }

  private fun cleanupOldBackups(dir: File?, maxFiles: Int) {
    if (dir == null || !dir.exists() || !dir.isDirectory) return
    try {
      val backupFiles = dir.listFiles { file ->
        file.isFile && file.name.startsWith("backup_") && file.name.endsWith(".json")
      } ?: return

      if (backupFiles.size > maxFiles) {
        // Sort oldest first
        val sorted = backupFiles.sortedBy { it.lastModified() }
        val toDeleteCount = sorted.size - maxFiles
        for (i in 0 until toDeleteCount) {
          sorted[i].delete()
          Log.d(TAG, "Cleaned up old backup file: ${sorted[i].name}")
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error cleaning up old backups: ${e.message}")
    }
  }
}
