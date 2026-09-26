package com.example

import android.app.Application
import android.util.Log
import com.example.work.AutoBackupScheduler

class PosApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    Log.i("PosApplication", "Initializing PosApplication...")

    // Schedule 24-hour recurring SQLite database auto-backup via WorkManager
    AutoBackupScheduler.schedulePeriodicBackup(this)
  }
}
