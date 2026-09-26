package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.TransactionWithItems
import com.example.data.remote.GoogleAppsScriptTemplate
import com.example.ui.CashflowSummaryUi
import com.example.ui.SyncUiStatus
import com.example.ui.TransactionDateFilter
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.work.AutoBackupStatus

@Composable
fun LaporanScreen(
  summary: CashflowSummaryUi,
  transactions: List<TransactionWithItems>,
  webhookUrl: String,
  syncStatus: SyncUiStatus,
  backupStatus: String?,
  autoBackupStatus: AutoBackupStatus,
  searchQuery: String,
  dateFilter: TransactionDateFilter,
  showScriptGuide: Boolean,
  onWebhookUrlChange: (String) -> Unit,
  onSaveWebhookUrl: () -> Unit,
  onCloseStoreAndSync: () -> Unit,
  onExportCsv: () -> Unit,
  onShareText: () -> Unit,
  onBackupDatabase: () -> Unit,
  onRestoreBackupUri: (Uri) -> Unit,
  onToggleAutoBackup: (Boolean) -> Unit,
  onTriggerImmediateAutoBackup: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onDateFilterChange: (TransactionDateFilter) -> Unit,
  onClearSearch: () -> Unit,
  onOpenScriptGuide: () -> Unit,
  onCloseScriptGuide: () -> Unit,
  onDeleteTransaction: (Long) -> Unit,
  onOpenSecuritySettings: () -> Unit,
  onLockSession: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val expandedReceipts = remember { mutableStateMapOf<Long, Boolean>() }
  var transactionToDelete by remember { mutableStateOf<TransactionWithItems?>(null) }

  val restoreFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { onRestoreBackupUri(it) }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. STATUS TOKO & SINKRONISASI BANNER
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("store_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = if (summary.isClosed) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
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
                .background(if (summary.isClosed) Color(0xFFDC2626) else Color(0xFF16A34A)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (summary.isClosed) Icons.Default.Store else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (summary.isClosed) "Toko Sudah Ditutup" else "Toko Masih Buka",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (summary.isClosed) Color(0xFF991B1B) else Color(0xFF166534)
              )
              Text(
                text = if (summary.isClosed && summary.closedAt != null)
                  "Ditutup pada ${DateUtils.formatTime(summary.closedAt)}"
                else "Siap menerima transaksi penjualan",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onOpenSecuritySettings,
              modifier = Modifier.testTag("btn_security_settings")
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Pengaturan Keamanan",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            IconButton(
              onClick = onLockSession,
              modifier = Modifier.testTag("btn_lock_session")
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Kunci Sesi Laporan",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }
    }

    // 2. KARTU REKAP LENGKAP ARUS KAS
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Rekap Arus Kas Harian",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Text(
            text = DateUtils.formatDisplay(summary.date),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          // Rows
          RekapRow(label = "1. Modal Awal", value = CurrencyFormatter.formatRupiah(summary.initialCash))
          RekapRow(
            label = "2. Total Pemasukan (${summary.transactionCount} struk)",
            value = "+ ${CurrencyFormatter.formatRupiah(summary.totalIncome)}",
            valueColor = Color(0xFF16A34A)
          )
          RekapRow(
            label = "3. Total Pengeluaran",
            value = "- ${CurrencyFormatter.formatRupiah(summary.totalExpense)}",
            valueColor = Color(0xFFDC2626)
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL AKHIR KAS",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "(Modal Awal + Pemasukan - Pengeluaran)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = CurrencyFormatter.formatRupiah(summary.finalCash),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 18.sp,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 3. BAGIAN SINKRONISASI GOOGLE APPS SCRIPT WEBHOOK
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
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Webhook Google Apps Script",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            TextButton(onClick = onOpenScriptGuide) {
              Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Salin Script", fontSize = 11.sp)
            }
          }

          Text(
            text = "Kirim laporan harian langsung masuk ke baris Google Sheets secara otomatis via HTTP POST JSON.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = webhookUrl,
            onValueChange = onWebhookUrlChange,
            label = { Text("Webhook URL Google Apps Script") },
            placeholder = { Text("https://script.google.com/macros/s/.../exec") },
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = onSaveWebhookUrl) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan URL")
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_webhook_url")
          )

          // Status Sinkronisasi
          when (syncStatus) {
            is SyncUiStatus.Loading -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Mengirim data ke Webhook Google Apps Script...", fontSize = 12.sp)
              }
            }
            is SyncUiStatus.Success -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFDCFCE7))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = syncStatus.message, fontSize = 12.sp, color = Color(0xFF166534))
              }
            }
            is SyncUiStatus.Error -> {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFFEE2E2))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = syncStatus.errorMessage, fontSize = 12.sp, color = Color(0xFF991B1B))
              }
            }
            is SyncUiStatus.Idle -> Unit
          }

          // Tombol Tutup Toko & Kirim Laporan
          Button(
            onClick = onCloseStoreAndSync,
            enabled = syncStatus !is SyncUiStatus.Loading,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_close_store_sync"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (summary.isClosed) "Kirim Ulang Laporan ke Webhook" else "Tutup Toko & Kirim Laporan",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 4. BAGIAN EXPORT & BAGIKAN (CSV / TEXT SHARE INTENT)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Bagikan Laporan (Share Intent)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "Simpan ke Google Drive, kirim via Gmail, atau bagikan ringkasan lewat WhatsApp.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = onExportCsv,
              modifier = Modifier
                .weight(1f)
                .testTag("btn_export_csv"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Export CSV", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onShareText,
              modifier = Modifier
                .weight(1f)
                .testTag("btn_share_text"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Teks Ringkasan", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // 5. CADANGAN DATABASE / DISASTER RECOVERY (MANUAL JSON BACKUP TO EXTERNAL STORAGE)
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("card_backup_database"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Cadangan Database (Disaster Recovery)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "Simpan seluruh SQLite ke JSON di penyimpanan eksternal HP",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = "Mengekspor semua ringkasan harian, riwayat transaksi kasir, detail item, dan catatan pengeluaran ke file JSON mandiri. File dapat disimpan di folder Dokumen HP atau dicadangkan ke Google Drive/Flashdisk untuk pemulihan darurat.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
          )

          if (!backupStatus.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Storage,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = backupStatus,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // SEKSI AUTO-BACKUP WORKMANAGER 24 JAM
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Auto-Backup 24 Jam (WorkManager)",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                    Text(
                      text = if (autoBackupStatus.isEnabled) "Aktif otomatis di latar belakang" else "Nonaktif",
                      fontSize = 11.sp,
                      color = if (autoBackupStatus.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                  }
                }

                Switch(
                  checked = autoBackupStatus.isEnabled,
                  onCheckedChange = onToggleAutoBackup,
                  modifier = Modifier.testTag("switch_auto_backup")
                )
              }

              Text(
                text = "WorkManager otomatis mengekspor seluruh SQLite database ke file JSON di memori HP secara berkala setiap 24 jam tanpa perlu membuka aplikasi.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
              )

              // Status info
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = "Terakhir Dijalankan:",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = autoBackupStatus.lastBackupFormatted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                if (autoBackupStatus.lastBackupFileName != null) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "File Cadangan:",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = "${autoBackupStatus.lastBackupFileName} (${autoBackupStatus.lastBackupRecordCount} data)",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }

              OutlinedButton(
                onClick = onTriggerImmediateAutoBackup,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_trigger_auto_backup"),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Sync,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Uji Coba Auto-Backup Sekarang", fontSize = 11.sp)
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onBackupDatabase,
              modifier = Modifier
                .weight(1f)
                .testTag("btn_backup_database"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Backup Database", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = { restoreFileLauncher.launch("application/json") },
              modifier = Modifier
                .weight(1f)
                .testTag("btn_restore_database"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Pulihkan Data", fontSize = 12.sp)
            }
          }

          OutlinedButton(
            onClick = onOpenSecuritySettings,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_open_security_settings"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pengaturan Keamanan & PIN Laporan", fontSize = 12.sp)
          }
        }
      }
    }

    // 6. RINCIAN BARANG TERJUAL PER STRUK & PENCARIAN TRANSAKSI
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Riwayat Transaksi Penjualan",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
          ) {
            Text(
              text = "${transactions.size} Struk",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        // Search Bar Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_search_transactions"),
          placeholder = {
            Text(
              text = "Cari nomor struk atau barang (cth: Minyak, TRX-001)...",
              fontSize = 12.sp
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Cari",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = onClearSearch) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Hapus kata kunci",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        // Date Range Filter Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )

          TransactionDateFilter.values().forEach { filter ->
            FilterChip(
              selected = dateFilter == filter,
              onClick = { onDateFilterChange(filter) },
              label = { Text(filter.label, fontSize = 11.sp) },
              modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          }
        }
      }
    }

    if (transactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = if (searchQuery.isNotBlank() || dateFilter != TransactionDateFilter.HARI_INI) {
                "Tidak ada transaksi yang cocok dengan filter"
              } else {
                "Belum Ada Transaksi Penjualan Hari Ini"
              },
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (searchQuery.isNotBlank() || dateFilter != TransactionDateFilter.HARI_INI) {
              Text(
                text = "Kata kunci: '${searchQuery.ifBlank { "(semua)" }}' | Rentang: ${dateFilter.label}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
              )
              TextButton(
                onClick = {
                  onClearSearch()
                  onDateFilterChange(TransactionDateFilter.HARI_INI)
                }
              ) {
                Text("Reset Pencarian & Filter", fontSize = 12.sp)
              }
            }
          }
        }
      }
    } else {
      items(transactions, key = { it.transaction.id }) { item ->
        val isExpanded = expandedReceipts[item.transaction.id] ?: false
        ReceiptDetailCard(
          transactionWithItems = item,
          isExpanded = isExpanded,
          onToggleExpand = {
            expandedReceipts[item.transaction.id] = !isExpanded
          },
          onDeleteClick = { transactionToDelete = item }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // DIALOG PANDUAN GOOGLE APPS SCRIPT
  if (showScriptGuide) {
    AppsScriptGuideDialog(
      onDismiss = onCloseScriptGuide,
      onCopyScript = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Google Apps Script", GoogleAppsScriptTemplate.scriptCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Kode Apps Script berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // DIALOG KONFIRMASI HAPUS TRANSAKSI
  transactionToDelete?.let { trxWithItems ->
    AlertDialog(
      onDismissRequest = { transactionToDelete = null },
      title = { Text(text = "Hapus Struk Transaksi?", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Yakin ingin menghapus struk ${trxWithItems.transaction.receiptNumber} senilai ${CurrencyFormatter.formatRupiah(trxWithItems.transaction.totalAmount)}? Data akan dihapus dari laporan dan saldo kas."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteTransaction(trxWithItems.transaction.id)
            transactionToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Ya, Hapus")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { transactionToDelete = null }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
private fun RekapRow(
  label: String,
  value: String,
  valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      text = value,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = valueColor,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun ReceiptDetailCard(
  transactionWithItems: TransactionWithItems,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onDeleteClick: () -> Unit
) {
  val t = transactionWithItems.transaction
  val items = transactionWithItems.items

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header Struk
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onToggleExpand)
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = t.receiptNumber,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = DateUtils.formatTime(t.timestamp),
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.outline
            )
          }
          Text(
            text = "${items.size} barang (${items.sumOf { it.qty }} pcs)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = CurrencyFormatter.formatRupiah(t.totalAmount),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Rincian Barang jika expanded
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Daftar Barang Terjual:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "• ${item.itemName} (${item.qty} x ${CurrencyFormatter.formatRupiah(item.unitPrice)})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = CurrencyFormatter.formatRupiah(item.subtotal),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Bayar: ${CurrencyFormatter.formatRupiah(t.cashPaid)} | Kembali: ${CurrencyFormatter.formatRupiah(t.changeAmount)}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
              onClick = onDeleteClick,
              colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Hapus Struk", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AppsScriptGuideDialog(
  onDismiss: () -> Unit,
  onCopyScript: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Cara Buat Webhook Google Sheets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Langkah mudah sinkronisasi:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Text(
          text = "1. Buka Google Sheets di laptop/browser Anda.\n2. Klik menu Ekstensi (Extensions) > Apps Script.\n3. Hapus kode default, lalu tempelkan (Paste) kode di bawah ini.\n4. Klik 'Terapkan' (Deploy) > 'Penerapan Baru' (New Deployment).\n5. Pilih jenis 'Aplikasi Web' (Web App).\n6. Akses: 'Siapa Saja' (Anyone).\n7. Salin URL Aplikasi Web yang diberikan ke kolom Webhook di aplikasi ini.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = GoogleAppsScriptTemplate.scriptCode,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(10.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onCopyScript, modifier = Modifier.testTag("btn_copy_script")) {
        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Salin Kode Script")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}
