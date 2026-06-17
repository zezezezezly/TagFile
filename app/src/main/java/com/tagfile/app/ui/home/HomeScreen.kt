package com.tagfile.app.ui.home

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.data.preferences.AppShortcut
import com.tagfile.app.ui.common.GlassCard
import com.tagfile.app.ui.common.GlassDialog

data class HomeItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToFiles: () -> Unit = {},
    onNavigateToCategory: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFilter: () -> Unit = {},
    onNavigateToShelf: () -> Unit = {},
    onNavigateToTrash: () -> Unit = {},
    onNavigateToFileList: (String) -> Unit = {},
    onNavigateToTaggedFiles: (Long) -> Unit = {},
    onNavigateToBookDetail: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = extractPathFromUri(context, uri)
            val displayName = (path ?: uri.lastPathSegment)?.substringAfterLast("/")?.let {
                it.ifEmpty { uri.lastPathSegment ?: "文件夹" }
            } ?: "文件夹"
            viewModel.addShortcut(AppShortcut(type = "folder", path = uri.toString(), label = displayName))
        }
        viewModel.dismissAddShortcutDialog()
    }

    val items = listOf(
        HomeItem("文件", "浏览和管理设备中的文件与文件夹", Icons.Default.Folder),
        HomeItem("书架", "阅读和管理图片类书籍，支持封面浏览", Icons.AutoMirrored.Filled.MenuBook),
        HomeItem("分类", "按类型查看最近文件与大文件", Icons.Default.Category),
        HomeItem("搜索", "按文件名、标签或类型搜索文件", Icons.Default.Search),
        HomeItem("标签管理", "创建、编辑和删除标签", Icons.AutoMirrored.Filled.Label),
        HomeItem("滤镜库", "新建、选择和管理画质增强滤镜", Icons.Default.AutoFixHigh),
        HomeItem("设置", "深色模式、导出导入标签数据", Icons.Default.Settings)
    )

    val actions = listOf(
        onNavigateToFiles,
        onNavigateToShelf,
        onNavigateToCategory,
        onNavigateToSearch,
        onNavigateToTags,
        onNavigateToFilter,
        onNavigateToSettings
    )

    if (uiState.showAddShortcutDialog) {
        val dialogType = uiState.shortcutDialogType
        GlassDialog(
            onDismissRequest = { viewModel.dismissAddShortcutDialog() },
            title = { Text("添加快捷访问") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = dialogType == "folder",
                            onClick = { viewModel.setShortcutDialogType("folder") },
                            label = { Text("文件夹") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = dialogType == "tag",
                            onClick = { viewModel.setShortcutDialogType("tag") },
                            label = { Text("标签") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = dialogType == "book",
                            onClick = { viewModel.setShortcutDialogType("book") },
                            label = { Text("书籍") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (dialogType) {
                        "folder" -> {
                            Text(
                                "选择要快捷访问的文件夹",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { folderPickerLauncher.launch(null) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("选择文件夹")
                            }
                        }
                        "tag" -> {
                            Text(
                                "选择要快捷访问的标签",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (uiState.allTags.isEmpty()) {
                                Text(
                                    "暂无标签",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                    uiState.allTags.forEach { tag ->
                                        Surface(
                                            onClick = {
                                                viewModel.addShortcut(
                                                    AppShortcut(type = "tag", path = tag.id.toString(), label = tag.name)
                                                )
                                                viewModel.dismissAddShortcutDialog()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Label,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(tag.name, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                        "book" -> {
                            Text(
                                "选择要快捷访问的书籍",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (uiState.allBooks.isEmpty()) {
                                Text(
                                    "暂无书籍",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                    uiState.allBooks.forEach { book ->
                                        Surface(
                                            onClick = {
                                                viewModel.addShortcut(
                                                    AppShortcut(type = "book", path = book.id.toString(), label = book.title)
                                                )
                                                viewModel.dismissAddShortcutDialog()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        book.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    book.author?.let { author ->
                                                        if (author.isNotBlank()) {
                                                            Text(
                                                                author,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddShortcutDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TagFile") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardCard(
                    label = "标签数",
                    icon = Icons.AutoMirrored.Filled.Label,
                    count = { formatCount(uiState.totalTags.toLong()) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTags
                )
                DashboardCard(
                    label = "书籍",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    count = { formatCount(uiState.totalBooks.toLong()) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToShelf
                )
                DashboardCard(
                    label = "回收站",
                    icon = Icons.Default.Delete,
                    count = { formatCount(uiState.trashCount.toLong()) },
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTrash
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.shortcuts.isNotEmpty()) {
                Text(
                    text = "快捷访问",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.shortcuts) { index, shortcut ->
                        ShortcutChip(
                            shortcut = shortcut,
                            onClick = {
                                when (shortcut.type) {
                                    "folder" -> {
                                        val path = extractPathFromUri(context, shortcut.path.toUri())
                                        if (path != null) {
                                            onNavigateToFileList(path)
                                        } else {
                                            onNavigateToFiles()
                                        }
                                    }
                                    "tag" -> {
                                        val tagId = shortcut.path.toLongOrNull()
                                        if (tagId != null) {
                                            onNavigateToTaggedFiles(tagId)
                                        }
                                    }
                                    "book" -> {
                                        val bookId = shortcut.path.toLongOrNull()
                                        if (bookId != null) {
                                            onNavigateToBookDetail(bookId)
                                        }
                                    }
                                }
                            },
                            onLongClick = { viewModel.removeShortcut(index) }
                        )
                    }

                    item {
                        ShortcutAddButton(onClick = { viewModel.showAddShortcutDialog() })
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "快捷访问",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ShortcutAddButton(onClick = { viewModel.showAddShortcutDialog() })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            items.forEachIndexed { index, item ->
                Surface(
                    onClick = actions[index],
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.description) },
                        leadingContent = {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        modifier = Modifier
                            .let { mod ->
                                when {
                                    index == 0 -> mod.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                                    index == items.lastIndex -> mod.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                                    else -> mod.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                                }
                            }
                    )
                }

                if (index < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardCard(
    label: String,
    icon: ImageVector,
    count: () -> String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier
            .combinedClickable(onClick = onClick)
            .padding(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutChip(
    shortcut: AppShortcut,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val icon = when (shortcut.type) {
        "folder" -> Icons.Default.Folder
        "tag" -> Icons.AutoMirrored.Filled.Label
        "book" -> Icons.AutoMirrored.Filled.MenuBook
        else -> Icons.Default.Link
    }

    GlassCard(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = shortcut.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShortcutAddButton(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "添加快捷访问",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .size(18.dp)
        )
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 10000 -> "${"%.1f".format(count / 10000.0)}万"
        count >= 1000 -> "${"%.1f".format(count / 1000.0)}k"
        else -> count.toString()
    }
}

@Suppress("UNUSED_PARAMETER")
private fun extractPathFromUri(context: android.content.Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val split = docId.split(":")
            if (split.size >= 2) {
                val type = split[0]
                val path = split[1]
                if (type.equals("primary", ignoreCase = true)) {
                    return "${Environment.getExternalStorageDirectory()}/$path"
                }
            }
        } catch (_: Exception) {
            return null
        }
    }
    if (uri.scheme == "file") {
        return uri.path
    }
    return null
}
