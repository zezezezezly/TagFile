package com.tagfile.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class HomeItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFiles: () -> Unit = {},
    onNavigateToCategory: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFilter: () -> Unit = {},
    onNavigateToShelf: () -> Unit = {}
) {
    val items = listOf(
        HomeItem("文件", "浏览和管理设备中的文件与文件夹", Icons.Default.Folder),
        HomeItem("书架", "阅读和管理图片类书籍，支持封面浏览", Icons.Default.MenuBook),
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                            .let {
                                if (index == 0) it.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                                else if (index == items.lastIndex) it.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                                else it.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
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
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
