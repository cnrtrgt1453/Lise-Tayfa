package com.cotx.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cotx.app.data.model.User
import com.cotx.app.data.repository.AuthRepository
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import com.cotx.app.util.BadgeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userId: String,
    onUpdateProfile: (
        displayName: String,
        bio: String,
        field: String,
        examType: String,
        targetSchool: String,
        targetDepartment: String,
        visibleBadges: List<String>,
        imageUri: Uri?,
        onFinished: () -> Unit
    ) -> Unit,
    onNavigateBack: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    var loadedUser by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        authRepository.getUserProfile(userId).onSuccess { fetchedUser ->
            loadedUser = fetchedUser
            isLoadingUser = false
        }.onFailure {
            isLoadingUser = false
        }
    }

    val user = loadedUser
    if (isLoadingUser || user == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profili Düzenle ✏️", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        }
        return
    }

    var displayName by remember { mutableStateOf(user.displayName) }
    var bio by remember { mutableStateOf(user.bio) }
    var selectedExamType by remember { mutableStateOf(user.examType.ifEmpty { "TYT/AYT" }) }
    var selectedField by remember { mutableStateOf(user.field.ifEmpty { "Sayısal" }) }
    var targetSchool by remember { mutableStateOf(user.targetUniversity) }
    var targetDepartment by remember { mutableStateOf(user.targetMajor) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val allEarnedBadges = remember(user) { BadgeHelper.getAllEarnedBadgeNames(user) }
    val visibleBadges = remember {
        mutableStateListOf<String>().apply {
            if (user.visibleBadges != null) {
                addAll(user.visibleBadges)
            } else {
                addAll(allEarnedBadges)
            }
        }
    }

    val fields = listOf("Sayısal", "Eşit Ağırlık", "Sözel", "Dil")
    val examTypes = listOf("TYT/AYT", "LGS")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle ✏️", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo Change Picker
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Yeni Fotoğraf",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = com.cotx.app.util.ImageModelResolver.resolve(user.photoUrl),
                        contentDescription = "Mevcut Fotoğraf",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Fotoğraf Yükle", tint = PrimaryPurple, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Fotoğrafı Değiştir", fontSize = 12.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            // Ad Soyad Input
            OutlinedTextField(
                value = displayName,
                onValueChange = { if (it.length <= 30) displayName = it },
                label = { Text("Ad Soyad") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hazırlanılan Sınav Türü Seçimi (TYT/AYT - LGS)
            Text(
                text = "Hazırlanılan Sınav",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (user.examType == "TYT/AYT") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryPurple.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎓 TYT / AYT", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("(LGS'ye geri dönüş yapılamaz)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    examTypes.forEach { type ->
                        FilterChip(
                            selected = selectedExamType == type,
                            onClick = { selectedExamType = type },
                            label = { Text(if (type == "TYT/AYT") "TYT / AYT'ye Yükselt 🚀" else "LGS", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alan Seçimi (Sayısal, Eşit Ağırlık, Sözel, Dil - YKS için)
            if (selectedExamType == "TYT/AYT") {
                Text(
                    text = "Alanınız (YKS)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fields) { fieldOption ->
                        FilterChip(
                            selected = selectedField == fieldOption,
                            onClick = { selectedField = fieldOption },
                            label = { Text(fieldOption, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Biyografi Input
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 150) bio = it },
                    label = { Text("Biyografi (Kısa tanıtım)") },
                    maxLines = 3,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${bio.length}/150",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp, end = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target School Input (Hedef Lise / Üniversite)
            OutlinedTextField(
                value = targetSchool,
                onValueChange = { if (it.length <= 50) targetSchool = it },
                label = { Text(if (selectedExamType == "LGS") "Hedef Lise (Örn: Kabataş Erkek Lisesi)" else "Hedef Üniversite (Örn: Dokuz Eylül Üniversitesi)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Department Input (Bölüm - TYT/AYT için)
            if (selectedExamType != "LGS") {
                OutlinedTextField(
                    value = targetDepartment,
                    onValueChange = { if (it.length <= 50) targetDepartment = it },
                    label = { Text("Hedef Bölüm (Örn: Bilgisayar Mühendisliği)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Rozet Görünürlük Seçimi
            if (allEarnedBadges.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Profilde Gösterilecek Rozetler 🏅",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        )
                        Text(
                            "Profilinde diğer öğrencilere görünmesini istediğin rozetleri seç:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        allEarnedBadges.forEach { badgeName ->
                            val isChecked = visibleBadges.contains(badgeName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) visibleBadges.remove(badgeName)
                                        else visibleBadges.add(badgeName)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(badgeName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) visibleBadges.add(badgeName)
                                        else visibleBadges.remove(badgeName)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action Buttons
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val finalField = if (selectedExamType == "LGS") "LGS Tayfası" else selectedField
                        onUpdateProfile(
                            displayName,
                            bio,
                            finalField,
                            selectedExamType,
                            targetSchool,
                            targetDepartment,
                            visibleBadges.toList(),
                            selectedImageUri
                        ) {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Değişiklikleri Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Vazgeç", fontSize = 16.sp)
            }
        }
    }
}
