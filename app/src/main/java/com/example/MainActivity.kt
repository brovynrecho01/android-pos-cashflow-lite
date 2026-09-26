package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthHelper
import com.example.ui.components.SecuritySettingsDialog
import com.example.ui.screens.LaporanLockScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.KasirScreen
import com.example.ui.screens.LaporanScreen
import com.example.ui.screens.PengeluaranScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        KasirKelontongApp()
      }
    }
  }
}

@Composable
fun KasirKelontongApp(viewModel: MainViewModel = viewModel()) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  // State collectors
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
  val summary by viewModel.cashflowSummary.collectAsStateWithLifecycle()
  val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
  val expenses by viewModel.expensesList.collectAsStateWithLifecycle()
  val transactions by viewModel.transactionsList.collectAsStateWithLifecycle()

  // Kasir Inputs
  val inputItemName by viewModel.inputItemName.collectAsStateWithLifecycle()
  val inputQty by viewModel.inputQty.collectAsStateWithLifecycle()
  val inputUnitPrice by viewModel.inputUnitPrice.collectAsStateWithLifecycle()
  val cashPaidInput by viewModel.cashPaidInput.collectAsStateWithLifecycle()
  val lastReceipt by viewModel.lastCompletedTransaction.collectAsStateWithLifecycle()

  // Pengeluaran Inputs
  val expenseDesc by viewModel.expenseDescription.collectAsStateWithLifecycle()
  val expenseAmount by viewModel.expenseAmount.collectAsStateWithLifecycle()
  val expenseCategory by viewModel.expenseCategory.collectAsStateWithLifecycle()

  // Laporan Inputs
  val webhookUrl by viewModel.webhookUrl.collectAsStateWithLifecycle()
  val syncStatus by viewModel.syncUiStatus.collectAsStateWithLifecycle()
  val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
  val autoBackupStatus by viewModel.autoBackupStatus.collectAsStateWithLifecycle()
  val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val searchQuery by viewModel.transactionSearchQuery.collectAsStateWithLifecycle()
  val dateFilter by viewModel.transactionDateFilter.collectAsStateWithLifecycle()
  val showScriptGuide by viewModel.showScriptGuideDialog.collectAsStateWithLifecycle()
  val showEditModalAwal by viewModel.showEditInitialCashDialog.collectAsStateWithLifecycle()

  // Security Collectors
  val isLaporanUnlocked by viewModel.isLaporanUnlocked.collectAsStateWithLifecycle()
  val isSecurityEnabled by viewModel.isSecurityEnabled.collectAsStateWithLifecycle()
  val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
  val hasCustomPin by viewModel.hasCustomPin.collectAsStateWithLifecycle()
  val showSecurityDialog by viewModel.showSecurityDialog.collectAsStateWithLifecycle()

  val isBiometricAvailable = remember(context) {
    BiometricAuthHelper.isBiometricAvailable(context)
  }

  // Listen for user messages / snackbars
  LaunchedEffect(Unit) {
    viewModel.userMessage.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  // Handle hardware / gesture Back button: if on secondary tab, return to Beranda
  BackHandler(enabled = currentTab != AppTab.BERANDA) {
    viewModel.selectTab(AppTab.BERANDA)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      AppTopBar(
        storeName = summary.storeName,
        selectedDate = selectedDate,
        isClosed = summary.isClosed,
        onPreviousDay = { viewModel.selectPreviousDay() },
        onNextDay = { viewModel.selectNextDay() },
        onResetToday = { viewModel.resetToToday() }
      )
    },
    bottomBar = {
      AppBottomBar(
        currentTab = currentTab,
        cartItemCount = cartItems.sumOf { it.qty },
        expenseCount = expenses.size,
        onTabSelected = { viewModel.selectTab(it) }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    Crossfade(
      targetState = currentTab,
      label = "ScreenTransition",
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) { tab ->
      when (tab) {
        AppTab.BERANDA -> {
          BerandaScreen(
            summary = summary,
            showEditModalAwal = showEditModalAwal,
            onOpenEditModalAwal = { viewModel.openEditInitialCashDialog() },
            onCloseEditModalAwal = { viewModel.closeEditInitialCashDialog() },
            onSaveModalAwal = { viewModel.saveInitialCash(it) },
            onNavigateTab = { viewModel.selectTab(it) }
          )
        }

        AppTab.KASIR -> {
          KasirScreen(
            cartItems = cartItems,
            inputItemName = inputItemName,
            inputQty = inputQty,
            inputUnitPrice = inputUnitPrice,
            cashPaidInput = cashPaidInput,
            onNameChange = { viewModel.setInputItemName(it) },
            onQtyChange = { viewModel.setInputQty(it) },
            onIncrementQty = { viewModel.incrementQty() },
            onDecrementQty = { viewModel.decrementQty() },
            onUnitPriceChange = { viewModel.setInputUnitPrice(it) },
            onAddPricePreset = { viewModel.addPricePreset(it) },
            onSetPricePreset = { viewModel.setUnitPricePreset(it) },
            onAddToCart = { viewModel.addItemToCart() },
            onRemoveCartItem = { viewModel.removeCartItem(it) },
            onUpdateCartItemQty = { id, qty -> viewModel.updateCartItemQty(id, qty) },
            onClearCart = { viewModel.clearCart() },
            onCashPaidChange = { viewModel.setCashPaidInput(it) },
            onSetCashExact = { viewModel.setCashExact() },
            onAddCashPreset = { viewModel.addCashPreset(it) },
            onSaveTransaction = { viewModel.saveTransaction() }
          )
        }

        AppTab.PENGELUARAN -> {
          PengeluaranScreen(
            expenses = expenses,
            descriptionInput = expenseDesc,
            amountInput = expenseAmount,
            categoryInput = expenseCategory,
            onDescriptionChange = { viewModel.setExpenseDescription(it) },
            onAmountChange = { viewModel.setExpenseAmount(it) },
            onCategoryChange = { viewModel.setExpenseCategory(it) },
            onSaveExpense = { viewModel.saveExpense() },
            onDeleteExpense = { viewModel.deleteExpense(it) }
          )
        }

        AppTab.LAPORAN -> {
          if (isSecurityEnabled && !isLaporanUnlocked) {
            LaporanLockScreen(
              isBiometricAvailable = isBiometricAvailable && isBiometricEnabled,
              hasCustomPin = hasCustomPin,
              onVerifyPin = { pin -> viewModel.unlockLaporanWithPin(pin) },
              onBiometricClick = {
                if (context is FragmentActivity) {
                  BiometricAuthHelper.showBiometricPrompt(
                    activity = context,
                    onSuccess = { viewModel.unlockLaporanBiometric() }
                  )
                }
              },
              onBackToBeranda = { viewModel.selectTab(AppTab.BERANDA) }
            )
          } else {
            LaporanScreen(
              summary = summary,
              transactions = filteredTransactions,
              webhookUrl = webhookUrl,
              syncStatus = syncStatus,
              backupStatus = backupStatus,
              autoBackupStatus = autoBackupStatus,
              searchQuery = searchQuery,
              dateFilter = dateFilter,
              showScriptGuide = showScriptGuide,
              onWebhookUrlChange = { viewModel.setWebhookUrl(it) },
              onSaveWebhookUrl = { viewModel.saveWebhookUrl() },
              onCloseStoreAndSync = { viewModel.closeStoreAndSendReport() },
              onExportCsv = { viewModel.exportAndShareCsv() },
              onShareText = { viewModel.shareTextReport() },
              onBackupDatabase = { viewModel.backupDatabase(shareFile = true) },
              onRestoreBackupUri = { viewModel.restoreDatabaseFromUri(it) },
              onToggleAutoBackup = { viewModel.toggleAutoBackup(it) },
              onTriggerImmediateAutoBackup = { viewModel.triggerImmediateAutoBackup() },
              onSearchQueryChange = { viewModel.setTransactionSearchQuery(it) },
              onDateFilterChange = { viewModel.setTransactionDateFilter(it) },
              onClearSearch = { viewModel.clearTransactionSearch() },
              onOpenScriptGuide = { viewModel.openScriptGuide() },
              onCloseScriptGuide = { viewModel.closeScriptGuide() },
              onDeleteTransaction = { viewModel.deleteTransaction(it) },
              onOpenSecuritySettings = { viewModel.openSecurityDialog() },
              onLockSession = { viewModel.lockLaporan() }
            )
          }
        }
      }
    }
  }

  // DIALOG STRUK SETELAH TRANSAKSI BERHASIL DISIMPAN
  lastReceipt?.let { receipt ->
    ReceiptDialog(
      transactionWithItems = receipt,
      storeName = summary.storeName,
      onDismiss = { viewModel.dismissReceiptDialog() }
    )
  }

  // DIALOG PENGATURAN KEAMANAN & PIN
  if (showSecurityDialog) {
    SecuritySettingsDialog(
      isSecurityEnabled = isSecurityEnabled,
      isBiometricEnabled = isBiometricEnabled,
      isBiometricAvailable = isBiometricAvailable,
      hasCustomPin = hasCustomPin,
      onDismiss = { viewModel.closeSecurityDialog() },
      onChangePin = { oldPin, newPin -> viewModel.changePin(oldPin, newPin) },
      onToggleSecurity = { viewModel.toggleSecurityEnabled(it) },
      onToggleBiometric = { viewModel.toggleBiometricEnabled(it) },
      onLockSessionNow = { viewModel.lockLaporan() }
    )
  }
}
