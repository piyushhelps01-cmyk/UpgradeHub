package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterItem
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

// ============================================================================
// 3. ADVANCED VISUAL LEARNING LAB (Multi-Simulations + Live Telemetry + Quiz)
// ============================================================================
@Composable
fun AdvancedVisualLearningView(viewModel: JeeViewModel) {
    val isNeet by viewModel.isNeetMode.collectAsState()
    var selectedSubject by remember(isNeet) {
        mutableStateOf(if (isNeet) "Biology" else "Physics")
    }
    var selectedSimIndex by remember { mutableIntStateOf(0) }

    // Physics State
    var distanceR by remember { mutableFloatStateOf(2.5f) }
    var chargeQ1 by remember { mutableFloatStateOf(2.0f) }
    var dielectricK by remember { mutableFloatStateOf(1.0f) }
    var launchAngle by remember { mutableFloatStateOf(45f) }
    var initialVelocity by remember { mutableFloatStateOf(25f) }

    // Chemistry State
    var temperatureK by remember { mutableFloatStateOf(350f) }
    var catalystActive by remember { mutableStateOf(false) }
    var selectedHybridization by remember { mutableStateOf("sp³") }
    var lonePairsCount by remember { mutableIntStateOf(0) }

    // Maths State
    var waveFreq by remember { mutableFloatStateOf(1.5f) }
    var waveAmp by remember { mutableFloatStateOf(1.0f) }
    var inspectX by remember { mutableFloatStateOf(1.5f) }
    var showDerivative by remember { mutableStateOf(true) }
    var eccentricity by remember { mutableFloatStateOf(0.7f) }

    // Biology State
    var bioStage by remember { mutableStateOf("Metaphase") }
    var spindleTension by remember { mutableFloatStateOf(0.65f) }
    var substrateConc by remember { mutableFloatStateOf(4.0f) }
    var inhibitorType by remember { mutableStateOf("None") }

    // Lab Mini-Quiz State
    var selectedQuizAnswer by remember(selectedSubject, selectedSimIndex) { mutableIntStateOf(-1) }

    val simulationsForSubject = when (selectedSubject) {
        "Physics" -> listOf("⚡ Electrostatics & Dielectric Field", "🚀 Projectile Motion & Trajectory")
        "Chemistry" -> listOf("🔥 Arrhenius & Catalyst Kinetics", "🧪 3D VSEPR & Hybridization")
        "Maths" -> listOf("📈 Calculus Derivative & Wave Tangent", "🎯 Conic Sections & Eccentricity")
        else -> listOf("🧬 Mitosis Chromosome Microscope", "🧪 Enzyme Kinetics (Vmax & Km)")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Subject Selector Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Interactive Virtual Science Lab",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Manipulate variables in real-time & verify laws visually",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "8 LIVE SIMS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Physics", "Chemistry", "Maths", "Biology")) { sub ->
                        val isSelected = selectedSubject == sub
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSubject = sub
                                selectedSimIndex = 0
                            },
                            label = {
                                Text(
                                    text = sub,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0070F3),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Sub-simulation switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    simulationsForSubject.forEachIndexed { idx, title ->
                        val active = selectedSimIndex == idx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) Color(0xFF0070F3).copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                1.dp,
                                if (active) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSimIndex = idx }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }
        }

        // Simulation Viewport & Controls
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (selectedSubject) {
                        "Physics" -> {
                            if (selectedSimIndex == 0) {
                                // SIM 1: ELECTROSTATICS & DIELECTRIC LAB
                                Text("Coulomb's Law & Dielectric Medium Explorer", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Observe how dielectric constant (κ) & separation (r) alter electric field lines and force.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val forceN = (9.0 * chargeQ1 * 2.0) / (dielectricK * distanceR * distanceR)
                                val potentialEnergy = -(9.0 * chargeQ1 * 2.0) / (dielectricK * distanceR)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val centerY = size.height / 2f
                                        val q1X = size.width * 0.22f
                                        val q2X = (size.width * 0.22f) + (distanceR * 48f).coerceAtMost(size.width * 0.65f)

                                        // Dielectric slab visual if dielectricK > 1
                                        if (dielectricK > 1.05f) {
                                            val midX = (q1X + q2X) / 2f
                                            drawRect(
                                                color = Color(0xFF8B5CF6).copy(alpha = (dielectricK / 12f).coerceIn(0.12f, 0.45f)),
                                                topLeft = Offset(midX - 36.dp.toPx(), 18.dp.toPx()),
                                                size = androidx.compose.ui.geometry.Size(72.dp.toPx(), size.height - 36.dp.toPx())
                                            )
                                        }

                                        // Equipotential shells around Q1
                                        for (ring in 1..3) {
                                            drawCircle(
                                                color = Color(0xFFEF4444).copy(alpha = 0.14f / ring),
                                                radius = (22 + ring * 18).dp.toPx(),
                                                center = Offset(q1X, centerY),
                                                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                                            )
                                        }

                                        // Field lines
                                        val lineAlpha = (0.75f / dielectricK.coerceAtLeast(1f)).coerceIn(0.2f, 0.85f)
                                        for (i in -3..3) {
                                            val path = Path().apply {
                                                moveTo(q1X, centerY)
                                                quadraticTo(
                                                    (q1X + q2X) / 2f,
                                                    centerY + (i * 25f * (chargeQ1 / 2f)),
                                                    q2X,
                                                    centerY
                                                )
                                            }
                                            drawPath(
                                                path = path,
                                                color = Color(0xFF38BDF8).copy(alpha = lineAlpha),
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }

                                        // Source Charge +q1
                                        drawCircle(
                                            color = Color(0xFFEF4444),
                                            radius = (14f + chargeQ1 * 2.5f).dp.toPx(),
                                            center = Offset(q1X, centerY)
                                        )
                                        // Test Charge -q2
                                        drawCircle(
                                            color = Color(0xFF0070F3),
                                            radius = 18.dp.toPx(),
                                            center = Offset(q2X, centerY)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("F = ${String.format(Locale.ENGLISH, "%.2f", forceN)} N", Color(0xFF38BDF8))
                                        TelemetryChip("U = ${String.format(Locale.ENGLISH, "%.2f", potentialEnergy)} J", Color(0xFF10B981))
                                        TelemetryChip("κ = ${String.format(Locale.ENGLISH, "%.1f", dielectricK)}", Color(0xFF8B5CF6))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text("+q₁ (${String.format(Locale.ENGLISH, "%.1f", chargeQ1)} μC)", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        if (dielectricK > 1.05f) {
                                            Text("Dielectric Slab (κ=${String.format(Locale.ENGLISH, "%.1f", dielectricK)})", color = Color(0xFFC4B5FD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("-q₂ (-2.0 μC)", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Separation Distance (r): ${String.format(Locale.ENGLISH, "%.1f", distanceR)} cm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = distanceR,
                                    onValueChange = { distanceR = it },
                                    valueRange = 1.0f..6.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0070F3), activeTrackColor = Color(0xFF0070F3))
                                )

                                Text("Source Charge Magnitude (q₁): ${String.format(Locale.ENGLISH, "%.1f", chargeQ1)} μC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = chargeQ1,
                                    onValueChange = { chargeQ1 = it },
                                    valueRange = 1.0f..5.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
                                )

                                Text("Medium Dielectric Constant (κ): ${String.format(Locale.ENGLISH, "%.1f", dielectricK)} (${if (dielectricK <= 1.1f) "Vacuum/Air" else "Dielectric Medium"})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = dielectricK,
                                    onValueChange = { dielectricK = it },
                                    valueRange = 1.0f..6.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF8B5CF6), activeTrackColor = Color(0xFF8B5CF6))
                                )

                                FormulaTakeawayBar("F_med = F_vac / κ = (1 / 4πε₀κ) · (|q₁q₂| / r²)")
                            } else {
                                // SIM 2: PROJECTILE MOTION TRAJECTORY LAB
                                Text("2D Projectile Motion & Optimal Range Analyzer", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Compare your launch angle θ against the 45° maximum range reference curve.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val g = 9.8f
                                val rad = (launchAngle * PI / 180.0).toFloat()
                                val rangeM = (initialVelocity * initialVelocity * sin(2 * rad)) / g
                                val maxHeightM = (initialVelocity * initialVelocity * sin(rad).pow(2)) / (2 * g)
                                val flightTimeS = (2 * initialVelocity * sin(rad)) / g

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val originX = 28.dp.toPx()
                                        val groundY = size.height - 24.dp.toPx()
                                        val maxPossibleRange = (40f * 40f) / g
                                        val scaleX = (size.width - 56.dp.toPx()) / maxPossibleRange
                                        val scaleY = (size.height - 48.dp.toPx()) / ((40f * 40f) / (2 * g))

                                        // Ground line
                                        drawLine(
                                            color = Color(0xFF334155),
                                            start = Offset(originX, groundY),
                                            end = Offset(size.width - 16.dp.toPx(), groundY),
                                            strokeWidth = 2.dp.toPx()
                                        )

                                        // Reference 45-deg dashed curve for current velocity
                                        val refPath = Path().apply {
                                            moveTo(originX, groundY)
                                            val refRad = (45.0 * PI / 180.0).toFloat()
                                            val refRange = (initialVelocity * initialVelocity * sin(2 * refRad)) / g
                                            for (step in 0..50) {
                                                val x = (refRange * step) / 50f
                                                val y = x * kotlin.math.tan(refRad) - (g * x * x) / (2 * initialVelocity * initialVelocity * cos(refRad).pow(2))
                                                lineTo(originX + x * scaleX, groundY - y.coerceAtLeast(0f) * scaleY)
                                            }
                                        }
                                        drawPath(
                                            path = refPath,
                                            color = Color(0xFF64748B).copy(alpha = 0.5f),
                                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                                        )

                                        // Active Trajectory Curve
                                        val activePath = Path().apply {
                                            moveTo(originX, groundY)
                                            for (step in 0..60) {
                                                val x = (rangeM * step) / 60f
                                                val denom = (2 * initialVelocity * initialVelocity * cos(rad).pow(2)).coerceAtLeast(0.001f)
                                                val y = x * kotlin.math.tan(rad) - (g * x * x) / denom
                                                lineTo(originX + x * scaleX, groundY - y.coerceAtLeast(0f) * scaleY)
                                            }
                                        }
                                        drawPath(
                                            path = activePath,
                                            color = Color(0xFF10B981),
                                            style = Stroke(width = 3.dp.toPx())
                                        )

                                        // Peak marker
                                        val peakX = originX + (rangeM / 2f) * scaleX
                                        val peakY = groundY - maxHeightM * scaleY
                                        drawCircle(
                                            color = Color(0xFFF59E0B),
                                            radius = 5.dp.toPx(),
                                            center = Offset(peakX, peakY)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("Range R = ${String.format(Locale.ENGLISH, "%.1f", rangeM)} m", Color(0xFF10B981))
                                        TelemetryChip("H_max = ${String.format(Locale.ENGLISH, "%.1f", maxHeightM)} m", Color(0xFFF59E0B))
                                        TelemetryChip("T = ${String.format(Locale.ENGLISH, "%.1f", flightTimeS)} s", Color(0xFF38BDF8))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Launch Angle (θ): ${launchAngle.toInt()}° (Complementary same-range angle: ${(90 - launchAngle.toInt())}°)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = launchAngle,
                                    onValueChange = { launchAngle = it },
                                    valueRange = 15f..80f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                                )

                                Text("Initial Velocity (u): ${initialVelocity.toInt()} m/s", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = initialVelocity,
                                    onValueChange = { initialVelocity = it },
                                    valueRange = 15f..40f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0070F3), activeTrackColor = Color(0xFF0070F3))
                                )

                                FormulaTakeawayBar("y = x·tanθ - (g·x²) / (2u²cos²θ)  |  R_max at θ = 45°")
                            }
                        }

                        "Chemistry" -> {
                            if (selectedSimIndex == 0) {
                                // SIM 1: ARRHENIUS & MAXWELL-BOLTZMANN KINETICS
                                Text("Maxwell-Boltzmann Distribution & Catalyst Action", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Toggle a positive catalyst to lower Activation Energy (Ea) and watch the reactive fraction surge.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val effectiveEa = if (catalystActive) 42f else 65f
                                val rateMultiplier = exp((temperatureK - 300f) / 45f) * (if (catalystActive) 3.2f else 1.0f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val shift = (temperatureK - 250f) / 500f
                                        val peakX = w * (0.28f + shift * 0.22f)
                                        val peakY = h * (0.18f + shift * 0.16f)

                                        val curvePath = Path().apply {
                                            moveTo(20f, h - 24f)
                                            cubicTo(
                                                peakX * 0.6f, peakY,
                                                peakX * 1.2f, peakY,
                                                w - 24f, h - 24f
                                            )
                                        }
                                        drawPath(curvePath, color = Color(0xFFF59E0B), style = Stroke(width = 3.dp.toPx()))

                                        // Uncatalyzed Ea line
                                        val eaUncatX = w * 0.68f
                                        drawLine(
                                            color = Color(0xFFEF4444),
                                            start = Offset(eaUncatX, 20f),
                                            end = Offset(eaUncatX, h - 24f),
                                            strokeWidth = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                        )

                                        // Catalyzed Ea line
                                        if (catalystActive) {
                                            val eaCatX = w * 0.50f
                                            drawLine(
                                                color = Color(0xFF10B981),
                                                start = Offset(eaCatX, 20f),
                                                end = Offset(eaCatX, h - 24f),
                                                strokeWidth = 2.5.dp.toPx()
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("Ea = ${effectiveEa.toInt()} kJ/mol", if (catalystActive) Color(0xFF10B981) else Color(0xFFEF4444))
                                        TelemetryChip("Rate k = ${String.format(Locale.ENGLISH, "%.1fx", rateMultiplier)}", Color(0xFFF59E0B))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Positive Catalyst (Lowers Ea Pathway)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = catalystActive,
                                        onCheckedChange = { catalystActive = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                                    )
                                }

                                Text("Absolute Temperature (T): ${temperatureK.toInt()} K (${(temperatureK - 273).toInt()} °C)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = temperatureK,
                                    onValueChange = { temperatureK = it },
                                    valueRange = 273f..600f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                                )

                                FormulaTakeawayBar("k = A · e^(-Ea / RT)  |  Catalyst alters Ea & A, NOT ΔG or K_eq")
                            } else {
                                // SIM 2: 3D VSEPR & HYBRIDIZATION GEOMETRY
                                Text("VSEPR Molecular Geometry & Lone Pair Repulsion", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Select hybridization and lone pairs (LP) to inspect bond angle compression (LP-LP > LP-BP > BP-BP).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val geometryInfo = when (selectedHybridization) {
                                    "sp" -> Triple("Linear", "180°", "BeCl₂, CO₂, C₂H₂")
                                    "sp²" -> if (lonePairsCount == 0) Triple("Trigonal Planar", "120°", "BF₃, SO₃, C₂H₄") else Triple("Bent / V-Shape", "119°", "SO₂, O₃, SnCl₂")
                                    "sp³" -> when (lonePairsCount) {
                                        0 -> Triple("Tetrahedral", "109.5°", "CH₄, CCl₄, NH₄⁺")
                                        1 -> Triple("Trigonal Pyramidal", "107°", "NH₃, PCl₃, H₃O⁺")
                                        else -> Triple("Bent / Angular", "104.5°", "H₂O, H₂S, OF₂")
                                    }
                                    "sp³d" -> when (lonePairsCount) {
                                        0 -> Triple("Trigonal Bipyramidal", "90° & 120°", "PCl₅, PF₅")
                                        1 -> Triple("See-Saw", "<120° & <90°", "SF₄, XeO₂F₂")
                                        else -> Triple("T-Shaped", "87.5°", "ClF₃, BrF₃")
                                    }
                                    else -> when (lonePairsCount) {
                                        0 -> Triple("Octahedral", "90°", "SF₆, TeF₆")
                                        1 -> Triple("Square Pyramidal", "84.8°", "BrF₅, XeOF₄")
                                        else -> Triple("Square Planar", "90°", "XeF₄, [PtCl₄]²⁻")
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val totalDomains = when (selectedHybridization) {
                                            "sp" -> 2
                                            "sp²" -> 3
                                            "sp³" -> 4
                                            "sp³d" -> 5
                                            else -> 6
                                        }
                                        val radius = 58.dp.toPx()

                                        for (i in 0 until totalDomains) {
                                            val angleRad = (2 * PI * i / totalDomains) - (PI / 2)
                                            val endX = cx + (cos(angleRad) * radius).toFloat()
                                            val endY = cy + (sin(angleRad) * radius).toFloat()
                                            val isLonePair = i < lonePairsCount

                                            if (isLonePair) {
                                                // Draw Lone Pair Lobe
                                                drawCircle(
                                                    color = Color(0xFFF59E0B).copy(alpha = 0.28f),
                                                    radius = 18.dp.toPx(),
                                                    center = Offset((cx + endX) / 2f, (cy + endY) / 2f)
                                                )
                                                drawCircle(
                                                    color = Color(0xFFF59E0B),
                                                    radius = 4.dp.toPx(),
                                                    center = Offset(endX - 5f, endY)
                                                )
                                                drawCircle(
                                                    color = Color(0xFFF59E0B),
                                                    radius = 4.dp.toPx(),
                                                    center = Offset(endX + 5f, endY)
                                                )
                                            } else {
                                                // Draw Sigma Bond & Terminal Atom
                                                drawLine(
                                                    color = Color(0xFF94A3B8),
                                                    start = Offset(cx, cy),
                                                    end = Offset(endX, endY),
                                                    strokeWidth = 3.dp.toPx()
                                                )
                                                drawCircle(
                                                    color = Color(0xFF10B981),
                                                    radius = 12.dp.toPx(),
                                                    center = Offset(endX, endY)
                                                )
                                            }
                                        }

                                        // Central Atom
                                        drawCircle(
                                            color = Color(0xFF0070F3),
                                            radius = 18.dp.toPx(),
                                            center = Offset(cx, cy)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip(geometryInfo.first, Color(0xFF10B981))
                                        TelemetryChip("Angle: ${geometryInfo.second}", Color(0xFF38BDF8))
                                    }

                                    Text(
                                        text = "Examples: ${geometryInfo.third}",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Select Orbital Hybridization:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("sp", "sp²", "sp³", "sp³d", "sp³d²")) { hyb ->
                                        FilterChip(
                                            selected = selectedHybridization == hyb,
                                            onClick = {
                                                selectedHybridization = hyb
                                                if (hyb == "sp") lonePairsCount = 0
                                            },
                                            label = { Text(hyb, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                if (selectedHybridization != "sp") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Lone Pairs on Central Atom: $lonePairsCount LP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val maxLp = if (selectedHybridization == "sp²") 1 else 2
                                        (0..maxLp).forEach { lp ->
                                            FilterChip(
                                                selected = lonePairsCount == lp,
                                                onClick = { lonePairsCount = lp },
                                                label = { Text("$lp Lone Pair(s)", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }

                                FormulaTakeawayBar("Steric Number = ½ [V + M - C + A]  |  Repulsion: LP-LP > LP-BP > BP-BP")
                            }
                        }

                        "Maths" -> {
                            if (selectedSimIndex == 0) {
                                // SIM 1: CALCULUS WAVE, DERIVATIVE & TANGENT
                                Text("Harmonic Wave f(x) & Live Derivative Tangent f'(x)", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Inspect how the instantaneous slope f'(x₀) = Aω·cos(ωx₀) changes along the wave.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val yAtX0 = waveAmp * sin(waveFreq * inspectX)
                                val slopeAtX0 = waveAmp * waveFreq * cos(waveFreq * inspectX)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val centerY = size.height / 2f
                                        val scaleX = size.width / (2f * PI.toFloat())
                                        val scaleY = 42.dp.toPx()

                                        // X-axis
                                        drawLine(
                                            color = Color(0xFF334155),
                                            start = Offset(0f, centerY),
                                            end = Offset(size.width, centerY),
                                            strokeWidth = 1.5.dp.toPx()
                                        )

                                        // Primary curve f(x) = A sin(wx)
                                        val sinPath = Path().apply {
                                            moveTo(0f, centerY)
                                            for (px in 0..size.width.toInt() step 3) {
                                                val xVal = px / scaleX
                                                val yVal = waveAmp * sin(waveFreq * xVal)
                                                lineTo(px.toFloat(), centerY - yVal * scaleY)
                                            }
                                        }
                                        drawPath(sinPath, color = Color(0xFF10B981), style = Stroke(width = 3.dp.toPx()))

                                        // Derivative curve f'(x) = A*w*cos(wx)
                                        if (showDerivative) {
                                            val cosPath = Path().apply {
                                                for (px in 0..size.width.toInt() step 3) {
                                                    val xVal = px / scaleX
                                                    val dyVal = (waveAmp * waveFreq * cos(waveFreq * xVal)).coerceIn(-2.2f, 2.2f)
                                                    val screenY = centerY - dyVal * (scaleY * 0.6f)
                                                    if (px == 0) moveTo(0f, screenY) else lineTo(px.toFloat(), screenY)
                                                }
                                            }
                                            drawPath(
                                                path = cosPath,
                                                color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                                                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                                            )
                                        }

                                        // Tangent line at inspectX
                                        val ptX = inspectX * scaleX
                                        val ptY = centerY - yAtX0 * scaleY
                                        val dx = 0.65f
                                        val x1 = (inspectX - dx) * scaleX
                                        val y1 = centerY - (yAtX0 - slopeAtX0 * dx) * scaleY
                                        val x2 = (inspectX + dx) * scaleX
                                        val y2 = centerY - (yAtX0 + slopeAtX0 * dx) * scaleY
                                        drawLine(
                                            color = Color(0xFFF59E0B),
                                            start = Offset(x1, y1),
                                            end = Offset(x2, y2),
                                            strokeWidth = 2.5.dp.toPx()
                                        )
                                        drawCircle(color = Color(0xFFF59E0B), radius = 5.dp.toPx(), center = Offset(ptX, ptY))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("f(x₀) = ${String.format(Locale.ENGLISH, "%.2f", yAtX0)}", Color(0xFF10B981))
                                        TelemetryChip("Slope f'(x₀) = ${String.format(Locale.ENGLISH, "%.2f", slopeAtX0)}", Color(0xFFF59E0B))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Inspection Point (x₀): ${String.format(Locale.ENGLISH, "%.2f", inspectX)} rad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = inspectX,
                                    onValueChange = { inspectX = it },
                                    valueRange = 0.2f..5.8f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                                )

                                Text("Angular Frequency (ω): ${String.format(Locale.ENGLISH, "%.1f", waveFreq)} rad/s", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = waveFreq,
                                    onValueChange = { waveFreq = it },
                                    valueRange = 0.5f..3.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                                )

                                FormulaTakeawayBar("d/dx [A·sin(ωx)] = Aω·cos(ωx)  |  Critical points where f'(x₀) = 0")
                            } else {
                                // SIM 2: CONIC SECTIONS & ECCENTRICITY
                                val conicName = when {
                                    eccentricity < 0.08f -> "Circle (e = 0)"
                                    eccentricity < 0.96f -> "Ellipse (0 < e < 1)"
                                    eccentricity <= 1.04f -> "Parabola (e = 1)"
                                    else -> "Hyperbola (e > 1)"
                                }
                                Text("Conic Sections Morphing Lab: $conicName", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Slide eccentricity (e = PS / PM) from 0 to 2.0 to watch the locus transform.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        // Directrix line
                                        drawLine(
                                            color = Color(0xFFEF4444).copy(alpha = 0.7f),
                                            start = Offset(36.dp.toPx(), 16.dp.toPx()),
                                            end = Offset(36.dp.toPx(), size.height - 16.dp.toPx()),
                                            strokeWidth = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                        )

                                        when {
                                            eccentricity < 0.96f -> {
                                                val rx = 68.dp.toPx()
                                                val ry = (rx * kotlin.math.sqrt((1f - eccentricity * eccentricity).coerceAtLeast(0.08f)))
                                                drawOval(
                                                    color = Color(0xFF38BDF8),
                                                    topLeft = Offset(cx - rx, cy - ry),
                                                    size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                                                    style = Stroke(width = 3.dp.toPx())
                                                )
                                            }
                                            eccentricity <= 1.04f -> {
                                                val pPath = Path().apply {
                                                    moveTo(cx + 80.dp.toPx(), cy - 65.dp.toPx())
                                                    quadraticTo(cx - 50.dp.toPx(), cy, cx + 80.dp.toPx(), cy + 65.dp.toPx())
                                                }
                                                drawPath(pPath, color = Color(0xFF10B981), style = Stroke(width = 3.dp.toPx()))
                                            }
                                            else -> {
                                                val hPath1 = Path().apply {
                                                    moveTo(cx + 75.dp.toPx(), cy - 68.dp.toPx())
                                                    quadraticTo(cx + 10.dp.toPx(), cy, cx + 75.dp.toPx(), cy + 68.dp.toPx())
                                                }
                                                val hPath2 = Path().apply {
                                                    moveTo(cx - 75.dp.toPx(), cy - 68.dp.toPx())
                                                    quadraticTo(cx - 10.dp.toPx(), cy, cx - 75.dp.toPx(), cy + 68.dp.toPx())
                                                }
                                                drawPath(hPath1, color = Color(0xFFF59E0B), style = Stroke(width = 3.dp.toPx()))
                                                drawPath(hPath2, color = Color(0xFFF59E0B), style = Stroke(width = 3.dp.toPx()))
                                            }
                                        }
                                        // Focus point S
                                        drawCircle(color = Color(0xFFF59E0B), radius = 5.dp.toPx(), center = Offset(cx + (eccentricity * 24.dp.toPx()), cy))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip(conicName, Color(0xFF38BDF8))
                                        TelemetryChip("e = ${String.format(Locale.ENGLISH, "%.2f", eccentricity)}", Color(0xFFF59E0B))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Eccentricity (e): ${String.format(Locale.ENGLISH, "%.2f", eccentricity)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = eccentricity,
                                    onValueChange = { eccentricity = it },
                                    valueRange = 0.0f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8))
                                )
                                FormulaTakeawayBar("Ellipse: b² = a²(1 - e²)  |  Hyperbola: b² = a²(e² - 1)")
                            }
                        }

                        else -> {
                            if (selectedSimIndex == 0) {
                                // SIM 1: MITOSIS CHROMOSOME MICROSCOPE
                                Text("Mitosis Spindle & Chromosome Alignment Microscope", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Select phase & spindle fiber tension to visualize chromatid separation.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val cellRadius = 68.dp.toPx()

                                        // Cell Membrane
                                        drawCircle(
                                            color = Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                            radius = cellRadius,
                                            center = Offset(cx, cy),
                                            style = Stroke(width = 3.dp.toPx())
                                        )

                                        // Centrosome poles
                                        val leftPole = Offset(cx - cellRadius * 0.78f, cy)
                                        val rightPole = Offset(cx + cellRadius * 0.78f, cy)
                                        drawCircle(color = Color(0xFFF59E0B), radius = 6.dp.toPx(), center = leftPole)
                                        drawCircle(color = Color(0xFFF59E0B), radius = 6.dp.toPx(), center = rightPole)

                                        val sepOffset = when (bioStage) {
                                            "Prophase" -> 0f
                                            "Metaphase" -> 0f
                                            "Anaphase" -> 26.dp.toPx() * spindleTension
                                            else -> 42.dp.toPx()
                                        }

                                        for (idx in -1..1) {
                                            val chrY = cy + idx * 24.dp.toPx()
                                            // Spindle lines
                                            drawLine(
                                                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                                                start = leftPole,
                                                end = Offset(cx - sepOffset, chrY),
                                                strokeWidth = 1.5.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                            )
                                            drawLine(
                                                color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                                                start = rightPole,
                                                end = Offset(cx + sepOffset, chrY),
                                                strokeWidth = 1.5.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                            )

                                            // Chromatids
                                            drawCircle(
                                                color = Color(0xFFEC4899),
                                                radius = 7.dp.toPx(),
                                                center = Offset(cx - sepOffset, chrY)
                                            )
                                            if (sepOffset > 2f) {
                                                drawCircle(
                                                    color = Color(0xFF10B981),
                                                    radius = 7.dp.toPx(),
                                                    center = Offset(cx + sepOffset, chrY)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("Phase: $bioStage", Color(0xFF8B5CF6))
                                        TelemetryChip(if (bioStage == "Anaphase") "2n → 4n Chromatids Split" else "Equatorial / Pole View", Color(0xFFEC4899))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("Prophase", "Metaphase", "Anaphase", "Telophase")) { stage ->
                                        FilterChip(
                                            selected = bioStage == stage,
                                            onClick = { bioStage = stage },
                                            label = { Text(stage, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF8B5CF6),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                if (bioStage == "Anaphase") {
                                    Text("Kinetochore Spindle Pull Tension: ${(spindleTension * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = spindleTension,
                                        onValueChange = { spindleTension = it },
                                        valueRange = 0.2f..1.0f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEC4899), activeTrackColor = Color(0xFFEC4899))
                                    )
                                }

                                FormulaTakeawayBar(
                                    when (bioStage) {
                                        "Prophase" -> "NCERT Key: Chromosomal material condenses; mitotic apparatus assembles."
                                        "Metaphase" -> "NCERT Key: Best stage to study chromosome morphology; kinetochores attach at equator."
                                        "Anaphase" -> "NCERT Key: Centromeres split and chromatids move to opposite poles (leading edge: centromere)."
                                        else -> "NCERT Key: ER, Golgi, nucleolus & nuclear envelope reform at opposite poles."
                                    }
                                )
                            } else {
                                // SIM 2: ENZYME KINETICS (MICHAELIS-MENTEN)
                                Text("Enzyme Kinetics: Michaelis-Menten Vmax & Km Lab", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Compare Competitive vs Non-Competitive Inhibition on Vmax and apparent Km.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                val vMax = if (inhibitorType == "Non-Competitive") 55f else 100f
                                val kM = if (inhibitorType == "Competitive") 4.5f else 1.8f
                                val currentV0 = (vMax * substrateConc) / (kM + substrateConc)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF090F1C))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width - 36.dp.toPx()
                                        val h = size.height - 32.dp.toPx()
                                        val ox = 24.dp.toPx()
                                        val oy = size.height - 20.dp.toPx()

                                        // Vmax asymptote line
                                        val vMaxY = oy - (vMax / 110f) * h
                                        drawLine(
                                            color = Color(0xFF94A3B8).copy(alpha = 0.5f),
                                            start = Offset(ox, vMaxY),
                                            end = Offset(ox + w, vMaxY),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                        )

                                        val mmPath = Path().apply {
                                            moveTo(ox, oy)
                                            for (step in 1..60) {
                                                val s = (step / 60f) * 10f
                                                val v = (vMax * s) / (kM + s)
                                                lineTo(ox + (s / 10f) * w, oy - (v / 110f) * h)
                                            }
                                        }
                                        drawPath(mmPath, color = Color(0xFF10B981), style = Stroke(width = 3.dp.toPx()))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryChip("V₀ = ${String.format(Locale.ENGLISH, "%.1f", currentV0)}%", Color(0xFF10B981))
                                        TelemetryChip("Vmax = ${vMax.toInt()}% | Km = $kM mM", Color(0xFF38BDF8))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Inhibitor Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("None", "Competitive", "Non-Competitive").forEach { mode ->
                                        FilterChip(
                                            selected = inhibitorType == mode,
                                            onClick = { inhibitorType = mode },
                                            label = { Text(mode, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Substrate Concentration [S]: ${String.format(Locale.ENGLISH, "%.1f", substrateConc)} mM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = substrateConc,
                                    onValueChange = { substrateConc = it },
                                    valueRange = 0.5f..10.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                                )

                                FormulaTakeawayBar("V₀ = (Vmax · [S]) / (Km + [S])  |  Competitive: Km ↑, Vmax same (e.g., Malonate on Succinate dehydrogenase)")
                            }
                        }
                    }
                }
            }
        }

        // Interactive Lab Concept Check Card
        item {
            val quizData = when (selectedSubject) {
                "Physics" -> Triple(
                    "Lab Check: If a dielectric slab of κ = 4 is inserted fully between two charges, the electrostatic force becomes:",
                    listOf("4 times larger", "1/4th of vacuum force", "1/16th of vacuum force", "Remains unchanged"),
                    1
                )
                "Chemistry" -> Triple(
                    "Lab Check: Adding a positive catalyst to a reversible reaction alters which of the following?",
                    listOf("Gibbs Free Energy (ΔG)", "Equilibrium Constant (K_eq)", "Activation Energy (Ea) of both forward & backward equally", "Enthalpy of Reaction (ΔH)"),
                    2
                )
                "Maths" -> Triple(
                    "Lab Check: The eccentricity (e) of a rectangular hyperbola x² - y² = a² is equal to:",
                    listOf("e = 1", "e = √2", "e = 1/√2", "e = 2"),
                    1
                )
                else -> Triple(
                    "Lab Check: In competitive enzyme inhibition (e.g. inhibition of succinic dehydrogenase by malonate):",
                    listOf("Km increases & Vmax remains unchanged", "Km decreases & Vmax decreases", "Km remains unchanged & Vmax decreases", "Both Km and Vmax increase"),
                    0
                )
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                                Text("Instant Lab Concept Verification · preview", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B5CF6))
                    }
                    Text(quizData.first, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                    quizData.second.forEachIndexed { idx, opt ->
                        val isPicked = selectedQuizAnswer == idx
                        val isCorrect = idx == quizData.third
                        val bg = when {
                            !isPicked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            isCorrect -> Color(0xFF10B981).copy(alpha = 0.18f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.18f)
                        }
                        val borderCol = when {
                            !isPicked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            isCorrect -> Color(0xFF10B981)
                            else -> Color(0xFFEF4444)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            border = BorderStroke(1.dp, borderCol),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuizAnswer = idx
                                    if (isCorrect) {
                                        viewModel.boostUserScore(points = 15, problems = 1)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${('A' + idx)}. $opt", fontSize = 12.sp, fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Medium)
                                if (isPicked) {
                                    Text(
                                        text = if (isCorrect) "✓ Correct" else "✗ Try Again",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
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

@Composable
private fun TelemetryChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FormulaTakeawayBar(formulaText: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0070F3).copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = formulaText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0070F3),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// ============================================================================
// 4. ADVANCED AI SYLLABUS & SMART PATH (Readiness Predictor + Prerequisites + Tuner)
// ============================================================================
@Composable
fun AdvancedAiSyllabusView(
    uiState: JeeUiState,
    viewModel: JeeViewModel
) {
    val activeChapters by viewModel.activeChapters.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val batchTargetDays by viewModel.batchTargetDays.collectAsState()
    val batchDailyHours by viewModel.batchDailyHours.collectAsState()
    val version by viewModel.version.collectAsState()

    var selectedSyllabusTab by remember { mutableIntStateOf(0) } // 0: AI High-Yield Path, 1: My Custom Syllabus, 2: AI Pacing Tuner
    var subjectFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val pinnedChapterIds = remember { mutableStateListOf<Int>() }

    // Compute live readiness metrics
    val completedChaptersCount = remember(activeChapters, version) {
        activeChapters.count { viewModel.getChapterChain(it.id).completedCount() >= 7 || viewModel.isBatchChapterComplete(it.title) }
    }
    val totalChaptersCount = activeChapters.size.coerceAtLeast(1)
    val masteryPct = ((completedChaptersCount.toFloat() / totalChaptersCount) * 100).toInt().coerceIn(0, 100)

    val filteredChapters = remember(activeChapters, subjectFilter, searchQuery, selectedSyllabusTab, pinnedChapterIds.size, version) {
        val base = activeChapters.filter { chap ->
            val matchesSub = subjectFilter == "All" || chap.subject.contains(subjectFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || chap.title.contains(searchQuery, ignoreCase = true) || chap.questionFocus.contains(searchQuery, ignoreCase = true)
            matchesSub && matchesSearch
        }
        if (selectedSyllabusTab == 0) {
            // Sort by High Priority & incomplete first so student sees highest ROI next chapters
            base.sortedWith(
                compareByDescending<ChapterItem> { pinnedChapterIds.contains(it.id) }
                    .thenByDescending { it.priority.equals("HIGH", ignoreCase = true) }
                    .thenBy { viewModel.getChapterChain(it.id).completedCount() }
            )
        } else {
            base.sortedByDescending { pinnedChapterIds.contains(it.id) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // AI Exam Readiness & Rank Predictor Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI SYLLABUS READINESS • ${selectedBatch.title.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "$completedChaptersCount / $totalChaptersCount High-Yield Chapters Mastered ($masteryPct%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (masteryPct >= 70) "99.2+ %ile Pace" else if (masteryPct >= 40) "97.5+ %ile Pace" else "Building Base",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (masteryPct / 100f).coerceIn(0.05f, 1f) },
                        color = Color(0xFF38BDF8),
                        trackColor = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sprint Target: $batchTargetDays Days • ${batchDailyHours.toInt()}h/day",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${pinnedChapterIds.size} Priority Pinned",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // 3 Mode Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = selectedSyllabusTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                contentColor = Color(0xFF0070F3),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSyllabusTab == 0,
                    onClick = { selectedSyllabusTab = 0 },
                    text = { Text("🚀 AI Smart Path", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedSyllabusTab == 1,
                    onClick = { selectedSyllabusTab = 1 },
                    text = { Text("🎯 Custom Syllabus", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedSyllabusTab == 2,
                    onClick = { selectedSyllabusTab = 2 },
                    text = { Text("⚙️ Pacing Tuner", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        if (selectedSyllabusTab == 2) {
            // TAB 2: AI PACING & AUTO-PLANNER TUNER
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("AI Curriculum Pacing & Backlog Tuner", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Recalculate your daily slots, NCERT pipeline & backlog buffers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text("Sprint Target Duration: $batchTargetDays Days", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = batchTargetDays.toFloat(),
                            onValueChange = { viewModel.setBatchTargetDays(it.toInt()) },
                            valueRange = 30f..150f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0070F3), activeTrackColor = Color(0xFF0070F3))
                        )

                        Text("Daily Focused Study Hours: ${batchDailyHours.toInt()} Hours/Day", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = batchDailyHours,
                            onValueChange = { viewModel.setBatchDailyHours(it) },
                            valueRange = 4f..16f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeAutoPlan() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-Generate AI Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.rescheduleMissedSlotsToUpcomingDays(uiState.selectedDay, 3) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fix Missed Slots", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.syncAllPendingNcertToSyllabusAndPlanner() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1-Tap Sync Pending NCERT → Syllabus → Today's Planner", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Search & Subject Filter Bar for AI Smart Path & Custom Syllabus
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search chapter, topic, or PYQ focus...", fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0070F3),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("All", "Physics", "Chemistry", "Maths", "Biology")) { sub ->
                            val selected = subjectFilter == sub
                            FilterChip(
                                selected = selected,
                                onClick = { subjectFilter = sub },
                                label = { Text(sub, fontSize = 11.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0070F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Chapter Cards with Prerequisite Insights, 10-Stage Milestone Bar & Direct Actions
            items(filteredChapters, key = { it.id }) { chapter ->
                val chain = remember(chapter.id, version) { viewModel.getChapterChain(chapter.id) }
                val completedMilestones = chain.completedCount()
                val isComplete = remember(chapter.title, completedMilestones, version) {
                    completedMilestones >= 10 || viewModel.isBatchChapterComplete(chapter.title)
                }
                val isPinned = pinnedChapterIds.contains(chapter.id)
                val prerequisiteHint = remember(chapter.title, chapter.subject) {
                    when {
                        chapter.title.contains("Electro", ignoreCase = true) -> "Prerequisite: Vectors & Coulomb's Law • Unlocks: Capacitance & Current"
                        chapter.title.contains("Current", ignoreCase = true) -> "Prerequisite: Electrostatics • Unlocks: Moving Charges & Magnetism"
                        chapter.title.contains("Integrat", ignoreCase = true) -> "Prerequisite: Continuity & Differentiation • Unlocks: Differential Equations"
                        chapter.title.contains("Organic", ignoreCase = true) || chapter.title.contains("Halo", ignoreCase = true) -> "Prerequisite: GOC & Isomerism • Unlocks: Aldehydes, Ketones & Amines"
                        chapter.title.contains("Cell", ignoreCase = true) || chapter.title.contains("Genet", ignoreCase = true) -> "Prerequisite: NCERT Biomolecules • Unlocks: Molecular Basis of Inheritance"
                        else -> "AI Sequence: Complete Lecture + DPP before attempting 5-Year PYQ Sprint"
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPinned) Color(0xFF0070F3).copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isPinned -> Color(0xFF0070F3)
                            isComplete -> Color(0xFF10B981).copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isComplete) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF0070F3).copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isComplete) "✓" else chapter.id.toString().takeLast(2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isComplete) Color(0xFF10B981) else Color(0xFF0070F3)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${chapter.subject} • ${chapter.lectures} • ${chapter.durationHours}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isPinned) pinnedChapterIds.remove(chapter.id) else pinnedChapterIds.add(chapter.id)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Pin Chapter",
                                    tint = if (isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (chapter.priority.equals("HIGH", ignoreCase = true)) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (chapter.priority.equals("HIGH", ignoreCase = true)) "HIGH ROI" else "CORE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (chapter.priority.equals("HIGH", ignoreCase = true)) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // AI Prerequisite & Focus Insight
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = "🔗 $prerequisiteHint",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "🎯 Focus: ${chapter.questionFocus} • Milestones: $completedMilestones/10 done",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3)
                                )
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.addToPracticeQueue(
                                        chapter = chapter,
                                        priority = "HIGH",
                                        focusArea = "AI Recommended PYQ + Concept Sprint",
                                        targetMinutes = 90
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Practice Stack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.addCustomDailyGoal(
                                        title = "[AI Path] ${chapter.subject}: ${chapter.title}",
                                        category = chapter.subject,
                                        targetHours = 2.5f,
                                        priority = "HIGH"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Today's Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleBatchChapterComplete(chapter.title)
                                    viewModel.markFullChapterChain(chapter.id, !isComplete)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isComplete) Color(0xFF10B981) else Color(0xFF0070F3)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isComplete) "✓ Done" else "Mark Done",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
