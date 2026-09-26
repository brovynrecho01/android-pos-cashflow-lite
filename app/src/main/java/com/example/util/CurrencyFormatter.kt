package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
  private val rupiahFormat: DecimalFormat by lazy {
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
      currencySymbol = "Rp "
      groupingSeparator = '.'
      monetaryDecimalSeparator = ','
    }
    DecimalFormat("'Rp '###,###,###", symbols)
  }

  private val numberFormat: DecimalFormat by lazy {
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
      groupingSeparator = '.'
      monetaryDecimalSeparator = ','
    }
    DecimalFormat("###,###,###", symbols)
  }

  fun formatRupiah(amount: Long): String {
    return if (amount == 0L) {
      "Rp 0"
    } else {
      rupiahFormat.format(amount)
    }
  }

  fun formatNumber(amount: Long): String {
    return if (amount == 0L) {
      "0"
    } else {
      numberFormat.format(amount)
    }
  }

  fun parseAmount(input: String): Long {
    val clean = input.replace("[^0-9]".toRegex(), "")
    return clean.toLongOrNull() ?: 0L
  }
}
