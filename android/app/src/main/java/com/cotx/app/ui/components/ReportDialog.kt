package com.cotx.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotx.app.ui.theme.PrimaryPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    title: String = "İçeriği Bildir 🚩",
    subtitle: String = "Lütfen topluluk kurallarını ihlal eden sebebi seçin:",
    onDismissRequest: () -> Unit,
    onConfirmReport: (reason: String, note: String) -> Unit
) {
    val reportReasons = listOf(
        "Spam / Yanıltıcı İçerik",
        "Uygunsuz / Taciz Edici İçerik",
        "Nefret Söylemi veya Hakaret",
        "Telif Hakkı / Gizlilik İhlali",
        "Diğer"
    )

    var selectedReason by remember { mutableStateOf(reportReasons.first()) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                reportReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 200) noteText = it },
                    label = { Text("Ek Açıklama (İsteğe bağlı)") },
                    placeholder = { Text("Detay belirtmek isterseniz yazın...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                Text(
                    text = "${noteText.length}/200",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmReport(selectedReason, noteText) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Bildir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("İptal")
            }
        }
    )
}
