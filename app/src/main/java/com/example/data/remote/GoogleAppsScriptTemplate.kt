package com.example.data.remote

object GoogleAppsScriptTemplate {
  val scriptCode: String = """
    // Copy kode ini ke Google Sheets -> Ekstensi (Extensions) -> Apps Script
    // Lalu klik "Terapkan" (Deploy) -> "Penerapan Baru" (New Deployment) -> Jenis: "Aplikasi Web" (Web App)
    // Akses: "Siapa Saja" (Anyone) -> Salin Webhook URL ke aplikasi ini.
    
    function doPost(e) {
      try {
        var contents = e.postData.contents;
        var data = JSON.parse(contents);
        var ss = SpreadsheetApp.getActiveSpreadsheet();
        
        // 1. Catat ke Sheet 'Arus Kas Harian'
        var summarySheet = ss.getSheetByName("Arus Kas Harian") || ss.insertSheet("Arus Kas Harian");
        if (summarySheet.getLastRow() === 0) {
          summarySheet.appendRow(["Tanggal", "Modal Awal", "Pemasukan", "Pengeluaran", "Total Akhir", "Jumlah Struk", "Waktu Tutup"]);
        }
        summarySheet.appendRow([
          data.reportDate,
          data.initialCash,
          data.totalIncome,
          data.totalExpense,
          data.finalCash,
          data.totalTransactions,
          data.closedAt
        ]);

        // 2. Catat ke Sheet 'Detail Penjualan'
        var salesSheet = ss.getSheetByName("Detail Penjualan") || ss.insertSheet("Detail Penjualan");
        if (salesSheet.getLastRow() === 0) {
          salesSheet.appendRow(["Tanggal", "No Struk", "Jam", "Nama Barang", "Qty", "Harga Satuan", "Subtotal"]);
        }
        if (data.transactions && Array.isArray(data.transactions)) {
          data.transactions.forEach(function(t) {
            if (t.items && Array.isArray(t.items)) {
              t.items.forEach(function(item) {
                salesSheet.appendRow([
                  data.reportDate,
                  t.receiptNumber,
                  t.time,
                  item.name,
                  item.qty,
                  item.unitPrice,
                  item.subtotal
                ]);
              });
            }
          });
        }

        // 3. Catat ke Sheet 'Detail Pengeluaran'
        var expenseSheet = ss.getSheetByName("Detail Pengeluaran") || ss.insertSheet("Detail Pengeluaran");
        if (expenseSheet.getLastRow() === 0) {
          expenseSheet.appendRow(["Tanggal", "Jam", "Keterangan", "Kategori", "Nominal"]);
        }
        if (data.expenses && Array.isArray(data.expenses)) {
          data.expenses.forEach(function(exp) {
            expenseSheet.appendRow([
              data.reportDate,
              exp.time,
              exp.description,
              exp.category,
              exp.amount
            ]);
          });
        }

        return ContentService.createTextOutput(JSON.stringify({
          status: "success",
          message: "Data laporan harian berhasil masuk ke Google Spreadsheet!"
        })).setMimeType(ContentService.MimeType.JSON);

      } catch (err) {
        return ContentService.createTextOutput(JSON.stringify({
          status: "error",
          message: err.toString()
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }
  """.trimIndent()
}
