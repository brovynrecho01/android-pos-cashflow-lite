package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun PengeluaranScreen(
  expenses: List<ExpenseEntity>,
  descriptionInput: String,
  amountInput: String,
  categoryInput: String,
  onDescriptionChange: (String) -> Unit,
  onAmountChange: (String) -> Unit,
  onCategoryChange: (String) -> Unit,
  onSaveExpense: () -> Unit,
  onDeleteExpense: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var itemToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

  val totalExpense = remember(expenses) {
    expenses.sumOf { it.amount }
  }

  val amountLong = remember(amountInput) {
    CurrencyFormatter.parseAmount(amountInput)
  }

  val categories = listOf("Operasional", "Belanja Dagangan", "Listrik & Air", "Gaji & Uang Makan", "Lain-lain")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. BANNER TOTAL PENGELUARAN HARI INI
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("banner_total_pengeluaran"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
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
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFDC2626)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.TrendingDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Total Pengeluaran Hari Ini",
                fontSize = 12.sp,
                color = Color(0xFF991B1B)
              )
              Text(
                text = CurrencyFormatter.formatRupiah(totalExpense),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B),
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Text(
            text = "${expenses.size} Catatan",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF991B1B)
          )
        }
      }
    }

    // 2. FORM INPUT PENGELUARAN
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Catat Pengeluaran Baru",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          // Keterangan Pengeluaran
          OutlinedTextField(
            value = descriptionInput,
            onValueChange = onDescriptionChange,
            label = { Text("Keterangan Pengeluaran") },
            placeholder = { Text("Contoh: Beli kresek, Token listrik, Bayar sales rokok") },
            trailingIcon = {
              if (descriptionInput.isNotEmpty()) {
                IconButton(onClick = { onDescriptionChange("") }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                }
              }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_expense_desc")
          )

          // Nominal Pengeluaran
          OutlinedTextField(
            value = if (amountInput.isNotEmpty()) CurrencyFormatter.formatNumber(amountLong) else "",
            onValueChange = onAmountChange,
            label = { Text("Nominal Pengeluaran (Rp)") },
            placeholder = { Text("0") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_expense_amount")
          )

          // Preset Chips Nominal
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(10_000L, 20_000L, 50_000L, 100_000L).forEach { preset ->
              FilterChip(
                selected = amountLong == preset,
                onClick = { onAmountChange(preset.toString()) },
                label = {
                  Text(
                    text = when (preset) {
                      10_000L -> "+10rb"
                      20_000L -> "+20rb"
                      50_000L -> "+50rb"
                      else -> "+100rb"
                    },
                    fontSize = 11.sp
                  )
                }
              )
            }
          }

          // Category Selector Chips
          Column {
            Text(
              text = "Kategori Pengeluaran",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(categories) { cat ->
                FilterChip(
                  selected = categoryInput == cat,
                  onClick = { onCategoryChange(cat) },
                  label = { Text(text = cat, fontSize = 11.sp) }
                )
              }
            }
          }

          // Tombol Simpan
          Button(
            onClick = onSaveExpense,
            enabled = descriptionInput.isNotBlank() && amountLong > 0,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_save_expense"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFDC2626)
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (amountLong > 0) "Simpan Pengeluaran (${CurrencyFormatter.formatRupiah(amountLong)})"
                     else "Simpan Pengeluaran",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 3. DAFTAR RIWAYAT PENGELUARAN HARI INI
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Riwayat Pengeluaran (${expenses.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    if (expenses.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Belum Ada Pengeluaran",
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Biaya operasional seperti beli kresek, listrik, atau bayar sales akan muncul di sini.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.outline,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(expenses, key = { it.id }) { exp ->
        ExpenseItemRow(
          expense = exp,
          onDeleteClick = { itemToDelete = exp }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // DIALOG KONFIRMASI HAPUS PENGELUARAN
  itemToDelete?.let { exp ->
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text(text = "Hapus Pengeluaran?", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Yakin ingin menghapus '${exp.description}' senilai ${CurrencyFormatter.formatRupiah(exp.amount)}? Data ini akan dikeluarkan dari perhitungan arus kas harian."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteExpense(exp.id)
            itemToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_expense")
        ) {
          Text("Ya, Hapus")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { itemToDelete = null }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
private fun ExpenseItemRow(
  expense: ExpenseEntity,
  onDeleteClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("expense_row_${expense.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text(
              text = expense.category,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Text(
            text = DateUtils.formatTime(expense.timestamp),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = expense.description,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "- ${CurrencyFormatter.formatRupiah(expense.amount)}",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color(0xFFDC2626),
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(
          onClick = onDeleteClick,
          modifier = Modifier.size(32.dp).testTag("delete_expense_${expense.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Hapus",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
