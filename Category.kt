package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val title: String,
    val shortName: String,
    val description: String,
    val accentColor: Color,
    val containerColor: Color,
    val onContainerColor: Color,
    val icon: ImageVector
) {
    SCIENCE(
        title = "USO Science Olympiad",
        shortName = "Science",
        description = "Physics, Chemistry, Biology, Earth & Space Science",
        accentColor = Color(0xFF00639B),
        containerColor = Color(0xFFC2E7FF),
        onContainerColor = Color(0xFF001D35),
        icon = Icons.Default.Biotech
    ),
    GK(
        title = "General Knowledge & Heritage",
        shortName = "GK & World",
        description = "National & Global Affairs, Geography, Inventions",
        accentColor = Color(0xFFB26A00),
        containerColor = Color(0xFFFFDEA5),
        onContainerColor = Color(0xFF281900),
        icon = Icons.Default.WorkspacePremium
    ),
    UN_INFO(
        title = "UN Information & Global Goals",
        shortName = "UN Test",
        description = "UN Organs, Peacekeeping, UNESCO, WHO, SDGs",
        accentColor = Color(0xFF00875A),
        containerColor = Color(0xFFC4EED0),
        onContainerColor = Color(0xFF00210E),
        icon = Icons.Default.Public
    ),
    MATH_LOGIC(
        title = "Mental Math & Reasoning",
        shortName = "Logic & Math",
        description = "Patterns, Series, Quantitative & Deductive Logic",
        accentColor = Color(0xFF6750A4),
        containerColor = Color(0xFFEADDFF),
        onContainerColor = Color(0xFF21005D),
        icon = Icons.Default.Calculate
    ),
    CYBER_IT(
        title = "Cyber & Digital Tech",
        shortName = "Cyber IT",
        description = "Computing basics, Internet safety, Algorithms",
        accentColor = Color(0xFF7D5260),
        containerColor = Color(0xFFFFD8E4),
        onContainerColor = Color(0xFF31111D),
        icon = Icons.Default.Computer
    )
}

data class Question(
    val id: String,
    val category: Category,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: String = "Intermediate",
    val subtopic: String = "General"
)

data class Flashcard(
    val id: String,
    val category: Category,
    val title: String,
    val concept: String,
    val details: String,
    val keyTakeaway: String
)
