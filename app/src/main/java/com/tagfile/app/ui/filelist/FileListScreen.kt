package com.tagfile.app.ui.filelist

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toUri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LabelOff
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
import com.tagfile.app.ui.theme.toTagColorOrGray
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    viewModel: FileListViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToImageViewer: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val safDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { treeUri ->
            val docDir = extractPathFromTreeUri(context, treeUri)
            if (docDir != null) {
                uiState.selectedPaths.forEach { path ->
                    viewModel.onEvent(FileListEvent.CopySelectedTo(docDir))
                }
                viewModel.onEvent(FileListEvent.HideOperationsMenu)
            }
        }
    }

    val safMoveDirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { treeUri ->
            val docDir = extractPathFromTreeUri(context, treeUri)
            if (docDir != null) {
                uiState.selectedPaths.forEach { path ->
                    viewModel.onEvent(FileListEvent.MoveSelectedTo(docDir))
                }
                viewModel.onEvent(FileListEvent.HideOperationsMenu)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                viewModel.onEvent(FileListEvent.RequestPermission)
            }
        }
    }

    LaunchedEffect(uiState.scrollRestoreKey) {
        if (uiState.isGridView) {
            gridState.scrollToItem(uiState.scrollToIndex, uiState.scrollToOffset)
        } else {
            listState.scrollToItem(uiState.scrollToIndex, uiState.scrollToOffset)
        }
    }

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(FileListEvent.ClearOperationMessage)
        }
    }

    BackHandler {
        if (uiState.isSelectionMode) {
            viewModel.onEvent(FileListEvent.ClearSelection)
        } else if (uiState.currentPath == uiState.entryPath || (uiState.entryPath == null && uiState.isAtRoot)) {
            onNavigateHome()
        } else {
            val index = if (uiState.isGridView) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
            val offset = if (uiState.isGridView) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
            viewModel.onEvent(FileListEvent.NavigateUp(uiState.currentPath, index, offset))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件") },
                navigationIcon = {
                    val atEntry = uiState.currentPath == uiState.entryPath || (uiState.entryPath == null && uiState.isAtRoot)
                    if (!atEntry) {
                        IconButton(onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.onEvent(FileListEvent.ClearSelection)
                            } else {
                                val index = if (uiState.isGridView) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
                                val offset = if (uiState.isGridView) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
                                viewModel.onEvent(FileListEvent.NavigateUp(uiState.currentPath, index, offset))
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    } else {
                        IconButton(onClick = onNavigateHome) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "主页")
                        }
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        val allSelected = uiState.selectedPaths.size == uiState.files.size && uiState.files.isNotEmpty()
                        TextButton(onClick = { viewModel.onEvent(FileListEvent.ToggleSelectAll) }) {
                            Text(if (allSelected) "取消全选" else "全选", style = MaterialTheme.typography.titleSmall)
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ClearSelection) }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                        Text(
                            text = "${uiState.selectedPaths.size}",
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    } else {
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ToggleShowHiddenFiles) }) {
                            Icon(
                                if (uiState.showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "显示隐藏文件"
                            )
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowSortSheet) }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "排序")
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ToggleGridView) }) {
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
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "打标签")
                                Text("标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowRemoveTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.LabelOff, contentDescription = "取消标签")
                                Text("取消标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowRenameDialog) }) {
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
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowDeleteConfirm) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                                Text("删除", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(FileListEvent.ShowOperationsMenu) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "更多操作")
                                Text("更多", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(FileListEvent.ShowNewFolderDialog) },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹", modifier = Modifier.size(36.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = uiState.error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToSettings) { Text("前往设置") }
                    }
                }
                uiState.files.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("目录为空", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> {
                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            state = gridState,
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
                                            viewModel.onEvent(FileListEvent.ToggleFileSelection(file.path))
                                        } else if (file.isDirectory) {
                                            viewModel.onEvent(FileListEvent.NavigateTo(file.path, gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset))
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
                                        viewModel.onEvent(FileListEvent.ToggleSelectionMode)
                                        viewModel.onEvent(FileListEvent.ToggleFileSelection(file.path))
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(uiState.files, key = { it.path }) { file ->
                                FileItemCard(
                                    file = file,
                                    selected = file.path in uiState.selectedPaths,
                                    inSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.onEvent(FileListEvent.ToggleFileSelection(file.path))
                                        } else if (file.isDirectory) {
                                            viewModel.onEvent(FileListEvent.NavigateTo(file.path, listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset))
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
                                            viewModel.onEvent(FileListEvent.ToggleSelectionMode)
                                            viewModel.onEvent(FileListEvent.ToggleFileSelection(file.path))
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

    if (uiState.showTagSelector) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(FileListEvent.HideTagSelector) },
            title = { Text("添加标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(FileListEvent.TagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.tagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(FileListEvent.TagSelectorSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.filteredSelectorTags.isEmpty()) {
                        Text(
                            "未找到匹配的标签",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(uiState.filteredSelectorTags, key = { it.id }) { tag ->
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    onClick = {
                                        viewModel.onEvent(FileListEvent.AddTagToSelectedFiles(tag.id))
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.HideTagSelector) }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showRemoveTagSelector) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(FileListEvent.HideRemoveTagSelector) },
            title = { Text("取消标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.removeTagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(FileListEvent.RemoveTagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.removeTagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(FileListEvent.RemoveTagSelectorSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.filteredRemoveSelectorTags.isEmpty()) {
                        Text(
                            "未找到匹配的标签",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(uiState.filteredRemoveSelectorTags, key = { it.id }) { tag ->
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    onClick = {
                                        viewModel.onEvent(FileListEvent.RemoveTagFromSelectedFiles(tag.id))
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.HideRemoveTagSelector) }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.currentSort,
            currentAscending = uiState.ascending,
            onSortSelected = {
                viewModel.onEvent(FileListEvent.SortChanged(it))
                viewModel.onEvent(FileListEvent.HideSortSheet)
            },
            onToggleOrder = {
                viewModel.onEvent(FileListEvent.ToggleSortOrder)
                viewModel.onEvent(FileListEvent.HideSortSheet)
            },
            onDismiss = { viewModel.onEvent(FileListEvent.HideSortSheet) }
        )
    }

    if (uiState.showNewFolderDialog) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(FileListEvent.HideNewFolderDialog) },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = uiState.newFolderName,
                    onValueChange = { viewModel.onEvent(FileListEvent.NewFolderNameChanged(it)) },
                    label = { Text("文件夹名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.CreateNewFolder) }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.HideNewFolderDialog) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRenameDialog) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(FileListEvent.HideRenameDialog) },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(
                    value = uiState.renameNewName,
                    onValueChange = { viewModel.onEvent(FileListEvent.RenameNameChanged(it)) },
                    label = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.ConfirmRename) }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.HideRenameDialog) }) { Text("取消") }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(FileListEvent.DismissDeleteConfirm) },
            title = { Text("删除确认") },
            text = { Text("确定要删除选中的 ${uiState.selectedPaths.size} 个文件吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.ConfirmDelete) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(FileListEvent.DismissDeleteConfirm) }) { Text("取消") }
            }
        )
    }

    if (uiState.showPermissionDialog) {
        PermissionDialog(
            onGoToSettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = ("package:${context.packageName}").toUri()
                    }
                    context.startActivity(intent)
                }
                viewModel.onEvent(FileListEvent.DismissPermissionDialog)
            },
            onDismiss = { viewModel.onEvent(FileListEvent.DismissPermissionDialog) }
        )
    }

    if (uiState.showOperationsMenu) {
        GlassBottomSheet(
            onDismissRequest = { viewModel.onEvent(FileListEvent.HideOperationsMenu) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "文件操作",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextButton(
                    onClick = {
                        safDirPicker.launch(null)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("复制到...", style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(
                    onClick = {
                        safMoveDirPicker.launch(null)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("移动到...", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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

private fun extractPathFromTreeUri(context: android.content.Context, treeUri: Uri): String? {
    val docId = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)?.uri?.lastPathSegment
        ?: treeUri.lastPathSegment ?: return null
    val split = docId.split(":".toRegex(), limit = 2)
    return if (split.size >= 2) {
        val storage = split[0]
        val subPath = split[1]
        if (storage.equals("primary", ignoreCase = true)) {
            "${Environment.getExternalStorageDirectory()}/$subPath"
        } else {
            "/storage/$storage/$subPath"
        }
    } else {
        if (docId.equals("primary", ignoreCase = true)) {
            Environment.getExternalStorageDirectory()?.absolutePath
        } else {
            "/storage/$docId"
        }
    }
}
