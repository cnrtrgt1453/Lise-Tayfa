package com.cotx.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
 * ayrıntısını (tam exception sınıf adı, mesaj, cause, stack trace) Logcat
 * olmadan doğrudan test cihazının ekranında okuyabilmek için kullanılır.
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
    val signatureText: String = "(context verilmedi — hesaplanamadı)"
) {
    companion object {
        // R8, string literal'leri ve runtime mesajlarını gizlemez; mesaj ve durum
        // kodları minify açıkken bile okunabilir kalır (sınıf ADLARI gizlenir).
        private val CODE_ACCESSORS = listOf(
            "getStatusCode", "getErrorCode", "getCode", "getType"
        )
        private val CODE_FIELDS = listOf("statusCode", "errorCode", "mStatusCode")

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

            return DebugErrorInfo(
                className = error.javaClass.name,
                message = error.message ?: error.localizedMessage ?: "(mesaj yok)",
                cause = causeText,
                statusCodes = if (codes.isEmpty()) "(bulunamadı)" else codes.joinToString("\n"),
                stackTrace = Log.getStackTraceString(error),
                signatureText = signatureText
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

    /** Panoya kopyalanacak tam metin. En yararlı alanlar en üstte. */
    fun asClipboardText(): String = buildString {
        appendLine("Exception Class : $className")
        appendLine("Message         : $message")
        appendLine("Status Code(s)  :")
        appendLine(statusCodes)
        appendLine("Running APK Signature (Firebase'e eklenecek parmak izi):")
        appendLine(signatureText)
        appendLine("Cause Chain     :")
        appendLine(cause)
        appendLine()
        appendLine("---- FULL STACK TRACE ----")
        append(stackTrace)
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
            Text(
                text = "🐞 DEBUG: Google Giriş Hatası",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DebugField("Exception Class Name", info.className)
                Spacer(Modifier.height(10.dp))
                DebugField("Message", info.message)
                Spacer(Modifier.height(10.dp))
                DebugField("Status Code(s)", info.statusCodes)
                Spacer(Modifier.height(10.dp))
                DebugField("Bu APK'nın İmzası (SHA-1 / SHA-256)", info.signatureText)
                Spacer(Modifier.height(10.dp))
                DebugField("Cause Chain", info.cause)
                Spacer(Modifier.height(10.dp))
                DebugField("Stack Trace", info.stackTrace)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("cotx debug error", info.asClipboardText())
                )
                Toast.makeText(
                    context,
                    "Hata metni (stack trace dahil) panoya kopyalandı",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text("Kopyala")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
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
