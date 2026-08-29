package com.cotx.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotx.app.ui.components.AcademicBackground
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.util.GoogleCredentialAuth
import com.cotx.app.util.GoogleSignInCancelled
import com.cotx.app.viewmodel.AuthUiState
import com.cotx.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    // --- Credential Manager tabanlı "Google ile Kaydol" akışı ---
    fun launchGoogleSignIn() {
        scope.launch {
            try {
                val idToken = GoogleCredentialAuth.requestGoogleIdToken(context)
                viewModel.loginWithGoogle(idToken)
            } catch (e: GoogleSignInCancelled) {
                viewModel.resetState()
            } catch (e: Exception) {
                val detail = buildString {
                    appendLine("⚠️ Google ile kaydolunamadı")
                    appendLine()
                    appendLine("Mesaj: ${e.localizedMessage ?: e.message ?: "Bilinmeyen hata"}")
                    appendLine("Paket Adı: ${context.packageName}")
                    appendLine()
                    appendLine("🛠️ Sık görülen nedenler:")
                    appendLine("1. Play App Signing SHA-1 / SHA-256 parmak izleri Firebase Console'a eklenmemiş.")
                    appendLine("2. Google Cloud OAuth izin ekranı hâlâ \"Testing\" modunda (Production'a alın).")
                    appendLine("3. Firebase ➔ Genel Ayarlar ➔ Destek E-postası alanı boş.")
                    append("4. google-services.json güncel değil (yenisini indirip değiştirin, yeni AAB derleyin).")
                }
                errorDialogMessage = detail
                viewModel.setError(detail)
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
        } else if (uiState is AuthUiState.Error) {
            errorDialogMessage = (uiState as AuthUiState.Error).message
        }
    }

    // --- Detaylı Hata Pop-Up Dialog ---
    if (errorDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = {
                Text(
                    text = "⚠️ Google Giriş Hatası Detayı",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                SelectionContainer {
                    Text(
                        text = errorDialogMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { errorDialogMessage = null }
                ) {
                    Text("Tamam")
                }
            }
        )
    }

    // --- Sanatsal Ders Temalı Arka Plan ---
    AcademicBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "cotx Academy'ye Katıl",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                    )

                    Text(
                        text = "Google hesabınla hızlıca kaydol ve sorularını sormaya başla.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
                    )

                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // --- Google ile Hızlı Kaydol ---
                    Button(
                        onClick = { launchGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.6f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Google ile Hızlı Kaydol",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Zaten bir hesabın var mı? Giriş Yap",
                            color = PrimaryPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
