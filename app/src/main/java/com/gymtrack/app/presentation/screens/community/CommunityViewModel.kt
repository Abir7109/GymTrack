package com.gymtrack.app.presentation.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtrack.app.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val selectedTab: CommunityTab = CommunityTab.FEED,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
    val posts: List<FirebasePost> = emptyList(),
    val userAchievements: List<FirebaseAchievement> = emptyList(),
    val challenges: List<FirebaseChallenge> = emptyList(),
    val leaderboard: List<FirebaseLeaderboardEntry> = emptyList(),
    val leaderboardCategory: LeaderboardCategory = LeaderboardCategory.WORKOUTS,
    val error: String? = null,
    val showAuthDialog: Boolean = false,
    val authMode: AuthMode = AuthMode.LOGIN
)

enum class CommunityTab(val label: String) {
    FEED("Feed"),
    ACHIEVEMENTS("Achievements"),
    CHALLENGES("Challenges"),
    LEADERBOARD("Leaderboard")
}

enum class LeaderboardCategory(val label: String) {
    WORKOUTS("Workouts"),
    STREAK("Streak"),
    MINUTES("Minutes")
}

enum class AuthMode {
    LOGIN,
    SIGNUP
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authService.authState.collect { authState ->
                _uiState.update {
                    it.copy(
                        isLoggedIn = authState.isLoggedIn,
                        currentUserId = authState.userId
                    )
                }
                if (authState.isLoggedIn && authState.userId != null) {
                    loadData(authState.userId)
                }
            }
        }
    }

    private fun loadData(userId: String) {
        loadPosts()
        loadUserAchievements(userId)
        loadChallenges()
        loadLeaderboard(_uiState.value.leaderboardCategory)
    }

    fun updateTab(tab: CommunityTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateLeaderboardCategory(category: LeaderboardCategory) {
        _uiState.update { it.copy(leaderboardCategory = category) }
        loadLeaderboard(category)
    }

    fun showAuthDialog(mode: AuthMode = AuthMode.LOGIN) {
        _uiState.update { it.copy(showAuthDialog = true, authMode = mode) }
    }

    fun hideAuthDialog() {
        _uiState.update { it.copy(showAuthDialog = false) }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authService.signUpWithEmail(email, password, displayName)
                .onSuccess { user ->
                    val profile = FirebaseUserProfile(
                        userId = user.uid,
                        displayName = displayName,
                        email = email
                    )
                    firestoreService.createUserProfile(profile)
                    _uiState.update { it.copy(isLoading = false, showAuthDialog = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authService.signInWithEmail(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, showAuthDialog = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _uiState.update {
                it.copy(
                    posts = emptyList(),
                    userAchievements = emptyList(),
                    challenges = emptyList(),
                    leaderboard = emptyList()
                )
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            firestoreService.getAllPosts().collect { posts ->
                val userId = _uiState.value.currentUserId
                val postsWithLikeStatus = posts.map { post ->
                    post.copy(likedBy = post.likedBy)
                }
                _uiState.update { it.copy(posts = postsWithLikeStatus) }
            }
        }
    }

    fun createPost(content: String, postType: String = "GENERAL") {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            val post = FirebasePost(
                authorId = userId,
                authorName = authService.authState.firstOrNull()?.displayName ?: "User",
                content = content,
                postType = postType,
                createdAt = System.currentTimeMillis()
            )
            firestoreService.createPost(post)
        }
    }

    fun likePost(postId: String) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            val post = _uiState.value.posts.find { it.postId == postId }
            if (post != null && post.likedBy.contains(userId)) {
                firestoreService.unlikePost(postId, userId)
            } else {
                firestoreService.likePost(postId, userId)
            }
        }
    }

    private fun loadUserAchievements(userId: String) {
        viewModelScope.launch {
            firestoreService.getUserAchievements(userId).collect { achievements ->
                _uiState.update { it.copy(userAchievements = achievements) }
            }
        }
    }

    private fun loadChallenges() {
        viewModelScope.launch {
            firestoreService.getActiveChallenges().collect { challenges ->
                _uiState.update { it.copy(challenges = challenges) }
            }
        }
    }

    fun joinChallenge(challengeId: String) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            firestoreService.joinChallenge(challengeId, userId)
        }
    }

    private fun loadLeaderboard(category: LeaderboardCategory) {
        val fbCategory = when (category) {
            LeaderboardCategory.WORKOUTS -> "WEEKLY_WORKOUTS"
            LeaderboardCategory.STREAK -> "TOTAL_STREAK"
            LeaderboardCategory.MINUTES -> "MONTHLY_MINUTES"
        }
        viewModelScope.launch {
            firestoreService.getLeaderboard(fbCategory, "ALL_TIME").collect { entries ->
                _uiState.update { it.copy(leaderboard = entries) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
