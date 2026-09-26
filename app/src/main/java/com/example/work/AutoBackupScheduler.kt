package com.example.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AutoBackupStatus(
  val isEnabled: Boolean,
  val lastBackupTimestamp: Long,
  val lastBackupFileName: String?,
  val lastBackupRecordCount: Int,
  val lastStatus: String?,
  val nextScheduledApprox: String
) {
  val lastBackupFormatted: String
    get() = if (lastBackupTimestamp > 0) {
      val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
      sdf.format(Date(lastBackupTimestamp))
    } else {
      "Belum pernah dicadangkan"
    }
}

object AutoBackupScheduler {

  private const val TAG = "AutoBackupScheduler"
  const val UNIQUE_PERIODIC_WORK_NAME = "auto_backup_sqlite_24h"
  const val UNIQUE_ONE_TIME_WORK_NAME = "auto_backup_sqlite_immediate"

  /**
   * Schedules a recurring WorkManager background task that triggers every 24 hours.
   */
  fun schedulePeriodicBackup(context: Context, forceUpdate: Boolean = false) {
    try {
      // Ensure enabled flag is stored
      val prefs = context.getSharedPreferences(AutoBackupWorker.PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().putBoolean(AutoBackupWorker.KEY_AUTO_BACKUP_ENABLED, true).apply()

      val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

      val periodicWorkRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
        repeatInterval = 24,
        repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 2,
        flexTimeIntervalUnit = TimeUnit.HOURS
      )
        .setConstraints(constraints)
        .addTag("auto_backup")
        .build()

      val policy = if (forceUpdate) {
        ExistingPeriodicWorkPolicy.UPDATE
      } else {
        ExistingPeriodicWorkPolicy.KEEP
      }

      try {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
          UNIQUE_PERIODIC_WORK_NAME,
          policy,
          periodicWorkRequest
        )
      } catch (e: Exception) {
        Log.w(TAG, "WorkManager enqueueUniquePeriodicWork warning: ${e.message}")
      }

      Log.i(TAG, "Recurring 24-hour database auto-backup task enqueued successfully.")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule recurring auto-backup: ${e.message}", e)
    }
  }

  /**
   * Triggers an immediate one-time backup task for testing or user action.
   */
  fun triggerImmediateBackup(context: Context) {
    try {
      val oneTimeRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
        .addTag("auto_backup_manual")
        .build()

      try {
        WorkManager.getInstance(context).enqueueUniqueWork(
          UNIQUE_ONE_TIME_WORK_NAME,
          ExistingWorkPolicy.REPLACE,
          oneTimeRequest
        )
      } catch (e: Exception) {
        Log.w(TAG, "WorkManager enqueueUniqueWork warning: ${e.message}")
      }
      Log.i(TAG, "Immediate one-time auto-backup enqueued.")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to trigger immediate backup: ${e.message}", e)
    }
  }

  /**
   * Cancels the periodic background backup.
   */
  fun cancelPeriodicBackup(context: Context) {
    try {
      val prefs = context.getSharedPreferences(AutoBackupWorker.PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().putBoolean(AutoBackupWorker.KEY_AUTO_BACKUP_ENABLED, false).apply()

      try {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
      } catch (e: Exception) {
        Log.w(TAG, "WorkManager cancelUniqueWork warning: ${e.message}")
      }
      Log.i(TAG, "Recurring auto-backup cancelled.")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to cancel auto-backup: ${e.message}", e)
    }
  }

  /**
   * Reads current auto-backup status from SharedPreferences.
   */
  fun getStatus(context: Context): AutoBackupStatus {
    val prefs = context.getSharedPreferences(AutoBackupWorker.PREFS_NAME, Context.MODE_PRIVATE)
    val isEnabled = prefs.getBoolean(AutoBackupWorker.KEY_AUTO_BACKUP_ENABLED, true)
    val lastTime = prefs.getLong(AutoBackupWorker.KEY_LAST_BACKUP_TIME, 0L)
    val lastFile = prefs.getString(AutoBackupWorker.KEY_LAST_BACKUP_FILE, null)
    val lastCount = prefs.getInt(AutoBackupWorker.KEY_LAST_BACKUP_COUNT, 0)
    val lastStatus = prefs.getString(AutoBackupWorker.KEY_LAST_BACKUP_STATUS, null)

    return AutoBackupStatus(
      isEnabled = isEnabled,
      lastBackupTimestamp = lastTime,
      lastBackupFileName = lastFile,
      lastBackupRecordCount = lastCount,
      lastStatus = lastStatus,
      nextScheduledApprox = if (isEnabled) "Setiap 24 Jam (Otomatis)" else "Dinonaktifkan"
    )
  }
}
