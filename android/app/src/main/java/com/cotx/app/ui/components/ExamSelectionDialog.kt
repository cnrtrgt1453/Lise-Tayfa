package com.cotx.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange

@Composable
fun ExamSelectionDialog(
    onExamSelected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = { /* Force explicit user choice */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎓",
                    fontSize = 42.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hangi Sınava Hazırlanıyorsunuz ?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sana özel dersleri ve soruları hazırlayabilmemiz için hedefini seç.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // TYT/AYT Option
                ExamOptionCard(
                    title = "TYT / AYT",
                    subtitle = "Fizik, Kimya, Biyoloji, Matematik, Geometri, Türkçe, Coğrafya, Tarih",
                    borderColor = PrimaryPurple,
                    badgeColor = PrimaryPurple,
                    onClick = { onExamSelected("TYT/AYT") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // LGS Option
                ExamOptionCard(
                    title = "LGS",
                    subtitle = "Türkçe, Matematik, Fen Bilgisi, Sosyal Bilgiler",
                    borderColor = SecondaryOrange,
                    badgeColor = SecondaryOrange,
                    onClick = { onExamSelected("LGS") }
                )
            }
        }
    }
}

@Composable
private fun ExamOptionCard(
    title: String,
    subtitle: String,
    borderColor: Color,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = badgeColor.copy(alpha = 0.08f),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = borderColor,
                        fontSize = 20.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = borderColor
                ) {
                    Text(
                        text = "Seç",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
