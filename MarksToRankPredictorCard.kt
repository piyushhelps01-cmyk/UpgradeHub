package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarksToRankPredictorCard(
    isNeetDefault: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedExam by remember(isNeetDefault) {
        mutableStateOf(if (isNeetDefault) "NEET UG (720)" else "JEE Main (300)")
    }
    var selectedCategory by remember { mutableStateOf("GEN") }
    var marksSlider by remember(selectedExam) {
        mutableFloatStateOf(
            when (selectedExam) {
                "NEET UG (720)" -> 645f
                "SSC CGL (200)" -> 158f
                else -> 195f
            }
        )
    }

    val maxMarks = when (selectedExam) {
        "NEET UG (720)" -> 720f
        "SSC CGL (200)" -> 200f
        else -> 300f
    }

    val categoryBonus = when (selectedCategory) {
        "EWS" -> 12
        "OBC-NCL" -> 18
        "SC" -> 48
        "ST" -> 65
        else -> 0
    }

    val effectiveScore = (marksSlider + categoryBonus).coerceAtMost(maxMarks)

    // Compute Percentile, Predicted AIR, and Target Colleges
    val prediction = remember(selectedExam, marksSlider, selectedCategory) {
        when (selectedExam) {
            "NEET UG (720)" -> {
                val m = marksSlider.toInt()
                val eff = (m + categoryBonus * 2).coerceAtMost(720)
                val percentile = when {
                    m >= 700 -> "99.98+ %ile"
                    m >= 675 -> "99.85 %ile"
                    m >= 650 -> "99.45 %ile"
                    m >= 620 -> "98.70 %ile"
                    m >= 580 -> "97.10 %ile"
                    m >= 520 -> "94.20 %ile"
                    else -> "88.50 %ile"
                }
                val airBand = when {
                    m >= 700 -> "AIR 1 – 350"
                    m >= 675 -> "AIR 350 – 2,200"
                    m >= 650 -> "AIR 2,200 – 8,500"
                    m >= 620 -> "AIR 8,500 – 19,500"
                    m >= 580 -> "AIR 19,500 – 42,000"
                    else -> "AIR 42,000+"
                }
                val collegeMatch = when {
                    eff >= 690 -> "AIIMS New Delhi, MAMC Delhi, VMMC & Safdarjung"
                    eff >= 660 -> "AIIMS Jodhpur / Bhopal / Rishikesh, KGMU Lucknow, Seth GS Mumbai"
                    eff >= 630 -> "IMS BHU, Top State Govt Medical Colleges (GMC AIQ 15%)"
                    eff >= 595 -> "State Quota Govt Medical Seat (MBBS) & ESIC Medical Colleges"
                    else -> "BDS Govt Colleges / Semi-Govt MBBS — Boost Biology NCERT (+40 marks)!"
                }
                val splitText = "Bio: ${(m * 0.52f).toInt().coerceAtMost(360)}/360 • Chem: ${(m * 0.25f).toInt().coerceAtMost(180)}/180 • Phys: ${(m * 0.23f).toInt().coerceAtMost(180)}/180"
                listOf(percentile, airBand, collegeMatch, splitText)
            }
            "SSC CGL (200)" -> {
                val m = marksSlider.toInt()
                val eff = (m + categoryBonus / 2).coerceAtMost(200)
                val percentile = when {
                    m >= 165 -> "99.6+ %ile"
                    m >= 150 -> "98.5 %ile"
                    m >= 135 -> "95.8 %ile"
                    else -> "89.0 %ile"
                }
                val airBand = when {
                    m >= 165 -> "AIR 1 – 800"
                    m >= 150 -> "AIR 800 – 3,500"
                    m >= 135 -> "AIR 3,500 – 12,000"
                    else -> "AIR 12,000+"
                }
                val postMatch = when {
                    eff >= 162 -> "ASO in MEA / CSS, Income Tax Inspector (CBDT)"
                    eff >= 148 -> "Preventive Officer (Customs), Central Excise Inspector, Auditor"
                    else -> "Tax Assistant (CBDT/CBIC), Postal Assistant — Boost Quant & Reasoning!"
                }
                val splitText = "Quant: ${(m * 0.26f).toInt()}/50 • Eng: ${(m * 0.26f).toInt()}/50 • Reas: ${(m * 0.26f).toInt()}/50 • GA: ${(m * 0.22f).toInt()}/50"
                listOf(percentile, airBand, postMatch, splitText)
            }
            else -> {
                val m = marksSlider.toInt()
                val eff = effectiveScore.toInt()
                val percentile = when {
                    m >= 250 -> "99.92+ %ile"
                    m >= 220 -> "99.72 %ile"
                    m >= 195 -> "99.38 %ile"
                    m >= 175 -> "98.85 %ile"
                    m >= 150 -> "97.60 %ile"
                    m >= 120 -> "94.80 %ile"
                    else -> "89.20 %ile"
                }
                val airBand = when {
                    m >= 250 -> "AIR 1 – 1,100"
                    m >= 220 -> "AIR 1,100 – 3,800"
                    m >= 195 -> "AIR 3,800 – 8,900"
                    m >= 175 -> "AIR 8,900 – 16,500"
                    m >= 150 -> "AIR 16,500 – 34,000"
                    else -> "AIR 34,000+"
                }
                val collegeMatch = when {
                    eff >= 235 -> "NIT Trichy, NIT Surathkal, NIT Warangal, IIIT Hyderabad (CSE / AI)"
                    eff >= 200 -> "NIT Allahabad, MNIT Jaipur,NIT Rourkela, DTU / NSUT (CSE / IT / ECE)"
                    eff >= 170 -> "Top 10 NITs (ECE / EE / Mech) & IIIT Lucknow / Gwalior (CS)"
                    eff >= 135 -> "NIT Core Branches, IIITs & Qualified for JEE Advanced"
                    else -> "Focus on High-ROI Physics & Chemistry Chapters (+45 Marks Boost)"
                }
                val splitText = "Phys: ${(m * 0.38f).toInt().coerceAtMost(100)}/100 • Chem: ${(m * 0.35f).toInt().coerceAtMost(100)}/100 • Maths: ${(m * 0.27f).toInt().coerceAtMost(100)}/100"
                listOf(percentile, airBand, collegeMatch, splitText)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("marks_to_rank_predictor_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.14f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Marks vs Percentile & College Predictor",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time AIR rank & college cutoff simulator",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Exam Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("JEE Main (300)", "NEET UG (720)", "SSC CGL (200)").forEach { exam ->
                    val active = selectedExam == exam
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (active) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedExam = exam }
                    ) {
                        Text(
                            text = exam,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Category Pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("GEN", "EWS", "OBC-NCL", "SC", "ST").forEach { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF8B5CF6),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Score Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expected Mock / Exam Score:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${marksSlider.toInt()} / ${maxMarks.toInt()} Marks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0070F3)
                )
            }

            Slider(
                value = marksSlider,
                onValueChange = { marksSlider = it },
                valueRange = 40f..maxMarks,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0070F3),
                    activeTrackColor = Color(0xFF0070F3)
                )
            )

            // Ideal Subject Split
            Text(
                text = "Suggested Subject Split: ${prediction[3]}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Predicted Rank & Percentile Output Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PREDICTED PERCENTILE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(prediction[0], fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ESTIMATED ALL-INDIA RANK", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(prediction[1], fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF38BDF8))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cutoff Unlock ($selectedCategory): ${prediction[2]}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
