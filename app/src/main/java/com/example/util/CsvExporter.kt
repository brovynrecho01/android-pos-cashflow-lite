package com.example.util

import android.content.Context
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionWithItems
import java.io.File
import java.io.FileWriter

object CsvExporter {

  fun generateCsvContent(
    date: String,
    storeName: String,
    initialCash: Long,
    totalIncome: Long,
    totalExpense: Long,
    finalCash: Long,
    transactions: List<TransactionWithItems>,
    expenses: List<ExpenseEntity>
  ): String {
    val sb = StringBuilder()

    // Header Ringkasan
    sb.appendLine("\"LAPORAN ARUS KAS HARIAN - $storeName\"")
    sb.appendLine("\"Tanggal\";\"${DateUtils.formatDisplay(date)}\"")
    sb.appendLine("\"Modal Awal (Rp)\";\"$initialCash\"")
    sb.appendLine("\"Total Pemasukan (Rp)\";\"$totalIncome\"")
    sb.appendLine("\"Total Pengeluaran (Rp)\";\"$totalExpense\"")
    sb.appendLine("\"TOTAL AKHIR KAS (Rp)\";\"$finalCash\"")
    sb.appendLine("\"Rumus\";\"Modal Awal + Pemasukan - Pengeluaran\"")
    sb.appendLine()

    // Tabel Penjualan
    sb.appendLine("\"--- DETAIL TRANSAKSI PENJUALAN ---\"")
    sb.appendLine("\"No Struk\";\"Waktu\";\"Nama Barang\";\"Jumlah (Qty)\";\"Harga Satuan (Rp)\";\"Subtotal (Rp)\";\"Metode Bayar\"")

    if (transactions.isEmpty()) {
      sb.appendLine("\"Belum ada transaksi penjualan\";\"\";\"\";\"\";\"\";\"\";\"\"")
    } else {
      transactions.forEach { t ->
        val timeStr = DateUtils.formatTime(t.transaction.timestamp)
        t.items.forEach { item ->
          sb.appendLine(
            "\"${t.transaction.receiptNumber}\";\"$timeStr\";\"${escape(item.itemName)}\";\"${item.qty}\";\"${item.unitPrice}\";\"${item.subtotal}\";\"${t.transaction.paymentMethod}\""
          )
        }
      }
    }

    sb.appendLine()

    // Tabel Pengeluaran
    sb.appendLine("\"--- DETAIL PENGELUARAN ---\"")
    sb.appendLine("\"Waktu\";\"Keterangan Pengeluaran\";\"Kategori\";\"Nominal (Rp)\"")

    if (expenses.isEmpty()) {
      sb.appendLine("\"Belum ada catatan pengeluaran\";\"\";\"\";\"\"")
    } else {
      expenses.forEach { exp ->
        val timeStr = DateUtils.formatTime(exp.timestamp)
        sb.appendLine(
          "\"$timeStr\";\"${escape(exp.description)}\";\"${escape(exp.category)}\";\"${exp.amount}\""
        )
      }
    }

    return sb.toString()
  }

  fun saveCsvToCache(context: Context, date: String, content: String): File {
    val reportsDir = File(context.cacheDir, "reports")
    if (!reportsDir.exists()) {
      reportsDir.mkdirs()
    }
    val file = File(reportsDir, "Laporan_Kasir_${date.replace("-", "")}.csv")
    FileWriter(file).use { writer ->
      writer.write(content)
    }
    return file
  }

  fun generateTextSummary(
    date: String,
    storeName: String,
    initialCash: Long,
    totalIncome: Long,
    totalExpense: Long,
    finalCash: Long,
    trxCount: Int,
    expenseCount: Int
  ): String {
    return """
      *LAPORAN ARUS KAS TOKO*
      🏪 $storeName
      📅 ${DateUtils.formatDisplay(date)}
      -----------------------------
      💰 Modal Awal: ${CurrencyFormatter.formatRupiah(initialCash)}
      📈 Pemasukan ($trxCount trx): ${CurrencyFormatter.formatRupiah(totalIncome)}
      📉 Pengeluaran ($expenseCount catatan): ${CurrencyFormatter.formatRupiah(totalExpense)}
      =============================
      💵 *TOTAL AKHIR KAS*: ${CurrencyFormatter.formatRupiah(finalCash)}
      -----------------------------
      _(Modal Awal + Pemasukan - Pengeluaran)_
      Laporan dibuat otomatis oleh Kasir Kelontong Lite.
    """.trimIndent()
  }

  private fun escape(text: String): String {
    return text.replace("\"", "\"\"")
  }
}
