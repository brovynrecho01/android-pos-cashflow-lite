package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab

@Composable
fun AppBottomBar(
  currentTab: AppTab,
  cartItemCount: Int,
  expenseCount: Int,
  onTabSelected: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    // 1. Beranda
    NavigationBarItem(
      selected = currentTab == AppTab.BERANDA,
      onClick = { onTabSelected(AppTab.BERANDA) },
      icon = {
        Icon(
          imageVector = if (currentTab == AppTab.BERANDA) Icons.Filled.Home else Icons.Outlined.Home,
          contentDescription = "Tab Beranda"
        )
      },
      label = {
        Text(
          text = "Beranda",
          fontWeight = if (currentTab == AppTab.BERANDA) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp
        )
      },
      colors = navigationItemColors(),
      modifier = Modifier.testTag("tab_beranda")
    )

    // 2. Kasir (Pemasukan)
    NavigationBarItem(
      selected = currentTab == AppTab.KASIR,
      onClick = { onTabSelected(AppTab.KASIR) },
      icon = {
        BadgedBox(badge = {
          if (cartItemCount > 0) {
            Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
              Text(text = cartItemCount.toString(), color = MaterialTheme.colorScheme.onTertiary)
            }
          }
        }) {
          Icon(
            imageVector = if (currentTab == AppTab.KASIR) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale,
            contentDescription = "Tab Kasir (Pemasukan)"
          )
        }
      },
      label = {
        Text(
          text = "Kasir",
          fontWeight = if (currentTab == AppTab.KASIR) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp
        )
      },
      colors = navigationItemColors(),
      modifier = Modifier.testTag("tab_kasir")
    )

    // 3. Pengeluaran
    NavigationBarItem(
      selected = currentTab == AppTab.PENGELUARAN,
      onClick = { onTabSelected(AppTab.PENGELUARAN) },
      icon = {
        BadgedBox(badge = {
          if (expenseCount > 0) {
            Badge(containerColor = MaterialTheme.colorScheme.outline) {
              Text(text = expenseCount.toString())
            }
          }
        }) {
          Icon(
            imageVector = if (currentTab == AppTab.PENGELUARAN) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
            contentDescription = "Tab Pengeluaran"
          )
        }
      },
      label = {
        Text(
          text = "Pengeluaran",
          fontWeight = if (currentTab == AppTab.PENGELUARAN) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp
        )
      },
      colors = navigationItemColors(),
      modifier = Modifier.testTag("tab_pengeluaran")
    )

    // 4. Laporan & Tutup Toko
    NavigationBarItem(
      selected = currentTab == AppTab.LAPORAN,
      onClick = { onTabSelected(AppTab.LAPORAN) },
      icon = {
        Icon(
          imageVector = if (currentTab == AppTab.LAPORAN) Icons.Filled.Assessment else Icons.Outlined.Assessment,
          contentDescription = "Tab Laporan & Tutup Toko"
        )
      },
      label = {
        Text(
          text = "Laporan",
          fontWeight = if (currentTab == AppTab.LAPORAN) FontWeight.Bold else FontWeight.Normal,
          fontSize = 12.sp
        )
      },
      colors = navigationItemColors(),
      modifier = Modifier.testTag("tab_laporan")
    )
  }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
  selectedIconColor = MaterialTheme.colorScheme.primary,
  selectedTextColor = MaterialTheme.colorScheme.primary,
  indicatorColor = MaterialTheme.colorScheme.primaryContainer,
  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)

