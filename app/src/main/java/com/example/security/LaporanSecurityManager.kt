package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object LaporanSecurityManager {

  private const val PREFS_NAME = "laporan_security_prefs"
  private const val KEY_SECURITY_ENABLED = "security_enabled"
  private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
  private const val KEY_PIN_HASH = "pin_hash"
  private const val KEY_PIN_SALT = "pin_salt"
  const val DEFAULT_PIN = "1234"

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isSecurityEnabled(context: Context): Boolean {
    val prefs = getPrefs(context)
    return prefs.getBoolean(KEY_SECURITY_ENABLED, true)
  }

  fun setSecurityEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply()
  }

  fun isBiometricEnabled(context: Context): Boolean {
    val prefs = getPrefs(context)
    return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
  }

  fun setBiometricEnabled(context: Context, enabled: Boolean) {
    getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
  }

  fun hasCustomPin(context: Context): Boolean {
    return getPrefs(context).contains(KEY_PIN_HASH)
  }

  fun verifyPin(context: Context, enteredPin: String): Boolean {
    val prefs = getPrefs(context)
    val savedHash = prefs.getString(KEY_PIN_HASH, null)
    val savedSalt = prefs.getString(KEY_PIN_SALT, null)

    if (savedHash == null || savedSalt == null) {
      // Using default PIN
      return enteredPin == DEFAULT_PIN
    }

    val computedHash = hashPin(enteredPin, savedSalt)
    return computedHash == savedHash
  }

  fun setPin(context: Context, newPin: String) {
    val salt = generateSalt()
    val hash = hashPin(newPin, salt)
    getPrefs(context).edit()
      .putString(KEY_PIN_HASH, hash)
      .putString(KEY_PIN_SALT, salt)
      .apply()
  }

  private fun generateSalt(): String {
    val random = SecureRandom()
    val saltBytes = ByteArray(16)
    random.nextBytes(saltBytes)
    return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
  }

  private fun hashPin(pin: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(salt.toByteArray(Charsets.UTF_8))
    val hashedBytes = md.digest(pin.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
  }
}
