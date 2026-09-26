package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {

  fun isBiometricAvailable(context: Context): Boolean {
    return try {
      val biometricManager = BiometricManager.from(context)
      val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
      biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    } catch (e: Exception) {
      false
    }
  }

  fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String = "Keamanan Laporan Keuangan",
    subtitle: String = "Verifikasi identitas pemilik untuk mengakses data sensitif",
    negativeButtonText: String = "Gunakan PIN",
    onSuccess: () -> Unit,
    onError: (String) -> Unit = {},
    onFailed: () -> Unit = {}
  ) {
    try {
      val executor = ContextCompat.getMainExecutor(activity)
      val biometricPrompt =
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
          }

          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailed()
          }
        })

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()

      biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Gagal memproses autentikasi biometrik")
    }
  }
}
