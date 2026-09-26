package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CartItem
import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PosRepository(
  private val posDao: PosDao,
  private val context: Context
) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("pos_kelontong_prefs", Context.MODE_PRIVATE)

  companion object {
    const val DEFAULT_WEBHOOK_URL = ""
  }

  // SharedPreferences for Webhook URL and Store Name
  fun getWebhookUrl(): String {
    return prefs.getString("webhook_url", DEFAULT_WEBHOOK_URL) ?: DEFAULT_WEBHOOK_URL
  }

  fun setWebhookUrl(url: String) {
    prefs.edit().putString("webhook_url", url.trim()).apply()
  }

  fun getStoreName(): String {
    return prefs.getString("store_name", "Toko Pak Kadi Cash System") ?: "Toko Pak Kadi Cash System"
  }

  fun setStoreName(name: String) {
    prefs.edit().putString("store_name", name.trim()).apply()
  }

  // Backup & Restore
  suspend fun getAllDailySummaries() = posDao.getAllDailySummaries()
  suspend fun getAllTransactions() = posDao.getAllTransactions()
  suspend fun getAllTransactionItems() = posDao.getAllTransactionItems()
  suspend fun getAllExpenses() = posDao.getAllExpenses()

  suspend fun restoreDatabase(
    summaries: List<DailySummaryEntity>,
    transactions: List<TransactionEntity>,
    items: List<TransactionItemEntity>,
    expenses: List<ExpenseEntity>
  ) {
    if (summaries.isNotEmpty()) posDao.restoreDailySummaries(summaries)
    if (transactions.isNotEmpty()) posDao.restoreTransactions(transactions)
    if (items.isNotEmpty()) posDao.restoreTransactionItems(items)
    if (expenses.isNotEmpty()) posDao.restoreExpenses(expenses)
  }

  // Daily Summary
  fun getDailySummary(date: String): Flow<DailySummaryEntity?> {
    return posDao.getDailySummary(date)
  }

  suspend fun ensureDailySummaryExists(date: String): DailySummaryEntity {
    val existing = posDao.getDailySummaryDirect(date)
    if (existing != null) return existing
    val newSummary = DailySummaryEntity(
      date = date,
      initialCash = 0L,
      isClosed = false,
      storeName = getStoreName()
    )
    posDao.insertOrUpdateDailySummary(newSummary)
    return newSummary
  }

  suspend fun updateInitialCash(date: String, initialCash: Long) {
    val existing = posDao.getDailySummaryDirect(date)
    if (existing == null) {
      posDao.insertOrUpdateDailySummary(
        DailySummaryEntity(
          date = date,
          initialCash = initialCash,
          storeName = getStoreName()
        )
      )
    } else {
      posDao.updateInitialCash(date, initialCash)
    }
  }

  suspend fun markDayClosed(date: String, closedAt: Long) {
    posDao.markDayClosed(date, closedAt)
  }

  suspend fun updateLastSync(date: String, syncAt: Long) {
    posDao.updateLastSyncTime(date, syncAt)
  }

  // Income & Transactions
  fun getIncomeFlow(date: String): Flow<Long> = posDao.getTotalIncomeFlow(date)

  fun getTransactionCountFlow(date: String): Flow<Int> = posDao.getTransactionCountFlow(date)

  fun getTransactionsWithItemsFlow(date: String): Flow<List<TransactionWithItems>> =
    posDao.getTransactionsWithItems(date)

  fun getAllTransactionsWithItemsFlow(): Flow<List<TransactionWithItems>> =
    posDao.getAllTransactionsWithItemsFlow()

  suspend fun getTransactionsWithItemsDirect(date: String): List<TransactionWithItems> =
    posDao.getTransactionsWithItemsDirect(date)

  suspend fun recordSale(
    date: String,
    receiptNumber: String,
    cartItems: List<CartItem>,
    cashPaid: Long,
    paymentMethod: String = "Tunai"
  ): Long {
    ensureDailySummaryExists(date)

    val grandTotal = cartItems.sumOf { it.subtotal }
    val changeAmount = (cashPaid - grandTotal).coerceAtLeast(0L)

    val trx = TransactionEntity(
      date = date,
      receiptNumber = receiptNumber,
      timestamp = System.currentTimeMillis(),
      totalAmount = grandTotal,
      cashPaid = cashPaid,
      changeAmount = changeAmount,
      paymentMethod = paymentMethod
    )

    val items = cartItems.map {
      Pair(it.itemName, Pair(it.qty, it.unitPrice))
    }

    return posDao.recordSale(trx, items)
  }

  suspend fun deleteSale(transactionId: Long) {
    posDao.deleteSale(transactionId)
  }

  // Expenses
  fun getExpenseFlow(date: String): Flow<Long> = posDao.getTotalExpenseFlow(date)

  fun getExpensesFlow(date: String): Flow<List<ExpenseEntity>> = posDao.getExpensesFlow(date)

  suspend fun getExpensesDirect(date: String): List<ExpenseEntity> = posDao.getExpensesDirect(date)

  suspend fun addExpense(
    date: String,
    description: String,
    amount: Long,
    category: String
  ): Long {
    ensureDailySummaryExists(date)
    val expense = ExpenseEntity(
      date = date,
      timestamp = System.currentTimeMillis(),
      description = description.trim(),
      category = category.trim(),
      amount = amount
    )
    return posDao.insertExpense(expense)
  }

  suspend fun deleteExpense(id: Long) {
    posDao.deleteExpense(id)
  }
}
