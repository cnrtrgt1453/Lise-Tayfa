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
    val cause: String,
    val stackTrace: String
) {
    companion object {
        fun from(error: Throwable): DebugErrorInfo = DebugErrorInfo(
            className = error.javaClass.name,
            message = error.message ?: error.localizedMessage ?: "(mesaj yok)",
            cause = error.cause?.let { "${it.javaClass.name}: ${it.message ?: "(mesaj yok)"}" }
                ?: "(cause yok)",
            stackTrace = Log.getStackTraceString(error)
        )
    }

    /** Panoya kopyalanacak tam metin. */
    fun asClipboardText(): String = buildString {
        appendLine("Exception Class : $className")
        appendLine("Message         : $message")
        appendLine("Cause           : $cause")
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
                DebugField("Cause", info.cause)
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
