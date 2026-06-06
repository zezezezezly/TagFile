package com.tagfile.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    val shelfFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val realPath = contentUriToRealPath(it)
            viewModel.onEvent(SettingsEvent.UpdateShelfFolderPath(realPath))
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SettingsEvent.ClearMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书架设置") },
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
                headlineContent = { Text("书架文件夹") },
                supportingContent = { Text(uiState.shelfFolderPath ?: "未设置") },
                leadingContent = {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                trailingContent = {
                    TextButton(onClick = { shelfFolderLauncher.launch(null) }) {
                        Text("选择")
                    }
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("自动检索新书籍") },
                supportingContent = { Text("扫描书架文件夹，自动添加新书籍") },
                leadingContent = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                trailingContent = {
                    if (uiState.isScanningShelf) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(
                            onClick = { viewModel.onEvent(SettingsEvent.ScanShelfBooks) },
                            enabled = uiState.shelfFolderPath != null
                        ) {
                            Text("检索")
                        }
                    }
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("重置书架数据库") },
                supportingContent = { Text("清空所有书籍数据，下次需要重新检索") },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    )
                },
                trailingContent = {
                    if (uiState.isResettingShelf) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("重置")
                        }
                    }
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.weight(1f))
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = Color.White,
                title = { Text("确认重置") },
                text = { Text("确定要清空所有书籍数据吗？此操作不可撤销，您需要重新检索书籍。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            viewModel.onEvent(SettingsEvent.ResetShelfDatabase)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("确认重置")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

private fun contentUriToRealPath(uri: Uri): String {
    val path = uri.lastPathSegment ?: return uri.toString()
    val decoded = java.net.URLDecoder.decode(path, "UTF-8")
    val colonIndex = decoded.indexOf(':')
    return if (colonIndex > 0) {
        val root = decoded.substring(0, colonIndex)
        val subPath = decoded.substring(colonIndex + 1)
        if (root == "primary") "/storage/emulated/0/$subPath"
        else "/storage/$root/$subPath"
    } else {
        decoded
    }
}
