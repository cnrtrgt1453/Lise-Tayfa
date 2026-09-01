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
import com.cotx.app.util.DebugErrorDialog
import com.cotx.app.util.DebugErrorInfo
import com.cotx.app.util.GoogleCredentialAuth
import com.cotx.app.util.GoogleSignInCancelled
import com.cotx.app.viewmodel.AuthUiState
import com.cotx.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    // GEÇİCİ DEBUG: ham Google/Play Services hatası için ekran içi pop-up.
    var debugErrorInfo by remember { mutableStateOf<DebugErrorInfo?>(null) }

    // --- Credential Manager tabanlı "Google ile Giriş" akışı ---
    fun launchGoogleSignIn() {
        scope.launch {
            try {
                val idToken = GoogleCredentialAuth.requestGoogleIdToken(context)
                viewModel.loginWithGoogle(idToken)
            } catch (e: GoogleSignInCancelled) {
                // Kullanıcı hesap seçmeden çıktı: sessizce başlangıç durumuna dön.
                viewModel.resetState()
            } catch (e: Exception) {
                // GEÇİCİ DEBUG: exception'ın tam sınıf adı + mesaj + stack trace'i +
                // bu APK'nın imza SHA-1'i doğrudan ekranda göster (Logcat gerekmesin).
                viewModel.setError(
                    e.message ?: e.localizedMessage ?: "Google ile giriş yapılamadı",
                    DebugErrorInfo.from(e, context)
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> {
                if (state.debugInfo != null) {
                    debugErrorInfo = state.debugInfo
                } else {
                    errorDialogMessage = state.message
                }
            }
            else -> {}
        }
    }

    // --- GEÇİCİ DEBUG: Ham Hata Pop-Up'ı (yayından önce kaldırın) ---
    debugErrorInfo?.let { info ->
        DebugErrorDialog(
            info = info,
            onDismiss = {
                debugErrorInfo = null
                viewModel.resetState()
            }
        )
    }

    // --- Detaylı Hata Pop-Up Dialog ---
    if (errorDialogMessage != null && debugErrorInfo == null) {
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
                        text = "cotx Academy",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = PrimaryPurple
                        )
                    )

                    Text(
                        text = "Soru Yardımlaşma Platformu",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "Sorularını paylaş, akranlarınla yardımlaş ve sınavlara birlikte hazırlan.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
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

                    // --- Tek Tıkla Google ile Giriş Yap Butonu ---
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
                                    text = "Google ile Giriş Yap",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "🔒 Google hesabın ile saniyeler içinde güvenli giriş yapabilirsin.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
