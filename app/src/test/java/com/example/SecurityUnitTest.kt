package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.BiometricAuthHelper
import com.example.security.LaporanSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecurityUnitTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    // Reset preferences for clean test run
    val prefs = context.getSharedPreferences("laporan_security_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()
  }

  @Test
  fun testDefaultPinVerification() {
    // Before any custom PIN is set, default PIN "1234" must be valid
    assertTrue("Default PIN 1234 should be accepted", LaporanSecurityManager.verifyPin(context, "1234"))
    assertFalse("Incorrect PIN should be rejected", LaporanSecurityManager.verifyPin(context, "0000"))
    assertFalse("Incorrect PIN should be rejected", LaporanSecurityManager.verifyPin(context, "9999"))
    assertFalse("Empty PIN should be rejected", LaporanSecurityManager.verifyPin(context, ""))
    assertFalse("Should not have custom PIN initially", LaporanSecurityManager.hasCustomPin(context))
  }

  @Test
  fun testCustomPinSetAndVerification() {
    val newPin = "5829"
    LaporanSecurityManager.setPin(context, newPin)

    assertTrue("Should indicate custom PIN is configured", LaporanSecurityManager.hasCustomPin(context))
    assertTrue("Newly set PIN must verify successfully", LaporanSecurityManager.verifyPin(context, newPin))
    assertFalse("Old default PIN should no longer work", LaporanSecurityManager.verifyPin(context, "1234"))
    assertFalse("Wrong PIN should be rejected", LaporanSecurityManager.verifyPin(context, "5820"))
  }

  @Test
  fun testSecurityAndBiometricToggles() {
    // Default enabled
    assertTrue("Security should default to true", LaporanSecurityManager.isSecurityEnabled(context))
    assertTrue("Biometric should default to true", LaporanSecurityManager.isBiometricEnabled(context))

    // Disable security
    LaporanSecurityManager.setSecurityEnabled(context, false)
    assertFalse(LaporanSecurityManager.isSecurityEnabled(context))

    // Re-enable security
    LaporanSecurityManager.setSecurityEnabled(context, true)
    assertTrue(LaporanSecurityManager.isSecurityEnabled(context))

    // Toggle biometric
    LaporanSecurityManager.setBiometricEnabled(context, false)
    assertFalse(LaporanSecurityManager.isBiometricEnabled(context))
  }

  @Test
  fun testBiometricHelperAvailabilityGraceful() {
    // In Robolectric environment, BiometricManager canAuthenticate should handle gracefully without crashing
    val available = BiometricAuthHelper.isBiometricAvailable(context)
    // Simply asserting that it executes without exception and returns boolean
    assertFalse(available) // Standard robolectric has no biometric hardware enrolled
  }
}
