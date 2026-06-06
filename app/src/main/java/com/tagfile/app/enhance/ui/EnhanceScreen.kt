package com.tagfile.app.enhance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhanceScreen(
    imagePath: String,
    onNavigateBack: () -> Unit,
    viewModel: EnhanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(imagePath) {
        viewModel.initialize(imagePath)
    }

    LaunchedEffect(uiState.error) {
        val errorMsg = uiState.error
        if (errorMsg != null) {
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.onEvent(EnhanceEvent.ClearError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "画质增强",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(EnhanceEvent.ResetToDefault) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                    if (uiState.processedBitmapPath != null) {
                        IconButton(onClick = { viewModel.onEvent(EnhanceEvent.SaveResult) }) {
                            Icon(Icons.Default.Save, contentDescription = "保存")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ImagePreviewSection(
                sourcePath = uiState.sourcePath,
                processedPath = uiState.processedBitmapPath,
                showBefore = uiState.showBefore,
                isProcessing = uiState.isProcessing,
                onToggleBeforeAfter = { viewModel.onEvent(EnhanceEvent.ToggleBeforeAfter) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PresetSection(
                selectedPreset = uiState.selectedPreset,
                onSelectPreset = { viewModel.onEvent(EnhanceEvent.SelectPreset(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSection(
                params = uiState.params,
                onStrengthChange = { viewModel.onEvent(EnhanceEvent.UpdateStrength(it)) },
                onSharpnessChange = { viewModel.onEvent(EnhanceEvent.UpdateSharpness(it)) },
                onDenoiseChange = { viewModel.onEvent(EnhanceEvent.UpdateDenoise(it)) },
                onLineDarkeningChange = { viewModel.onEvent(EnhanceEvent.UpdateLineDarkening(it)) },
                onContrastChange = { viewModel.onEvent(EnhanceEvent.UpdateContrast(it)) },
                onSaturationChange = { viewModel.onEvent(EnhanceEvent.UpdateSaturation(it)) },
                onUpscaleFactorChange = { viewModel.onEvent(EnhanceEvent.UpdateUpscaleFactor(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onEvent(EnhanceEvent.ProcessImage) },
                enabled = !uiState.isProcessing && uiState.sourcePath.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("处理中...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始增强")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImagePreviewSection(
    sourcePath: String,
    processedPath: String?,
    showBefore: Boolean,
    isProcessing: Boolean,
    onToggleBeforeAfter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val displayPath = if (showBefore || processedPath == null) sourcePath else processedPath

        if (displayPath.isNotEmpty() && File(displayPath).exists()) {
            AsyncImage(
                model = File(displayPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在增强处理...", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        if (processedPath != null && !showBefore) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "增强后",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        if (processedPath != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onToggleBeforeAfter() }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Compare,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showBefore) "对比：原始 → 增强" else "对比：增强 → 原始",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetSection(
    selectedPreset: PresetType,
    onSelectPreset: (PresetType) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "预设方案",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetType.entries.forEach { preset ->
                FilterChip(
                    selected = selectedPreset == preset,
                    onClick = { onSelectPreset(preset) },
                    label = {
                        Text(
                            preset.label,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ParameterSection(
    params: com.tagfile.app.enhance.domain.model.EnhanceParams,
    onStrengthChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onDenoiseChange: (Float) -> Unit,
    onLineDarkeningChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onUpscaleFactorChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "参数调节",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ParamSlider(
            label = "增强强度",
            value = params.strength,
            onValueChange = onStrengthChange,
            description = "控制整体增强效果的强度"
        )

        ParamSlider(
            label = "锐化程度",
            value = params.sharpness,
            onValueChange = onSharpnessChange,
            description = "增强边缘细节，使画面更清晰"
        )

        ParamSlider(
            label = "降噪强度",
            value = params.denoise,
            onValueChange = onDenoiseChange,
            description = "去除画面噪点和压缩伪影"
        )

        ParamSlider(
            label = "线条加深",
            value = params.lineDarkening,
            onValueChange = onLineDarkeningChange,
            description = "加深漫画线条，使轮廓更清晰"
        )

        ParamSlider(
            label = "对比度",
            value = params.contrast,
            onValueChange = onContrastChange,
            description = "调整画面明暗对比"
        )

        ParamSlider(
            label = "色彩饱和度",
            value = params.saturation,
            onValueChange = onSaturationChange,
            description = "调整画面色彩鲜艳度"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "放大倍率",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 2, 3, 4).forEach { factor ->
                FilterChip(
                    selected = params.upscaleFactor == factor,
                    onClick = { onUpscaleFactorChange(factor) },
                    label = {
                        Text(
                            "${factor}x",
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ParamSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
