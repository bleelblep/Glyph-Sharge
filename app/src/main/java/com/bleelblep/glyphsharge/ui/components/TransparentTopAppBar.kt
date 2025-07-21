package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.ui.utils.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    // Simple and precise scroll detection - transparent ONLY when exactly at original position
    val isScrolled by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
        }
    }
    
    val appBarBackgroundColor = if (isScrolled) {
        MaterialTheme.colorScheme.surface // Solid when any scroll at all
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.0f) // Transparent only when exactly at original position
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = { 
                HapticUtils.triggerLightFeedback(haptic, context)
                onBackClick() 
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarBackgroundColor
        ),
        windowInsets = WindowInsets.statusBars,
        modifier = modifier
    )
} 