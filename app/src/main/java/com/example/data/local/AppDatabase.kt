package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity

@Database(
  entities = [
    DailySummaryEntity::class,
    TransactionEntity::class,
    TransactionItemEntity::class,
    ExpenseEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun posDao(): PosDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "kasir_kelontong.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
