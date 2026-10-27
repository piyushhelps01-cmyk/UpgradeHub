package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.MemeHubItem
import java.util.Locale
import kotlinx.coroutines.delay

enum class MemesDisplayMode {
    GALLERY_GRID,
    STREAM_FEED
}

@Composable
fun MemesHubScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val memes by viewModel.memesHubPosts.collectAsState()
    val infinitePageCount by viewModel.infiniteMemesPage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var displayMode by remember { mutableStateOf(MemesDisplayMode.GALLERY_GRID) }
    var selectedMemeForDetail by remember { mutableStateOf<MemeHubItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var expandedCommentMemeId by remember { mutableStateOf<String?>(null) }
    var commentDraft by remember { mutableStateOf("") }
    var isPostComposerOpen by remember { mutableStateOf(false) }

    val defaultStudentHandle = remember(currentUser) {
        val base = currentUser?.fullName
            ?.trim()
            ?.lowercase()
            ?.replace(Regex("[^a-z0-9]+"), "_")
            ?.trim('_')
            .orEmpty()
        if (base.isNotBlank()) "@$base" else "@student_aspirant"
    }

    val shareMeme: (MemeHubItem) -> Unit = { meme ->
        val shareText = buildString {
            appendLine("😂 UpgradeHub Memes Hub (${meme.handle} • ${meme.examTag})")
            appendLine()
            if (!meme.quoteCanvasText.isNullOrBlank()) {
                appendLine("“${meme.quoteCanvasText}”")
                appendLine()
            }
            appendLine("📌 ${meme.topSetupText}")
            appendLine("💥 ${meme.bottomPunchlineText}")
            appendLine()
            appendLine("💡 Takeaway: ${meme.conceptTakeaway}")
            appendLine("— Shared via UpgradeHub Memes Hub")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share with Batch"))
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val categories = listOf(
        "All",
        "Saved 🔖",
        "Physics ⚡",
        "Chemistry 🧪",
        "Maths 📐",
        "Formula Meme 💡",
        "Prep Excuse 🤡",
        "Daily Problem 😭",
        "Mock Logic 🏆"
    )

    val filteredMemes = remember(memes, searchQuery, selectedCategoryFilter) {
        memes.filter { meme ->
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Saved 🔖" -> meme.isBookmarked
                "Physics ⚡" -> meme.subjectForFlashcard.contains("Physics", ignoreCase = true) ||
                    meme.examTag.contains("Physics", ignoreCase = true) ||
                    meme.category.contains("Physics", ignoreCase = true)
                "Chemistry 🧪" -> meme.subjectForFlashcard.contains("Chemistry", ignoreCase = true) ||
                    meme.examTag.contains("Chemistry", ignoreCase = true) ||
                    meme.category.contains("Chemistry", ignoreCase = true)
                "Maths 📐" -> meme.subjectForFlashcard.contains("Math", ignoreCase = true) ||
                    meme.examTag.contains("Math", ignoreCase = true) ||
                    meme.category.contains("Math", ignoreCase = true)
                "Formula Meme 💡" -> meme.category.contains("Formula", ignoreCase = true) ||
                    meme.postType == "FORMULA_MEME"
                "Mock Logic 🏆" -> meme.category.contains("Mock", ignoreCase = true) ||
                    meme.topSetupText.contains("Mock", ignoreCase = true)
                else -> meme.category.equals(selectedCategoryFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                meme.topSetupText.contains(searchQuery, ignoreCase = true) ||
                meme.bottomPunchlineText.contains(searchQuery, ignoreCase = true) ||
                meme.handle.contains(searchQuery, ignoreCase = true) ||
                meme.examTag.contains(searchQuery, ignoreCase = true) ||
                meme.conceptTakeaway.contains(searchQuery, ignoreCase = true) ||
                (meme.quoteCanvasText?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    // Automatically load next batch when student scrolls near the bottom of the Infinite Scroll feed
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 4 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && searchQuery.isBlank() && selectedCategoryFilter == "All") {
            delay(300L)
            viewModel.loadNextInfiniteMemesBatch()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("memes_hub_screen")
    ) {
        // 1. Top Header Bar: Memes Hub Title + View Mode Switcher + Single "Post" Action Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0066FF).copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.35f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEmotions,
                                contentDescription = "Memes Hub",
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Memes Hub",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0066FF).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (displayMode == MemesDisplayMode.GALLERY_GRID) "GALLERY GRID" else "FEED STREAM",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0066FF),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Study Motivation & Humour • Relatable JEE/NEET Memes",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // View Mode Toggle (Gallery Grid vs Stream Feed)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        // Gallery Grid Button
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (displayMode == MemesDisplayMode.GALLERY_GRID) Color(0xFF0066FF) else Color.Transparent,
                            modifier = Modifier
                                .clickable { displayMode = MemesDisplayMode.GALLERY_GRID }
                                .testTag("memes_view_mode_grid")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Gallery Grid",
                                    tint = if (displayMode == MemesDisplayMode.GALLERY_GRID) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Gallery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (displayMode == MemesDisplayMode.GALLERY_GRID) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Feed Stream Button
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (displayMode == MemesDisplayMode.STREAM_FEED) Color(0xFF0066FF) else Color.Transparent,
                            modifier = Modifier
                                .clickable { displayMode = MemesDisplayMode.STREAM_FEED }
                                .testTag("memes_view_mode_feed")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "Feed Stream",
                                    tint = if (displayMode == MemesDisplayMode.STREAM_FEED) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Feed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (displayMode == MemesDisplayMode.STREAM_FEED) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Post Button so any student can share memes directly
                Button(
                    onClick = { isPostComposerOpen = !isPostComposerOpen },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPostComposerOpen) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF0066FF),
                        contentColor = if (isPostComposerOpen) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.testTag("post_excuse_or_problem_button")
                ) {
                    Icon(
                        imageVector = if (isPostComposerOpen) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isPostComposerOpen) "Close Post Composer" else "Create Post",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPostComposerOpen) "Close" else "Post",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. Persistent Search Bar & Category Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick "Write a Post..." Bar if composer is closed
            if (!isPostComposerOpen) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPostComposerOpen = true }
                        .testTag("quick_student_post_prompt_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0066FF).copy(alpha = 0.12f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF0066FF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share a study meme, formula trap or prep struggle with the batch...",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0066FF)
                        ) {
                            Text(
                                text = "+ Post",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Post Composer Card (when active)
            if (isPostComposerOpen) {
                StudentPostComposerCard(
                    defaultHandle = defaultStudentHandle,
                    onPublishPost = { handle, postType, flair, headline, body, quoteText, cardStyle, illustrationKey, tone, takeaway ->
                        viewModel.publishStudentProblemOrExcuse(
                            handle = handle,
                            postType = postType,
                            flairTag = flair,
                            headline = headline,
                            bodyText = body,
                            quoteBoxText = quoteText,
                            cardVisualStyle = cardStyle,
                            illustrationKey = illustrationKey,
                            quoteTone = tone,
                            solutionOrTakeaway = takeaway
                        )
                        isPostComposerOpen = false
                        selectedCategoryFilter = "All"
                    },
                    onDismiss = { isPostComposerOpen = false }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search @handle, thermodynamics, organic, backlog, 3 AM...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Memes",
                        tint = Color(0xFF0066FF)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memes_search_input")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0066FF),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag(
                            "meme_filter_${category.lowercase().replace(" ", "_")}"
                        )
                    )
                }
            }

            // 3. Study Motivation Banner to maintain focus during preparation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0066FF).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.22f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memes_motivation_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Study Motivation & Brain Reset",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0066FF)
                            )
                            Text(
                                text = "A 2-min laugh cuts cortisol. Read the formula takeaway and get right back to the grind!",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0066FF)
                    ) {
                        Text(
                            text = "${filteredMemes.size} Memes",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 4. MAIN CONTENT AREA: GALLERY GRID VIEW vs STREAM FEED
        if (displayMode == MemesDisplayMode.GALLERY_GRID) {
            // =========================================================================
            // GALLERY VIEW FOR THE MEMES USING A GRID LAYOUT
            // =========================================================================
            if (filteredMemes.isEmpty()) {
                EmptyMemesGalleryState(
                    searchQuery = searchQuery,
                    onClearSearch = { searchQuery = "" },
                    onCreatePost = { isPostComposerOpen = true }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("memes_gallery_grid"),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredMemes,
                        key = { it.id }
                    ) { meme ->
                        GalleryMemeGridCard(
                            meme = meme,
                            onClick = { selectedMemeForDetail = meme },
                            onShare = { shareMeme(meme) },
                            onUpvote = { viewModel.toggleMemeUpvote(meme.id) },
                            onUsBhaiUs = { viewModel.toggleUsBhaiUs(meme.id) },
                            onBookmark = { viewModel.toggleMemeBookmark(meme.id) }
                        )
                    }

                    // Bottom info banner in the grid
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 Tap any meme for full explanation & study takeaway",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Switch to Feed ↗",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0066FF),
                                    modifier = Modifier.clickable { displayMode = MemesDisplayMode.STREAM_FEED }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // STREAM FEED (Original Infinite Scroll LazyColumn)
            // =========================================================================
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("infinite_prep_feed_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // The 4 Illustrated Preparation Stage Cards Integrated in the Stream
                if (searchQuery.isBlank() && selectedCategoryFilter == "All") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StoryCard01DistractionBattle()
                            StoryCard02TheDelusion3Am()
                            StoryCard03TheOverwhelm()
                            StoryCard04TheDenialProductiveRest()
                        }
                    }
                }

                // Stream of Student Posts, Memes & Excuses
                itemsIndexed(
                    items = filteredMemes,
                    key = { index, item -> "${item.id}_$index" }
                ) { index, meme ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StudentFeedPostCard(
                            meme = meme,
                            isCommentDrawerOpen = expandedCommentMemeId == meme.id,
                            onToggleCommentDrawer = {
                                expandedCommentMemeId = if (expandedCommentMemeId == meme.id) null else meme.id
                            },
                            commentDraft = if (expandedCommentMemeId == meme.id) commentDraft else "",
                            onCommentDraftChange = { commentDraft = it },
                            onSubmitComment = {
                                viewModel.addMemeComment(meme.id, commentDraft)
                                commentDraft = ""
                            },
                            onUpvote = { viewModel.toggleMemeUpvote(meme.id) },
                            onUsBhaiUs = { viewModel.toggleUsBhaiUs(meme.id) },
                            onBookmark = { viewModel.toggleMemeBookmark(meme.id) },
                            onSaveToFlashcards = {
                                viewModel.addFlashcard(
                                    subject = meme.subjectForFlashcard,
                                    chapter = meme.chapterForFlashcard,
                                    question = meme.topSetupText,
                                    answer = "${meme.bottomPunchlineText}\n\n💡 Takeaway: ${meme.conceptTakeaway}",
                                    difficulty = "Formula Meme"
                                )
                            },
                            onShare = { shareMeme(meme) }
                        )

                        // Show Plot Twist Reality-Check Card periodically in the stream
                        if ((index + 1) % 6 == 0 || index == 8) {
                            PlotTwistMockReminderCard(
                                scrollCount = index + 1,
                                onStartMockTest = { viewModel.setPage(JeePage.TESTS) },
                                onStartFocusSession = { viewModel.setPage(JeePage.STREAK) }
                            )
                        }
                    }
                }

                // Infinite Scroll Loader Footer
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0066FF).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadNextInfiniteMemesBatch() }
                            .testTag("infinite_scroll_load_more_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "∞ INFINITE SCROLL ACTIVE • WAVE #$infinitePageCount",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0066FF),
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Scroll down to automatically load more student posts & memes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0066FF)
                            ) {
                                Text(
                                    text = "Load +4 ↓",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Interactive Full-Screen / Modal Meme Motivation Viewer Dialog
    if (selectedMemeForDetail != null) {
        val activeMeme = selectedMemeForDetail!!
        MemeDetailMotivationDialog(
            meme = activeMeme,
            onDismiss = { selectedMemeForDetail = null },
            onShare = { shareMeme(activeMeme) },
            onUpvote = { viewModel.toggleMemeUpvote(activeMeme.id) },
            onUsBhaiUs = { viewModel.toggleUsBhaiUs(activeMeme.id) },
            onBookmark = { viewModel.toggleMemeBookmark(activeMeme.id) },
            onAddComment = { comment -> viewModel.addMemeComment(activeMeme.id, comment) },
            onStartFocus = {
                selectedMemeForDetail = null
                viewModel.setPage(JeePage.STREAK)
            }
        )
    }
}

/**
 * Aesthetic Gallery Meme Card for the Grid Layout.
 * Features vibrant colors, relatable quote canvas, punchy concept takeaway, upvotes, and 1-tap share.
 */
@Composable
private fun GalleryMemeGridCard(
    meme: MemeHubItem,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onUpvote: () -> Unit,
    onUsBhaiUs: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgToneColor = when (meme.quoteCanvasTone.uppercase()) {
        "ROSE" -> Color(0xFFFFF1F2)
        "MINT" -> Color(0xFFF0FDF4)
        "SKY" -> Color(0xFFF0F9FF)
        "CREAM" -> Color(0xFFFFFBEB)
        else -> Color(0xFFF8FAFC)
    }
    val borderToneColor = when (meme.quoteCanvasTone.uppercase()) {
        "ROSE" -> Color(0xFFFDA4AF)
        "MINT" -> Color(0xFF86EFAC)
        "SKY" -> Color(0xFF7DD3FC)
        "CREAM" -> Color(0xFFFDE68A)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, borderToneColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("meme_grid_card_${meme.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Top Bar: Category Pill & Bookmark Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0066FF).copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = meme.examTag.ifBlank { meme.category },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0066FF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("bookmark_meme_${meme.id}")
                ) {
                    Icon(
                        imageVector = if (meme.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (meme.isBookmarked) "Saved to Vault" else "Save to Vault",
                        tint = if (meme.isBookmarked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Visual Meme Canvas Preview (Mini Card)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bgToneColor,
                border = BorderStroke(1.dp, borderToneColor.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = meme.emojiBanner.ifBlank { "😂" },
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!meme.quoteCanvasText.isNullOrBlank()) {
                                meme.quoteCanvasText
                            } else {
                                meme.bottomPunchlineText
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Relatable Setup Text Snippet
            Text(
                text = meme.topSetupText,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.5.sp
            )

            // 💡 Study Motivation / Concept Takeaway Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0066FF).copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF0066FF),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = meme.conceptTakeaway,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0066FF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.5.sp
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.8.dp
            )

            // Action Row: Likes, Us Bhai Us, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote Button
                    Row(
                        modifier = Modifier
                            .clickable { onUpvote() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (meme.isLiked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${meme.likesCount}",
                            fontSize = 9.5.sp,
                            fontWeight = if (meme.isLiked) FontWeight.Bold else FontWeight.Normal,
                            color = if (meme.isLiked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Us Bhai Us Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (meme.isUsBhaiUs) Color(0xFFFF6D00).copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.clickable { onUsBhaiUs() }
                    ) {
                        Text(
                            text = "🤝 ${meme.usBhaiUsCount}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (meme.isUsBhaiUs) Color(0xFFFF6D00) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // 1-Tap Share Button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("share_meme_grid_${meme.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Meme",
                        tint = Color(0xFF0066FF),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-Screen / Modal Viewer for any meme selected from the Gallery Grid or Feed.
 * Displays high-contrast visual artwork, study motivation takeaway, sharing CTAs, and student discussions.
 */
@Composable
private fun MemeDetailMotivationDialog(
    meme: MemeHubItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onUpvote: () -> Unit,
    onUsBhaiUs: () -> Unit,
    onBookmark: () -> Unit,
    onAddComment: (String) -> Unit,
    onStartFocus: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    val bgToneColor = when (meme.quoteCanvasTone.uppercase()) {
        "ROSE" -> Color(0xFFFFF1F2)
        "MINT" -> Color(0xFFF0FDF4)
        "SKY" -> Color(0xFFF0F9FF)
        "CREAM" -> Color(0xFFFFFBEB)
        else -> Color(0xFFF8FAFC)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("meme_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Author & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(meme.avatarColorHex),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = meme.handle.take(2).uppercase().replace("@", ""),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = meme.handle,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${meme.examTag} • ${meme.timeAgo}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_meme_detail_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Visual Meme Canvas
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgToneColor,
                    border = BorderStroke(1.2.dp, Color(0xFF0066FF).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = meme.emojiBanner,
                            fontSize = 26.sp
                        )
                        if (!meme.quoteCanvasText.isNullOrBlank()) {
                            Text(
                                text = "“${meme.quoteCanvasText}”",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = meme.topSetupText,
                            fontSize = 12.5.sp,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = meme.bottomPunchlineText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0066FF),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 💡 Study Motivation & High-Yield Concept Takeaway Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0066FF).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STUDY MOTIVATION & EXAM TAKEAWAY",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0066FF),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = meme.conceptTakeaway,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (meme.subjectForFlashcard.isNotBlank() && meme.chapterForFlashcard.isNotBlank()) {
                            Text(
                                text = "📌 ${meme.subjectForFlashcard} • ${meme.chapterForFlashcard}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Primary & Secondary Action CTAs (Share with Friends & Start Study Block)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 46.dp)
                            .testTag("share_meme_primary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Meme with Study Group",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onStartFocus,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.2.dp, Color(0xFFFF6D00)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 46.dp)
                            .testTag("start_focus_from_meme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Laugh's Over! Start 25m Focus Block 🎯",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6D00)
                        )
                    }
                }

                // Social Engagement Counter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onUpvote,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (meme.isLiked) Color(0xFF0066FF).copy(alpha = 0.12f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (meme.isLiked) Color(0xFF0066FF) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = if (meme.isLiked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${meme.likesCount} Upvotes",
                            fontSize = 11.sp,
                            color = if (meme.isLiked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onUsBhaiUs,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (meme.isUsBhaiUs) Color(0xFFFF6D00).copy(alpha = 0.12f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (meme.isUsBhaiUs) Color(0xFFFF6D00) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            text = "🤝 Resonate (${meme.usBhaiUsCount})",
                            fontSize = 11.sp,
                            color = if (meme.isUsBhaiUs) Color(0xFFFF6D00) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onBookmark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (meme.isBookmarked) Color(0xFF0066FF) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (meme.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (meme.isBookmarked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (meme.isBookmarked) "Saved" else "Save",
                            fontSize = 11.sp,
                            color = if (meme.isBookmarked) Color(0xFF0066FF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Comments Section
                Text(
                    text = "Discussion & Student Reactions (${meme.comments.size})",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (meme.comments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        meme.comments.forEach { comment ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = comment,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Add Comment Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add relatable comment...", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("meme_detail_comment_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("submit_meme_comment_button")
                    ) {
                        Text("Post", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Empty State for the Memes Gallery Grid when no search results match.
 */
@Composable
private fun EmptyMemesGalleryState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    onCreatePost: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🔍", fontSize = 32.sp)
            Text(
                text = if (searchQuery.isNotBlank()) "No memes found for \"$searchQuery\"" else "No memes in this category yet",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Be the first to share a relatable study meme or formula joke with the batch!",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (searchQuery.isNotBlank()) {
                    OutlinedButton(onClick = onClearSearch) {
                        Text("Clear Search", fontSize = 11.5.sp)
                    }
                }
                Button(
                    onClick = onCreatePost,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) {
                    Text("+ Post Meme", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================================
// STUDENT POST COMPOSER: ANY STUDENT CAN POST TO THE INFINITE FEED
// =========================================================================================
@Composable
private fun StudentPostComposerCard(
    defaultHandle: String,
    onPublishPost: (
        handle: String,
        postType: String,
        flair: String,
        headline: String,
        body: String,
        quoteText: String,
        cardStyle: String,
        illustrationKey: String,
        tone: String,
        takeaway: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var postMode by remember { mutableStateOf("PREP_EXCUSE") }
    var cardStyle by remember { mutableStateOf("QUOTE_ORIGINAL") }
    var selectedIllustration by remember { mutableStateOf("FLOW_STATE") }
    var selectedTone by remember { mutableStateOf("ROSE") }
    var handleInput by remember(defaultHandle) { mutableStateOf(defaultHandle) }
    var flairInput by remember { mutableStateOf("STUDENT POST") }
    var quoteCanvasInput by remember { mutableStateOf("") }
    var headlineInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var takeawayInput by remember { mutableStateOf("") }

    val quickPresets = listOf(
        listOf(
            "PREP_EXCUSE",
            "@paper_review_expert",
            "DAMAGE CONTROL",
            "Marks discuss nahi karenge. User experience solid tha.",
            "Mummy: Test kaisa gaya?",
            "Me: Paper ka font bohot readable tha aur invigilator ka nature bhi acha tha.",
            "ROSE"
        ),
        listOf(
            "DAILY_PROBLEM",
            "@pyq_archaeologist",
            "JEE + NEET PYQ",
            "Chapter: “Main easy hoon.” First PYQ: “Toh prove karo.”",
            "Easy chapter bolke shuru kiya.",
            "Do PYQs baad confidence, calculator aur weekend — teeno missing report mein hain.",
            "MINT"
        ),
        listOf(
            "PREP_INSIGHT",
            "@topper_translator",
            "EXAM PSYCHOLOGY",
            "Understatement: Claiming zero preparation while having mastered NCERT, PYQs, and 14 full mocks.",
            "Candidate: “Hardly revised the syllabus.”",
            "Scorecard: 99.8th percentile, 3 complete revisions, and fully indexed error logbook.",
            "CREAM"
        ),
        listOf(
            "DAILY_PROBLEM",
            "@backlog_warrior",
            "BACKLOG",
            "Alarm 4:30 AM ka lagaya tha, utha 9:15 AM pe.",
            "Raat ko 4 alarm lagaye the.",
            "Subah chaaro ko neend mein hi solve karke wapas so gaya.",
            "SKY"
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF0066FF)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memes_studio_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✍️ Create a Post",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Close ✕",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            // Quick Sample Templates (Optional 1-tap auto-fill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPresets.forEachIndexed { idx, preset ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0066FF).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF0066FF).copy(alpha = 0.3f)),
                        modifier = Modifier.clickable {
                            postMode = preset[0]
                            handleInput = preset[1]
                            flairInput = preset[2]
                            quoteCanvasInput = preset[3]
                            headlineInput = preset[4]
                            bodyInput = preset[5]
                            selectedTone = preset[6]
                        }
                    ) {
                        Text(
                            text = "Sample #${idx + 1}: ${preset[2]}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0066FF),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Post Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "PREP_EXCUSE" to "Prep Excuse 🤡",
                    "DAILY_PROBLEM" to "Daily Problem 😭",
                    "FORMULA_MEME" to "Formula / Concept Meme ⚡"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = postMode == key,
                        onClick = { postMode = key },
                        label = { Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    )
                }
            }

            // Card Visual Style Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = cardStyle == "QUOTE_ORIGINAL",
                    onClick = { cardStyle = "QUOTE_ORIGINAL" },
                    label = { Text("Quote Card", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                )
                FilterChip(
                    selected = cardStyle == "ILLUSTRATED_ORIGINAL",
                    onClick = { cardStyle = "ILLUSTRATED_ORIGINAL" },
                    label = { Text("Illustrated Card", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                )
            }

            if (cardStyle == "QUOTE_ORIGINAL") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CREAM", "ROSE", "MINT", "SKY").forEach { tone ->
                        FilterChip(
                            selected = selectedTone == tone,
                            onClick = { selectedTone = tone },
                            label = { Text(tone, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                        )
                    }
                }
                OutlinedTextField(
                    value = quoteCanvasInput,
                    onValueChange = { quoteCanvasInput = it },
                    label = { Text("Quote Box Text", fontSize = 11.5.sp) },
                    placeholder = { Text("e.g., Marks discuss nahi karenge. User experience solid tha.") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "FLOW_STATE" to "01 Flow Desk",
                        "LAMP_3AM" to "02 3 AM Lamp",
                        "BOOK_PILE" to "03 Book Pile",
                        "NAP_BOOKS" to "04 5-Min Nap",
                        "FORMULA_BOARD" to "05 Physics Board",
                        "TROPHY_WIN" to "06 Rare W Trophy"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedIllustration == key,
                            onClick = { selectedIllustration = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = handleInput,
                    onValueChange = { handleInput = it },
                    label = { Text("Your @handle", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = flairInput,
                    onValueChange = { flairInput = it },
                    label = { Text("Tag / Flair", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = headlineInput,
                onValueChange = { headlineInput = it },
                label = { Text("Post Title / Headline", fontSize = 11.5.sp) },
                placeholder = { Text("e.g., Mummy: Test kaisa gaya?") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_top_setup_input")
            )

            OutlinedTextField(
                value = bodyInput,
                onValueChange = { bodyInput = it },
                label = { Text("Write your post, problem or excuse", fontSize = 11.5.sp) },
                placeholder = { Text("e.g., Paper ka font bohot readable tha aur invigilator ka nature bhi acha tha.") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_bottom_punchline_input")
            )

            OutlinedTextField(
                value = takeawayInput,
                onValueChange = { takeawayInput = it },
                label = { Text("Study Tip / Takeaway (Optional)", fontSize = 11.5.sp) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onPublishPost(
                        handleInput,
                        postMode,
                        flairInput,
                        headlineInput,
                        bodyInput,
                        quoteCanvasInput.ifBlank { headlineInput },
                        cardStyle,
                        selectedIllustration,
                        selectedTone,
                        takeawayInput
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066FF),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_meme_button")
            ) {
                Text(
                    text = "Post",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// =========================================================================================
// SCREENSHOT 1 & 2 CARDS: 01 DISTRACTION BATTLE, 02 DELUSION, 03 OVERWHELM, 04 DENIAL
// =========================================================================================
@Composable
private fun StoryCard01DistractionBattle() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "01  /  THE DISTRACTION BATTLE",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0066FF),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "When you finally hit that flow state...",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "And suddenly have to fend off your phone, snacks, and the overwhelming urge to take a 'quick' nap.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Taped Polaroid Frame Illustration
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFEF9E7),
                    border = BorderStroke(6.dp, Color.White),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(230.dp)
                        .rotate(-1.5f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meme_flow_state),
                        contentDescription = "Flow State Illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Top masking tape strip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF8FAFC).copy(alpha = 0.9f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-8).dp)
                        .width(96.dp)
                        .height(24.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun StoryCard02TheDelusion3Am() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2B705)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "02  /  THE DELUSION",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF422006).copy(alpha = 0.75f),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "The 3 AM motivation spike.",
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(4.dp, Color(0xFFFDE047).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(220.dp)
                        .padding(bottom = 30.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meme_3am_lamp),
                        contentDescription = "3 AM Motivation Spike",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Tilted White Quote Note at Bottom
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .rotate(1.5f)
                ) {
                    Text(
                        text = "\"Organic Chem at 3 AM makes perfect sense.\"",
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryCard03TheOverwhelm() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF5FF)),
        border = BorderStroke(1.dp, Color(0xFFD0E2FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(210.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meme_book_pile),
                        contentDescription = "Towering Revision Pile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "03  /  THE OVERWHELM",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0066FF),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Facing the reality.",
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "My pending revision pile staring at me while I calculate the absolute minimum marks needed to pass.",
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155),
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StoryCard04TheDenialProductiveRest() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(215.dp)
                            .padding(top = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_meme_nap_books),
                            contentDescription = "Just 5 Minutes Nap on Books",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Yellow tilted banner: "Just 5 minutes..."
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF2B705),
                        shadowElevation = 6.dp,
                        modifier = Modifier.rotate(2f)
                    ) {
                        Text(
                            text = "\"Just 5 minutes...\"",
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "04  /  THE DENIAL",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF475569),
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The 'productive' rest.",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Taking a well-deserved, heavily-funded two-hour break after exactly fifteen minutes of intense reading.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )
        }
    }
}

// =========================================================================================
// STUDENT FEED POST CARD (Matches Screenshots 3, 4 & 5: Handle Avatar, @handle, ▲ 2.7K Pill,
// Illustrated Artwork 01-06 OR Polka-Dot Quote Canvas, "Us Bhai Us", "Share with batch ↗")
// =========================================================================================
@Composable
private fun StudentFeedPostCard(
    meme: MemeHubItem,
    isCommentDrawerOpen: Boolean,
    onToggleCommentDrawer: () -> Unit,
    commentDraft: String,
    onCommentDraftChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onUpvote: () -> Unit,
    onUsBhaiUs: () -> Unit,
    onBookmark: () -> Unit,
    onSaveToFlashcards: () -> Unit,
    onShare: () -> Unit
) {
    val avatarColor = Color(meme.avatarColorHex)
    val avatarInitials = remember(meme.handle) {
        meme.handle
            .removePrefix("@")
            .split("_", " ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "ST" }
    }
    val formattedLikes = remember(meme.likesCount) {
        if (meme.likesCount >= 1000) {
            String.format(Locale.US, "%.1fK", meme.likesCount / 1000f)
        } else {
            meme.likesCount.toString()
        }
    }
    val formattedUsBhai = remember(meme.usBhaiUsCount) {
        if (meme.usBhaiUsCount >= 1000) {
            String.format(Locale.US, "%.1fK", meme.usBhaiUsCount / 1000f)
        } else {
            meme.usBhaiUsCount.toString()
        }
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meme_card_${meme.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header Row: Student Avatar + @handle + timeAgo • FLAIR + Top-Right "▲ 2.7K" Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = avatarColor,
                        border = BorderStroke(2.dp, Color(0xFF0F172A)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = avatarInitials,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meme.handle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${meme.timeAgo}  •  ${meme.examTag.uppercase()}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top-Right Upvote Pill: "▲ 2.7K"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (meme.isLiked) avatarColor.copy(alpha = 0.18f) else avatarColor.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, avatarColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clickable { onUpvote() }
                        .testTag("meme_top_upvote_${meme.id}")
                ) {
                    Text(
                        text = "▲  $formattedLikes",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        color = avatarColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Visual Artwork OR Polka-Dot Quote Canvas OR Classic 2-Panel Meme Canvas
            when (meme.templateStyle) {
                "ILLUSTRATED_ORIGINAL" -> {
                    IllustratedOriginalCanvas(
                        illustrationKey = meme.illustrationKey ?: "FLOW_STATE",
                        badgeNumber = meme.badgeNumber ?: "01"
                    )
                }
                "QUOTE_ORIGINAL" -> {
                    PolkaDotQuoteOriginalCanvas(
                        quoteText = meme.quoteCanvasText ?: meme.topSetupText,
                        tone = meme.quoteCanvasTone
                    )
                }
                else -> {
                    MemeVisualCanvas(meme = meme)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Bold Headline & Relatable Hinglish Subtitle
            Text(
                text = meme.topSetupText,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = meme.bottomPunchlineText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Compact "+4 Marks Takeaway / Anti-Excuse Fix" Strip + Save to Flashcards
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF0066FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = meme.conceptTakeaway,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0066FF).copy(alpha = 0.12f),
                        modifier = Modifier
                            .clickable { onSaveToFlashcards() }
                            .testTag("meme_save_flashcard_${meme.id}")
                    ) {
                        Text(
                            text = "+ Card",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0066FF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // 5. Screenshot-Accurate Footer Row: "▲ 2.7K   💬 184   🫂 Us Bhai" ———— "Share with batch ↗"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ▲ 2.7K
                    Text(
                        text = "▲ $formattedLikes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (meme.isLiked) Color(0xFF0066FF) else Color(0xFF475569),
                        maxLines = 1,
                        modifier = Modifier
                            .clickable { onUpvote() }
                            .testTag("meme_like_btn_${meme.id}")
                    )

                    // 💬 184
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onToggleCommentDrawer() }
                            .testTag("meme_comment_btn_${meme.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${meme.laughsCount + meme.comments.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            maxLines = 1
                        )
                    }

                    // 🫂 Us Bhai Us Counter
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (meme.isUsBhaiUs) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (meme.isUsBhaiUs) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .clickable { onUsBhaiUs() }
                            .testTag("meme_us_bhai_btn_${meme.id}")
                    ) {
                        Text(
                            text = "🤝 Resonate ($formattedUsBhai)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (meme.isUsBhaiUs) Color(0xFFB45309) else Color(0xFF475569),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("meme_bookmark_btn_${meme.id}")
                    ) {
                        Icon(
                            imageVector = if (meme.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (meme.isBookmarked) Color(0xFFF59E0B) else Color(0xFF64748B),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Share with batch ↗",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        modifier = Modifier
                            .clickable { onShare() }
                            .testTag("meme_share_btn_${meme.id}")
                    )
                }
            }

            // 6. Expandable Comments Section
            AnimatedVisibility(visible = isCommentDrawerOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Batch Comments & Relatable Replies (${meme.comments.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF475569)
                            )

                            meme.comments.forEach { comment ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = comment,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentDraft,
                                    onValueChange = onCommentDraftChange,
                                    placeholder = {
                                        Text(
                                            "Type your batch reply...",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("meme_comment_input_${meme.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = onSubmitComment,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0066FF))
                                        .testTag("meme_comment_send_${meme.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Post Comment",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// ILLUSTRATED ARTWORK CANVAS (Matches Cards 01–06 in Screenshots 3 & 4)
// =========================================================================================
@Composable
private fun IllustratedOriginalCanvas(
    illustrationKey: String,
    badgeNumber: String
) {
    val drawableRes = when (illustrationKey) {
        "FLOW_STATE" -> R.drawable.img_meme_flow_state
        "LAMP_3AM" -> R.drawable.img_meme_3am_lamp
        "BOOK_PILE" -> R.drawable.img_meme_book_pile
        "NAP_BOOKS" -> R.drawable.img_meme_nap_books
        "FORMULA_BOARD" -> R.drawable.img_meme_formula_board
        else -> R.drawable.img_meme_flow_state
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8FAFC))
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = illustrationKey,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (illustrationKey == "TROPHY_WIN") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFEF3C7).copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "🏆 RARE W • MOCK SCORE > ROOM TEMP",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Bottom-Right Numbered Pill Badge ("01", "02", "03", "04", "05", "06")
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.5.dp, Color(0xFF0F172A)),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Text(
                text = badgeNumber,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// =========================================================================================
// POLKA-DOT QUOTE ORIGINAL CANVAS (Matches Cards 07, 08, 09 in Screenshots 4 & 5)
// =========================================================================================
@Composable
private fun PolkaDotQuoteOriginalCanvas(
    quoteText: String,
    tone: String
) {
    val (bgColor, borderColor, quoteMarkColor, dotColor) = when (tone.uppercase()) {
        "ROSE" -> listOf(
            Color(0xFFFFF1F2),
            Color(0xFFFECDD3),
            Color(0xFFF43F5E).copy(alpha = 0.28f),
            Color(0xFFF43F5E).copy(alpha = 0.08f)
        )
        "MINT" -> listOf(
            Color(0xFFECFDF5),
            Color(0xFFA7F3D0),
            Color(0xFF10B981).copy(alpha = 0.28f),
            Color(0xFF10B981).copy(alpha = 0.08f)
        )
        "SKY" -> listOf(
            Color(0xFFF0F9FF),
            Color(0xFFBAE6FD),
            Color(0xFF0284C7).copy(alpha = 0.28f),
            Color(0xFF0284C7).copy(alpha = 0.08f)
        )
        else -> listOf( // CREAM
            Color(0xFFFFFBEB),
            Color(0xFFFDE68A),
            Color(0xFFD97706).copy(alpha = 0.28f),
            Color(0xFFD97706).copy(alpha = 0.08f)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 215.dp)
        ) {
            // Subtle Polka-Dot Pattern matching Screenshots 4 & 5
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 22.dp.toPx()
                val radius = 2.dp.toPx()
                var x = step / 2f
                while (x < size.width) {
                    var y = step / 2f
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = radius,
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }

            // Large decorative opening quotation mark “ in top-left
            Text(
                text = "“",
                fontSize = 68.sp,
                fontWeight = FontWeight.Black,
                color = quoteMarkColor,
                lineHeight = 68.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 6.dp)
            )

            // Centered handwritten/italic bold quote text
            Text(
                text = quoteText,
                fontSize = 21.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                lineHeight = 29.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            )
        }
    }
}

// =========================================================================================
// SCREENSHOT 5 CARD: "PLOT TWIST — Itna scroll kar liya? Ab ek test bhi de hi do."
// =========================================================================================
@Composable
private fun PlotTwistMockReminderCard(
    scrollCount: Int,
    onStartMockTest: () -> Unit,
    onStartFocusSession: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFFDE047)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plot_twist_card_$scrollCount")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFFEF9C3).copy(alpha = 0.65f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "P L O T   T W I S T",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0066FF),
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Itna scroll kar liya? Ab ek test bhi de hi do.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Memes marks nahi badhaate. Honest mocks kabhi-kabhi badha dete hain.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartMockTest,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🎯 Give Quick Mock",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = onStartFocusSession,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF0F172A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⏱️ Start Focus Timer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================================
// CLASSIC 2-PANEL FORMULA MEME VISUAL CANVAS
// =========================================================================================
@Composable
private fun MemeVisualCanvas(meme: MemeHubItem) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meme.emojiBanner,
                    fontSize = 20.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+4 MARKS FORMULA TRAP",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meme.quoteCanvasText ?: meme.topSetupText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
