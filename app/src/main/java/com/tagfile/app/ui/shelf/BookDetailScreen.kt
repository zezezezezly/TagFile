package com.tagfile.app.ui.shelf

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.ui.common.GlassCard
import com.tagfile.app.ui.common.TagChip
import com.tagfile.app.ui.theme.TagColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToRead: (Long) -> Unit = {},
    onNavigateToAuthor: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书籍详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.book == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("书籍不存在", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                val book = uiState.book!!
                var descriptionExpanded by remember { mutableStateOf(false) }
                var isEditingDescription by remember { mutableStateOf(false) }
                val descLineCount = if (book.description.isNotBlank()) {
                    book.description.lines().size + (book.description.length / 30).coerceAtLeast(1)
                } else 0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.width(140.dp).aspectRatio(0.7f)) {
                            AsyncImage(
                                model = File(book.coverPath),
                                contentDescription = book.title,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.onEvent(BookDetailEvent.ToggleCoverPicker) },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "更换封面",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (book.author != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = book.author,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onNavigateToAuthor(book.author) }
                                    )
                                    IconButton(
                                        onClick = { viewModel.showAuthorEditor() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "编辑作者",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "未知作者",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onNavigateToAuthor("未知作者") }
                                    )
                                    IconButton(
                                        onClick = { viewModel.showAuthorEditor() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "编辑作者",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { viewModel.showScoreEditor() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (book.score > 0f) String.format(java.util.Locale.getDefault(), "%.1f", book.score) else "0.0",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "/ 10.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (book.description.isNotBlank() && !isEditingDescription) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .animateContentSize()
                        ) {
                            Text(
                                text = book.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (descLineCount > 3) {
                                    TextButton(
                                        onClick = { descriptionExpanded = !descriptionExpanded },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            if (descriptionExpanded) "收起" else "展开",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Icon(
                                            if (descriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.onDescriptionChanged(book.description)
                                        isEditingDescription = true
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("编辑", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = uiState.editDescription,
                            onValueChange = { viewModel.onDescriptionChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .heightIn(min = 80.dp),
                            placeholder = { Text("添加简介...") },
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        TextButton(
                                            onClick = {
                                                isEditingDescription = true
                                                viewModel.onDescriptionChanged("")
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("取消")
                                        }
                                        if (uiState.editDescription.isNotBlank() && uiState.editDescription != book.description) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.saveDescription()
                                                    isEditingDescription = false
                                                },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("保存")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "标签",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.showTagEditor() }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("编辑标签")
                        }
                    }

                    val tagList = book.tags.split("/").filter { it.isNotBlank() }
                    if (tagList.isNotEmpty()) {
                        val allTags = uiState.allTags
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tagList.forEach { tagName ->
                                val tag = allTags.find { it.name == tagName }
                                val color = tag?.let { Color(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant
                                TagChip(
                                    name = tagName,
                                    color = color,
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "暂无标签",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Default.Collections,
                            label = "页数",
                            value = "${book.pageCount}"
                        )
                        StatItem(
                            icon = Icons.Default.Visibility,
                            label = "浏览次数",
                            value = "${book.viewCount}"
                        )
                        StatItem(
                            icon = Icons.Default.Timer,
                            label = "阅读时长",
                            value = formatDuration(book.totalDuration)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onNavigateToRead(book.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始阅读", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (uiState.showTagEditor) {
        TagSelectDialog(
            allTags = uiState.allTags,
            selectedTagIds = uiState.selectedTagIds,
            newTagName = uiState.newTagName,
            newTagColorIndex = uiState.newTagColorIndex,
            onToggleTag = { viewModel.toggleTag(it) },
            onNewTagNameChanged = { viewModel.onNewTagNameChanged(it) },
            onNewTagColorChanged = { viewModel.onNewTagColorChanged(it) },
            onAddTag = { viewModel.addTag() },
            onDismiss = { viewModel.dismissTagEditor() },
            onConfirm = { viewModel.saveTags() }
        )
    }

    if (uiState.showScoreEditor) {
        ScoreEditDialog(
            scoreText = uiState.editScoreText,
            onScoreTextChanged = { viewModel.onScoreTextChanged(it) },
            onDismiss = { viewModel.dismissScoreEditor() },
            onConfirm = { viewModel.saveScore() }
        )
    }

    if (uiState.showAuthorEditor) {
        uiState.book?.let { book ->
            val authorPattern = Regex("""^[\[［](.+?)[]］]\s*(.*)""")
            val titlePart = authorPattern.find(File(book.folderPath).name)?.let { match ->
                match.groupValues[2].trim().takeIf { it.isNotBlank() }
            } ?: File(book.folderPath).name

            AuthorEditDialog(
                titlePart = titlePart,
                authorName = uiState.editAuthorName,
                onAuthorNameChanged = { viewModel.onAuthorNameChanged(it) },
                onDismiss = { viewModel.dismissAuthorEditor() },
                onConfirm = { viewModel.saveAuthor() }
            )
        }
    }

    if (uiState.showCoverPicker) {
        CoverPickerDialog(
            images = uiState.coverPickerImages,
            currentCoverPath = uiState.book?.coverPath ?: "",
            onSelect = { viewModel.onEvent(BookDetailEvent.SelectCover(it)) },
            onDismiss = { viewModel.onEvent(BookDetailEvent.ToggleCoverPicker) }
        )
    }
}

@Composable
private fun ScoreEditDialog(
    scoreText: String,
    onScoreTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("评分") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = onScoreTextChanged,
                    placeholder = { Text("0.0") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "取值范围 0.0 ~ 10.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun TagSelectDialog(
    allTags: List<Tag>,
    selectedTagIds: Set<Long>,
    newTagName: String,
    newTagColorIndex: Int,
    onToggleTag: (Long) -> Unit,
    onNewTagNameChanged: (String) -> Unit,
    onNewTagColorChanged: (Int) -> Unit,
    onAddTag: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val displayedTags = allTags.filter { it.id in selectedTagIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑标签") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = onNewTagNameChanged,
                        placeholder = { Text("输入标签名称") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onAddTag,
                        enabled = newTagName.isNotBlank()
                    ) {
                        Text("添加")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(60.dp)
                ) {
                    itemsIndexed(TagColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (index == newTagColorIndex)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    else Modifier
                                )
                                .clickable { onNewTagColorChanged(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == newTagColorIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(4.dp))

                if (displayedTags.isEmpty()) {
                    Text(
                        "暂无标签，请在上方添加。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        itemsIndexed(displayedTags, key = { _, tag -> tag.id }) { _, tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = { onToggleTag(tag.id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TagChip(
                                    name = tag.name,
                                    color = Color(tag.color)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AuthorEditDialog(
    titlePart: String,
    authorName: String,
    onAuthorNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改作者") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 文件夹名预览
                Text(
                    text = "[${authorName.ifBlank { "______" }}] $titlePart",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = authorName,
                    onValueChange = onAuthorNameChanged,
                    placeholder = { Text("输入作者名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("作者") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}分"
}

@Composable
private fun CoverPickerDialog(
    images: List<String>,
    currentCoverPath: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("选择封面", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                if (images.isEmpty()) {
                    Text(
                        "该文件夹中没有图片",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 360.dp)
                    ) {
                        itemsIndexed(images, key = { _, path -> path }) { _, path ->
                            val isCurrent = path == currentCoverPath
                            GlassCard(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .aspectRatio(0.7f)
                                    .then(
                                        if (isCurrent) {
                                            Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(8.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { onSelect(path) }
                            ) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}