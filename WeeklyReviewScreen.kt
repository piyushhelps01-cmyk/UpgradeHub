package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.JeeData
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun WeeklyReviewScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val activeWeeks by viewModel.activeWeeks.collectAsState()
    val isNeet by viewModel.isNeetMode.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()

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
                            Icons.Default.CalendarViewWeek,
                            contentDescription = null,
                            tint = if (isNeet) Color(0xFF00897B) else Color(0xFF6574FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isNeet) "9-Week NEET Audit & Review" else "9-Week Audit & Review",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isNeet) Color(0xFFE8F5E9) else Color(0xFFEEF0FF)
                            ) {
                                Text(
                                    text = selectedBatch.title,
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
                            "Weekly checkpoints for NEET syllabus pacing, 720-mark test scores, NCERT line-by-line review, and Botany/Zoology mastery."
                        } else {
                            "Weekly checkpoints to verify syllabus pacing, test scores, error logs, and module depth across the 60 days."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Weekly Mistake Report & Auto Mistake Book Section
        item {
            com.example.ui.components.SmartAutoMistakeAndTestAnalysisSection(
                viewModel = viewModel
            )
        }

        // 9 Weeks
        items(activeWeeks) { (wNum, dates, coverage) ->
            val wData = viewModel.getWeeklyReview(wNum)

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6574FF).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "WEEK $wNum",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF5266CF),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dates,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC4EED0)
                        ) {
                            Text(
                                text = "${wData.actualHours.toInt()}h logged / ${wData.targetHours}h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00210E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Key Syllabus Focus: $coverage",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Weekly Quality Audit:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WeeklyAuditChip(
                            label = "PYQs Done",
                            checked = wData.pyqDone,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleWeeklyFlag(wNum, "pyq", wData.pyqDone)
                        }

                        WeeklyAuditChip(
                            label = "Modules",
                            checked = wData.modulesDone,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleWeeklyFlag(wNum, "modules", wData.modulesDone)
                        }

                        WeeklyAuditChip(
                            label = "Weak Topics",
                            checked = wData.weakTopicsDone,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleWeeklyFlag(wNum, "weaks", wData.weakTopicsDone)
                        }

                        WeeklyAuditChip(
                            label = "Test Analysis",
                            checked = wData.testAnalysisDone,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleWeeklyFlag(wNum, "analysis", wData.testAnalysisDone)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = wData.nextPriority,
                        onValueChange = { viewModel.updateWeeklyString(wNum, "next", it) },
                        label = { Text("Next Week's High Priority Actions / Backlogs", fontSize = 11.sp) },
                        placeholder = { Text("Finish Optics refraction problems, complete 50 calculus PYQs...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun WeeklyAuditChip(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (checked) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00875A),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
