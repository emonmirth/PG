package com.eonmirth.panelguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eonmirth.panelguard.domain.Capability
import com.eonmirth.panelguard.domain.CapabilityStatus
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    oemFamily: OemFamily,
    capabilities: List<Capability>,
    onBackClicked: () -> Unit,
    onProceedToSetupClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Capabilities") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detected Device: ${oemFamily.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verified capability evaluation based on official Android platform boundaries and vendor settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(capabilities) { capability ->
                    CapabilityCard(capability = capability)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceedToSetupClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Proceed to Setup Guide")
            }
        }
    }
}

@Composable
fun CapabilityCard(capability: Capability) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = capability.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                CapabilityBadge(status = capability.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = capability.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Recommendation: ${capability.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Verification Basis: ${capability.confidenceReason}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CapabilityBadge(status: CapabilityStatus) {
    val isDark = isSystemInDarkTheme()
    val (statusColors, label, icon) = when (status) {
        CapabilityStatus.SUPPORTED -> Triple(getActiveStatusColors(isDark), "Supported", Icons.Default.CheckCircle)
        CapabilityStatus.OEM_GUIDANCE -> Triple(getManagedStatusColors(isDark), "OEM Setup", Icons.Default.Settings)
        CapabilityStatus.PARTIAL -> Triple(getPartialStatusColors(isDark), "Partial", Icons.Default.Warning)
        CapabilityStatus.MANAGED_ONLY -> Triple(getManagedStatusColors(isDark), "Managed", Icons.Default.AdminPanelSettings)
        CapabilityStatus.UNSUPPORTED -> Triple(getInactiveStatusColors(isDark), "Unsupported", Icons.Default.Block)
        CapabilityStatus.UNKNOWN -> Triple(getNeutralStatusColors(isDark), "Unknown", Icons.AutoMirrored.Filled.HelpOutline)
    }

    Box(
        modifier = Modifier
            .background(statusColors.containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColors.contentColor,
                modifier = Modifier.size(12.dp)
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
fun CompatibilityScreenPreview() {
    PanelGuardTheme {
        CompatibilityScreen(
            oemFamily = OemFamily.SAMSUNG,
            capabilities = listOf(
                Capability(
                    id = "samsung_shade_toggle",
                    title = "Samsung One UI Lock Screen Shade Setting",
                    status = CapabilityStatus.OEM_GUIDANCE,
                    description = "Samsung provides an official toggle under Lock screen > Secure lock settings.",
                    recommendation = "Enable 'Lock network and security' in Samsung system settings.",
                    confidenceReason = "Verified OEM setting key present in One UI documentation."
                )
            ),
            onBackClicked = {},
            onProceedToSetupClicked = {}
        )
    }
}
