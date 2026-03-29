package com.gymtrack.app.presentation.screens.community

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtrack.app.data.remote.*
import com.gymtrack.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel(),
    selectedTab: Int = 4,
    onTabSelected: (Int) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreatePost by remember { mutableStateOf(false) }
    var showSignIn by remember { mutableStateOf(false) }
    var showSignUp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Community", style = MaterialTheme.typography.titleLarge, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
                    }
                },
                actions = {
                    if (uiState.isLoggedIn) {
                        IconButton(onClick = { showCreatePost = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Post", tint = NeonCyan)
                        }
                        IconButton(onClick = { viewModel.signOut() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = TextSecondaryDark)
                        }
                    } else {
                        TextButton(onClick = { showSignIn = true }) {
                            Text("Sign In", color = NeonCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (!uiState.isLoggedIn) {
                NotLoggedInState(onSignIn = { showSignIn = true }, onSignUp = { showSignUp = true })
            } else {
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = DarkBackground,
                    contentColor = NeonCyan,
                    edgePadding = 16.dp
                ) {
                    CommunityTab.entries.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.updateTab(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    color = if (uiState.selectedTab == tab) NeonCyan else TextSecondaryDark
                                )
                            }
                        )
                    }
                }

                when (uiState.selectedTab) {
                    CommunityTab.FEED -> FeedContent(
                        posts = uiState.posts,
                        currentUserId = uiState.currentUserId,
                        onLike = { viewModel.likePost(it) }
                    )
                    CommunityTab.ACHIEVEMENTS -> AchievementsContent(achievements = uiState.userAchievements)
                    CommunityTab.CHALLENGES -> ChallengesContent(
                        challenges = uiState.challenges,
                        currentUserId = uiState.currentUserId,
                        onJoin = { viewModel.joinChallenge(it) }
                    )
                    CommunityTab.LEADERBOARD -> LeaderboardContent(
                        entries = uiState.leaderboard,
                        currentCategory = uiState.leaderboardCategory,
                        onCategoryChange = { viewModel.updateLeaderboardCategory(it) }
                    )
                }
            }
        }
    }

    if (showSignIn) {
        AuthDialog(
            isLogin = true,
            onDismiss = { showSignIn = false },
            onLogin = { email, password -> viewModel.signIn(email, password); showSignIn = false },
            onSwitchMode = { showSignIn = false; showSignUp = true }
        )
    }

    if (showSignUp) {
        AuthDialog(
            isLogin = false,
            onDismiss = { showSignUp = false },
            onSignUp = { email, password, name -> viewModel.signUp(email, password, name); showSignUp = false },
            onSwitchMode = { showSignUp = false; showSignIn = true }
        )
    }

    if (showCreatePost) {
        CreatePostDialog(
            onDismiss = { showCreatePost = false },
            onPost = { content ->
                viewModel.createPost(content)
                showCreatePost = false
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
private fun NotLoggedInState(onSignIn: () -> Unit, onSignUp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.People, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(50.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Join the Community", style = MaterialTheme.typography.headlineSmall, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Connect with athletes worldwide", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold, color = DarkBackground)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NeonCyan)
        ) {
            Text("Sign In", color = NeonCyan)
        }
    }
}

@Composable
private fun FeedContent(
    posts: List<FirebasePost>,
    currentUserId: String?,
    onLike: (String) -> Unit
) {
    if (posts.isEmpty()) {
        EmptyState("No Posts Yet", "Be the first to share something!")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts) { post ->
                PostCard(post = post, currentUserId = currentUserId, onLike = onLike)
            }
        }
    }
}

@Composable
private fun PostCard(post: FirebasePost, currentUserId: String?, onLike: (String) -> Unit) {
    val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(ElectricBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.authorName.take(1).uppercase(), color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                    Text(getTimeAgo(post.createdAt), color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
                if (post.postType != "GENERAL") {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(post.postType, style = MaterialTheme.typography.labelSmall, color = NeonCyan) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ElectricPurple.copy(alpha = 0.2f))
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(post.content, color = TextPrimaryDark)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike(post.postId) }
                ) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) ErrorRed else TextSecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${post.likesCount}", color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${post.commentsCount}", color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun AchievementsContent(achievements: List<FirebaseAchievement>) {
    if (achievements.isEmpty()) {
        EmptyState("No Achievements", "Complete workouts to earn badges!")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: FirebaseAchievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) ElectricBlue.copy(alpha = 0.1f) else DarkCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (achievement.iconName) {
                    "trophy" -> Icons.Default.EmojiEvents
                    "fire" -> Icons.Default.LocalFireDepartment
                    "star" -> Icons.Default.Star
                    else -> Icons.Default.FitnessCenter
                },
                contentDescription = null,
                tint = if (achievement.isUnlocked) NeonCyan else TextMuted,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.name,
                    color = if (achievement.isUnlocked) TextPrimaryDark else TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(achievement.description, color = TextSecondaryDark, style = MaterialTheme.typography.bodySmall)
            }
            if (achievement.isUnlocked) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
            }
        }
    }
}

@Composable
private fun ChallengesContent(
    challenges: List<FirebaseChallenge>,
    currentUserId: String?,
    onJoin: (String) -> Unit
) {
    if (challenges.isEmpty()) {
        EmptyState("No Active Challenges", "Challenges will appear here")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    isJoined = currentUserId != null && challenge.participantIds.contains(currentUserId),
                    onJoin = { onJoin(challenge.challengeId) }
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: FirebaseChallenge, isJoined: Boolean, onJoin: () -> Unit) {
    val daysLeft = ((challenge.endDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(challenge.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                SuggestionChip(
                    onClick = {},
                    label = { Text("${daysLeft}d left", style = MaterialTheme.typography.labelSmall, color = EnergeticOrange) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = EnergeticOrange.copy(alpha = 0.2f))
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(challenge.description, color = TextSecondaryDark, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${challenge.participantIds.size}", color = TextSecondaryDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = EnergeticOrange, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${challenge.xpReward} XP", color = TextSecondaryDark)
                    }
                }
                if (isJoined) {
                    Text("Joined", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                } else {
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Join", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardContent(
    entries: List<FirebaseLeaderboardEntry>,
    currentCategory: LeaderboardCategory,
    onCategoryChange: (LeaderboardCategory) -> Unit
) {
    Column {
        ScrollableTabRow(
            selectedTabIndex = LeaderboardCategory.entries.indexOf(currentCategory),
            containerColor = DarkBackground,
            contentColor = NeonCyan,
            edgePadding = 16.dp
        ) {
            LeaderboardCategory.entries.forEach { category ->
                Tab(
                    selected = currentCategory == category,
                    onClick = { onCategoryChange(category) },
                    text = {
                        Text(
                            category.label,
                            color = if (currentCategory == category) NeonCyan else TextSecondaryDark
                        )
                    }
                )
            }
        }

        if (entries.isEmpty()) {
            EmptyState("Leaderboard Empty", "Complete workouts to rank up!")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(entries) { index, entry ->
                    LeaderboardRow(entry = entry, rank = index + 1)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: FirebaseLeaderboardEntry, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> EnergeticOrange.copy(alpha = 0.15f)
                2 -> ElectricBlue.copy(alpha = 0.1f)
                3 -> ElectricPurple.copy(alpha = 0.1f)
                else -> DarkCard
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                color = when (rank) {
                    1 -> EnergeticOrange
                    2 -> ElectricBlue
                    3 -> ElectricPurple
                    else -> TextMuted
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier.size(40.dp).background(ElectricBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.userName.take(1).uppercase(), color = NeonCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.userName, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "${entry.score}",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = ElectricBlue.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun AuthDialog(
    isLogin: Boolean,
    onDismiss: () -> Unit,
    onLogin: ((String, String) -> Unit)? = null,
    onSignUp: ((String, String, String) -> Unit)? = null,
    onSwitchMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isLogin) "Sign In" else "Create Account", color = TextPrimaryDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isLogin) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = TextSecondaryDark,
                            cursorColor = NeonCyan
                        )
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondaryDark,
                        cursorColor = NeonCyan
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondaryDark,
                        cursorColor = NeonCyan
                    )
                )
                TextButton(onClick = onSwitchMode) {
                    Text(
                        if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                        color = NeonCyan
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isLogin) {
                        onLogin?.invoke(email, password)
                    } else {
                        onSignUp?.invoke(email, password, name)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(if (isLogin) "Sign In" else "Sign Up", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun CreatePostDialog(onDismiss: () -> Unit, onPost: (String) -> Unit) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Post", color = TextPrimaryDark) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkCard,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedLabelColor = NeonCyan,
                    unfocusedLabelColor = TextSecondaryDark,
                    cursorColor = NeonCyan
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (content.isNotBlank()) onPost(content) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Post", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
        },
        containerColor = DarkSurface
    )
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
