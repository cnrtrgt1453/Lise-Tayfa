package com.cotx.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Uygulamayı fiilen imzalayan sertifikanın SHA-1 / SHA-256 parmak izini
 * çalışma zamanında hesaplar.
 *
 * NEDEN GEREKLI:
 * Play Store "İç Test / Kapalı Test" kanallarından indirilen APK, sizin
 * upload (yükleme) anahtarınızla DEĞİL, Google'ın **Play App Signing** anahtarıyla
 * yeniden imzalanır. Google Sign-In (Credential Manager) `StatusCode 10
 * (DEVELOPER_ERROR)` hatası, bu çalışan imzanın SHA-1'i Firebase/Google Cloud
 * OAuth istemcilerinde kayıtlı olmadığında oluşur.
 *
 * Bu yardımcı, o cihazdaki gerçek imzayı ekrana/loga basar; böylece Firebase'e
 * hangi parmak izini eklemeniz gerektiğini tahmin etmeden görebilirsiniz.
 */
object SignatureInfo {

    data class Fingerprints(
        val sha1: String,
        val sha256: String,
        val packageName: String,
    ) {
        fun asReadableText(): String = buildString {
            appendLine("Package : $packageName")
            appendLine("SHA-1   : $sha1")
            append("SHA-256 : $sha256")
        }
    }

    /** Çalışan APK'yı imzalayan ilk sertifikanın parmak izlerini döndürür. */
    fun current(context: Context): Fingerprints {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            val certBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                val signers = signingInfo?.apkContentsSigners
                    ?: signingInfo?.signingCertificateHistory
                signers?.firstOrNull()?.toByteArray()
            } else {
                @Suppress("DEPRECATION", "PackageManagerGetSignatures")
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                info.signatures?.firstOrNull()?.toByteArray()
            }

            if (certBytes == null) {
                Fingerprints("(imza sertifikası okunamadı)", "(imza sertifikası okunamadı)", pkg)
            } else {
                Fingerprints(
                    sha1 = hashOf(certBytes, "SHA-1"),
                    sha256 = hashOf(certBytes, "SHA-256"),
                    packageName = pkg,
                )
            }
        } catch (e: Exception) {
            Fingerprints("(hata: ${e.message})", "(hata: ${e.message})", context.packageName)
        }
    }

    private fun hashOf(certBytes: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm).digest(certBytes)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
