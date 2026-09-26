package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupContent(
  val jsonString: String,
  val fileName: String,
  val totalRecords: Int,
  val summaryCount: Int,
  val transactionCount: Int,
  val itemCount: Int,
  val expenseCount: Int
)

data class RestoreResult(
  val isSuccess: Boolean,
  val message: String,
  val summaries: List<DailySummaryEntity> = emptyList(),
  val transactions: List<TransactionEntity> = emptyList(),
  val items: List<TransactionItemEntity> = emptyList(),
  val expenses: List<ExpenseEntity> = emptyList()
)

object DatabaseBackupManager {

  private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
  private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

  fun generateBackupJson(
    storeName: String,
    summaries: List<DailySummaryEntity>,
    transactions: List<TransactionEntity>,
    items: List<TransactionItemEntity>,
    expenses: List<ExpenseEntity>
  ): BackupContent {
    val now = System.currentTimeMillis()
    val timeFormatted = fileDateFormat.format(Date(now))
    val fileName = "backup_kasir_kelontong_$timeFormatted.json"

    val root = JSONObject().apply {
      put("app", "Toko Pak Kadi Cash System")
      put("backupVersion", 1)
      put("backupTimestamp", now)
      put("backupDate", displayDateFormat.format(Date(now)))
      put("storeName", storeName)

      val counts = JSONObject().apply {
        put("dailySummaries", summaries.size)
        put("transactions", transactions.size)
        put("transactionItems", items.size)
        put("expenses", expenses.size)
      }
      put("counts", counts)

      // Summaries array
      val sumArray = JSONArray()
      summaries.forEach { s ->
        sumArray.put(JSONObject().apply {
          put("date", s.date)
          put("initialCash", s.initialCash)
          put("isClosed", s.isClosed)
          put("closedAt", s.closedAt ?: JSONObject.NULL)
          put("lastSyncAt", s.lastSyncAt ?: JSONObject.NULL)
          put("storeName", s.storeName)
        })
      }
      put("dailySummaries", sumArray)

      // Transactions array
      val trxArray = JSONArray()
      transactions.forEach { t ->
        trxArray.put(JSONObject().apply {
          put("id", t.id)
          put("date", t.date)
          put("receiptNumber", t.receiptNumber)
          put("timestamp", t.timestamp)
          put("totalAmount", t.totalAmount)
          put("cashPaid", t.cashPaid)
          put("changeAmount", t.changeAmount)
          put("paymentMethod", t.paymentMethod)
        })
      }
      put("transactions", trxArray)

      // Items array
      val itemsArray = JSONArray()
      items.forEach { item ->
        itemsArray.put(JSONObject().apply {
          put("id", item.id)
          put("transactionId", item.transactionId)
          put("date", item.date)
          put("itemName", item.itemName)
          put("qty", item.qty)
          put("unitPrice", item.unitPrice)
          put("subtotal", item.subtotal)
        })
      }
      put("transactionItems", itemsArray)

      // Expenses array
      val expArray = JSONArray()
      expenses.forEach { e ->
        expArray.put(JSONObject().apply {
          put("id", e.id)
          put("date", e.date)
          put("timestamp", e.timestamp)
          put("description", e.description)
          put("category", e.category)
          put("amount", e.amount)
        })
      }
      put("expenses", expArray)
    }

    val jsonStr = root.toString(2)
    val total = summaries.size + transactions.size + items.size + expenses.size

    return BackupContent(
      jsonString = jsonStr,
      fileName = fileName,
      totalRecords = total,
      summaryCount = summaries.size,
      transactionCount = transactions.size,
      itemCount = items.size,
      expenseCount = expenses.size
    )
  }

  fun saveToExternalStorage(context: Context, backup: BackupContent): File {
    // Priority 1: External Documents directory (accessible to file manager & user)
    val extDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
      ?: File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }

    if (!extDir.exists()) {
      extDir.mkdirs()
    }

    val file = File(extDir, backup.fileName)
    FileWriter(file).use { writer ->
      writer.write(backup.jsonString)
    }
    return file
  }

  fun saveBackupToUri(context: Context, uri: Uri, jsonString: String): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(jsonString.toByteArray(Charsets.UTF_8))
        stream.flush()
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  fun shareBackupFile(context: Context, backupFile: File) {
    try {
      val authority = "${context.packageName}.fileprovider"
      val uri = FileProvider.getUriForFile(context, authority, backupFile)

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Backup Database Kasir Kelontong (${backupFile.name})")
        putExtra(Intent.EXTRA_TEXT, "File backup database SQLite JSON untuk pemulihan bencana (disaster recovery).")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Simpan / Kirim File Backup Database:")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Gagal membagikan file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun parseBackupJson(jsonString: String): RestoreResult {
    return try {
      val root = JSONObject(jsonString)

      val summaries = mutableListOf<DailySummaryEntity>()
      val sumArray = root.optJSONArray("dailySummaries") ?: JSONArray()
      for (i in 0 until sumArray.length()) {
        val obj = sumArray.getJSONObject(i)
        summaries.add(
          DailySummaryEntity(
            date = obj.getString("date"),
            initialCash = obj.optLong("initialCash", 0L),
            isClosed = obj.optBoolean("isClosed", false),
            closedAt = if (obj.isNull("closedAt")) null else obj.optLong("closedAt"),
            lastSyncAt = if (obj.isNull("lastSyncAt")) null else obj.optLong("lastSyncAt"),
            storeName = obj.optString("storeName", "Toko Kelontong")
          )
        )
      }

      val transactions = mutableListOf<TransactionEntity>()
      val trxArray = root.optJSONArray("transactions") ?: JSONArray()
      for (i in 0 until trxArray.length()) {
        val obj = trxArray.getJSONObject(i)
        transactions.add(
          TransactionEntity(
            id = obj.optLong("id", 0L),
            date = obj.getString("date"),
            receiptNumber = obj.getString("receiptNumber"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            totalAmount = obj.getLong("totalAmount"),
            cashPaid = obj.optLong("cashPaid", 0L),
            changeAmount = obj.optLong("changeAmount", 0L),
            paymentMethod = obj.optString("paymentMethod", "Tunai")
          )
        )
      }

      val items = mutableListOf<TransactionItemEntity>()
      val itemsArray = root.optJSONArray("transactionItems") ?: JSONArray()
      for (i in 0 until itemsArray.length()) {
        val obj = itemsArray.getJSONObject(i)
        items.add(
          TransactionItemEntity(
            id = obj.optLong("id", 0L),
            transactionId = obj.getLong("transactionId"),
            date = obj.getString("date"),
            itemName = obj.getString("itemName"),
            qty = obj.getInt("qty"),
            unitPrice = obj.getLong("unitPrice"),
            subtotal = obj.getLong("subtotal")
          )
        )
      }

      val expenses = mutableListOf<ExpenseEntity>()
      val expArray = root.optJSONArray("expenses") ?: JSONArray()
      for (i in 0 until expArray.length()) {
        val obj = expArray.getJSONObject(i)
        expenses.add(
          ExpenseEntity(
            id = obj.optLong("id", 0L),
            date = obj.getString("date"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            description = obj.getString("description"),
            category = obj.optString("category", "Operasional"),
            amount = obj.getLong("amount")
          )
        )
      }

      RestoreResult(
        isSuccess = true,
        message = "File backup valid: ${summaries.size} hari, ${transactions.size} struk transaksi, ${items.size} item, ${expenses.size} catatan pengeluaran.",
        summaries = summaries,
        transactions = transactions,
        items = items,
        expenses = expenses
      )
    } catch (e: Exception) {
      RestoreResult(
        isSuccess = false,
        message = "Format file backup tidak valid: ${e.message}"
      )
    }
  }
}
