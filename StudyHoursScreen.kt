package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SixtyDayProgressRechartsCard
import com.example.ui.components.StudyHoursTrendCard
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import java.util.Calendar

@Composable
fun StudyHoursScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val trendData by viewModel.sevenDayTrendData.collectAsState()
    val sixtyDayProgress by viewModel.sixtyDayProgressData.collectAsState()
    val progression by viewModel.curriculumProgression.collectAsState()

    var workloadInput by remember(uiState.workloadHours) { mutableStateOf(uiState.workloadHours.toInt().toString()) }
    var rateInput by remember(uiState.dailyRate) { mutableStateOf(uiState.dailyRate.toString()) }

    val baseTarget = progression.targetTotalHours
    val hoursPct = if (baseTarget > 0f) (uiState.totalFocusedHours / baseTarget).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Total Focused Hours Meter (from miss60.netlify.app)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${progression.totalDays}-Day Focused Study Hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Target: ${progression.targetTotalHours.toInt()}h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0070F3),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${uiState.totalFocusedHours.toInt()}h",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { hoursPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF6574FF),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${(hoursPct * 100).toInt()}% of the ${progression.targetTotalHours.toInt()}h sprint target logged",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 60-Day Study Hours & Syllabus Coverage Progress Visualizer (D3 / Recharts)
        item {
            SixtyDayProgressRechartsCard(
                progressData = sixtyDayProgress,
                onSelectDay = { dayNumber ->
                    viewModel.selectDay(dayNumber)
                }
            )
        }

        // 7-Day Study Hours Trend & Consistency Line Chart (Recharts-inspired)
        item {
            StudyHoursTrendCard(
                trendData = trendData,
                onSelectDay = { dayNumber ->
                    viewModel.selectDay(dayNumber)
                }
            )
        }

        // Completion Calculator (Interactive Tool from PDF page 2 & miss60)
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
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color(0xFF6574FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Completion Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Estimate days required to cover full syllabus + practice workload",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = workloadInput,
                            onValueChange = {
                                workloadInput = it
                                it.toFloatOrNull()?.let { w -> viewModel.setWorkloadHours(w) }
                            },
                            label = { Text("Workload Hours") },
                            placeholder = { Text("1347") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = {
                                rateInput = it
                                it.toFloatOrNull()?.let { r -> viewModel.setDailyRate(r) }
                            },
                            label = { Text("Focused h/day") },
                            placeholder = { Text("12") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ESTIMATED COMPLETION SPAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.1f days · ≈ %d days", uiState.calculatedDays, Math.ceil(uiState.calculatedDays.toDouble()).toInt()),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reference Pace Matrix from PDF
                    Text(
                        text = "Standard Workload Models (1347 Hours):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val models = listOf(
                        "8 focused h/day" to "≈ 169 days (480h / 60 days)",
                        "10 focused h/day" to "≈ 135 days (600h / 60 days)",
                        "12 focused h/day ★" to "≈ 113 days (720h / 60 days)",
                        "14 focused h/day" to "≈ 97 days (840h / 60 days)",
                        "16 focused h/day" to "≈ 85 days (960h / 60 days)"
                    )
                    models.forEach { (pace, days) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = pace, fontSize = 11.sp, color = Color(0xFF26334E))
                            Text(text = days, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Daily Work Record Header
        item {
            Text(
                text = "${progression.totalDays}-DAY DAILY WORK RECORD",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dynamic Days rows
        items(progression.totalDays) { dayIdx ->
            val dayNum = dayIdx + 1
            val cal = remember(dayIdx) {
                Calendar.getInstance().apply {
                    set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
                    add(Calendar.DAY_OF_YEAR, dayIdx)
                }
            }
            val months = listOf("Sep", "Oct")
            val mStr = if (cal.get(Calendar.MONTH) == Calendar.SEPTEMBER) "Sep" else "Oct"
            val dStr = String.format("%02d %s", cal.get(Calendar.DAY_OF_MONTH), mStr)
            val isWeekendOrWed = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            val targetHours = if (isWeekendOrWed) "15h" else "12h"

            val actualHours = viewModel.getDayRecord(dayIdx, "hours")
            val dayScore = viewModel.getDayRecord(dayIdx, "score")
            val dayNotes = viewModel.getDayRecord(dayIdx, "notesPlan")

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6574FF).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "D $dayNum",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF5266CF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dStr, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Target: $targetHours", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Day Score badge
                        if (dayScore.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFC4EED0)
                            ) {
                                Text(
                                    text = "Score: $dayScore/10",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00210E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = actualHours,
                            onValueChange = { viewModel.updateDayRecord(dayIdx, "hours", it) },
                            label = { Text("Hours", fontSize = 10.sp) },
                            placeholder = { Text("12") },
                            modifier = Modifier.width(92.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dayNotes,
                            onValueChange = { viewModel.updateDayRecord(dayIdx, "notesPlan", it) },
                            label = { Text("Execution Log / Notes", fontSize = 10.sp) },
                            placeholder = { Text("Lec 1-4 done, DPP completed, 30 PYQs...") },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
