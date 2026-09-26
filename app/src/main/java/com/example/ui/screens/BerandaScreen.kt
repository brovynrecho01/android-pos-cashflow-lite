package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.CashflowSummaryUi
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun BerandaScreen(
  summary: CashflowSummaryUi,
  showEditModalAwal: Boolean,
  onOpenEditModalAwal: () -> Unit,
  onCloseEditModalAwal: () -> Unit,
  onSaveModalAwal: (Long) -> Unit,
  onNavigateTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. BANNER MODAL AWAL
    ModalAwalCard(
      initialCash = summary.initialCash,
      onEditClick = onOpenEditModalAwal
    )

    // 2. KARTU UTAMA TOTAL AKHIR KAS (RUMUS UTAMA)
    TotalAkhirKasCard(
      initialCash = summary.initialCash,
      totalIncome = summary.totalIncome,
      totalExpense = summary.totalExpense,
      finalCash = summary.finalCash
    )

    // 3. REALTIME SUMMARY CARDS: Pemasukan & Pengeluaran
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      SummaryMetricCard(
        title = "Pemasukan",
        subtitle = "${summary.transactionCount} Transaksi",
        amount = summary.totalIncome,
        icon = Icons.Default.TrendingUp,
        accentColor = Color(0xFF16A34A),
        backgroundColor = Color(0xFFDCFCE7),
        modifier = Modifier
          .weight(1f)
          .clickable { onNavigateTab(AppTab.KASIR) }
          .testTag("metric_card_pemasukan")
      )

      SummaryMetricCard(
        title = "Pengeluaran",
        subtitle = "Arus Keluar",
        amount = summary.totalExpense,
        icon = Icons.Default.TrendingDown,
        accentColor = Color(0xFFDC2626),
        backgroundColor = Color(0xFFFEE2E2),
        modifier = Modifier
          .weight(1f)
          .clickable { onNavigateTab(AppTab.PENGELUARAN) }
          .testTag("metric_card_pengeluaran")
      )
    }

    // 4. CASHFLOW BREAKDOWN BAR
    CashflowProgressBarCard(
      income = summary.totalIncome,
      expense = summary.totalExpense
    )

    // 5. TOMBOL PINTAS UTAMA
    Text(
      text = "Aksi Cepat",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      modifier = Modifier.padding(top = 4.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      QuickActionButton(
        title = "+ Transaksi Kasir",
        subtitle = "Input keranjang belanja",
        icon = Icons.Default.PointOfSale,
        color = MaterialTheme.colorScheme.primary,
        onClick = { onNavigateTab(AppTab.KASIR) },
        modifier = Modifier
          .weight(1f)
          .testTag("btn_quick_kasir")
      )

      QuickActionButton(
        title = "+ Catat Biaya",
        subtitle = "Listrik, kresek, dll",
        icon = Icons.Default.ReceiptLong,
        color = Color(0xFFD97706),
        onClick = { onNavigateTab(AppTab.PENGELUARAN) },
        modifier = Modifier
          .weight(1f)
          .testTag("btn_quick_pengeluaran")
      )
    }

    // 6. TUTUP TOKO CARD
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateTab(AppTab.LAPORAN) }
        .testTag("card_tutup_toko_shortcut"),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = if (summary.isClosed) "Laporan & Status Sinkron" else "Tutup Toko & Kirim Laporan",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = if (summary.isClosed) "Toko sudah ditutup hari ini" else "Sinkronkan ke Google Apps Script Webhook",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
    }
  }

  // DIALOG EDIT MODAL AWAL
  if (showEditModalAwal) {
    EditModalAwalDialog(
      currentAmount = summary.initialCash,
      onDismiss = onCloseEditModalAwal,
      onSave = onSaveModalAwal
    )
  }
}

@Composable
private fun ModalAwalCard(
  initialCash: Long,
  onEditClick: () -> Unit
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFFEF3C7)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color(0xFFB45309),
            modifier = Modifier.size(26.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Modal Awal Hari Ini",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyFormatter.formatRupiah(initialCash),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      OutlinedButton(
        onClick = onEditClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("edit_modal_awal_button")
      ) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Ubah", fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun TotalAkhirKasCard(
  initialCash: Long,
  totalIncome: Long,
  totalExpense: Long,
  finalCash: Long
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("card_total_akhir_kas"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TOTAL AKHIR KAS (REAL-TIME)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
        Text(
          text = "Rumus Kas Harian",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = CurrencyFormatter.formatRupiah(finalCash),
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
      Spacer(modifier = Modifier.height(8.dp))

      // Formula visualization
      Text(
        text = "${CurrencyFormatter.formatRupiah(initialCash)} (Modal) + ${CurrencyFormatter.formatRupiah(totalIncome)} (Masuk) - ${CurrencyFormatter.formatRupiah(totalExpense)} (Keluar)",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun SummaryMetricCard(
  title: String,
  subtitle: String,
  amount: Long,
  icon: ImageVector,
  accentColor: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier
) {
  ElevatedCard(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = CurrencyFormatter.formatRupiah(amount),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun CashflowProgressBarCard(
  income: Long,
  expense: Long
) {
  val total = (income + expense).coerceAtLeast(1L)
  val incomeRatio = (income.toFloat() / total.toFloat()).coerceIn(0f, 1f)

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Rasio Arus Kas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
          text = if (income + expense > 0) "${(incomeRatio * 100).toInt()}% Masuk" else "Belum ada transaksi",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Multi-segment progress bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp))
          .background(Color(0xFFE2E8F0))
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          if (income > 0) {
            Box(
              modifier = Modifier
                .weight(incomeRatio.coerceAtLeast(0.01f))
                .fillMaxSize()
                .background(Color(0xFF16A34A))
            )
          }
          if (expense > 0) {
            Box(
              modifier = Modifier
                .weight((1f - incomeRatio).coerceAtLeast(0.01f))
                .fillMaxSize()
                .background(Color(0xFFDC2626))
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Masuk: ${CurrencyFormatter.formatRupiah(income)}", fontSize = 11.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFDC2626)))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Keluar: ${CurrencyFormatter.formatRupiah(expense)}", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
private fun QuickActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun EditModalAwalDialog(
  currentAmount: Long,
  onDismiss: () -> Unit,
  onSave: (Long) -> Unit
) {
  var inputAmount by remember {
    mutableStateOf(if (currentAmount > 0) currentAmount.toString() else "")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = "Atur Modal Awal Kasir", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Modal awal adalah uang tunai kembalian di laci kasir saat toko mulai buka hari ini.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = if (inputAmount.isNotBlank()) CurrencyFormatter.formatNumber(CurrencyFormatter.parseAmount(inputAmount)) else "",
          onValueChange = { inputAmount = it.replace("[^0-9]".toRegex(), "") },
          label = { Text("Modal Awal (Rp)") },
          prefix = { Text("Rp ") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_modal_awal")
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Pilihan Cepat:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))

        // Preset chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(100_000L, 200_000L, 500_000L, 1_000_000L).forEach { preset ->
            FilterChip(
              selected = CurrencyFormatter.parseAmount(inputAmount) == preset,
              onClick = { inputAmount = preset.toString() },
              label = {
                Text(
                  text = when (preset) {
                    100_000L -> "100rb"
                    200_000L -> "200rb"
                    500_000L -> "500rb"
                    else -> "1jt"
                  },
                  fontSize = 11.sp
                )
              }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amount = CurrencyFormatter.parseAmount(inputAmount)
          onSave(amount)
        },
        modifier = Modifier.testTag("save_modal_awal_button")
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}
