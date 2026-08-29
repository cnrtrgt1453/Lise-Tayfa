package com.cotx.app.ui.screens.question

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cotx.app.data.model.User
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import com.cotx.app.viewmodel.AddQuestionUiState
import com.cotx.app.viewmodel.AddQuestionViewModel

import com.cotx.app.util.ExamSubjectHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(
    viewModel: AddQuestionViewModel,
    currentUser: User,
    onNavigateBack: () -> Unit,
    onQuestionUploaded: () -> Unit
) {
    val context = LocalContext.current
    val subjects = remember(currentUser.examType) { ExamSubjectHelper.getSubjectsForExam(currentUser.examType) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: "Matematik") }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Yardım İstiyorum") }

    val uiState by viewModel.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(uiState) {
        if (uiState is AddQuestionUiState.Success) {
            viewModel.resetState()
            onQuestionUploaded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soru Paylaş", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Picker Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Seçilen Soru",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Fotoğraf Ekle",
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sorunun Fotoğrafını Çek veya Galeriden Seç",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "(Otomatik olarak WebP formatına sıkıştırılır)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject Selector Dropdown
            Text("Ders Seçimi", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val selectedTabIndex = subjects.indexOf(selectedSubject).coerceAtLeast(0)
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 0.dp) {
                subjects.forEachIndexed { index, subject ->
                    Tab(
                        selected = selectedSubject == subject,
                        onClick = { selectedSubject = subject },
                        text = { Text(subject, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { if (it.length <= 50) topic = it },
                label = { Text("Konu (Örn: Türev, Üslü Sayılar)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 500) description = it },
                    label = { Text("Açıklama / Anlamadığın Kısım") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "${description.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp, end = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selection: "Yardım İstiyorum" vs "Taktik"
            Text("Paylaşım Modu", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedMode == "Yardım İstiyorum",
                    onClick = { selectedMode = "Yardım İstiyorum" },
                    label = { Text("🆘 Yardım İstiyorum") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMode == "Taktik / Soru Tipi",
                    onClick = { selectedMode = "Taktik / Soru Tipi" },
                    label = { Text("💡 Taktik Paylaşıyorum") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is AddQuestionUiState.Error) {
                Text(
                    text = (uiState as AddQuestionUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.uploadQuestion(
                        context = context,
                        authorId = currentUser.uid,
                        authorName = currentUser.displayName,
                        authorPhotoUrl = currentUser.photoUrl,
                        examType = currentUser.examType.ifEmpty { "TYT/AYT" },
                        subject = selectedSubject,
                        topic = topic,
                        description = description,
                        mode = selectedMode,
                        imageUri = selectedImageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = uiState !is AddQuestionUiState.Loading
            ) {
                if (uiState is AddQuestionUiState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Soruyu Paylaş", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
