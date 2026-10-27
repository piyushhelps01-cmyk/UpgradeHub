package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.JeeData
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectedFlowScreen(
    viewModel: JeeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val flows = viewModel?.activeFlows?.collectAsState()?.value ?: JeeData.CONNECTED_FLOWS
    val facultyList = viewModel?.activeFaculty?.collectAsState()?.value ?: JeeData.FACULTY_STACK
    val isNeet = viewModel?.isNeetMode?.collectAsState()?.value ?: false
    val batchTitle = viewModel?.selectedBatch?.collectAsState()?.value?.title ?: "Curriculum Strategy"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = if (isNeet) Color(0xFF00897B) else Color(0xFF6574FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isNeet) "NEET Connected Flow & Strategy" else "Connected Flow & Strategy",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isNeet) Color(0xFFE8F5E9) else Color(0xFFEEF0FF)
                            ) {
                                Text(
                                    text = batchTitle,
                                    color = if (isNeet) Color(0xFF2E7D32) else Color(0xFF3F51B5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isNeet) {
                            "Follow the prerequisite dependency order for Botany, Zoology, NEET Physics, and Chemistry strictly."
                        } else {
                            "Follow the prerequisite dependency order strictly. Never skip foundational building blocks before advanced concepts."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Golden Rules Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF7EA)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFB26A00),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNeet) "60-Day NEET Golden Rules" else "60-Day Golden Rules",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF4A3400)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val rules = if (isNeet) {
                        listOf(
                            "1. Class 09:00 AM – 02:00 PM is NOT counted as self-study.",
                            "2. Biology: NCERT line-by-line reading with diagrams & tables is NON-NEGOTIABLE.",
                            "3. Watching a lecture NEVER equals completing a chapter.",
                            "4. Mark COMPLETE only after: Lecture → Notes → DPP MCQs → NCERT → Prarambh → Prabal → PYQ (2018-2025) → Revision → Test.",
                            "5. Physics & Chemistry: Minimum 45 MCQs timed practice per chapter.",
                            "6. Maintain an active error log notebook for every mock test."
                        )
                    } else {
                        listOf(
                            "1. Class 09:00 AM – 02:00 PM is NOT counted as self-study.",
                            "2. Self-study must only occur in 3-hour uninterrupted focus blocks.",
                            "3. Watching a lecture NEVER equals completing a chapter.",
                            "4. Mark COMPLETE only after: Lecture → Notes → DPP → NCERT → Prarambh → Prabal → Prakshit → PYQ → Revision → Test.",
                            "5. Maintain an active error log notebook for every mock test."
                        )
                    }

                    rules.forEach { rule ->
                        Text(
                            text = rule,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A3400),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Connected Chapter Flows
        if (viewModel != null) {
            item {
                com.example.ui.components.SmartRevisionSystemCard(
                    viewModel = viewModel
                )
            }
        }

        item {
            Text(
                text = "CONNECTED CHAPTER PREREQUISITE FLOWS",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(flows) { flow ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = flow.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    flow.icon,
                                    contentDescription = null,
                                    tint = flow.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = flow.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal scrolling chain or flow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        flow.steps.forEachIndexed { idx, step ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = flow.color.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, flow.color.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = step,
                                    color = flow.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            if (idx < flow.steps.size - 1) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = flow.color.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Faculty & Resource Stack
        item {
            Text(
                text = "FACULTY & RESOURCE STACK",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(facultyList) { faculty ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = faculty.facultyName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = faculty.subject,
                                fontSize = 11.sp,
                                color = Color(0xFF5266CF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEADDFF).copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = faculty.course,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = faculty.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
