package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
  storeName: String,
  selectedDate: String,
  isClosed: Boolean,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onResetToday: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isToday = selectedDate == DateUtils.today()

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.primary,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      // Row 1: Brand & Offline-First Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Storefront,
              contentDescription = "Toko Kelontong",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = storeName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Kasir & Arus Kas Harian",
              style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
              )
            )
          }
        }

        // Status badges
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isClosed) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE11D48))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Toko Ditutup",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          } else {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4ADE80))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Buka • Offline",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.padding(top = 8.dp))

      // Row 2: Date Selector Navigation
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onPreviousDay,
            modifier = Modifier
              .size(36.dp)
              .testTag("prev_day_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Hari Sebelumnya",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          Row(
            modifier = Modifier
              .clickable(onClick = onResetToday)
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isToday) "Hari ini (${DateUtils.formatShort(selectedDate)})"
                     else DateUtils.formatDisplay(selectedDate),
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            )
          }

          IconButton(
            onClick = onNextDay,
            modifier = Modifier
              .size(36.dp)
              .testTag("next_day_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Hari Berikutnya",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
