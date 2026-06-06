package com.tagfile.app.ui.taggedfiles

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.ui.common.*
import com.tagfile.app.ui.theme.TagColors
import com.tagfile.app.ui.theme.toTagColorOrGray
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaggedFilesScreen(
    viewModel: TaggedFilesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDirectory: (String) -> Unit = {},
    onNavigateToImageViewer: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(TaggedFilesEvent.ClearOperationMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        uiState.tag?.let { tag ->
                            TagChip(
                                name = tag.name,
                                color = tag.color.toTagColorOrGray()
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ClearSelection) }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                        Text(
                            text = "${uiState.selectedPaths.size}",
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isSelectionMode && uiState.selectedPaths.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ShowTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "打标签")
                                Text("标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ShowRemoveTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LabelOff, contentDescription = "取消标签")
                                Text("取消标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ShowRenameDialog) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Edit, contentDescription = "重命名")
                                Text("重命名", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                                    context, "${context.packageName}.file_provider",
                                    File(uiState.selectedPaths.first())
                                ))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享"))
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Share, contentDescription = "分享")
                                Text("分享", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ShowDeleteConfirm) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                                Text("删除", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.files.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("该标签下暂无文件", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "长按文件即可选择并批量操作",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(uiState.files, key = { it.path }) { file ->
                            FileItemCard(
                                file = file,
                                selected = file.path in uiState.selectedPaths,
                                inSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.onEvent(TaggedFilesEvent.ToggleFileSelection(file.path))
                                    } else if (file.isDirectory) {
                                        onNavigateToDirectory(file.path)
                                    } else if (FileType.fromExtension(file.extension) == FileType.IMAGE) {
                                        val imageFiles = uiState.files.filter {
                                            !it.isDirectory && FileType.fromExtension(it.extension) == FileType.IMAGE
                                        }
                                        onNavigateToImageViewer(
                                            imageFiles.map { it.path },
                                            imageFiles.indexOfFirst { it.path == file.path }.coerceAtLeast(0)
                                        )
                                    } else {
                                        openFile(context, file.path)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.onEvent(TaggedFilesEvent.ToggleSelectionMode)
                                        viewModel.onEvent(TaggedFilesEvent.ToggleFileSelection(file.path))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaggedFilesEvent.HideTagSelector) },
            title = { Text("添加标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(TaggedFilesEvent.TagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.tagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.TagSelectorSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.filteredSelectorTags.isEmpty()) {
                        Text("未找到匹配的标签", modifier = Modifier.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(uiState.filteredSelectorTags, key = { it.id }) { tag ->
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    onClick = { viewModel.onEvent(TaggedFilesEvent.AddTagToSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.HideTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRemoveTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaggedFilesEvent.HideRemoveTagSelector) },
            title = { Text("取消标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.removeTagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(TaggedFilesEvent.RemoveTagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.removeTagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(TaggedFilesEvent.RemoveTagSelectorSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.filteredRemoveSelectorTags.isEmpty()) {
                        Text("未找到匹配的标签", modifier = Modifier.padding(vertical = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(uiState.filteredRemoveSelectorTags, key = { it.id }) { tag ->
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    onClick = { viewModel.onEvent(TaggedFilesEvent.RemoveTagFromSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.HideRemoveTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRenameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaggedFilesEvent.HideRenameDialog) },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(
                    value = uiState.renameNewName,
                    onValueChange = { viewModel.onEvent(TaggedFilesEvent.RenameNameChanged(it)) },
                    label = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ConfirmRename) }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.HideRenameDialog) }) { Text("取消") }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TaggedFilesEvent.DismissDeleteConfirm) },
            title = { Text("删除确认") },
            text = { Text("确定要删除选中的 ${uiState.selectedPaths.size} 个文件吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.ConfirmDelete) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TaggedFilesEvent.DismissDeleteConfirm) }) { Text("取消") }
            }
        )
    }
}

private fun openFile(context: android.content.Context, filePath: String) {
    try {
        val file = File(filePath)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.file_provider", file
        )
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: Exception) { }
}
