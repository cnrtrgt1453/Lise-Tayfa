package com.cotx.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ============================================================================
 * GEÇİCİ HATA AYIKLAMA (DEBUG) BİLEŞENİ - YAYINDAN ÖNCE KALDIRIN
 * ============================================================================
 * Google Sign-In / Credential Manager / Firebase Auth hatalarının ham
 * ayrıntısını (tam exception sınıf adı, mesaj, cause, stack trace, etkilenen API)
 * Logcat olmadan doğrudan test cihazının ekranında okuyabilmek için kullanılır.
 */
data class DebugErrorInfo(
    val className: String,
    val message: String,
    /** Tüm `cause` zinciri (her seviye: sınıf adı + mesaj). */
    val cause: String,
    /** GMS ApiException vb. içinden reflection ile çekilen sayısal kodlar. */
    val statusCodes: String,
    val stackTrace: String,
    /** Bu cihazdaki APK'yı imzalayan sertifikanın SHA-1 / SHA-256 parmak izi. */
    val signatureText: String = "(context verilmedi — hesaplanamadı)",
    /** Hangi API veya servis ile sorun yaşandığının kısa özeti. */
    val affectedApi: String = "",
    /** Logcat konsoluna basılan tam biçimlendirilmiş metin. */
    val fullLogcatText: String = ""
) {
    companion object {
        // R8, string literal'leri ve runtime mesajlarını gizlemez; mesaj ve durum
        // kodları minify açıkken bile okunabilir kalır (sınıf ADLARI gizlenir).
        private val CODE_ACCESSORS = listOf(
            "getStatusCode", "getErrorCode", "getCode", "getType"
        )
        private val CODE_FIELDS = listOf("statusCode", "errorCode", "mStatusCode")

        private fun getResString(context: Context?, resName: String): String {
            if (context == null) return "(context verilmedi)"
            return runCatching {
                val resId = context.resources.getIdentifier(resName, "string", context.packageName)
                if (resId != 0) context.getString(resId) else "(google-services.json içinde yok)"
            }.getOrElse { "(okunamadı: ${it.message})" }
        }

        fun from(error: Throwable, context: Context? = null): DebugErrorInfo {
            val chain = generateSequence<Throwable>(error) { it.cause }.take(12).toList()

            val causeText = if (chain.size <= 1) {
                "(cause yok)"
            } else {
                chain.drop(1).mapIndexed { i, t ->
                    "  #${i + 1} ${t.javaClass.name}: ${t.message ?: "(mesaj yok)"}"
                }.joinToString("\n")
            }

            val codes = chain
                .mapNotNull { t -> probeCode(t)?.let { "${t.javaClass.simpleName} -> $it" } }
                .distinct()

            val signatureText = if (context != null) {
                runCatching { SignatureInfo.current(context).asReadableText() }
                    .getOrElse { "(imza okunamadı: ${it.message})" }
            } else {
                "(context verilmedi — hesaplanamadı)"
            }

            val className = error.javaClass.name
            val message = error.message ?: error.localizedMessage ?: "(mesaj yok)"
            val statusCodesStr = if (codes.isEmpty()) "(bulunamadı)" else codes.joinToString("\n")
            val stackTrace = Log.getStackTraceString(error)

            // Tüm hata mesajlarını ve cause zincirini tarayarak kök nedeni ve ilgili API'yi tespit et
            val haystack = buildString {
                append(className).append(' ').append(message)
                chain.forEach { c ->
                    append(' ').append(c.javaClass.name).append(": ").append(c.message ?: "")
                }
            }

            val isApiKeyExpired = haystack.contains("API key expired", ignoreCase = true) ||
                    haystack.contains("renew the API key", ignoreCase = true) ||
                    haystack.contains("API_KEY_EXPIRED", ignoreCase = true)

            val isApiKeyInvalid = haystack.contains("API key invalid", ignoreCase = true) ||
                    haystack.contains("API_KEY_INVALID", ignoreCase = true) ||
                    haystack.contains("Invalid API key", ignoreCase = true)

            val isDeveloperError = haystack.contains("DEVELOPER_ERROR", ignoreCase = true) ||
                    Regex("""\b(status\s*code|code)\s*[:=]?\s*10\b""", RegexOption.IGNORE_CASE).containsMatchIn(haystack)

            val webApiKey = getResString(context, "google_api_key")
            val projectId = getResString(context, "project_id")
            val webClientId = getResString(context, "default_web_client_id")
            val appId = getResString(context, "google_app_id")

            val affectedApi = when {
                isApiKeyExpired -> "Identity Toolkit API (Firebase Authentication) — Web API Key Süresi Dolmuş (Expired)"
                isApiKeyInvalid -> "Identity Toolkit API (Firebase Authentication) — Geçersiz/Kısıtlanmış Web API Key"
                isDeveloperError -> "Google Identity Services API — OAuth İstemci / SHA-1 Doğrulama Hatası (DEVELOPER_ERROR 10)"
                error is com.google.firebase.auth.FirebaseAuthException -> "Firebase Authentication API (${error.errorCode})"
                else -> "Firebase Auth / Google Identity Services API"
            }

            val apiDiagnosisBlock = when {
                isApiKeyExpired || isApiKeyInvalid -> buildString {
                    appendLine("📌 SORUN YAŞANAN HEDEF API VE SERVİS BİLGİSİ (AFFECTED API):")
                    appendLine("• Etkilenen Servis / API : Identity Toolkit API (Firebase Authentication REST API)")
                    appendLine("• İsteğin Atıldığı Uç    : https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp")
                    appendLine("• Çağrılan Metot         : FirebaseAuth.getInstance().signInWithCredential(...)")
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🔑 PROJE VE UYGULAMA YAPILANDIRMASI (google-services.json):")
                    appendLine("• Firebase Project ID    : $projectId")
                    appendLine("• Hatalı Web API Key     : $webApiKey")
                    appendLine("• GCM Web Client ID      : $webClientId")
                    appendLine("• Google App ID          : $appId")
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🚨 HATA NEDENİ TEŞHİSİ:")
                    if (isApiKeyExpired) {
                        appendLine("Google Cloud Console'da tanımlı Web API Key'in (\"google_api_key\") süresi dolmuştur (EXPIRED).")
                        appendLine("Firebase Auth SDK, ID Token'ı sunucuya doğrulattırırken bu anahtarı kullanır.")
                    } else {
                        appendLine("Google Cloud Console'da tanımlı Web API Key (\"google_api_key\") geçersizdir veya Identity Toolkit API servisine erişimi engellenmiştir.")
                    }
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🛠️ ÇÖZÜM ADIMLARI:")
                    appendLine("1. Google Cloud Console (https://console.cloud.google.com/apis/credentials) sayfasına gidin.")
                    appendLine("2. '$projectId' projesinde API Keys altındaki \"Web API Key\" (veya Android key) ayarlarına girin.")
                    appendLine("3. Anahtarın Expiration (Son Kullanma Tarihi) süresini kontrol edin veya yenileyin (Renew API Key).")
                    appendLine("4. API Restrictions (Kısıtlamalar) sekmesinde \"Identity Toolkit API\" servisinin İZİNLİ olduğunu doğrulayın.")
                    appendLine("5. Firebase Console (https://console.firebase.google.com) > Proje Ayarları'ndan güncel google-services.json dosyasını indirip android/app/ klasörüne kaydedin.")
                }
                isDeveloperError -> buildString {
                    appendLine("📌 SORUN YAŞANAN HEDEF API VE SERVİS BİLGİSİ (AFFECTED API):")
                    appendLine("• Etkilenen Servis / API : Google Identity Services API / OAuth 2.0 Client Verification")
                    appendLine("• Çağrılan Metot         : CredentialManager.getCredential(...) (StatusCode 10 / DEVELOPER_ERROR)")
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🔑 PROJE YAPILANDIRMASI:")
                    appendLine("• GCM Web Client ID      : $webClientId")
                    appendLine("• Firebase Project ID    : $projectId")
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🚨 HATA NEDENİ TEŞHİSİ:")
                    appendLine("Bu APK'yı imzalayan sertifikanın SHA-1 parmak izi Firebase Console / GCP OAuth 2.0 İstemcileri arasında kayıtlı değil.")
                }
                else -> buildString {
                    appendLine("📌 SORUN YAŞANAN HEDEF API VE SERVİS BİLGİSİ (AFFECTED API):")
                    appendLine("• Etkilenen Servis / API : Firebase Authentication / Google Identity Services")
                    appendLine("--------------------------------------------------------------------------------")
                    appendLine("🔑 PROJE YAPILANDIRMASI:")
                    appendLine("• Firebase Project ID    : $projectId")
                    appendLine("• Web API Key            : $webApiKey")
                    appendLine("• GCM Web Client ID      : $webClientId")
                }
            }

            val fullLogcatText = buildString {
                appendLine("================================================================================")
                appendLine("🚨🚨🚨 GOOGLE İLE GİRİŞ HATASI DETAYLI KONSOL LOGU 🚨🚨🚨")
                appendLine("================================================================================")
                appendLine("Hata Türü (Exception Class) : $className")
                appendLine("Hata Mesajı (Message)         : $message")
                if (error is com.google.firebase.auth.FirebaseAuthException) {
                    appendLine("Firebase Auth Hata Kodu       : ${error.errorCode}")
                }
                appendLine("Status Code(s)                : $statusCodesStr")
                appendLine("--------------------------------------------------------------------------------")
                appendLine(apiDiagnosisBlock)
                appendLine("--------------------------------------------------------------------------------")
                appendLine("APK İmzası (SHA-1 / SHA-256):")
                appendLine(signatureText)
                appendLine("--------------------------------------------------------------------------------")
                appendLine("Hata Kök Nedeni (Cause Chain):")
                appendLine(causeText)
                appendLine("--------------------------------------------------------------------------------")
                appendLine("📋 TAM HATA İZİ (STACK TRACE):")
                append(stackTrace)
                appendLine("================================================================================")
            }

            return DebugErrorInfo(
                className = className,
                message = message,
                cause = causeText,
                statusCodes = statusCodesStr,
                stackTrace = stackTrace,
                signatureText = signatureText,
                affectedApi = affectedApi,
                fullLogcatText = fullLogcatText
            )
        }

        /** Bir throwable üzerinde bilinen kod alanı/metodlarını reflection ile dener. */
        private fun probeCode(t: Throwable): String? {
            for (name in CODE_ACCESSORS) {
                runCatching {
                    val m = t.javaClass.getMethod(name)
                    m.isAccessible = true
                    m.invoke(t)?.let { return "$name()=$it" }
                }
            }
            for (name in CODE_FIELDS) {
                runCatching {
                    val f = t.javaClass.getDeclaredField(name)
                    f.isAccessible = true
                    f.get(t)?.let { return "$name=$it" }
                }
            }
            return null
        }
    }

    /** Panoya kopyalanacak tam metin (Logcat formatında). */
    fun asClipboardText(): String = fullLogcatText.ifBlank {
        buildString {
            appendLine("Exception Class : $className")
            appendLine("Message         : $message")
            appendLine("Status Code(s)  : $statusCodes")
            appendLine("Running APK Signature : $signatureText")
            appendLine("Affected API    : $affectedApi")
            appendLine("Cause Chain     : $cause")
            appendLine()
            appendLine("---- FULL STACK TRACE ----")
            append(stackTrace)
        }
    }
}

@Composable
fun DebugErrorDialog(
    info: DebugErrorInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            androidx.compose.material3.Text(
                text = "🚨 LOGCAT: Google Giriş Hatası Logu",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (info.affectedApi.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📌 ETKİLENEN API / SERVİS:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = info.affectedApi,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                androidx.compose.material3.Text(
                    text = "Aşağıdaki metin Logcat konsoluna basılan log metninin birebir aynısıdır:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SelectionContainer {
                    androidx.compose.material3.Text(
                        text = info.fullLogcatText.ifBlank { info.asClipboardText() },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("cotx logcat error log", info.asClipboardText())
                    )
                    Toast.makeText(
                        context,
                        "Logcat log metni panoya kopyalandı",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                androidx.compose.material3.Text("Logcat Logunu Kopyala")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Kapat")
            }
        }
    )
}


@Composable
private fun DebugField(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        SelectionContainer {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
