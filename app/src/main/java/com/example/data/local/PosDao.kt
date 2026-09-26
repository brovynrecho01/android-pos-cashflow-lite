package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {

  // Daily Summary Queries
  @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
  fun getDailySummary(date: String): Flow<DailySummaryEntity?>

  @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
  suspend fun getDailySummaryDirect(date: String): DailySummaryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateDailySummary(summary: DailySummaryEntity)

  @Query("UPDATE daily_summaries SET initialCash = :initialCash WHERE date = :date")
  suspend fun updateInitialCash(date: String, initialCash: Long)

  @Query("UPDATE daily_summaries SET isClosed = 1, closedAt = :closedAt WHERE date = :date")
  suspend fun markDayClosed(date: String, closedAt: Long)

  @Query("UPDATE daily_summaries SET lastSyncAt = :syncAt WHERE date = :date")
  suspend fun updateLastSyncTime(date: String, syncAt: Long)

  // Transactions & Items
  @Transaction
  @Query("SELECT * FROM transactions WHERE date = :date ORDER BY timestamp DESC")
  fun getTransactionsWithItems(date: String): Flow<List<TransactionWithItems>>

  @Transaction
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun getAllTransactionsWithItemsFlow(): Flow<List<TransactionWithItems>>

  @Transaction
  @Query("SELECT * FROM transactions WHERE date = :date ORDER BY timestamp DESC")
  suspend fun getTransactionsWithItemsDirect(date: String): List<TransactionWithItems>

  @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM transactions WHERE date = :date")
  fun getTotalIncomeFlow(date: String): Flow<Long>

  @Query("SELECT COUNT(*) FROM transactions WHERE date = :date")
  fun getTransactionCountFlow(date: String): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

  @Transaction
  suspend fun recordSale(
    transaction: TransactionEntity,
    items: List<Pair<String, Pair<Int, Long>>> // itemName, qty, unitPrice
  ): Long {
    val trxId = insertTransaction(transaction)
    val entityItems = items.map { (name, pair) ->
      val (qty, price) = pair
      TransactionItemEntity(
        transactionId = trxId,
        date = transaction.date,
        itemName = name,
        qty = qty,
        unitPrice = price,
        subtotal = qty.toLong() * price
      )
    }
    insertTransactionItems(entityItems)
    return trxId
  }

  @Query("DELETE FROM transactions WHERE id = :transactionId")
  suspend fun deleteTransaction(transactionId: Long)

  @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
  suspend fun deleteTransactionItems(transactionId: Long)

  @Transaction
  suspend fun deleteSale(transactionId: Long) {
    deleteTransactionItems(transactionId)
    deleteTransaction(transactionId)
  }

  // Expenses
  @Query("SELECT * FROM expenses WHERE date = :date ORDER BY timestamp DESC")
  fun getExpensesFlow(date: String): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE date = :date ORDER BY timestamp DESC")
  suspend fun getExpensesDirect(date: String): List<ExpenseEntity>

  @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date = :date")
  fun getTotalExpenseFlow(date: String): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpense(id: Long)

  // Query for items sold summary
  @Query("SELECT * FROM transaction_items WHERE date = :date ORDER BY id DESC")
  fun getAllItemsSoldFlow(date: String): Flow<List<TransactionItemEntity>>

  // Full Database Backup & Disaster Recovery Queries
  @Query("SELECT * FROM daily_summaries")
  suspend fun getAllDailySummaries(): List<DailySummaryEntity>

  @Query("SELECT * FROM transactions")
  suspend fun getAllTransactions(): List<TransactionEntity>

  @Query("SELECT * FROM transaction_items")
  suspend fun getAllTransactionItems(): List<TransactionItemEntity>

  @Query("SELECT * FROM expenses")
  suspend fun getAllExpenses(): List<ExpenseEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun restoreDailySummaries(summaries: List<DailySummaryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun restoreTransactions(transactions: List<TransactionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun restoreTransactionItems(items: List<TransactionItemEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun restoreExpenses(expenses: List<ExpenseEntity>)
}
