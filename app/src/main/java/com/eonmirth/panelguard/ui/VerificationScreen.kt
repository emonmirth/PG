package com.eonmirth.panelguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eonmirth.panelguard.domain.Capability
import com.eonmirth.panelguard.domain.CapabilityStatus
import com.eonmirth.panelguard.domain.VerificationResult
import com.eonmirth.panelguard.domain.VerificationStatus
import com.eonmirth.panelguard.ui.theme.*

/**
 * Phase 4 — Verification Screen.
 *
 * Displays the result of [VerificationEvaluator.evaluate] without inferring protection
 * from user actions. Every status badge shown here corresponds to a concrete evidence signal
 * recorded in [VerificationResult.reason].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    result: VerificationResult,
    isRefreshing: Boolean,
    onRefreshClicked: () -> Unit,
    onActionClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onRefreshClicked) {
                            Icon(Icons.Default.Refresh, contentDescription = "Re-evaluate")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Status hero card ──────────────────────────────────────────────────────
            StatusHeroCard(result)

            // ── Evidence reason card ──────────────────────────────────────────────────
            EvidenceCard(reason = result.reason)

            // ── CTA button ────────────────────────────────────────────────────────────
            result.actionLabel?.let { label ->
                Button(
                    onClick = onActionClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(label)
                }
            }

            // ── Capability breakdown ──────────────────────────────────────────────────
            if (result.capabilities.isNotEmpty()) {
                Text(
                    text = "Capability Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                result.capabilities.forEach { cap ->
                    CapabilityRow(capability = cap)
                }
            }

            // ── Disclaimer ────────────────────────────────────────────────────────────
            Text(
                text = "Verification reflects the device state at the time of the last evaluation. " +
                    "Tap the refresh button to re-read current device signals.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusHeroCard(result: VerificationResult) {
    val (icon, iconTint, containerColor) = statusVisuals(result.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.status.displayLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = iconTint
                    )
                }
            }
            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EvidenceCard(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Evidence Signal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CapabilityRow(capability: Capability) {
    val isDark = isSystemInDarkTheme()
    val (statusLabel, statusColor) = capabilityStatusVisuals(capability.status, isDark)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = capability.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = capability.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Evidence: ${capability.confidenceReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class StatusVisuals(
    val icon: ImageVector,
    val tint: Color,
    val containerColor: Color
)

@Composable
private fun statusVisuals(status: VerificationStatus): StatusVisuals {
    val isDark = isSystemInDarkTheme()
    return when (status) {
        VerificationStatus.ACTIVE -> {
            val colors = getActiveStatusColors(isDark)
            StatusVisuals(Icons.Default.CheckCircle, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.MANAGED_ONLY -> {
            val colors = getManagedStatusColors(isDark)
            StatusVisuals(Icons.Default.Shield, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.PARTIAL -> {
            val colors = getPartialStatusColors(isDark)
            StatusVisuals(Icons.Default.Warning, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.SETUP_REQUIRED -> {
            val colors = getPartialStatusColors(isDark)
            StatusVisuals(Icons.Default.Build, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.UNSUPPORTED -> {
            val colors = getInactiveStatusColors(isDark)
            StatusVisuals(Icons.Default.Cancel, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.UNKNOWN -> {
            val colors = getNeutralStatusColors(isDark)
            StatusVisuals(Icons.AutoMirrored.Filled.HelpOutline, colors.contentColor, colors.containerColor)
        }
        VerificationStatus.ERROR -> {
            val colors = getInactiveStatusColors(isDark)
            StatusVisuals(Icons.Default.Error, colors.contentColor, colors.containerColor)
        }
    }
}

private fun VerificationStatus.displayLabel(): String = when (this) {
    VerificationStatus.ACTIVE         -> "✔ Active"
    VerificationStatus.MANAGED_ONLY   -> "✔ Managed — Active"
    VerificationStatus.PARTIAL        -> "⚠ Partial"
    VerificationStatus.SETUP_REQUIRED -> "● Setup Required"
    VerificationStatus.UNSUPPORTED    -> "✗ Not Supported"
    VerificationStatus.UNKNOWN        -> "? Unknown"
    VerificationStatus.ERROR          -> "! Error"
}

private fun capabilityStatusVisuals(status: CapabilityStatus, isDark: Boolean): Pair<String, Color> = when (status) {
    CapabilityStatus.SUPPORTED    -> "Supported"   to getActiveStatusColors(isDark).contentColor
    CapabilityStatus.PARTIAL      -> "Partial"     to getPartialStatusColors(isDark).contentColor
    CapabilityStatus.OEM_GUIDANCE -> "OEM Guide"   to getManagedStatusColors(isDark).contentColor
    CapabilityStatus.MANAGED_ONLY -> "Managed"     to getManagedStatusColors(isDark).contentColor
    CapabilityStatus.UNSUPPORTED  -> "Unsupported" to getInactiveStatusColors(isDark).contentColor
    CapabilityStatus.UNKNOWN      -> "Unknown"     to getNeutralStatusColors(isDark).contentColor
}

@Preview(showBackground = true)
@Composable
fun VerificationScreenPreview() {
    PanelGuardTheme {
        VerificationScreen(
            result = VerificationResult(
                status = VerificationStatus.PARTIAL,
                title = "OEM Setting Enabled (User-Confirmed)",
                summary = "Samsung One UI provides an official system setting to disable the notification shade. You confirmed this setting is active.",
                reason = "Evidence: OemFamily=SAMSUNG, userConfirmedOemSetting=true",
                actionLabel = "Re-evaluate"
            ),
            isRefreshing = false,
            onRefreshClicked = {},
            onActionClicked = {},
            onBackClicked = {}
        )
    }
}
