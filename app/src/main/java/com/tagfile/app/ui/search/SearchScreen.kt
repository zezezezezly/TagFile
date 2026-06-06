package com.tagfile.app.ui.search

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.TagMode
import com.tagfile.app.ui.common.*
import com.tagfile.app.ui.theme.TagColors
import com.tagfile.app.ui.theme.toTagColorOrGray
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDirectory: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToTaggedFiles: (Long) -> Unit = {},
    onNavigateToImageViewer: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SearchEvent.ClearOperationMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索文件") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.onEvent(SearchEvent.ClearSelection) }) {
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
                        IconButton(onClick = { viewModel.onEvent(SearchEvent.ShowTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "打标签")
                                Text("标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(SearchEvent.ShowRemoveTagSelector) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LabelOff, contentDescription = "取消标签")
                                Text("取消标签", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.results.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item(key = "search_bar") {
                    SearchBar(
                        query = uiState.query,
                        onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) }
                    )
                }

                item(key = "filter_actions") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.selectedTagIds.isNotEmpty()) {
                            AssistChip(
                                onClick = { viewModel.onEvent(SearchEvent.ToggleTagMode) },
                                label = {
                                    Text(if (uiState.tagMode == TagMode.AND) "AND" else "OR")
                                }
                            )
                        }
                        if (uiState.selectedTagIds.isNotEmpty() || uiState.selectedFileTypes.isNotEmpty() || uiState.searchDirectories) {
                            TextButton(onClick = { viewModel.onEvent(SearchEvent.ClearFilters) }) {
                                Text("清除筛选")
                            }
                        }
                    }
                }

                item(key = "tag_search") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.tagSearchQuery,
                            onValueChange = { viewModel.onEvent(SearchEvent.TagSearchQueryChanged(it)) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("搜索标签...") },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null)
                            },
                            trailingIcon = {
                                if (uiState.tagSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onEvent(SearchEvent.TagSearchQueryChanged("")) }) {
                                        Icon(Icons.Default.Close, contentDescription = "清除")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(onClick = { viewModel.onEvent(SearchEvent.ShowTagPicker) }) {
                            Text("选择标签")
                        }
                    }
                }

                if (uiState.tagSearchQuery.isNotEmpty() && uiState.filteredTags.isNotEmpty()) {
                    item(key = "filtered_tags") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filteredTags, key = { it.id }) { tag ->
                                TagChip(
                                    name = tag.name,
                                    color = tag.color.toTagColorOrGray(),
                                    selected = tag.id in uiState.selectedTagIds,
                                    onClick = { viewModel.onEvent(SearchEvent.ToggleTag(tag.id)) }
                                )
                            }
                        }
                    }
                }

                item(key = "type_filters") {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.searchDirectories,
                                onClick = { viewModel.onEvent(SearchEvent.ToggleSearchDirectories) },
                                label = { Text("文件夹") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        items(FileType.entries.filter { it != FileType.OTHER }) { fileType ->
                            FilterChip(
                                selected = fileType in uiState.selectedFileTypes,
                                onClick = { viewModel.onEvent(SearchEvent.ToggleFileType(fileType)) },
                                label = { Text(fileType.label) }
                            )
                        }
                    }
                }

                item(key = "divider") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                items(uiState.results, key = { it.path }) { file ->
                    FileItemCard(
                        file = file,
                        selected = file.path in uiState.selectedPaths,
                        inSelectionMode = uiState.isSelectionMode,
                        onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.onEvent(SearchEvent.ToggleFileSelection(file.path))
                            } else if (file.isDirectory) {
                                onNavigateToDirectory(file.path)
                            } else if (FileType.fromExtension(file.extension) == FileType.IMAGE) {
                                val imageFiles = uiState.results.filter {
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
                                viewModel.onEvent(SearchEvent.ToggleSelectionMode)
                                viewModel.onEvent(SearchEvent.ToggleFileSelection(file.path))
                            }
                        }
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                SearchBar(
                    query = uiState.query,
                    onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.selectedTagIds.isNotEmpty()) {
                        AssistChip(
                            onClick = { viewModel.onEvent(SearchEvent.ToggleTagMode) },
                            label = {
                                Text(if (uiState.tagMode == TagMode.AND) "AND" else "OR")
                            }
                        )
                    }
                    if (uiState.selectedTagIds.isNotEmpty() || uiState.selectedFileTypes.isNotEmpty() || uiState.searchDirectories) {
                        TextButton(onClick = { viewModel.onEvent(SearchEvent.ClearFilters) }) {
                            Text("清除筛选")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.tagSearchQuery,
                        onValueChange = { viewModel.onEvent(SearchEvent.TagSearchQueryChanged(it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.tagSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(SearchEvent.TagSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(onClick = { viewModel.onEvent(SearchEvent.ShowTagPicker) }) {
                        Text("选择标签")
                    }
                }

                if (uiState.tagSearchQuery.isNotEmpty() && uiState.filteredTags.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredTags, key = { it.id }) { tag ->
                            TagChip(
                                name = tag.name,
                                color = tag.color.toTagColorOrGray(),
                                selected = tag.id in uiState.selectedTagIds,
                                onClick = { viewModel.onEvent(SearchEvent.ToggleTag(tag.id)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.searchDirectories,
                            onClick = { viewModel.onEvent(SearchEvent.ToggleSearchDirectories) },
                            label = { Text("文件夹") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    items(FileType.entries.filter { it != FileType.OTHER }) { fileType ->
                        FilterChip(
                            selected = fileType in uiState.selectedFileTypes,
                            onClick = { viewModel.onEvent(SearchEvent.ToggleFileType(fileType)) },
                            label = { Text(fileType.label) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (uiState.isSearching) {
                    LoadingIndicator()
                } else if (uiState.query.isNotEmpty() || uiState.selectedTagIds.isNotEmpty()
                    || uiState.selectedFileTypes.isNotEmpty() || uiState.searchDirectories) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("未找到匹配的文件", style = MaterialTheme.typography.bodyLarge)
                    }
                } else if (uiState.searchHistory.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("搜索历史", style = MaterialTheme.typography.titleSmall)
                            TextButton(onClick = { viewModel.onEvent(SearchEvent.ClearHistory) }) {
                                Text("清除")
                            }
                        }
                        uiState.searchHistory.forEach { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(SearchEvent.QueryChanged(historyItem))
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(historyItem, style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SearchEvent.HideTagSelector) },
            containerColor = Color.White,
            title = { Text("添加标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(SearchEvent.TagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.tagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(SearchEvent.TagSelectorSearchQueryChanged("")) }) {
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
                                        viewModel.onEvent(SearchEvent.AddTagToSelectedFiles(tag.id))
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SearchEvent.HideTagSelector) }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showRemoveTagSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SearchEvent.HideRemoveTagSelector) },
            containerColor = Color.White,
            title = { Text("取消标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.removeTagSelectorSearchQuery,
                        onValueChange = { viewModel.onEvent(SearchEvent.RemoveTagSelectorSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.removeTagSelectorSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(SearchEvent.RemoveTagSelectorSearchQueryChanged("")) }) {
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
                                        viewModel.onEvent(SearchEvent.RemoveTagFromSelectedFiles(tag.id))
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SearchEvent.HideRemoveTagSelector) }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showTagPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SearchEvent.HideTagPicker) },
            containerColor = Color.White,
            title = { Text("选择标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.tagPickerSearchQuery,
                        onValueChange = { viewModel.onEvent(SearchEvent.TagPickerSearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索标签...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.tagPickerSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(SearchEvent.TagPickerSearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.filteredPickerTags.isEmpty()) {
                        Text(
                            "未找到匹配的标签",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 320.dp)
                        ) {
                            items(uiState.filteredPickerTags, key = { it.id }) { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onEvent(SearchEvent.TogglePickerTag(tag.id)) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TagChip(
                                        name = tag.name,
                                        color = tag.color.toTagColorOrGray(),
                                        selected = tag.id in uiState.pickerSelectedTagIds
                                    )
                                    if (tag.id in uiState.pickerSelectedTagIds) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SearchEvent.ConfirmTagPicker) }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(SearchEvent.HideTagPicker) }) {
                    Text("取消")
                }
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