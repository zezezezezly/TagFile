package com.tagfile.app.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.ui.common.GlassDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPersonalization: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportToUri(it) }
    }

    // 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.readImportFile(it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SettingsEvent.ClearMessage)
        }
    }

    // 导入模式选择对话框
    if (uiState.showImportModeDialog) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(SettingsEvent.DismissImportModeDialog) },
            title = { Text("导入模式") },
            text = { Text("请选择导入方式：\n\n· 全量替换：清空现有数据后导入\n· 合并：按主键覆盖已有数据") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(SettingsEvent.ConfirmImportMode(isReplace = true))
                }) {
                    Text("全量替换")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.onEvent(SettingsEvent.ConfirmImportMode(isReplace = false))
                    }) {
                        Text("合并")
                    }
                    TextButton(onClick = {
                        viewModel.onEvent(SettingsEvent.DismissImportModeDialog)
                    }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            ListItem(
                headlineContent = { Text("深色模式") },
                supportingContent = { Text("切换应用的亮色/暗色主题") },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleDarkMode) }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("持续增强") },
                supportingContent = { Text("开启后浏览图片时自动应用画质增强效果（仅显示，不保存文件）") },
                trailingContent = {
                    Switch(
                        checked = uiState.isContinuousEnhance,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleContinuousEnhance) }
                    )
                }
            )

            HorizontalDivider()

            Surface(
                onClick = onNavigateToPersonalization,
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("个性化") },
                    supportingContent = { Text("自定义文字颜色、图标颜色与软件背景") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                )
            }

            HorizontalDivider()

            // --- 数据导入导出 ---
            Text(
                text = "数据管理",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )

            Surface(
                onClick = {
                    if (!uiState.isExporting) {
                        exportLauncher.launch("tagfile_backup.json")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("导出数据") },
                    supportingContent = {
                        Text(if (uiState.isExporting) "正在导出..." else "将所有数据导出为 JSON 文件")
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            HorizontalDivider()

            Surface(
                onClick = {
                    if (!uiState.isImporting) {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("导入数据") },
                    supportingContent = {
                        Text(if (uiState.isImporting) "正在导入..." else "从 JSON 文件恢复数据")
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TagFile v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "标签化文件管理器",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}