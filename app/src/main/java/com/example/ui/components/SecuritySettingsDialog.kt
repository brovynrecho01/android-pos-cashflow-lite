package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecuritySettingsDialog(
  isSecurityEnabled: Boolean,
  isBiometricEnabled: Boolean,
  isBiometricAvailable: Boolean,
  hasCustomPin: Boolean,
  onDismiss: () -> Unit,
  onChangePin: (oldPin: String, newPin: String) -> Boolean,
  onToggleSecurity: (Boolean) -> Unit,
  onToggleBiometric: (Boolean) -> Unit,
  onLockSessionNow: () -> Unit
) {
  var showChangePinSection by remember { mutableStateOf(false) }
  var oldPin by remember { mutableStateOf("") }
  var newPin by remember { mutableStateOf("") }
  var confirmNewPin by remember { mutableStateOf("") }
  var changePinError by remember { mutableStateOf<String?>(null) }
  var changePinSuccess by remember { mutableStateOf(false) }

  var showOldPinPassword by remember { mutableStateOf(false) }
  var showNewPinPassword by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Keamanan & Autentikasi",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Amankan ringkasan finansial, sinkronisasi Google Sheets, dan pencadangan database.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Toggle PIN Security
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Kunci Layar Laporan (PIN)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
              Text(
                text = if (isSecurityEnabled) "Wajib verifikasi untuk membuka tab ini" else "Tanpa proteksi keamanan",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isSecurityEnabled,
              onCheckedChange = onToggleSecurity,
              modifier = Modifier.testTag("switch_security_enabled")
            )
          }
        }

        // Toggle Biometric if available
        if (isBiometricAvailable) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Buka dengan Sidik Jari / Biometrik",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
                Text(
                  text = "Gunakan sensor biometrik perangkat untuk membuka kunci lebih cepat",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Switch(
                checked = isBiometricEnabled,
                onCheckedChange = onToggleBiometric,
                modifier = Modifier.testTag("switch_biometric_enabled")
              )
            }
          }
        }

        HorizontalDivider()

        if (!showChangePinSection) {
          Button(
            onClick = {
              showChangePinSection = true
              changePinSuccess = false
              changePinError = null
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_show_change_pin"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hasCustomPin) "Ubah PIN Keamanan" else "Buat PIN Baru (Ganti Default 1234)",
              fontSize = 12.sp
            )
          }
        } else {
          // Form Ubah PIN
          Text(
            text = "Form Pengubahan PIN",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
          )

          OutlinedTextField(
            value = oldPin,
            onValueChange = { if (it.length <= 6) oldPin = it },
            label = { Text("PIN Saat Ini (default: 1234)", fontSize = 11.sp) },
            visualTransformation = if (showOldPinPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = { showOldPinPassword = !showOldPinPassword }) {
                Icon(
                  imageVector = if (showOldPinPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_old_pin")
          )

          OutlinedTextField(
            value = newPin,
            onValueChange = { if (it.length <= 6) newPin = it },
            label = { Text("PIN Baru (4-6 Angka)", fontSize = 11.sp) },
            visualTransformation = if (showNewPinPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = { showNewPinPassword = !showNewPinPassword }) {
                Icon(
                  imageVector = if (showNewPinPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_new_pin")
          )

          OutlinedTextField(
            value = confirmNewPin,
            onValueChange = { if (it.length <= 6) confirmNewPin = it },
            label = { Text("Konfirmasi PIN Baru", fontSize = 11.sp) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_confirm_pin")
          )

          if (changePinError != null) {
            Text(
              text = changePinError ?: "",
              color = MaterialTheme.colorScheme.error,
              fontSize = 11.sp
            )
          }

          if (changePinSuccess) {
            Text(
              text = "PIN berhasil diubah!",
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { showChangePinSection = false },
              modifier = Modifier.weight(1f)
            ) {
              Text("Batal", fontSize = 11.sp)
            }

            Button(
              onClick = {
                if (newPin.length < 4) {
                  changePinError = "PIN minimal 4 angka"
                  return@Button
                }
                if (newPin != confirmNewPin) {
                  changePinError = "Konfirmasi PIN tidak cocok"
                  return@Button
                }
                val success = onChangePin(oldPin, newPin)
                if (success) {
                  changePinSuccess = true
                  changePinError = null
                  oldPin = ""
                  newPin = ""
                  confirmNewPin = ""
                } else {
                  changePinError = "PIN saat ini salah"
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("btn_submit_change_pin")
            ) {
              Text("Simpan PIN", fontSize = 11.sp)
            }
          }
        }

        OutlinedButton(
          onClick = {
            onLockSessionNow()
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_lock_session_now")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Kunci Sesi Laporan Sekarang", fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}
