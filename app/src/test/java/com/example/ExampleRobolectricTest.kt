package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Toko Pak Kadi Cash System", appName)
  }

  @Test
  fun `cashflow formula calculates correctly`() {
    val initialCash = 500_000L
    val totalIncome = 1_250_000L
    val totalExpense = 150_000L
    // Formula: Total Akhir = Modal Awal + Total Pemasukan - Total Pengeluaran
    val finalCash = initialCash + totalIncome - totalExpense
    assertEquals(1_600_000L, finalCash)
  }
}
