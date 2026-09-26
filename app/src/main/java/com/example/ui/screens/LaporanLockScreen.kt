package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LaporanLockScreen(
  isBiometricAvailable: Boolean,
  hasCustomPin: Boolean,
  onVerifyPin: (String) -> Boolean,
  onBiometricClick: () -> Unit,
  onBackToBeranda: () -> Unit,
  modifier: Modifier = Modifier
) {
  var enteredPin by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  fun handleNumberInput(digit: String) {
    if (enteredPin.length < 6) {
      val newPin = enteredPin + digit
      enteredPin = newPin
      errorMessage = null

      if (newPin.length == 4) {
        val isValid = onVerifyPin(newPin)
        if (isValid) {
          // Success
          enteredPin = ""
        } else {
          errorMessage = "PIN salah. Silakan coba lagi."
          enteredPin = ""
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      errorMessage = null
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 420.dp)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Navigation / Return back
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
      ) {
        TextButton(
          onClick = onBackToBeranda,
          modifier = Modifier.testTag("btn_back_to_beranda")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Kembali ke Beranda",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Kembali ke Beranda", fontSize = 12.sp)
        }
      }

      // Security Icon & Header
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Keamanan Data",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(38.dp)
          )
        }
      }

      Text(
        text = "Verifikasi Pemilik Toko",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Masukkan PIN keamanan untuk melihat laporan keuangan, ekspor data, dan tutup toko.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
      )

      if (!hasCustomPin) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PIN awal default: 1234 (dapat diubah setelah masuk)",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
      }

      // PIN Indicator Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
      ) {
        val totalDots = 4
        for (i in 0 until totalDots) {
          val isFilled = i < enteredPin.length
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(
                if (isFilled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .border(
                width = 1.5.dp,
                color = if (isFilled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape
              )
          )
        }
      }

      // Error message
      AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Text(
          text = errorMessage ?: "",
          color = MaterialTheme.colorScheme.error,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center
        )
      }

      // Numeric Keypad
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val keys = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("biometric", "0", "backspace")
        )

        for (row in keys) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (key in row) {
              when (key) {
                "biometric" -> {
                  if (isBiometricAvailable) {
                    Surface(
                      onClick = onBiometricClick,
                      shape = CircleShape,
                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                      modifier = Modifier
                        .size(64.dp)
                        .testTag("btn_biometric_key")
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.Fingerprint,
                          contentDescription = "Autentikasi Sidik Jari",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(30.dp)
                        )
                      }
                    }
                  } else {
                    Spacer(modifier = Modifier.size(64.dp))
                  }
                }
                "backspace" -> {
                  Surface(
                    onClick = { handleBackspace() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                      .size(64.dp)
                      .testTag("btn_backspace_key")
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Hapus Angka",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                      )
                    }
                  }
                }
                else -> {
                  Surface(
                    onClick = { handleNumberInput(key) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                      .size(64.dp)
                      .testTag("btn_pin_key_$key")
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = key,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Biometric shortcut button if available
      if (isBiometricAvailable) {
        OutlinedButton(
          onClick = onBiometricClick,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_biometric_bottom"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Buka dengan Sidik Jari / Biometrik", fontSize = 13.sp)
        }
      }
    }
  }
}
