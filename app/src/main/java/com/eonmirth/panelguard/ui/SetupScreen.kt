package com.eonmirth.panelguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.ui.theme.PanelGuardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    oemFamily: OemFamily,
    initialConfirmed: Boolean = false,
    onBackClicked: () -> Unit,
    onOpenSettingsClicked: () -> Unit,
    onConfirmationChanged: (Boolean) -> Unit,
    onFinishSetupClicked: () -> Unit
) {
    var isConfirmed by remember { mutableStateOf(initialConfirmed) }
    val steps = oemFamily.getGuidanceSteps()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Protection") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Device Guidance: ${oemFamily.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (oemFamily.hasKnownLockScreenShadeSetting) {
                            "Follow these official vendor steps to lock down the shade and quick settings on your lock screen."
                        } else {
                            "Stock Android guidance: Third-party apps cannot force shade lockdown. Follow official system recommendations below."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "Step-by-Step Instructions:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            steps.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Button(
                onClick = onOpenSettingsClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Device Settings")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isConfirmed,
                        onCheckedChange = { checked ->
                            isConfirmed = checked
                            onConfirmationChanged(checked)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I have configured the setting in system settings",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onFinishSetupClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = isConfirmed || !oemFamily.hasKnownLockScreenShadeSetting
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Return to Dashboard")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    PanelGuardTheme {
        SetupScreen(
            oemFamily = OemFamily.SAMSUNG,
            initialConfirmed = false,
            onBackClicked = {},
            onOpenSettingsClicked = {},
            onConfirmationChanged = {},
            onFinishSetupClicked = {}
        )
    }
}
