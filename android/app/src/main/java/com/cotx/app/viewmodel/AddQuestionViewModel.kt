package com.cotx.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotx.app.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddQuestionUiState {
    object Idle : AddQuestionUiState
    object Loading : AddQuestionUiState
    object Success : AddQuestionUiState
    data class Error(val message: String) : AddQuestionUiState
}

class AddQuestionViewModel(
    private val repository: QuestionRepository = QuestionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddQuestionUiState>(AddQuestionUiState.Idle)
    val uiState: StateFlow<AddQuestionUiState> = _uiState.asStateFlow()

    fun uploadQuestion(
        context: Context,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        examType: String = "TYT/AYT",
        subject: String,
        topic: String,
        description: String,
        mode: String,
        imageUri: Uri?
    ) {
        if (imageUri == null) {
            _uiState.value = AddQuestionUiState.Error("Lütfen çözemediğiniz sorunun fotoğrafını ekleyin.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddQuestionUiState.Loading
            repository.postQuestion(
                context = context,
                authorId = authorId,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                examType = examType,
                subject = subject,
                topic = topic,
                description = description,
                mode = mode,
                imageUri = imageUri
            ).onSuccess {
                _uiState.value = AddQuestionUiState.Success
            }.onFailure { error ->
                _uiState.value = AddQuestionUiState.Error(error.localizedMessage ?: "Soru yüklenirken bir hata oluştu.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddQuestionUiState.Idle
    }
}
