package com.cotx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cotx.app.data.model.Question
import com.cotx.app.data.repository.AuthRepository
import com.cotx.app.data.repository.QuestionRepository
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.util.ExamSubjectHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FeedTab {
    EXPLORE,
    FOLLOWING
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(val questions: List<Question>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val questionRepository: QuestionRepository = QuestionRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(FeedTab.EXPLORE)
    val selectedTab: StateFlow<FeedTab> = _selectedTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Tümü")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _subjects = MutableStateFlow(ExamSubjectHelper.getFeedSubjectsForExam("TYT/AYT"))
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val _followingUserIds = MutableStateFlow<List<String>>(emptyList())
    val followingUserIds: StateFlow<List<String>> = _followingUserIds.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<List<String>>(emptyList())

    private var currentExamType: String? = null

    init {
        loadFeed()
    }

    fun selectTab(tab: FeedTab) {
        _selectedTab.value = tab
        loadFeed()
    }

    fun updateFollowingList(following: List<String>) {
        _followingUserIds.value = following
        if (_selectedTab.value == FeedTab.FOLLOWING) {
            loadFeed()
        }
    }

    fun updateBlockedList(blocked: List<String>) {
        if (_blockedUserIds.value != blocked) {
            _blockedUserIds.value = blocked
            loadFeed()
        }
    }

    fun updateSubjectsForExam(examType: String?) {
        currentExamType = examType
        val newSubjects = ExamSubjectHelper.getFeedSubjectsForExam(examType)
        _subjects.value = newSubjects
        if (!_selectedSubject.value.isNullOrEmpty() && !newSubjects.contains(_selectedSubject.value)) {
            _selectedSubject.value = "Tümü"
        }
        loadFeed()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        loadFeed()
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            val isFollowingOnly = _selectedTab.value == FeedTab.FOLLOWING
            val sortAscending = _sortOrder.value == SortOrder.OLDEST_FIRST

            questionRepository.getFeedQuestions(
                selectedSubject = _selectedSubject.value,
                examType = currentExamType,
                isFollowingOnly = isFollowingOnly,
                followingUserIds = _followingUserIds.value,
                blockedUserIds = _blockedUserIds.value,
                sortAscending = sortAscending
            )
                .onSuccess { list ->
                    _uiState.value = FeedUiState.Success(list)
                }
                .onFailure { error ->
                    _uiState.value = FeedUiState.Error(error.localizedMessage ?: "Sorular yüklenemedi.")
                }
        }
    }

    fun toggleLike(questionId: String, currentUser: UserSummary, isLiked: Boolean) {
        viewModelScope.launch {
            questionRepository.toggleLikeQuestion(
                questionId = questionId,
                userId = currentUser.id,
                userName = currentUser.displayName,
                userPhotoUrl = currentUser.avatarUrl ?: "",
                isLiked = isLiked
            ).onSuccess {
                loadFeed()
            }
        }
    }

    fun toggleFollowUser(
        currentUserId: String,
        targetUserId: String,
        isCurrentlyFollowing: Boolean,
        currentUserName: String = "",
        currentUserPhotoUrl: String = "",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (isCurrentlyFollowing) {
                authRepository.unfollowUser(currentUserId, targetUserId).onSuccess {
                    _followingUserIds.value = _followingUserIds.value - targetUserId
                    onDone()
                    loadFeed()
                }
            } else {
                authRepository.followUser(
                    currentUserId = currentUserId,
                    targetUserId = targetUserId,
                    currentUserName = currentUserName,
                    currentUserPhotoUrl = currentUserPhotoUrl
                ).onSuccess {
                    _followingUserIds.value = _followingUserIds.value + targetUserId
                    onDone()
                    loadFeed()
                }
            }
        }
    }

    fun deleteQuestion(questionId: String, currentUserId: String) {
        viewModelScope.launch {
            questionRepository.deleteQuestion(questionId, currentUserId).onSuccess {
                loadFeed()
            }
        }
    }
}
