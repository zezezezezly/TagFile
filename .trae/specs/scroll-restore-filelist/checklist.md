# Checklist

## UiState 字段
- [x] `FileListUiState` 包含 `scrollToIndex: Int = 0` 字段
- [x] `FileListUiState` 包含 `scrollToOffset: Int = 0` 字段
- [x] `FileListUiState` 包含 `scrollRestoreKey: Long = 0L` 字段

## ViewModel 滚动位置缓存
- [x] `FileListViewModel` 中存在 `scrollPositionCache` 映射，以路径为 key 存储滚动位置
- [x] `saveCurrentScrollPosition(index, offset)` 方法正确保存当前路径的滚动位置
- [x] `loadFiles()` 在加载完成后检查缓存并设置 scrollRestoreKey 以触发滚动恢复
- [x] `NavigateTo` 事件处理流程支持先保存当前滚动位置再加载新目录
- [x] `NavigateUp` 事件处理流程支持加载父目录后恢复之前缓存的滚动位置

## Screen 滚动恢复
- [x] `LazyColumn` 使用 `rememberLazyListState()` 显式持有滚动状态
- [x] `LazyVerticalGrid` 使用 `rememberLazyGridState()` 显式持有滚动状态
- [x] `LaunchedEffect(scrollRestoreKey)` 在目录加载完成后将列表滚动到目标位置
- [x] 文件夹点击事件在触发 `NavigateTo` 前保存当前滚动位置
- [x] 返回按钮和 BackHandler 在触发 `NavigateUp` 前保存当前滚动位置

## 行为验证
- [x] 进入子文件夹后返回，列表滚动位置恢复到之前浏览的位置
- [x] 网格视图下进入子文件夹后返回，网格滚动位置也正确恢复
- [x] 首次进入某个目录时列表从顶部开始显示
