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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Kullanıcı, Google hesap seçim ekranını seçim yapmadan kapattığında fırlatılır. */
class GoogleSignInCancelled : Exception("İşlem iptal edildi. Google hesabı seçilmedi.")

/** Cihazda tanımlı bir Google hesabı bulunamadığında fırlatılır. */
class GoogleSignInNoAccount : Exception(
    "Cihazda giriş yapılmış bir Google hesabı bulunamadı. " +
        "Ayarlar > Hesaplar bölümünden bir Google hesabı ekleyip tekrar deneyin."
)

/**
 * Legacy `GoogleSignIn` API'sinin yerine geçen, Credential Manager tabanlı
 * "Google ile oturum aç" akışı. Firebase Auth'a verilecek ID token'ı döndürür.
 */
object GoogleCredentialAuth {

    /**
     * Google hesap seçim akışını başlatır ve seçilen hesabın ID token'ını döndürür.
     *
     * @param activityContext Compose içindeki `LocalContext.current` (Activity context olmalı;
     *   hesap seçim alt sayfası bu context üzerinden gösterilir).
     * @throws GoogleSignInCancelled kullanıcı seçim yapmadan çıktıysa
     * @throws GoogleSignInNoAccount cihazda hiç Google hesabı yoksa
     * @throws Exception diğer tüm Credential Manager / token ayrıştırma hataları
     */
    suspend fun requestGoogleIdToken(activityContext: Context): String {
        val webClientId = activityContext.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            // Yalnızca daha önce yetki verilmiş hesaplarla sınırlama; ilk girişte de çalışsın.
            .setFilterByAuthorizedAccounts(false)
            // Her seferinde kullanıcıya hesap seçtir (otomatik seçim yok).
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activityContext)

        val response = try {
            credentialManager.getCredential(activityContext, request)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInCancelled()
        } catch (e: NoCredentialException) {
            throw GoogleSignInNoAccount()
        } catch (e: GetCredentialException) {
            // DEBUG: orijinal exception'ı `cause` olarak koru ki ekrandaki hata
            // pop-up'ı Google Play Services'in gerçek iç hata kodunu gösterebilsin.
            throw Exception(
                "Google giriş hatası (${e.javaClass.simpleName}): ${e.errorMessage ?: "ayrıntı yok"}",
                e
            )
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
}
