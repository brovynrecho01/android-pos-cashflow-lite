package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SyncResult {
  data class Success(val message: String, val statusCode: Int) : SyncResult()
  data class Error(val errorMessage: String, val statusCode: Int? = null) : SyncResult()
}

data class WebhookPayload(
  val storeName: String,
  val reportDate: String,
  val closedAt: String,
  val initialCash: Long,
  val totalIncome: Long,
  val totalExpense: Long,
  val finalCash: Long,
  val totalTransactions: Int,
  val transactions: List<WebhookTransaction>,
  val expenses: List<WebhookExpense>
)

data class WebhookTransaction(
  val receiptNumber: String,
  val time: String,
  val totalAmount: Long,
  val cashPaid: Long,
  val change: Long,
  val paymentMethod: String,
  val items: List<WebhookItem>
)

data class WebhookItem(
  val name: String,
  val qty: Int,
  val unitPrice: Long,
  val subtotal: Long
)

data class WebhookExpense(
  val time: String,
  val description: String,
  val category: String,
  val amount: Long
)

class WebhookSyncManager {
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun sendDailyReport(webhookUrl: String, payload: WebhookPayload): SyncResult {
    return withContext(Dispatchers.IO) {
      if (webhookUrl.isBlank()) {
        return@withContext SyncResult.Error("URL Webhook Google Apps Script belum diisi.")
      }

      if (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://")) {
        return@withContext SyncResult.Error("URL Webhook harus diawali dengan http:// atau https://")
      }

      try {
        val rootJson = JSONObject().apply {
          put("event", "DAILY_STORE_CLOSE")
          put("storeName", payload.storeName)
          put("reportDate", payload.reportDate)
          put("closedAt", payload.closedAt)
          put("initialCash", payload.initialCash)
          put("totalIncome", payload.totalIncome)
          put("totalExpense", payload.totalExpense)
          put("finalCash", payload.finalCash)
          put("totalTransactions", payload.totalTransactions)

          // Transactions array
          val trxArray = JSONArray()
          payload.transactions.forEach { t ->
            val trxObj = JSONObject().apply {
              put("receiptNumber", t.receiptNumber)
              put("time", t.time)
              put("totalAmount", t.totalAmount)
              put("cashPaid", t.cashPaid)
              put("change", t.change)
              put("paymentMethod", t.paymentMethod)

              val itemsArray = JSONArray()
              t.items.forEach { item ->
                val itemObj = JSONObject().apply {
                  put("name", item.name)
                  put("qty", item.qty)
                  put("unitPrice", item.unitPrice)
                  put("subtotal", item.subtotal)
                }
                itemsArray.put(itemObj)
              }
              put("items", itemsArray)
            }
            trxArray.put(trxObj)
          }
          put("transactions", trxArray)

          // Expenses array
          val expArray = JSONArray()
          payload.expenses.forEach { exp ->
            val expObj = JSONObject().apply {
              put("time", exp.time)
              put("description", exp.description)
              put("category", exp.category)
              put("amount", exp.amount)
            }
            expArray.put(expObj)
          }
          put("expenses", expArray)
        }

        val jsonString = rootJson.toString()
        val requestBody = jsonString.toRequestBody(jsonMediaType)

        val request = Request.Builder()
          .url(webhookUrl.trim())
          .post(requestBody)
          .header("User-Agent", "KasirKelontongAndroid/1.0")
          .header("Accept", "application/json")
          .build()

        client.newCall(request).execute().use { response ->
          val code = response.code
          val responseBody = response.body?.string().orEmpty()

          Log.d("WebhookSync", "Response code: $code, body: $responseBody")

          if (response.isSuccessful || code == 302 || code == 301) {
            val userMsg = if (responseBody.contains("success", ignoreCase = true)) {
              "Berhasil dikirim! Google Apps Script mencatat laporan ke Spreadsheet."
            } else {
              "Berhasil terhubung ke Webhook (HTTP $code)."
            }
            SyncResult.Success(userMsg, code)
          } else {
            SyncResult.Error("Gagal mengirim data. Server merespon HTTP $code: $responseBody", code)
          }
        }
      } catch (e: Exception) {
        Log.e("WebhookSync", "Sync error", e)
        SyncResult.Error("Gagal menghubungi Webhook: ${e.localizedMessage ?: "Cek koneksi internet atau URL"}")
      }
    }
  }
}
