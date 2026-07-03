package com.tagfile.app.ui.tagmanager

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.ui.common.GlassBottomSheet
import com.tagfile.app.ui.common.GlassCard
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
                    IconButton(onClick = {
                        if (uiState.selectedGroup != null) {
                            viewModel.onEvent(TagManagerEvent.ShowCreateDialogWithGroup(uiState.selectedGroup))
                        } else {
                            viewModel.onEvent(TagManagerEvent.ShowCreateDialog)
                        }
                    }) {
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

            if (uiState.allGroups.isNotEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedGroup == null,
                                onClick = { viewModel.onEvent(TagManagerEvent.SelectGroup(null)) },
                                label = { Text("全部") }
                            )
                        }
                        items(uiState.allGroups) { group ->
                            FilterChip(
                                selected = uiState.selectedGroup == group,
                                onClick = { viewModel.onEvent(TagManagerEvent.SelectGroup(group)) },
                                label = { Text(group) }
                            )
                        }
                        item {
                            IconButton(
                                onClick = { viewModel.onEvent(TagManagerEvent.ShowGroupManagement) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "管理分组",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

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
                            TextButton(onClick = {
                                if (uiState.selectedGroup != null) {
                                    viewModel.onEvent(TagManagerEvent.ShowCreateDialogWithGroup(uiState.selectedGroup))
                                } else {
                                    viewModel.onEvent(TagManagerEvent.ShowCreateDialog)
                                }
                            }) {
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
                                Column(modifier = Modifier.weight(1f)) {
                                    TagChip(
                                        name = tag.name,
                                        color = tag.color.toTagColorOrGray()
                                    )
                                    if (!tag.groupName.isNullOrBlank()) {
                                        Text(
                                            text = tag.groupName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
            groupName = uiState.editorGroupName,
            isEditing = uiState.editingTag != null,
            onNameChange = { viewModel.onEvent(TagManagerEvent.EditorNameChanged(it)) },
            onColorSelected = { viewModel.onEvent(TagManagerEvent.EditorColorChanged(it)) },
            onGroupNameChange = { viewModel.onEvent(TagManagerEvent.EditorGroupNameChanged(it)) },
            onDismiss = { viewModel.onEvent(TagManagerEvent.DismissEditorDialog) },
            onConfirm = { viewModel.onEvent(TagManagerEvent.SaveTag) }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TagManagerEvent.DismissDeleteConfirm) },
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

    if (uiState.showGroupManagement) {
        GroupManagementSheet(
            groups = uiState.allGroups,
            message = uiState.groupManagementMessage,
            onDismiss = { viewModel.onEvent(TagManagerEvent.DismissGroupManagement) },
            onRenameGroup = { oldName, newName ->
                viewModel.onEvent(TagManagerEvent.RenameGroup(oldName, newName))
            },
            onDeleteGroup = { groupName ->
                viewModel.onEvent(TagManagerEvent.DeleteGroup(groupName))
            },
            onMergeGroups = { fromGroup, toGroup ->
                viewModel.onEvent(TagManagerEvent.MergeGroups(fromGroup, toGroup))
            },
            onDismissMessage = { viewModel.onEvent(TagManagerEvent.DismissGroupManagementMessage) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupManagementSheet(
    groups: List<String>,
    message: String?,
    onDismiss: () -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onMergeGroups: (String, String) -> Unit,
    onDismissMessage: () -> Unit
) {
    var editingGroup by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var showMergePicker by remember { mutableStateOf(false) }
    var mergeSource by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    GlassBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "管理分组",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (message != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissMessage, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (groups.isEmpty()) {
                Text(
                    "暂无分组",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                groups.forEach { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (editingGroup == group) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                OutlinedTextField(
                                    value = editText,
                                    onValueChange = { editText = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("新名称") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { editingGroup = null }) { Text("取消") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = {
                                            if (editText.isNotBlank() && editText != group) {
                                                onRenameGroup(group, editText)
                                            }
                                            editingGroup = null
                                        },
                                        enabled = editText.isNotBlank() && editText != group
                                    ) { Text("保存") }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    group,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        editingGroup = group
                                        editText = group
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "重命名", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { showDeleteConfirm = group },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "合并分组",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (showMergePicker) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "将「${mergeSource}」合并到：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    groups.filter { it != mergeSource }.forEach { target ->
                        Surface(
                            onClick = {
                                mergeSource?.let { onMergeGroups(it, target) }
                                showMergePicker = false
                                mergeSource = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(target, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    TextButton(onClick = { showMergePicker = false; mergeSource = null }) {
                        Text("取消")
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                        items(groups) { group ->
                            Surface(
                                onClick = { mergeSource = group; showMergePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("从「$group」合并", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除分组") },
            text = {
                Text("确定要删除分组「${showDeleteConfirm}」吗？\n该分组下的标签将被保留，仅移除分组归属。")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGroup(showDeleteConfirm!!)
                    showDeleteConfirm = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("取消")
                }
            }
        )
    }
}