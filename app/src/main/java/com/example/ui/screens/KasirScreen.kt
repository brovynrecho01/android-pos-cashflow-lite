package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.util.CurrencyFormatter

@Composable
fun KasirScreen(
  cartItems: List<CartItem>,
  inputItemName: String,
  inputQty: Int,
  inputUnitPrice: String,
  cashPaidInput: String,
  onNameChange: (String) -> Unit,
  onQtyChange: (Int) -> Unit,
  onIncrementQty: () -> Unit,
  onDecrementQty: () -> Unit,
  onUnitPriceChange: (String) -> Unit,
  onAddPricePreset: (Long) -> Unit,
  onSetPricePreset: (Long) -> Unit,
  onAddToCart: () -> Unit,
  onRemoveCartItem: (CartItem) -> Unit,
  onUpdateCartItemQty: (String, Int) -> Unit,
  onClearCart: () -> Unit,
  onCashPaidChange: (String) -> Unit,
  onSetCashExact: () -> Unit,
  onAddCashPreset: (Long) -> Unit,
  onSaveTransaction: () -> Unit,
  modifier: Modifier = Modifier
) {
  val unitPriceLong by remember(inputUnitPrice) {
    derivedStateOf { CurrencyFormatter.parseAmount(inputUnitPrice) }
  }

  val itemSubtotal by remember(inputQty, unitPriceLong) {
    derivedStateOf { inputQty.toLong() * unitPriceLong }
  }

  val grandTotal by remember(cartItems) {
    derivedStateOf { cartItems.sumOf { it.subtotal } }
  }

  val cashPaidLong by remember(cashPaidInput) {
    derivedStateOf { CurrencyFormatter.parseAmount(cashPaidInput) }
  }

  val changeAmount by remember(cashPaidLong, grandTotal) {
    derivedStateOf { cashPaidLong - grandTotal }
  }

  val canCheckout by remember(cartItems, cashPaidLong, grandTotal) {
    derivedStateOf { cartItems.isNotEmpty() && cashPaidLong >= grandTotal }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. HEADER INFO (Tanpa master barang banner)
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.PointOfSale,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Ketik bebas nama barang, qty, dan harga satuan tanpa perlu stok.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 2. INPUT CEPAT BARANG (KERANJANG BELANJA)
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
          Text(
            text = "Input Barang Belanja",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          // Input Nama Barang (Bebas)
          OutlinedTextField(
            value = inputItemName,
            onValueChange = onNameChange,
            label = { Text("Nama Barang (bebas)") },
            placeholder = { Text("Contoh: Beras 5kg, Telur 1/2kg, Aqua 600ml") },
            trailingIcon = {
              if (inputItemName.isNotEmpty()) {
                IconButton(onClick = { onNameChange("") }) {
                  Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                }
              }
            },
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.Words,
              imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_item_name")
          )

          // Qty & Harga Satuan Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Qty Stepper
            Column(modifier = Modifier.width(130.dp)) {
              Text(
                text = "Jumlah (Qty)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                IconButton(
                  onClick = onDecrementQty,
                  modifier = Modifier.size(36.dp).testTag("btn_qty_minus")
                ) {
                  Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                }
                Text(
                  text = inputQty.toString(),
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = onIncrementQty,
                  modifier = Modifier.size(36.dp).testTag("btn_qty_plus")
                ) {
                  Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                }
              }
            }

            // Harga Satuan Input
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Harga Satuan (Rp)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = if (inputUnitPrice.isNotEmpty()) CurrencyFormatter.formatNumber(unitPriceLong) else "",
                onValueChange = onUnitPriceChange,
                placeholder = { Text("0") },
                prefix = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Number,
                  imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onAddToCart() }),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_unit_price")
              )
            }
          }

          // Quick Price Presets
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf(2_000L, 5_000L, 10_000L, 15_000L, 20_000L).forEach { preset ->
              FilterChip(
                selected = unitPriceLong == preset,
                onClick = { onSetPricePreset(preset) },
                label = {
                  Text(
                    text = when (preset) {
                      2_000L -> "2rb"
                      5_000L -> "5rb"
                      10_000L -> "10rb"
                      15_000L -> "15rb"
                      else -> "20rb"
                    },
                    fontSize = 11.sp
                  )
                }
              )
            }
          }

          // Subtotal Display & "+ Tambah ke Struk" Button
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Subtotal Item:",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = CurrencyFormatter.formatRupiah(itemSubtotal),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.primary,
                  fontFamily = FontFamily.Monospace
                )
              }

              Button(
                onClick = onAddToCart,
                modifier = Modifier.testTag("btn_add_to_cart")
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Tambah ke Struk")
              }
            }
          }
        }
      }
    }

    // 3. DAFTAR ITEM DI STRUK SAAT INI
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Daftar Barang di Struk (${cartItems.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        if (cartItems.isNotEmpty()) {
          TextButton(
            onClick = onClearCart,
            modifier = Modifier.testTag("btn_clear_cart")
          ) {
            Text(text = "Kosongkan", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
          }
        }
      }
    }

    if (cartItems.isEmpty()) {
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
              imageVector = Icons.Default.ShoppingCart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Struk Belanja Masih Kosong",
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Ketik nama barang dan harga di atas, lalu klik '+ Tambah ke Struk'",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.outline,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(cartItems, key = { it.id }) { item ->
        CartItemRow(
          item = item,
          onQtyChange = { newQty -> onUpdateCartItemQty(item.id, newQty) },
          onDelete = { onRemoveCartItem(item) }
        )
      }
    }

    // 4. KALKULATOR TOTAL & KEMBALIAN (CHECKOUT)
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .testTag("checkout_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // GRAND TOTAL
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "GRAND TOTAL",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${cartItems.size} Jenis Barang (${cartItems.sumOf { it.qty }} pcs)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
              )
            }

            Text(
              text = CurrencyFormatter.formatRupiah(grandTotal),
              fontSize = 26.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = FontFamily.Monospace
            )
          }

          HorizontalDivider()

          // Uang Tunai Diterima Input
          Column {
            Text(
              text = "Uang Diterima / Bayar (Rp)",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = if (cashPaidInput.isNotEmpty()) CurrencyFormatter.formatNumber(cashPaidLong) else "",
              onValueChange = onCashPaidChange,
              placeholder = { Text("0") },
              prefix = { Text("Rp ") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_cash_paid")
            )
          }

          // Quick Cash Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = cashPaidLong == grandTotal && grandTotal > 0,
              onClick = onSetCashExact,
              label = { Text("Uang Pas", fontSize = 11.sp) },
              modifier = Modifier.testTag("btn_cash_pas")
            )
            listOf(20_000L, 50_000L, 100_000L).forEach { amount ->
              FilterChip(
                selected = cashPaidLong == amount,
                onClick = { onCashPaidChange(amount.toString()) },
                label = {
                  Text(
                    text = when (amount) {
                      20_000L -> "20rb"
                      50_000L -> "50rb"
                      else -> "100rb"
                    },
                    fontSize = 11.sp
                  )
                }
              )
            }
          }

          // Kembalian Banner
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              cashPaidLong == 0L -> MaterialTheme.colorScheme.surfaceVariant
              changeAmount >= 0 -> Color(0xFFDCFCE7)
              else -> Color(0xFFFEE2E2)
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (changeAmount >= 0) "KEMBALIAN" else "KURANG BAYAR",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (changeAmount >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Text(
                  text = if (changeAmount >= 0) "Kembalikan ke pembeli" else "Nominal uang kurang",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Text(
                text = CurrencyFormatter.formatRupiah(kotlin.math.abs(changeAmount)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (changeAmount >= 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                fontFamily = FontFamily.Monospace
              )
            }
          }

          // Tombol SIMPAN TRANSAKSI
          Button(
            onClick = onSaveTransaction,
            enabled = canCheckout,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_save_transaction"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (cartItems.isEmpty()) "Tambahkan Barang Dahulu"
                     else if (cashPaidLong < grandTotal) "Masukkan Pembayaran"
                     else "Simpan Transaksi (Rp ${CurrencyFormatter.formatNumber(grandTotal)})",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
private fun CartItemRow(
  item: CartItem,
  onQtyChange: (Int) -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("cart_item_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.itemName,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${item.qty} x ${CurrencyFormatter.formatRupiah(item.unitPrice)}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Quantity controls
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        IconButton(
          onClick = { onQtyChange(item.qty - 1) },
          modifier = Modifier.size(30.dp)
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
        }

        Text(
          text = item.qty.toString(),
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          modifier = Modifier.width(24.dp),
          textAlign = TextAlign.Center
        )

        IconButton(
          onClick = { onQtyChange(item.qty + 1) },
          modifier = Modifier.size(30.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Subtotal
      Text(
        text = CurrencyFormatter.formatRupiah(item.subtotal),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.primary
      )

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(32.dp).testTag("delete_cart_item_${item.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Hapus",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
