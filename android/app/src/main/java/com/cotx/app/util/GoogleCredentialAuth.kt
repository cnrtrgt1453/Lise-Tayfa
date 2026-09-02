package com.cotx.app.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.cotx.app.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Kullanıcı, Google hesap seçim ekranını seçim yapmadan kapattığında fırlatılır. */
class GoogleSignInCancelled(cause: Throwable? = null) : Exception("İşlem iptal edildi veya Google Play Hizmetleri pencereyi kapattı.", cause)

/** Cihazda tanımlı bir Google hesabı bulunamadığında veya OAuth doğrulama başarısız olduğunda fırlatılır. */
class GoogleSignInNoAccount(cause: Throwable? = null) : Exception(
    "Cihazda giriş yapılmış bir Google hesabı bulunamadı veya OAuth yapılandırması (SHA-1 / Web Client ID) doğrulanamadı. " +
        "Ayarlar > Hesaplar bölümünden bir Google hesabı eklendiğinden ve SHA-1 tanımlarının yapıldığından emin olun.",
    cause
)

/**
 * `StatusCode 10 (DEVELOPER_ERROR)` — çalışan APK'nın imza SHA-1'i Firebase /
 * Google Cloud OAuth istemcilerinde kayıtlı değil. Play Store İç/Kapalı Test
 * sürümlerinde tipik olarak Play App Signing anahtarının SHA-1'i eksiktir.
 */
class GoogleSignInConfigError(
    val runningSha1: String,
    val runningSha256: String,
    cause: Throwable? = null,
) : Exception(
    buildString {
        appendLine("Google Sign-In yapılandırma hatası (DEVELOPER_ERROR / StatusCode 10).")
        appendLine("Bu cihazdaki APK'yı imzalayan sertifika Firebase'de kayıtlı değil.")
        appendLine()
        appendLine("Çalışan imza SHA-1 : $runningSha1")
        appendLine("Çalışan imza SHA-256: $runningSha256")
        appendLine()
        appendLine("YAPILMASI GEREKEN: Yukarıdaki SHA-1 ve SHA-256 değerlerini")
        appendLine("Firebase Console → Proje Ayarları → com.cotx.app → Parmak izi ekle")
        append("bölümüne ekleyin, ardından güncel google-services.json dosyasını indirin.")
    },
    cause
)

/**
 * Legacy `GoogleSignIn` API'sinin yerine geçen, Credential Manager tabanlı
 * "Google ile oturum aç" akışı. Firebase Auth'a verilecek ID token'ı döndürür.
 */
object GoogleCredentialAuth {

    /**
     * Google hesap seçim akışını başlatır ve seçilen hesabın ID token'ını döndürür.
     *
     * Önce `GetGoogleIdOption` (bottom-sheet) denenir; cihazda uygulamayla daha
     * önce kullanılmış hesap yoksa `NoCredentialException` gelir ve otomatik olarak
     * `GetSignInWithGoogleOption` (klasik "Sign in with Google" buton akışı) ile
     * yeniden denenir.
     *
     * @param activityContext Compose içindeki `LocalContext.current` (Activity context olmalı).
     * @throws GoogleSignInCancelled kullanıcı seçim yapmadan çıktıysa
     * @throws GoogleSignInConfigError StatusCode 10 (imza SHA-1 kayıtlı değil)
     * @throws GoogleSignInNoAccount cihazda hiç Google hesabı yoksa
     * @throws Exception diğer tüm Credential Manager / token ayrıştırma hataları
     */
    suspend fun requestGoogleIdToken(activityContext: Context): String {
        val webClientId = activityContext.getString(R.string.default_web_client_id)
        val credentialManager = CredentialManager.create(activityContext)

        val bottomSheetRequest = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        val buttonFlowRequest = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(webClientId).build()
            )
            .build()

        val response = try {
            credentialManager.getCredential(activityContext, bottomSheetRequest)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInCancelled(e)
        } catch (e: NoCredentialException) {
            // Bottom-sheet için uygun hesap yok — klasik buton akışıyla yeniden dene.
            try {
                credentialManager.getCredential(activityContext, buttonFlowRequest)
            } catch (e2: GetCredentialCancellationException) {
                throw GoogleSignInCancelled(e2)
            } catch (e2: NoCredentialException) {
                throw GoogleSignInNoAccount(e2)
            } catch (e2: GetCredentialException) {
                throw mapCredentialException(activityContext, e2)
            }
        } catch (e: GetCredentialException) {
            throw mapCredentialException(activityContext, e)
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                throw Exception("Google kimlik jetonu okunamadı: ${e.message}", e)
            }
        }

        throw Exception("Beklenmeyen kimlik bilgisi türü döndü: ${credential.type}")
    }

    /**
     * Credential Manager hatasını, mümkünse anlamlı bir alt tipe çevirir.
     * Hata zincirinde `StatusCode 10` / `DEVELOPER_ERROR` görülürse
     * [GoogleSignInConfigError] fırlatır (çalışan imza SHA-1 gömülür).
     */
    private fun mapCredentialException(context: Context, e: GetCredentialException): Exception {
        val haystack = buildString {
            append(e.type).append(' ')
            append(e.errorMessage ?: "")
            var c: Throwable? = e.cause
            var depth = 0
            while (c != null && depth < 12) {
                append(' ').append(c.javaClass.name).append(": ").append(c.message ?: "")
                c = c.cause
                depth++
            }
        }

        val looksLikeCode10 = haystack.contains("DEVELOPER_ERROR", ignoreCase = true) ||
            Regex("""\b(status\s*code|code)\s*[:=]?\s*10\b""", RegexOption.IGNORE_CASE).containsMatchIn(haystack) ||
            haystack.contains("10:", ignoreCase = true) && haystack.contains("ApiException")

        if (looksLikeCode10) {
            val fp = SignatureInfo.current(context)
            return GoogleSignInConfigError(fp.sha1, fp.sha256, e)
        }

        return Exception(
            "Google giriş hatası (${e.javaClass.simpleName}): ${e.errorMessage ?: "ayrıntı yok"}",
            e
        )
    }
}
