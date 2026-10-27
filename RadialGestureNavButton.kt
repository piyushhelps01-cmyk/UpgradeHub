package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.JeePage
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Navigation destination specification for the 3-Layer Orbital (9 options) Hold & Release Menu.
 */
data class OrbitalNavItem(
    val page: JeePage,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val layer: Int, // 1, 2, or 3
    val offsetX: Float, // dp offset from button center (negative = left)
    val offsetY: Float  // dp offset from button center (negative = up)
)

@Composable
fun RadialGestureNavButton(
    currentPage: JeePage,
    onNavigateToPage: (JeePage) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val density = LocalDensity.current

    var isHolding by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var selectedItem by remember { mutableStateOf<OrbitalNavItem?>(null) }
    var showAccessibleSheet by remember { mutableStateOf(false) }

    // Exactly 9 options organized in 3 concentric orbital layers (as in reference sample "4. Third Layer (9 options)"):
    // Radius 1: ~80dp, Radius 2: ~145dp, Radius 3: ~210dp
    val orbitalItems = remember {
        listOf(
            // Layer 1 (Inner Arc ~80dp radius) - 2 options: Quick thumb access
            OrbitalNavItem(
                page = JeePage.DAILY_PLANNER,
                title = "Planner",
                subtitle = "Today's Tasks",
                icon = Icons.Default.CalendarMonth,
                layer = 1,
                offsetX = -18f,
                offsetY = -80f
            ),
            OrbitalNavItem(
                page = JeePage.HOURS,
                title = "Focus",
                subtitle = "Study Timer",
                icon = Icons.Default.Timer,
                layer = 1,
                offsetX = -78f,
                offsetY = -22f
            ),

            // Layer 2 (Middle Arc ~145dp radius) - 3 options: Study engines
            OrbitalNavItem(
                page = JeePage.AI,
                title = "AI Coach",
                subtitle = "Study Engine",
                icon = Icons.Default.SmartToy,
                layer = 2,
                offsetX = -25f,
                offsetY = -142f
            ),
            OrbitalNavItem(
                page = JeePage.PRACTICE,
                title = "Practice",
                subtitle = "DPP & Flashcards",
                icon = Icons.Default.FitnessCenter,
                layer = 2,
                offsetX = -95f,
                offsetY = -105f
            ),
            OrbitalNavItem(
                page = JeePage.TESTS,
                title = "Tests",
                subtitle = "CBT Mocks",
                icon = Icons.Default.Description,
                layer = 2,
                offsetX = -142f,
                offsetY = -35f
            ),

            // Layer 3 (Outer Arc ~210dp radius) - 4 options: Long reach modules
            OrbitalNavItem(
                page = JeePage.CHAPTERS,
                title = "Syllabus",
                subtitle = "Tracker",
                icon = Icons.Default.MenuBook,
                layer = 3,
                offsetX = -30f,
                offsetY = -208f
            ),
            OrbitalNavItem(
                page = JeePage.EVENTS,
                title = "Events",
                subtitle = "Contests",
                icon = Icons.Default.Event,
                layer = 3,
                offsetX = -105f,
                offsetY = -180f
            ),
            OrbitalNavItem(
                page = JeePage.BOOKMARKS,
                title = "Bookmarks",
                subtitle = "Formulas",
                icon = Icons.Default.Bookmark,
                layer = 3,
                offsetX = -165f,
                offsetY = -128f
            ),
            OrbitalNavItem(
                page = JeePage.PROGRESS,
                title = "Analytics",
                subtitle = "Weekly & Trends",
                icon = Icons.Default.ShowChart,
                layer = 3,
                offsetX = -208f,
                offsetY = -50f
            )
        )
    }

    // Haptic feedback trigger
    fun triggerHaptic(type: Int = HapticFeedbackConstants.CLOCK_TICK) {
        try {
            view.performHapticFeedback(type)
        } catch (_: Exception) {}
    }

    Box(modifier = modifier) {
        // Full-screen dimmed frosted scrim when menu is active
        AnimatedVisibility(
            visible = isHolding,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090E17).copy(alpha = 0.65f))
            )
        }

        // Concentric Orbital Background Arcs & Orbital Items
        if (isHolding) {
            // Draw the 3 concentric circular orbital guide lines originating from the button center
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Button center in canvas coordinates (bottom-right anchor)
                val buttonCenterPx = Offset(
                    x = size.width - 28.dp.toPx(),
                    y = size.height - 28.dp.toPx()
                )

                val radius1Px = 80.dp.toPx()
                val radius2Px = 145.dp.toPx()
                val radius3Px = 210.dp.toPx()

                val arcStroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // Outer Arc 3
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.20f),
                    radius = radius3Px,
                    center = buttonCenterPx,
                    style = arcStroke
                )

                // Middle Arc 2
                drawCircle(
                    color = Color(0xFF60A5FA).copy(alpha = 0.25f),
                    radius = radius2Px,
                    center = buttonCenterPx,
                    style = arcStroke
                )

                // Inner Arc 1
                drawCircle(
                    color = Color(0xFF818CF8).copy(alpha = 0.30f),
                    radius = radius1Px,
                    center = buttonCenterPx,
                    style = arcStroke
                )
            }

            // Top Status HUD Capsule: indicates highlighted action or release guidance
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-40).dp, y = (-275).dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF121B2C).copy(alpha = 0.95f),
                    border = BorderStroke(
                        1.dp,
                        if (selectedItem != null) Color(0xFF38BDF8) else Color(0xFF1E2D4A)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selectedItem != null) Color(0xFF38BDF8) else Color(0xFF64748B),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedItem != null) {
                                "Release to open: ${selectedItem?.title}"
                            } else {
                                "Hold & slide thumb • Center to cancel"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedItem != null) Color(0xFF38BDF8) else Color.White
                        )
                    }
                }
            }

            // The 9 Orbital Option Bubbles
            orbitalItems.forEach { item ->
                val isSelected = (selectedItem?.page == item.page)
                val itemScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                    label = "orbital_item_scale_${item.title}"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset {
                            IntOffset(
                                x = (item.offsetX * density.density).roundToInt(),
                                y = (item.offsetY * density.density).roundToInt()
                            )
                        }
                        .scale(itemScale),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF121B2C),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                        ),
                        shadowElevation = if (isSelected) 12.dp else 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Center Floating Action Trigger Button (Anchored in bottom-right corner)
        val buttonScale by animateFloatAsState(
            targetValue = if (isHolding) 1.15f else 1.0f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
            label = "fab_button_scale"
        )

        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .scale(buttonScale)
                .size(56.dp)
                .testTag("floating_gesture_nav_button")
                // Tap opens the accessible grid sheet
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            triggerHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                            showAccessibleSheet = true
                        }
                    )
                }
                // Long-press + Slide initiates orbital selection
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isHolding = true
                            dragOffsetPx = Offset.Zero
                            selectedItem = null
                            triggerHaptic(HapticFeedbackConstants.LONG_PRESS)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetPx += dragAmount

                            val currentDxDp = dragOffsetPx.x / density.density
                            val currentDyDp = dragOffsetPx.y / density.density
                            val distanceFromCenter = hypot(currentDxDp, currentDyDp)

                            // Cancel threshold: thumb returned within 30dp of button center
                            if (distanceFromCenter < 30f) {
                                if (selectedItem != null) {
                                    selectedItem = null
                                    triggerHaptic(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            } else {
                                // Find closest item within reach
                                var closestItem: OrbitalNavItem? = null
                                var minDistance = Float.MAX_VALUE

                                for (item in orbitalItems) {
                                    val dist = hypot(currentDxDp - item.offsetX, currentDyDp - item.offsetY)
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        closestItem = item
                                    }
                                }

                                if (minDistance < 50f && closestItem != null) {
                                    if (selectedItem?.page != closestItem.page) {
                                        selectedItem = closestItem
                                        triggerHaptic(HapticFeedbackConstants.CLOCK_TICK)
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            val targetPage = selectedItem?.page
                            if (targetPage != null) {
                                triggerHaptic(HapticFeedbackConstants.CONFIRM)
                                onNavigateToPage(targetPage)
                            }
                            isHolding = false
                            selectedItem = null
                            dragOffsetPx = Offset.Zero
                        },
                        onDragCancel = {
                            isHolding = false
                            selectedItem = null
                            dragOffsetPx = Offset.Zero
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        ),
                        CircleShape
                    )
                    .border(1.5.dp, Color(0xFF60A5FA).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isHolding) {
                    Text(
                        text = if (selectedItem != null) "OPEN" else "HOLD",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Hold to Navigate",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Accessible Sheet for standard click/tap navigation
        if (showAccessibleSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { showAccessibleSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color(0xFF121B2C),
                    border = BorderStroke(1.dp, Color(0xFF1E2D4A)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Orbital Quick Launcher",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "3 Concentric Layers • 9 Core Aspirant Actions",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            IconButton(
                                onClick = { showAccessibleSheet = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Menu",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid of all 9 items
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(orbitalItems) { item ->
                                val isCurrent = (currentPage == item.page)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isCurrent) Color(0xFF2563EB).copy(alpha = 0.3f) else Color(0xFF0D1527),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) Color(0xFF38BDF8) else Color(0xFF1E2D4A)
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            triggerHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onNavigateToPage(item.page)
                                            showAccessibleSheet = false
                                        }
                                        .testTag("orbital_sheet_item_${item.title.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (isCurrent) Color(0xFF38BDF8) else Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color(0xFF38BDF8) else Color.White
                                        )
                                        Text(
                                            text = "Layer ${item.layer}",
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
