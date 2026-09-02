package com.cotx.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotx.app.data.model.User
import com.cotx.app.data.repository.AuthRepository
import com.cotx.app.util.DebugErrorInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    /**
     * @param debugInfo GEÇİCİ: dolu olduğunda ekranda ham hata pop-up'ı (tam exception
     *   sınıf adı + mesaj + stack trace kopyalama) gösterilir. Yayından önce kaldırın.
     */
    data class Error(val message: String, val debugInfo: DebugErrorInfo? = null) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUserProfile().onSuccess { user ->
                _currentUser.value = user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Lütfen e-posta ve şifrenizi girin.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginUser(email, pass)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Giriş başarısız oldu.")
                }
        }
    }

    fun loginWithGoogle(idToken: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithGoogle(idToken)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                }
                .onFailure { error ->
                    val debugInfo = DebugErrorInfo.from(error, context)
                    Log.e("AuthViewModel", debugInfo.fullLogcatText)
                    _uiState.value = AuthUiState.Error(debugInfo.message, debugInfo)
                }
        }
    }

    fun register(
        email: String,
        pass: String,
        displayName: String,
        field: String,
        targetUniversity: String,
        targetMajor: String,
        gradeLevel: String
    ) {
        if (email.isBlank() || pass.isBlank() || displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("Lütfen tüm zorunlu alanları doldurun.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.registerUser(
                email = email,
                pass = pass,
                displayName = displayName,
                field = field,
                targetUniversity = targetUniversity,
                targetMajor = targetMajor,
                gradeLevel = gradeLevel
            ).onSuccess { user ->
                _currentUser.value = user
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Kayıt başarısız oldu.")
            }
        }
    }

    fun updateExamType(examType: String) {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.updateExamType(uid, examType).onSuccess {
                val updatedUser = _currentUser.value?.copy(examType = examType)
                _currentUser.value = updatedUser
                if (updatedUser != null) {
                    _uiState.value = AuthUiState.Success(updatedUser)
                }
            }
        }
    }

    fun updateProfile(
        context: android.content.Context,
        displayName: String,
        bio: String,
        field: String,
        examType: String,
        targetUniversity: String,
        targetMajor: String,
        visibleBadges: List<String> = emptyList(),
        imageUri: android.net.Uri? = null,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.updateUserProfile(
                context = context,
                uid = uid,
                displayName = displayName,
                bio = bio,
                field = field,
                examType = examType,
                targetUniversity = targetUniversity,
                targetMajor = targetMajor,
                visibleBadges = visibleBadges,
                imageUri = imageUri
            ).onSuccess { updatedUser ->
                _currentUser.value = updatedUser
                _uiState.value = AuthUiState.Success(updatedUser)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Profil güncellenemedi.")
                onError()
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.deleteUserAccount(uid).onSuccess {
                _currentUser.value = null
                _uiState.value = AuthUiState.Idle
                onSuccess()
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Hesap silinirken bir hata oluştu.")
            }
        }
    }

    fun setError(message: String, debugInfo: DebugErrorInfo? = null) {
        _uiState.value = AuthUiState.Error(message, debugInfo)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }
}
