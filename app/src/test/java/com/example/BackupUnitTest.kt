package com.example

import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.util.DatabaseBackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupUnitTest {

  @Test
  fun testBackupAndRestoreJsonSerialization() {
    val summaries = listOf(
      DailySummaryEntity(
        date = "2026-09-26",
        initialCash = 500_000L,
        isClosed = true,
        closedAt = 1727330000000L,
        storeName = "Toko Kelontong Berkah"
      )
    )

    val transactions = listOf(
      TransactionEntity(
        id = 1L,
        date = "2026-09-26",
        receiptNumber = "TRX-20260926-001",
        timestamp = 1727331000000L,
        totalAmount = 50_000L,
        cashPaid = 100_000L,
        changeAmount = 50_000L,
        paymentMethod = "Tunai"
      )
    )

    val items = listOf(
      TransactionItemEntity(
        id = 1L,
        transactionId = 1L,
        date = "2026-09-26",
        itemName = "Minyak Goreng 2L",
        qty = 1,
        unitPrice = 35_000L,
        subtotal = 35_000L
      ),
      TransactionItemEntity(
        id = 2L,
        transactionId = 1L,
        date = "2026-09-26",
        itemName = "Gula Pasir 1kg",
        qty = 1,
        unitPrice = 15_000L,
        subtotal = 15_000L
      )
    )

    val expenses = listOf(
      ExpenseEntity(
        id = 1L,
        date = "2026-09-26",
        timestamp = 1727332000000L,
        description = "Beli plastik kresek",
        category = "Operasional",
        amount = 15_000L
      )
    )

    // 1. Generate JSON
    val backupContent = DatabaseBackupManager.generateBackupJson(
      storeName = "Toko Kelontong Berkah",
      summaries = summaries,
      transactions = transactions,
      items = items,
      expenses = expenses
    )

    assertEquals(5, backupContent.totalRecords)
    assertTrue(backupContent.jsonString.contains("Toko Kelontong Berkah"))
    assertTrue(backupContent.jsonString.contains("TRX-20260926-001"))
    assertTrue(backupContent.jsonString.contains("Minyak Goreng 2L"))

    // 2. Parse & Verify Restore
    val restoreResult = DatabaseBackupManager.parseBackupJson(backupContent.jsonString)
    assertTrue(restoreResult.isSuccess)
    assertEquals(1, restoreResult.summaries.size)
    assertEquals("2026-09-26", restoreResult.summaries[0].date)
    assertEquals(500_000L, restoreResult.summaries[0].initialCash)

    assertEquals(1, restoreResult.transactions.size)
    assertEquals(50_000L, restoreResult.transactions[0].totalAmount)

    assertEquals(2, restoreResult.items.size)
    assertEquals("Minyak Goreng 2L", restoreResult.items[0].itemName)

    assertEquals(1, restoreResult.expenses.size)
    assertEquals(15_000L, restoreResult.expenses[0].amount)
  }

  @Test
  fun testAutoBackupSchedulerStatus() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val statusInitial = com.example.work.AutoBackupScheduler.getStatus(context)
    assertTrue(statusInitial.isEnabled)

    com.example.work.AutoBackupScheduler.schedulePeriodicBackup(context)
    val statusScheduled = com.example.work.AutoBackupScheduler.getStatus(context)
    assertTrue(statusScheduled.isEnabled)
    assertEquals("Setiap 24 Jam (Otomatis)", statusScheduled.nextScheduledApprox)

    com.example.work.AutoBackupScheduler.cancelPeriodicBackup(context)
    val statusCancelled = com.example.work.AutoBackupScheduler.getStatus(context)
    org.junit.Assert.assertFalse(statusCancelled.isEnabled)
    assertEquals("Dinonaktifkan", statusCancelled.nextScheduledApprox)
  }
}
