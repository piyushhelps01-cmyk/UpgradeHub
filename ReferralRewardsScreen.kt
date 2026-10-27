package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ReferralRewardsDashboard
import com.example.data.repository.ReferralRewardsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun ReferralRewardsScreen() {
    val repository = remember { ReferralRewardsRepository() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var dashboard by remember { mutableStateOf<ReferralRewardsDashboard?>(null) }
    var referralInput by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            dashboard = null
            return
        }
        dashboard = repository.dashboard()
    }

    LaunchedEffect(Unit) {
        try {
            refresh()
        } catch (exception: Exception) {
            error = exception.localizedMessage ?: "Rewards data could not be loaded."
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Referral & XP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Progress is calculated by the secure server. XP is not cash and cannot be converted to money.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Sign in to use referrals", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("Referral codes and XP are linked to your verified account, not this device.")
                    }
                }
            }
        } else if (dashboard == null && error == null) {
            item { CircularProgressIndicator() }
        } else {
            val data = dashboard
            if (data != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("XP wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Metric("Lifetime", data.lifetimeXp)
                                Metric("This week", data.weeklyXp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Metric("Study XP", data.studyXp)
                                Metric("Referral XP", data.referralXp)
                            }
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Refer & Earn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "A referred learner needs ${data.requiredDays} separate qualifying UTC days, " +
                                    "with at least ${data.minutesPerDay} eligible minutes on each day. " +
                                    "After review, the referrer receives ${data.referralRewardXp} XP.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("Your referral: ${data.referralState ?: "No referral linked"}")
                            if (data.referralState != null) {
                                Text("Qualifying days: ${data.qualifiedDays}/${data.requiredDays}")
                                Text("Today: ${data.todayEligibleMinutes.coerceAtMost(data.minutesPerDay)}/${data.minutesPerDay} min")
                                LinearProgressIndicator(
                                    progress = (data.qualifiedDays.toFloat() / data.requiredDays.coerceAtLeast(1)).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            if (data.code == null) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            error = null
                                            try {
                                                repository.createReferralCode()
                                                refresh()
                                                message = "Your referral code is ready."
                                            } catch (exception: Exception) {
                                                error = exception.localizedMessage ?: "Could not create a code."
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    },
                                    enabled = !busy
                                ) { Text(if (busy) "Creating…" else "Create my code") }
                            } else {
                                Text("Your code", fontWeight = FontWeight.SemiBold)
                                Text(data.code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                OutlinedButton(onClick = {
                                    clipboard.setText(AnnotatedString(data.code))
                                    message = "Referral code copied."
                                }) { Text("Copy code") }
                            }
                        }
                    }
                }
                if (data.referralState == null) item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Have a referral code?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Codes can be linked only to recently created accounts, and one account can use one code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = referralInput,
                                onValueChange = { referralInput = it.uppercase().filter(Char::isLetterOrDigit).take(12) },
                                label = { Text("Referral code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        error = null
                                        try {
                                            repository.claimReferralCode(referralInput)
                                            referralInput = ""
                                            refresh()
                                            message = "Referral linked. Qualifying days are tracked from validated sessions."
                                        } catch (exception: Exception) {
                                            error = exception.localizedMessage ?: "Could not link that code."
                                        } finally {
                                            busy = false
                                        }
                                    }
                                },
                                enabled = !busy && referralInput.length >= 8
                            ) { Text(if (busy) "Checking…" else "Link code") }
                        }
                    }
                }
                item {
                    StudySessionCard(
                        activeSessionId = data.activeStudySessionId,
                        startedAtMs = data.activeStudySessionStartedAtMs,
                        busy = busy,
                        onStart = {
                            scope.launch {
                                busy = true
                                error = null
                                try {
                                    repository.startStudySession()
                                    refresh()
                                    message = "Session started. The server measures the elapsed time."
                                } catch (exception: Exception) {
                                    error = exception.localizedMessage ?: "Could not start a session."
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        onComplete = {
                            scope.launch {
                                busy = true
                                error = null
                                try {
                                    val sessionId = data.activeStudySessionId ?: return@launch
                                    repository.completeStudySession(sessionId)
                                    refresh()
                                    message = "Session submitted. XP and referral progress were recalculated by the server."
                                } catch (exception: Exception) {
                                    error = exception.localizedMessage ?: "Could not complete this session."
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    )
                }
                if (data.referrals.isNotEmpty()) {
                    item {
                        Text("Referrals you shared", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    itemsIndexed(data.referrals) { index, referral ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("Referral ${index + 1}: ${referral.first.replace('_', ' ')}")
                                Text("Qualifying days: ${referral.second}/${data.requiredDays}")
                            }
                        }
                    }
                }
                if (data.recentLedger.isNotEmpty()) {
                    item { Text("Recent XP ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    itemsIndexed(data.recentLedger) { _, entry ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.category.replaceFirstChar(Char::uppercase))
                                Text("+${entry.amount} XP", fontWeight = FontWeight.Bold)
                            }
                            entry.createdAtMs?.let {
                                Text(
                                    DateFormat.getDateTimeInstance().format(Date(it)),
                                    modifier = Modifier.padding(start = 14.dp, bottom = 12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (message != null) item {
            Text(message!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
        }
        if (error != null) item {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
        }
        item {
            Text(
                "Study time is server-timed and learner-confirmed; it is not independent proof of learning. " +
                    "Referral awards wait for a review and are never cash.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: Int) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudySessionCard(
    activeSessionId: String?,
    startedAtMs: Long?,
    busy: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeSessionId) {
        while (activeSessionId != null) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Study session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Earn up to 10 study XP per confirmed 15-minute session, capped at 30 per UTC day.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            if (activeSessionId == null) {
                Button(onClick = onStart, enabled = !busy) { Text(if (busy) "Please wait…" else "Start session") }
            } else {
                val elapsedMinutes = ((now - (startedAtMs ?: now)).coerceAtLeast(0) / 60_000).toInt()
                Text("Active · $elapsedMinutes min")
                if (elapsedMinutes > 180) {
                    Button(
                        onClick = onStart,
                        enabled = !busy,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text(if (busy) "Please wait…" else "Discard expired and start new") }
                } else {
                    Button(
                        onClick = onComplete,
                        enabled = !busy && elapsedMinutes >= 15,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text(if (busy) "Submitting…" else "Finish and confirm") }
                }
                if (elapsedMinutes < 15) {
                    Text(
                        "The server requires at least 15 minutes before a session can be submitted.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
