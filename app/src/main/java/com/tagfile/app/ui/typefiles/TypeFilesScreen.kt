package com.tagfile.app.ui.typefiles

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
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
fun TypeFilesScreen(
    fileType: FileType,
    viewModel: TypeFilesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToImageViewer: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(fileType) {
        viewModel.initialize(fileType)
    }

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(TypeFilesEvent.ClearOperationMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.typeLabel) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ClearSelection) }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                        Text(
                            text = "${uiState.selectedPaths.size}",
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    } else {
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ShowSortSheet) }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "排序")
                        }
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ToggleGridView) }) {
                            Icon(
                                if (uiState.isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                contentDescription = "切换视图"
                            )
                        }
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
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ShowTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "打标签")
                                Text("标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ShowRemoveTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LabelOff, contentDescription = "取消标签")
                                Text("取消标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ShowRenameDialog) }) {
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
                        IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.ShowDeleteConfirm) }) {
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
                uiState.files.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无${uiState.typeLabel}文件", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> {
                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(100.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(uiState.files, key = { it.path }) { file ->
                                FileGridItem(
                                    file = file,
                                    selected = file.path in uiState.selectedPaths,
                                    inSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.onEvent(TypeFilesEvent.ToggleFileSelection(file.path))
                                        } else if (fileType == FileType.IMAGE) {
                                            val paths = uiState.files.map { it.path }
                                            onNavigateToImageViewer(paths, paths.indexOf(file.path).coerceAtLeast(0))
                                        } else {
                                            openFile(context, file.path)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.onEvent(TypeFilesEvent.ToggleSelectionMode)
                                            viewModel.onEvent(TypeFilesEvent.ToggleFileSelection(file.path))
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(uiState.files, key = { it.path }) { file ->
                                FileItemCard(
                                    file = file,
                                    selected = file.path in uiState.selectedPaths,
                                    inSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.onEvent(TypeFilesEvent.ToggleFileSelection(file.path))
                                        } else if (fileType == FileType.IMAGE) {
                                            val paths = uiState.files.map { it.path }
                                            onNavigateToImageViewer(paths, paths.indexOf(file.path).coerceAtLeast(0))
                                        } else {
                                            openFile(context, file.path)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.onEvent(TypeFilesEvent.ToggleSelectionMode)
                                            viewModel.onEvent(TypeFilesEvent.ToggleFileSelection(file.path))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.currentSort,
            currentAscending = uiState.ascending,
            onSortSelected = {
                viewModel.onEvent(TypeFilesEvent.SortChanged(it))
                viewModel.onEvent(TypeFilesEvent.HideSortSheet)
            },
            onToggleOrder = {
                viewModel.onEvent(TypeFilesEvent.ToggleSortOrder)
                viewModel.onEvent(TypeFilesEvent.HideSortSheet)
            },
            onDismiss = { viewModel.onEvent(TypeFilesEvent.HideSortSheet) }
        )
    }

    if (uiState.showTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TypeFilesEvent.HideTagSelector) },
            title = { Text("添加标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(TypeFilesEvent.TagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.tagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.TagSelectorSearchQueryChanged("")) }) {
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
                                    onClick = { viewModel.onEvent(TypeFilesEvent.AddTagToSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.HideTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRemoveTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TypeFilesEvent.HideRemoveTagSelector) },
            title = { Text("取消标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.removeTagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(TypeFilesEvent.RemoveTagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.removeTagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(TypeFilesEvent.RemoveTagSelectorSearchQueryChanged("")) }) {
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
                                    onClick = { viewModel.onEvent(TypeFilesEvent.RemoveTagFromSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.HideRemoveTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRenameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TypeFilesEvent.HideRenameDialog) },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(
                    value = uiState.renameNewName,
                    onValueChange = { viewModel.onEvent(TypeFilesEvent.RenameNameChanged(it)) },
                    label = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.ConfirmRename) }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.HideRenameDialog) }) { Text("取消") }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TypeFilesEvent.DismissDeleteConfirm) },
            title = { Text("删除确认") },
            text = { Text("确定要删除选中的 ${uiState.selectedPaths.size} 个文件吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.ConfirmDelete) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TypeFilesEvent.DismissDeleteConfirm) }) { Text("取消") }
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
