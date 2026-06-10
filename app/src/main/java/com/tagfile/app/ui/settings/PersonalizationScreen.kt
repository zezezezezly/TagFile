package com.tagfile.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonalizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStrokeColorPicker by remember { mutableStateOf(false) }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importWallpaper(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个性化") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                "软件背景",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(if (uiState.wallpaperPath != null) "已设置壁纸" else "选择背景图片") },
                supportingContent = { Text("选择一张图片作为软件背景") },
                leadingContent = {
                    Icon(Icons.Default.Image, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp))
                },
                trailingContent = {
                    Row {
                        if (uiState.wallpaperPath != null) {
                            IconButton(onClick = { viewModel.onEvent(PersonalizationEvent.RemoveWallpaper) }) {
                                Icon(Icons.Default.Delete, contentDescription = "移除",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        TextButton(onClick = { wallpaperPickerLauncher.launch("image/*") }) {
                            Text(if (uiState.wallpaperPath != null) "更换" else "选择")
                        }
                    }
                }
            )

            if (uiState.wallpaperPath != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("壁纸透明度", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("${(uiState.wallpaperOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = uiState.wallpaperOpacity,
                        onValueChange = { viewModel.onEvent(PersonalizationEvent.UpdateWallpaperOpacity(it)) },
                        valueRange = 0.02f..1f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ListItem(
                headlineContent = { Text("文字描边") },
                supportingContent = { Text("为所有文字添加描边效果，增强在壁纸上的可读性") },
                leadingContent = {
                    Icon(Icons.Default.BorderStyle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp))
                },
                trailingContent = {
                    Switch(
                        checked = uiState.strokeEnabled,
                        onCheckedChange = { viewModel.onEvent(PersonalizationEvent.ToggleStroke) }
                    )
                }
            )

            AnimatedVisibility(
                visible = uiState.strokeEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    StrokeColorSection(
                        selectedColor = Color(uiState.strokeColor),
                        expanded = showStrokeColorPicker,
                        onToggle = { showStrokeColorPicker = !showStrokeColorPicker },
                        onColorSelected = { viewModel.onEvent(PersonalizationEvent.UpdateStrokeColor(it.toArgb())) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "修改将立即生效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StrokeColorSection(
    selectedColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
                    .background(selectedColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "描边颜色",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "#${selectedColor.toHex()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            PsColorPicker(
                selectedColor = selectedColor,
                onColorSelected = onColorSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

private fun Color.toHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("%02X%02X%02X", r, g, b)
}
