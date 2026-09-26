package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PosRepository
import com.example.data.model.CartItem
import com.example.data.model.DailySummaryEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionWithItems
import com.example.data.remote.SyncResult
import com.example.data.remote.WebhookExpense
import com.example.data.remote.WebhookItem
import com.example.data.remote.WebhookPayload
import com.example.data.remote.WebhookSyncManager
import com.example.data.remote.WebhookTransaction
import com.example.util.CsvExporter
import com.example.util.CurrencyFormatter
import com.example.util.DatabaseBackupManager
import com.example.util.DateUtils
import com.example.util.FileSharer
import com.example.security.LaporanSecurityManager
import com.example.work.AutoBackupScheduler
import com.example.work.AutoBackupStatus
import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppTab(val title: String) {
  BERANDA("Beranda"),
  KASIR("Kasir"),
  PENGELUARAN("Pengeluaran"),
  LAPORAN("Laporan")
}

enum class TransactionDateFilter(val label: String) {
  HARI_INI("Hari Ini"),
  TUJUH_HARI("7 Hari"),
  TIGA_PULUH_HARI("30 Hari"),
  SEMUA("Semua Waktu")
}

sealed class SyncUiStatus {
  object Idle : SyncUiStatus()
  object Loading : SyncUiStatus()
  data class Success(val message: String) : SyncUiStatus()
  data class Error(val errorMessage: String) : SyncUiStatus()
}

data class CashflowSummaryUi(
  val date: String = DateUtils.today(),
  val initialCash: Long = 0L,
  val totalIncome: Long = 0L,
  val totalExpense: Long = 0L,
  val transactionCount: Int = 0,
  val isClosed: Boolean = false,
  val closedAt: Long? = null,
  val lastSyncAt: Long? = null,
  val storeName: String = "Toko Pak Kadi Cash System"
) {
  // RUMUS ARUS KAS HARIAN: Total Akhir = Modal Awal + Total Pemasukan - Total Pengeluaran
  val finalCash: Long
    get() = initialCash + totalIncome - totalExpense
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: PosRepository
  private val syncManager = WebhookSyncManager()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = PosRepository(db.posDao(), application)
  }

  // Active Date selection
  private val _selectedDate = MutableStateFlow(DateUtils.today())
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  // Active Navigation Tab
  private val _currentTab = MutableStateFlow(AppTab.BERANDA)
  val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

  // Snackbars / one-time events
  private val _userMessage = MutableSharedFlow<String>()
  val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

  // Cashflow Summary Reactive State
  val cashflowSummary: StateFlow<CashflowSummaryUi> = _selectedDate.flatMapLatest { date ->
    combine(
      repository.getDailySummary(date),
      repository.getIncomeFlow(date),
      repository.getExpenseFlow(date),
      repository.getTransactionCountFlow(date)
    ) { summaryEntity, income, expense, count ->
      CashflowSummaryUi(
        date = date,
        initialCash = summaryEntity?.initialCash ?: 0L,
        totalIncome = income,
        totalExpense = expense,
        transactionCount = count,
        isClosed = summaryEntity?.isClosed ?: false,
        closedAt = summaryEntity?.closedAt,
        lastSyncAt = summaryEntity?.lastSyncAt,
        storeName = summaryEntity?.storeName ?: repository.getStoreName()
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = CashflowSummaryUi()
  )

  // Reactive Transactions
  val transactionsList: StateFlow<List<TransactionWithItems>> = _selectedDate.flatMapLatest { date ->
    repository.getTransactionsWithItemsFlow(date)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Search & Date Range Filter for Transactions in Laporan
  private val _transactionSearchQuery = MutableStateFlow("")
  val transactionSearchQuery: StateFlow<String> = _transactionSearchQuery.asStateFlow()

  private val _transactionDateFilter = MutableStateFlow(TransactionDateFilter.HARI_INI)
  val transactionDateFilter: StateFlow<TransactionDateFilter> = _transactionDateFilter.asStateFlow()

  val allPastTransactions: StateFlow<List<TransactionWithItems>> =
    repository.getAllTransactionsWithItemsFlow().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val filteredTransactions: StateFlow<List<TransactionWithItems>> = combine(
    allPastTransactions,
    _selectedDate,
    _transactionSearchQuery,
    _transactionDateFilter
  ) { allList, selectedDate, query, dateFilter ->
    val dateFiltered = when (dateFilter) {
      TransactionDateFilter.HARI_INI -> allList.filter { it.transaction.date == selectedDate }
      TransactionDateFilter.TUJUH_HARI -> {
        val minDate = DateUtils.offsetDate(selectedDate, -7)
        allList.filter { it.transaction.date in minDate..selectedDate }
      }
      TransactionDateFilter.TIGA_PULUH_HARI -> {
        val minDate = DateUtils.offsetDate(selectedDate, -30)
        allList.filter { it.transaction.date in minDate..selectedDate }
      }
      TransactionDateFilter.SEMUA -> allList
    }

    if (query.isBlank()) {
      dateFiltered
    } else {
      val trimmedQuery = query.trim().lowercase()
      dateFiltered.filter { trxWithItems ->
        val receiptMatch = trxWithItems.transaction.receiptNumber.lowercase().contains(trimmedQuery)
        val paymentMatch = trxWithItems.transaction.paymentMethod.lowercase().contains(trimmedQuery)
        val amountMatch = trxWithItems.transaction.totalAmount.toString().contains(trimmedQuery) ||
          CurrencyFormatter.formatRupiah(trxWithItems.transaction.totalAmount).lowercase().contains(trimmedQuery)
        val itemMatch = trxWithItems.items.any { item ->
          item.itemName.lowercase().contains(trimmedQuery)
        }
        receiptMatch || paymentMatch || amountMatch || itemMatch
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun setTransactionSearchQuery(query: String) {
    _transactionSearchQuery.value = query
  }

  fun setTransactionDateFilter(filter: TransactionDateFilter) {
    _transactionDateFilter.value = filter
  }

  fun clearTransactionSearch() {
    _transactionSearchQuery.value = ""
  }

  // Reactive Expenses
  val expensesList: StateFlow<List<ExpenseEntity>> = _selectedDate.flatMapLatest { date ->
    repository.getExpensesFlow(date)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // ================= BERANDA STATE & ACTIONS =================
  private val _showEditInitialCashDialog = MutableStateFlow(false)
  val showEditInitialCashDialog: StateFlow<Boolean> = _showEditInitialCashDialog.asStateFlow()

  fun openEditInitialCashDialog() {
    _showEditInitialCashDialog.value = true
  }

  fun closeEditInitialCashDialog() {
    _showEditInitialCashDialog.value = false
  }

  fun saveInitialCash(newAmount: Long) {
    viewModelScope.launch {
      repository.updateInitialCash(_selectedDate.value, newAmount)
      _showEditInitialCashDialog.value = false
      _userMessage.emit("Modal awal diperbarui: ${CurrencyFormatter.formatRupiah(newAmount)}")
    }
  }

  // ================= KASIR (PEMASUKAN) STATE & ACTIONS =================
  private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
  val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

  // Current inputs in Kasir
  private val _inputItemName = MutableStateFlow("")
  val inputItemName: StateFlow<String> = _inputItemName.asStateFlow()

  private val _inputQty = MutableStateFlow(1)
  val inputQty: StateFlow<Int> = _inputQty.asStateFlow()

  private val _inputUnitPrice = MutableStateFlow("")
  val inputUnitPrice: StateFlow<String> = _inputUnitPrice.asStateFlow()

  private val _cashPaidInput = MutableStateFlow("")
  val cashPaidInput: StateFlow<String> = _cashPaidInput.asStateFlow()

  // Completed transaction receipt dialog
  private val _lastCompletedTransaction = MutableStateFlow<TransactionWithItems?>(null)
  val lastCompletedTransaction: StateFlow<TransactionWithItems?> = _lastCompletedTransaction.asStateFlow()

  fun setInputItemName(name: String) {
    _inputItemName.value = name
  }

  fun setInputQty(qty: Int) {
    _inputQty.value = qty.coerceAtLeast(1)
  }

  fun incrementQty() {
    _inputQty.value = _inputQty.value + 1
  }

  fun decrementQty() {
    if (_inputQty.value > 1) {
      _inputQty.value = _inputQty.value - 1
    }
  }

  fun setInputUnitPrice(priceText: String) {
    val clean = priceText.replace("[^0-9]".toRegex(), "")
    _inputUnitPrice.value = clean
  }

  fun addPricePreset(additional: Long) {
    val current = CurrencyFormatter.parseAmount(_inputUnitPrice.value)
    _inputUnitPrice.value = (current + additional).toString()
  }

  fun setUnitPricePreset(amount: Long) {
    _inputUnitPrice.value = amount.toString()
  }

  fun addItemToCart() {
    val name = _inputItemName.value.trim()
    val qty = _inputQty.value
    val price = CurrencyFormatter.parseAmount(_inputUnitPrice.value)

    if (name.isBlank()) {
      viewModelScope.launch { _userMessage.emit("Nama barang wajib diisi!") }
      return
    }

    if (price <= 0L) {
      viewModelScope.launch { _userMessage.emit("Harga satuan harus lebih dari 0!") }
      return
    }

    val newItem = CartItem(
      itemName = name,
      qty = qty,
      unitPrice = price
    )

    _cartItems.value = _cartItems.value + newItem
    // Reset quick inputs
    _inputItemName.value = ""
    _inputQty.value = 1
    _inputUnitPrice.value = ""
  }

  fun removeCartItem(item: CartItem) {
    _cartItems.value = _cartItems.value.filter { it.id != item.id }
  }

  fun updateCartItemQty(itemId: String, newQty: Int) {
    if (newQty <= 0) {
      _cartItems.value = _cartItems.value.filter { it.id != itemId }
    } else {
      _cartItems.value = _cartItems.value.map {
        if (it.id == itemId) it.copy(qty = newQty) else it
      }
    }
  }

  fun clearCart() {
    _cartItems.value = emptyList()
    _cashPaidInput.value = ""
  }

  fun setCashPaidInput(amountText: String) {
    val clean = amountText.replace("[^0-9]".toRegex(), "")
    _cashPaidInput.value = clean
  }

  fun setCashExact() {
    val grandTotal = _cartItems.value.sumOf { it.subtotal }
    _cashPaidInput.value = grandTotal.toString()
  }

  fun addCashPreset(amount: Long) {
    val current = CurrencyFormatter.parseAmount(_cashPaidInput.value)
    _cashPaidInput.value = (current + amount).toString()
  }

  fun saveTransaction() {
    val items = _cartItems.value
    if (items.isEmpty()) {
      viewModelScope.launch { _userMessage.emit("Keranjang belanja masih kosong!") }
      return
    }

    val grandTotal = items.sumOf { it.subtotal }
    val paid = CurrencyFormatter.parseAmount(_cashPaidInput.value)

    if (paid < grandTotal) {
      viewModelScope.launch { _userMessage.emit("Uang yang diterima kurang dari total belanja!") }
      return
    }

    viewModelScope.launch {
      val date = _selectedDate.value
      val receiptNum = "TRX-${date.replace("-", "")}-${System.currentTimeMillis() % 10000}"

      val trxId = repository.recordSale(
        date = date,
        receiptNumber = receiptNum,
        cartItems = items,
        cashPaid = paid,
        paymentMethod = "Tunai"
      )

      // Reset cart
      _cartItems.value = emptyList()
      _cashPaidInput.value = ""

      // Fetch newly recorded transaction for receipt preview
      val todayTrxs = repository.getTransactionsWithItemsDirect(date)
      val recorded = todayTrxs.firstOrNull { it.transaction.id == trxId }
      _lastCompletedTransaction.value = recorded

      _userMessage.emit("Transaksi $receiptNum berhasil disimpan! Rp ${CurrencyFormatter.formatNumber(grandTotal)}")
    }
  }

  fun dismissReceiptDialog() {
    _lastCompletedTransaction.value = null
  }

  // ================= PENGELUARAN STATE & ACTIONS =================
  private val _expenseDescription = MutableStateFlow("")
  val expenseDescription: StateFlow<String> = _expenseDescription.asStateFlow()

  private val _expenseAmount = MutableStateFlow("")
  val expenseAmount: StateFlow<String> = _expenseAmount.asStateFlow()

  private val _expenseCategory = MutableStateFlow("Operasional")
  val expenseCategory: StateFlow<String> = _expenseCategory.asStateFlow()

  fun setExpenseDescription(desc: String) {
    _expenseDescription.value = desc
  }

  fun setExpenseAmount(amountText: String) {
    val clean = amountText.replace("[^0-9]".toRegex(), "")
    _expenseAmount.value = clean
  }

  fun setExpenseCategory(cat: String) {
    _expenseCategory.value = cat
  }

  fun saveExpense() {
    val desc = _expenseDescription.value.trim()
    val amount = CurrencyFormatter.parseAmount(_expenseAmount.value)
    val cat = _expenseCategory.value

    if (desc.isBlank()) {
      viewModelScope.launch { _userMessage.emit("Keterangan pengeluaran wajib diisi!") }
      return
    }

    if (amount <= 0L) {
      viewModelScope.launch { _userMessage.emit("Nominal pengeluaran harus lebih dari 0!") }
      return
    }

    viewModelScope.launch {
      repository.addExpense(_selectedDate.value, desc, amount, cat)
      _expenseDescription.value = ""
      _expenseAmount.value = ""
      _userMessage.emit("Pengeluaran disimpan: ${CurrencyFormatter.formatRupiah(amount)}")
    }
  }

  fun deleteExpense(id: Long) {
    viewModelScope.launch {
      repository.deleteExpense(id)
      _userMessage.emit("Catatan pengeluaran berhasil dihapus")
    }
  }

  fun deleteTransaction(id: Long) {
    viewModelScope.launch {
      repository.deleteSale(id)
      _userMessage.emit("Transaksi penjualan berhasil dihapus")
    }
  }

  // ================= LAPORAN & TUTUP TOKO STATE & ACTIONS =================
  private val _webhookUrl = MutableStateFlow(repository.getWebhookUrl())
  val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()

  private val _syncUiStatus = MutableStateFlow<SyncUiStatus>(SyncUiStatus.Idle)
  val syncUiStatus: StateFlow<SyncUiStatus> = _syncUiStatus.asStateFlow()

  private val _showScriptGuideDialog = MutableStateFlow(false)
  val showScriptGuideDialog: StateFlow<Boolean> = _showScriptGuideDialog.asStateFlow()

  fun setWebhookUrl(url: String) {
    _webhookUrl.value = url
  }

  fun saveWebhookUrl() {
    val url = _webhookUrl.value.trim()
    repository.setWebhookUrl(url)
    viewModelScope.launch {
      _userMessage.emit("URL Webhook berhasil disimpan")
    }
  }

  fun openScriptGuide() {
    _showScriptGuideDialog.value = true
  }

  fun closeScriptGuide() {
    _showScriptGuideDialog.value = false
  }

  fun closeStoreAndSendReport() {
    val url = _webhookUrl.value.trim()
    if (url.isBlank()) {
      viewModelScope.launch {
        _userMessage.emit("Silakan masukkan URL Webhook Google Apps Script terlebih dahulu!")
      }
      return
    }

    viewModelScope.launch {
      _syncUiStatus.value = SyncUiStatus.Loading

      val date = _selectedDate.value
      val summary = cashflowSummary.value
      val trxs = repository.getTransactionsWithItemsDirect(date)
      val expenses = repository.getExpensesDirect(date)

      val payload = WebhookPayload(
        storeName = summary.storeName,
        reportDate = date,
        closedAt = DateUtils.formatIsoDateTime(System.currentTimeMillis()),
        initialCash = summary.initialCash,
        totalIncome = summary.totalIncome,
        totalExpense = summary.totalExpense,
        finalCash = summary.finalCash,
        totalTransactions = trxs.size,
        transactions = trxs.map { t ->
          WebhookTransaction(
            receiptNumber = t.transaction.receiptNumber,
            time = DateUtils.formatTime(t.transaction.timestamp),
            totalAmount = t.transaction.totalAmount,
            cashPaid = t.transaction.cashPaid,
            change = t.transaction.changeAmount,
            paymentMethod = t.transaction.paymentMethod,
            items = t.items.map { item ->
              WebhookItem(
                name = item.itemName,
                qty = item.qty,
                unitPrice = item.unitPrice,
                subtotal = item.subtotal
              )
            }
          )
        },
        expenses = expenses.map { exp ->
          WebhookExpense(
            time = DateUtils.formatTime(exp.timestamp),
            description = exp.description,
            category = exp.category,
            amount = exp.amount
          )
        }
      )

      when (val result = syncManager.sendDailyReport(url, payload)) {
        is SyncResult.Success -> {
          val now = System.currentTimeMillis()
          repository.markDayClosed(date, now)
          repository.updateLastSync(date, now)
          _syncUiStatus.value = SyncUiStatus.Success(result.message)
          _userMessage.emit("Toko berhasil ditutup & laporan terkirim ke Webhook!")
        }
        is SyncResult.Error -> {
          _syncUiStatus.value = SyncUiStatus.Error(result.errorMessage)
          _userMessage.emit("Gagal kirim Webhook: ${result.errorMessage}")
        }
      }
    }
  }

  fun exportAndShareCsv() {
    viewModelScope.launch {
      val date = _selectedDate.value
      val summary = cashflowSummary.value
      val trxs = repository.getTransactionsWithItemsDirect(date)
      val expenses = repository.getExpensesDirect(date)

      val csvString = CsvExporter.generateCsvContent(
        date = date,
        storeName = summary.storeName,
        initialCash = summary.initialCash,
        totalIncome = summary.totalIncome,
        totalExpense = summary.totalExpense,
        finalCash = summary.finalCash,
        transactions = trxs,
        expenses = expenses
      )

      val summaryText = CsvExporter.generateTextSummary(
        date = date,
        storeName = summary.storeName,
        initialCash = summary.initialCash,
        totalIncome = summary.totalIncome,
        totalExpense = summary.totalExpense,
        finalCash = summary.finalCash,
        trxCount = trxs.size,
        expenseCount = expenses.size
      )

      val file = CsvExporter.saveCsvToCache(getApplication(), date, csvString)
      FileSharer.shareCsvFile(getApplication(), file, summaryText)
    }
  }

  fun shareTextReport() {
    val date = _selectedDate.value
    val summary = cashflowSummary.value
    val text = CsvExporter.generateTextSummary(
      date = date,
      storeName = summary.storeName,
      initialCash = summary.initialCash,
      totalIncome = summary.totalIncome,
      totalExpense = summary.totalExpense,
      finalCash = summary.finalCash,
      trxCount = summary.transactionCount,
      expenseCount = expensesList.value.size
    )
    FileSharer.shareText(getApplication(), text)
  }

  // ================= DATABASE BACKUP & DISASTER RECOVERY =================
  private val _backupStatus = MutableStateFlow<String?>(null)
  val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

  private val _latestBackupFile = MutableStateFlow<File?>(null)
  val latestBackupFile: StateFlow<File?> = _latestBackupFile.asStateFlow()

  private val _showBackupDialog = MutableStateFlow(false)
  val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

  fun openBackupDialog() {
    _showBackupDialog.value = true
  }

  fun closeBackupDialog() {
    _showBackupDialog.value = false
  }

  fun backupDatabase(shareFile: Boolean = true) {
    viewModelScope.launch {
      _backupStatus.value = "Membuat cadangan database..."
      val summaries = repository.getAllDailySummaries()
      val transactions = repository.getAllTransactions()
      val items = repository.getAllTransactionItems()
      val expenses = repository.getAllExpenses()

      val backupContent = DatabaseBackupManager.generateBackupJson(
        storeName = repository.getStoreName(),
        summaries = summaries,
        transactions = transactions,
        items = items,
        expenses = expenses
      )

      val savedFile = DatabaseBackupManager.saveToExternalStorage(getApplication(), backupContent)
      _latestBackupFile.value = savedFile
      _backupStatus.value = "Tersimpan di Dokumen: ${savedFile.name} (${backupContent.totalRecords} total data)"
      _userMessage.emit("Backup database SQLite berhasil disimpan: ${savedFile.name}")

      if (shareFile) {
        DatabaseBackupManager.shareBackupFile(getApplication(), savedFile)
      }
    }
  }

  fun restoreDatabaseFromJson(jsonString: String) {
    viewModelScope.launch {
      val result = DatabaseBackupManager.parseBackupJson(jsonString)
      if (result.isSuccess) {
        repository.restoreDatabase(
          summaries = result.summaries,
          transactions = result.transactions,
          items = result.items,
          expenses = result.expenses
        )
        _userMessage.emit("Pemulihan bencana berhasil! ${result.message}")
        _backupStatus.value = "Database berhasil dipulihkan dari file backup."
      } else {
        _userMessage.emit("Gagal memulihkan database: ${result.message}")
      }
    }
  }

  fun restoreDatabaseFromUri(uri: Uri) {
    viewModelScope.launch {
      try {
        val jsonContent = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
          stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        if (!jsonContent.isNullOrBlank()) {
          restoreDatabaseFromJson(jsonContent)
        } else {
          _userMessage.emit("File backup kosong atau tidak dapat dibaca.")
        }
      } catch (e: Exception) {
        _userMessage.emit("Gagal membaca file: ${e.message}")
      }
    }
  }

  // ================= AUTO BACKUP (WORKMANAGER 24H) =================
  private val _autoBackupStatus = MutableStateFlow(AutoBackupScheduler.getStatus(getApplication()))
  val autoBackupStatus: StateFlow<AutoBackupStatus> = _autoBackupStatus.asStateFlow()

  fun refreshAutoBackupStatus() {
    _autoBackupStatus.value = AutoBackupScheduler.getStatus(getApplication())
  }

  fun toggleAutoBackup(enabled: Boolean) {
    if (enabled) {
      AutoBackupScheduler.schedulePeriodicBackup(getApplication(), forceUpdate = true)
      viewModelScope.launch {
        _userMessage.emit("Auto-backup 24 jam diaktifkan.")
      }
    } else {
      AutoBackupScheduler.cancelPeriodicBackup(getApplication())
      viewModelScope.launch {
        _userMessage.emit("Auto-backup 24 jam dinonaktifkan.")
      }
    }
    refreshAutoBackupStatus()
  }

  fun triggerImmediateAutoBackup() {
    viewModelScope.launch {
      AutoBackupScheduler.triggerImmediateBackup(getApplication())
      _userMessage.emit("Menjalankan tugas auto-backup di latar belakang...")
      kotlinx.coroutines.delay(1200)
      refreshAutoBackupStatus()
    }
  }

  // ================= LAPORAN SECURITY & PIN/BIOMETRIC =================
  private val _isLaporanUnlocked = MutableStateFlow(false)
  val isLaporanUnlocked: StateFlow<Boolean> = _isLaporanUnlocked.asStateFlow()

  private val _isSecurityEnabled = MutableStateFlow(LaporanSecurityManager.isSecurityEnabled(getApplication()))
  val isSecurityEnabled: StateFlow<Boolean> = _isSecurityEnabled.asStateFlow()

  private val _isBiometricEnabled = MutableStateFlow(LaporanSecurityManager.isBiometricEnabled(getApplication()))
  val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

  private val _hasCustomPin = MutableStateFlow(LaporanSecurityManager.hasCustomPin(getApplication()))
  val hasCustomPin: StateFlow<Boolean> = _hasCustomPin.asStateFlow()

  private val _showSecurityDialog = MutableStateFlow(false)
  val showSecurityDialog: StateFlow<Boolean> = _showSecurityDialog.asStateFlow()

  fun unlockLaporanWithPin(pin: String): Boolean {
    val valid = LaporanSecurityManager.verifyPin(getApplication(), pin)
    if (valid) {
      _isLaporanUnlocked.value = true
    }
    return valid
  }

  fun unlockLaporanBiometric() {
    _isLaporanUnlocked.value = true
  }

  fun lockLaporan() {
    _isLaporanUnlocked.value = false
  }

  fun openSecurityDialog() {
    _showSecurityDialog.value = true
  }

  fun closeSecurityDialog() {
    _showSecurityDialog.value = false
  }

  fun toggleSecurityEnabled(enabled: Boolean) {
    LaporanSecurityManager.setSecurityEnabled(getApplication(), enabled)
    _isSecurityEnabled.value = enabled
    if (!enabled) {
      _isLaporanUnlocked.value = true
    }
  }

  fun toggleBiometricEnabled(enabled: Boolean) {
    LaporanSecurityManager.setBiometricEnabled(getApplication(), enabled)
    _isBiometricEnabled.value = enabled
  }

  fun changePin(oldPin: String, newPin: String): Boolean {
    val validOld = LaporanSecurityManager.verifyPin(getApplication(), oldPin)
    if (!validOld) return false

    LaporanSecurityManager.setPin(getApplication(), newPin)
    _hasCustomPin.value = true
    viewModelScope.launch {
      _userMessage.emit("PIN keamanan berhasil diperbarui!")
    }
    return true
  }

  // Navigation & Date controls
  fun selectTab(tab: AppTab) {
    _currentTab.value = tab
  }

  fun changeSelectedDate(date: String) {
    _selectedDate.value = date
  }

  fun selectPreviousDay() {
    _selectedDate.value = DateUtils.offsetDate(_selectedDate.value, -1)
  }

  fun selectNextDay() {
    _selectedDate.value = DateUtils.offsetDate(_selectedDate.value, 1)
  }

  fun resetToToday() {
    _selectedDate.value = DateUtils.today()
  }
}
