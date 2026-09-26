package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionWithItems
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.FileSharer

@Composable
fun ReceiptDialog(
  transactionWithItems: TransactionWithItems,
  storeName: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val t = transactionWithItems.transaction
  val items = transactionWithItems.items

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Success icon
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFDCFCE7)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Sukses",
            tint = Color(0xFF16A34A),
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Transaksi Berhasil!",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Receipt Paper look
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Text(
              text = storeName,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "Struk Pembelian Kasir",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "No: ${t.receiptNumber}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
              Text(
                text = "${DateUtils.formatShort(t.date)} ${DateUtils.formatTime(t.timestamp)}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Items List
            items.forEach { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = item.itemName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text(
                    text = "${item.qty} x ${CurrencyFormatter.formatRupiah(item.unitPrice)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = CurrencyFormatter.formatRupiah(item.subtotal),
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Totals
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "TOTAL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                text = CurrencyFormatter.formatRupiah(t.totalAmount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Tunai Diterima", fontSize = 12.sp)
              Text(
                text = CurrencyFormatter.formatRupiah(t.cashPaid),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "KEMBALIAN", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text(
                text = CurrencyFormatter.formatRupiah(t.changeAmount),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (t.changeAmount > 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              val receiptText = buildString {
                appendLine("=== $storeName ===")
                appendLine("No: ${t.receiptNumber}")
                appendLine("Waktu: ${DateUtils.formatDisplay(t.date)} ${DateUtils.formatTime(t.timestamp)}")
                appendLine("------------------------------")
                items.forEach {
                  appendLine("${it.itemName}")
                  appendLine("  ${it.qty} x ${CurrencyFormatter.formatRupiah(it.unitPrice)} = ${CurrencyFormatter.formatRupiah(it.subtotal)}")
                }
                appendLine("------------------------------")
                appendLine("TOTAL: ${CurrencyFormatter.formatRupiah(t.totalAmount)}")
                appendLine("BAYAR: ${CurrencyFormatter.formatRupiah(t.cashPaid)}")
                appendLine("KEMBALI: ${CurrencyFormatter.formatRupiah(t.changeAmount)}")
                appendLine("Terima kasih telah berbelanja!")
              }
              FileSharer.shareText(context, receiptText)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_receipt_button")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Bagikan")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("close_receipt_button")
          ) {
            Text(text = "Selesai")
          }
        }
      }
    }
  }
}
