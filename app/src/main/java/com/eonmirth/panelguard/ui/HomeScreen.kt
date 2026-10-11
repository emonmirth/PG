package com.eonmirth.panelguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionLevel
import com.eonmirth.panelguard.domain.ProtectionState
import com.eonmirth.panelguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    oemFamily: OemFamily,
    protectionState: ProtectionState,
    managedDeviceOwner: Boolean,
    lockTaskPermitted: Boolean,
    onViewCapabilitiesClicked: () -> Unit,
    onSetupGuideClicked: () -> Unit,
    onVerifyProtectionClicked: () -> Unit,
    onStartManagedLockTask: () -> Unit,
    onExitManagedLockTask: () -> Unit
) {
    var confirmManagedMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("EonMirth Panel Guard")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // ── Device Profile Card ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Device Profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = oemFamily.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // ── Protection Status Card ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Protection Status",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ProtectionStatusBadge(level = protectionState.level)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = protectionState.summary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    protectionState.verifiedReason?.let { reason ->
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Basis: $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Managed Device Card ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (managedDeviceOwner) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Managed Device Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = if (managedDeviceOwner) {
                            if (lockTaskPermitted) {
                                "This app is allow-listed by the device owner."
                            } else {
                                "Device Owner detected. Managed Lock Task is not configured."
                            }
                        } else {
                            "Lock Task is available only on devices provisioned to this app as Device Owner."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (managedDeviceOwner) {
                        Button(
                            onClick = { confirmManagedMode = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start managed Lock Task")
                        }
                        OutlinedButton(
                            onClick = onExitManagedLockTask,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Exit managed mode and restore controls")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Action Buttons ───────────────────────────────────────────────
            Button(
                onClick = onVerifyProtectionClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify Protection")
            }

            OutlinedButton(
                onClick = onViewCapabilitiesClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Detailed Capabilities")
            }

            OutlinedButton(
                onClick = onSetupGuideClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Setup Guide")
            }
        }
    }

    if (confirmManagedMode) {
        AlertDialog(
            onDismissRequest = { confirmManagedMode = false },
            title = { Text("Enter managed Lock Task?") },
            text = {
                Text(
                    "This managed kiosk mode disables system UI features, including the power " +
                        "menu. Use the in-app exit control to leave Lock Task and restore controls."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmManagedMode = false
                        onStartManagedLockTask()
                    }
                ) {
                    Text("Enter managed mode")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmManagedMode = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProtectionStatusBadge(level: ProtectionLevel) {
    val isDark = isSystemInDarkTheme()
    val (statusColors, label, icon) = when (level) {
        ProtectionLevel.ACTIVE, ProtectionLevel.MANAGED_ENFORCED ->
            Triple(getActiveStatusColors(isDark), "PROTECTED", Icons.Default.CheckCircle)
        ProtectionLevel.PARTIAL ->
            Triple(getPartialStatusColors(isDark), "PARTIAL", Icons.Default.Warning)
        ProtectionLevel.NOT_ACTIVE ->
            Triple(getInactiveStatusColors(isDark), "NOT ACTIVE", Icons.Default.Cancel)
        ProtectionLevel.UNSUPPORTED ->
            Triple(getInactiveStatusColors(isDark), "UNSUPPORTED", Icons.Default.Block)
        ProtectionLevel.UNKNOWN ->
            Triple(getNeutralStatusColors(isDark), "UNKNOWN", Icons.AutoMirrored.Filled.HelpOutline)
    }

    Box(
        modifier = Modifier
            .background(statusColors.containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColors.contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = statusColors.contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PanelGuardTheme {
        HomeScreen(
            oemFamily = OemFamily.SAMSUNG,
            protectionState = ProtectionState(
                level = ProtectionLevel.PARTIAL,
                mode = com.eonmirth.panelguard.domain.ProtectionMode.OEM_SETTING,
                summary = "Samsung One UI provides an official lock-screen shade setting. Please confirm in Setup Guide.",
                verifiedReason = "OemFamily=SAMSUNG, userConfirmed=false"
            ),
            managedDeviceOwner = false,
            lockTaskPermitted = false,
            onViewCapabilitiesClicked = {},
            onSetupGuideClicked = {},
            onVerifyProtectionClicked = {},
            onStartManagedLockTask = {},
            onExitManagedLockTask = {}
        )
    }
}
