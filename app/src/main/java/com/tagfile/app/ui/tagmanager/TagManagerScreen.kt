package com.tagfile.app.ui.tagmanager

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.ui.common.SearchBar
import com.tagfile.app.ui.common.TagChip
import com.tagfile.app.ui.theme.toTagColorOrGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagerScreen(
    viewModel: TagManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTaggedFiles: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(TagManagerEvent.ClearMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("标签管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(TagManagerEvent.ToggleSortMode) }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "排序")
                    }
                    IconButton(onClick = { viewModel.onEvent(TagManagerEvent.ShowCreateDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = "创建标签")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(TagManagerEvent.SearchQueryChanged(it)) },
                placeholder = "搜索标签..."
            )

            val sortLabel = when (uiState.sortMode) {
                TagSortMode.COLOR -> "按颜色排序"
                TagSortMode.FILE_COUNT -> "按文件数量排序"
                TagSortMode.DEFAULT -> "默认排序"
            }
            Text(
                text = sortLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (uiState.filteredTags.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (uiState.searchQuery.isNotEmpty()) "未找到匹配的标签"
                            else "暂无标签",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (uiState.searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.onEvent(TagManagerEvent.ShowCreateDialog) }) {
                                Text("创建第一个标签")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(uiState.filteredTags, key = { _, tag -> tag.id }) { _, tag ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToTaggedFiles(tag.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.sortMode == TagSortMode.FILE_COUNT) {
                                    val count = uiState.tagFileCounts[tag.id] ?: 0
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(TagManagerEvent.ShowEditDialog(tag)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "编辑",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(TagManagerEvent.ShowDeleteConfirm(tag)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showEditorDialog) {
        TagEditorDialog(
            name = uiState.editorName,
            selectedColorIndex = uiState.editorColorIndex,
            isEditing = uiState.editingTag != null,
            onNameChange = { viewModel.onEvent(TagManagerEvent.EditorNameChanged(it)) },
            onColorSelected = { viewModel.onEvent(TagManagerEvent.EditorColorChanged(it)) },
            onDismiss = { viewModel.onEvent(TagManagerEvent.DismissEditorDialog) },
            onConfirm = { viewModel.onEvent(TagManagerEvent.SaveTag) }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TagManagerEvent.DismissDeleteConfirm) },
            containerColor = Color.White,
            title = { Text("删除标签") },
            text = {
                Text("确定要删除标签「${uiState.deleteTargetTag?.name}」吗？所有文件上的此标签关联将被移除。")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TagManagerEvent.ConfirmDeleteTag) }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TagManagerEvent.DismissDeleteConfirm) }) {
                    Text("取消")
                }
            }
        )
    }
}