package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues

/**
 * A simple split-button group recommended in Material 3 Expressive.
 * Each button has a shared container; first/last get rounded ends, middle is square.
 */
@Composable
fun SplitButtonGroup(
    actions: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Row(modifier = modifier) {
        actions.forEachIndexed { index, (label, onClick) ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                actions.lastIndex -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Button(
                onClick = onClick,
                shape = shape,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = label)
            }
            if (index < actions.lastIndex) Spacer(modifier = Modifier.size(1.dp))
        }
    }
}

/**
 * Expressive Extended FAB with a simple dropdown menu (three demo actions).
 */
@Composable
fun ExtendedFabMenu(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        text = { Text("Menu") },
        icon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
        onClick = { expanded = true },
        modifier = modifier
    )
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { (title, callback) ->
            DropdownMenuItem(text = { Text(title) }, onClick = {
                expanded = false
                callback()
            })
        }
    }
}

/**
 * Colorful indeterminate progress indicator tailored for Expressive theme.
 */
@Composable
fun ExpressiveProgressIndicator(modifier: Modifier = Modifier, trackColor: Color = MaterialTheme.colorScheme.primaryContainer) {
    CircularProgressIndicator(
        modifier = modifier.size(40.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = trackColor,
        strokeWidth = 4.dp
    )
} 