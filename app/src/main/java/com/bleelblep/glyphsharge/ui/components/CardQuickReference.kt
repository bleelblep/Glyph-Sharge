package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Quick reference for common card patterns in GlyphZen
 * Copy and modify these examples for your specific needs
 */

// PATTERN 1: Feature Card with Action
@Composable
fun FeatureCard() {
    ActionCard(
        title = "Feature Name",
        description = "Brief description of what this feature does",
        actionText = "Use Feature",
        onActionClick = { /* Navigate or trigger action */ }
    )
}

// PATTERN 2: Settings Card
@Composable
fun SettingsCard() {
    IconCard(
        title = "Settings Category",
        subtitle = "Configure your preferences",
        icon = Icons.Default.Settings,
        actionText = "Configure",
        onCardClick = { /* Navigate to settings */ },
        onActionClick = { /* Quick action */ }
    )
}

// PATTERN 3: Status/Information Card
@Composable
fun StatusCard() {
    SimpleCard(
        title = "Current Status",
        description = "Everything is working normally",
        onClick = { /* Show details */ }
    )
}

// PATTERN 4: Statistics Card
@Composable
fun StatsCard() {
    ContentCard(
        title = "Your Progress",
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Total", "42")
                StatItem("Today", "3")
                StatItem("Streak", "7")
            }
        }
    )
}

// PATTERN 5: Control Card
@Composable
fun ControlCard() {
    StandardCard(
        title = "Glyph Control",
        icon = Icons.Default.Star,
        description = "Control your device's glyph lights",
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Turn on */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("On")
                }
                OutlinedButton(
                    onClick = { /* Turn off */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Off")
                }
            }
        }
    )
}

// PATTERN 6: Notification Card
@Composable
fun NotificationCard() {
    StandardCard(
        title = "Notification Title",
        description = "Notification message content goes here",
        icon = Icons.Default.Notifications,
        actionText = "View",
        onActionClick = { /* Handle notification */ }
    )
}

// PATTERN 7: Progress Card
@Composable
fun ProgressCard() {
    ContentCard(
        title = "Session Progress",
        content = {
            Text(
                text = "5 minutes remaining",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearWavyProgressIndicator(
                progress = 0.6f,
                modifier = Modifier.fillMaxWidth(),
                amplitude = 0.8f,
                wavelength = 32.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Pause/Resume */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pause")
            }
        }
    )
}

// PATTERN 8: List Item Card (for use in LazyColumn)
@Composable
fun ListItemCard() {
    SimpleCard(
        title = "List Item Title",
        description = "Brief description",
        onClick = { /* Handle selection */ }
    )
}

// PATTERN 9: Empty State Card
@Composable
fun EmptyStateCard() {
    IconCard(
        title = "No Data Available",
        subtitle = "Get started by adding your first item",
        icon = Icons.Default.Add,
        actionText = "Add Item",
        onActionClick = { /* Create new item */ }
    )
}

// PATTERN 10: Error State Card
@Composable
fun ErrorCard() {
    IconCard(
        title = "Something went wrong",
        subtitle = "Please try again or contact support",
        icon = Icons.Default.Warning,
        actionText = "Retry",
        onActionClick = { /* Retry action */ }
    )
}

// Helper composable for statistics
@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/*
USAGE TIPS:

1. Choose the right card type:
   - SimpleCard: Basic information display
   - IconCard: When you need visual hierarchy with icons
   - ActionCard: When the primary purpose is to trigger an action
   - ContentCard: When you need custom layouts
   - StandardCard: When you need maximum flexibility

2. Common modifiers to add:
   - .padding(16.dp) for spacing between cards
   - .fillMaxWidth() is already included
   - .clickable() is handled by onClick parameters

3. Accessibility:
   - Cards automatically support click semantics
   - Icons should have meaningful contentDescription when needed
   - Use appropriate text contrast ratios

4. Performance:
   - Cards are optimized for LazyColumn usage
   - Avoid heavy computations in card content lambdas
   - Use remember for expensive operations

5. Customization:
   - Override elevation for special emphasis
   - Adjust contentPadding for edge-to-edge content
   - Use content lambda for complex layouts
*/ 