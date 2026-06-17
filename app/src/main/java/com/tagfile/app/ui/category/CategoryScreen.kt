package com.tagfile.app.ui.category

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.ui.common.*
import com.tagfile.app.ui.theme.toTagColorOrGray
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTaggedFiles: (Long) -> Unit = {},
    onNavigateToFileBrowser: (String) -> Unit = {},
    onNavigateToTypeFiles: (FileType) -> Unit = {},
    onNavigateToUntaggedFiles: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CategoryEvent.ClearOperationMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类浏览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.onEvent(CategoryEvent.ClearSelection) }) {
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
                        IconButton(onClick = { viewModel.onEvent(CategoryEvent.ShowTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "打标签")
                                Text("标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(CategoryEvent.ShowRemoveTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                @Suppress("DEPRECATION")
                                Icon(Icons.Default.LabelOff, contentDescription = "取消标签")
                                Text("取消标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(CategoryEvent.ShowRenameDialog) }) {
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
                        IconButton(onClick = { viewModel.onEvent(CategoryEvent.ShowDeleteConfirm) }) {
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
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("文件类型", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    val typeIcons = mapOf(
                        "图片" to Icons.Default.Image,
                        "视频" to Icons.Default.Videocam,
                        "音频" to Icons.Default.MusicNote,
                        "文档" to Icons.Default.Description,
                        "压缩包" to Icons.Default.FolderZip,
                        "安装包" to Icons.Default.Android
                    )
                    val labelToFileType = mapOf(
                        FileType.IMAGE.label to FileType.IMAGE,
                        FileType.VIDEO.label to FileType.VIDEO,
                        FileType.AUDIO.label to FileType.AUDIO,
                        FileType.DOCUMENT.label to FileType.DOCUMENT,
                        FileType.ARCHIVE.label to FileType.ARCHIVE,
                        FileType.APK.label to FileType.APK
                    )
                    val types = uiState.typeResults.keys.toList()
                    val columns = 3
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (rowIndex in types.indices step columns) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (colIndex in 0 until columns) {
                                    val type = types.getOrNull(rowIndex + colIndex) ?: continue
                                    val count = uiState.typeResults[type] ?: 0
                                    @Suppress("DEPRECATION")
                                    val icon = typeIcons[type] ?: Icons.Default.InsertDriveFile
                                    val fileType = labelToFileType[type]
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        onClick = { fileType?.let { onNavigateToTypeFiles(it) } }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(type, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                "$count 个文件",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                val itemsInRow = minOf(columns, types.size - rowIndex)
                                val missing = columns - itemsInRow
                                repeat(missing) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    Text("标签", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToUntaggedFiles() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            @Suppress("DEPRECATION")
                            Icon(
                                Icons.Default.LabelOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("未分类", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${uiState.untaggedCount} 个文件",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            @Suppress("DEPRECATION")
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    if (uiState.tags.isEmpty()) {
                        Text(
                            "暂无标签，创建标签后可在此查看",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.tags.forEach { tag ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToTaggedFiles(tag.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TagChip(
                                            name = tag.name,
                                            color = tag.color.toTagColorOrGray()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    Text("最近文件", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    if (uiState.recentFiles.isEmpty()) {
                        Text(
                            "扫描中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.recentFiles.take(10).forEach { file ->
                            FileItemCard(
                                file = file,
                                showTags = false,
                                selected = file.path in uiState.selectedPaths,
                                inSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.onEvent(CategoryEvent.ToggleFileSelection(file.path))
                                    } else {
                                        onNavigateToFileBrowser(file.path)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.onEvent(CategoryEvent.ToggleSelectionMode)
                                        viewModel.onEvent(CategoryEvent.ToggleFileSelection(file.path))
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEvent(CategoryEvent.ToggleLargeFiles) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("大文件 (>50MB)", style = MaterialTheme.typography.titleMedium)
                        Icon(
                            if (uiState.showLargeFiles) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (uiState.showLargeFiles) {
                    item {
                        if (uiState.largeFiles.isEmpty()) {
                            Text(
                                "扫描中...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.largeFiles.take(20).forEach { file ->
                                FileItemCard(
                                    file = file,
                                    showTags = false,
                                    selected = file.path in uiState.selectedPaths,
                                    inSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.onEvent(CategoryEvent.ToggleFileSelection(file.path))
                                        } else {
                                            onNavigateToFileBrowser(file.path)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.onEvent(CategoryEvent.ToggleSelectionMode)
                                            viewModel.onEvent(CategoryEvent.ToggleFileSelection(file.path))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (uiState.showTagSelector) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(CategoryEvent.HideTagSelector) },
            title = { Text("添加标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(CategoryEvent.TagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.tagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(CategoryEvent.TagSelectorSearchQueryChanged("")) }) {
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
                                    onClick = { viewModel.onEvent(CategoryEvent.AddTagToSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.HideTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRemoveTagSelector) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(CategoryEvent.HideRemoveTagSelector) },
            title = { Text("取消标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.removeTagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(CategoryEvent.RemoveTagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.removeTagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(CategoryEvent.RemoveTagSelectorSearchQueryChanged("")) }) {
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
                                    onClick = { viewModel.onEvent(CategoryEvent.RemoveTagFromSelectedFiles(tag.id)) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.HideRemoveTagSelector) }) { Text("取消") }
            }
        )
    }

    if (uiState.showRenameDialog) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(CategoryEvent.HideRenameDialog) },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(
                    value = uiState.renameNewName,
                    onValueChange = { viewModel.onEvent(CategoryEvent.RenameNameChanged(it)) },
                    label = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.ConfirmRename) }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.HideRenameDialog) }) { Text("取消") }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        GlassDialog(
            onDismissRequest = { viewModel.onEvent(CategoryEvent.DismissDeleteConfirm) },
            title = { Text("删除确认") },
            text = { Text("确定要删除选中的 ${uiState.selectedPaths.size} 个文件吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.ConfirmDelete) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(CategoryEvent.DismissDeleteConfirm) }) { Text("取消") }
            }
        )
    }
}
