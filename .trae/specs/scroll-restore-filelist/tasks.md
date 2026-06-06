# Tasks

## Task 1: FileListUiState 增加滚动恢复相关字段 ✅
在 FileListUiState 中添加用于滚动位置恢复的状态字段。

### Steps
- [x] 添加 `scrollToIndex: Int = 0` 字段，表示需要滚动到的目标项索引
- [x] 添加 `scrollToOffset: Int = 0` 字段，表示需要滚动到的目标项偏移量
- [x] 添加 `scrollRestoreKey: Long = 0L` 字段，作为滚动恢复的触发键（每次需要恢复时递增，确保 LaunchedEffect 被重新触发）
- [x] 添加 `ScrollPosition` 数据类（index, offset）
- [x] 修改 `NavigateTo` 和 `NavigateUp` 事件，增加 `scrollIndex` 和 `scrollOffset` 参数

## Task 2: FileListViewModel 增加滚动位置缓存与恢复逻辑 ✅
在 ViewModel 中维护路径→滚动位置的缓存映射，在导航时保存/恢复滚动位置。

### Steps
- [x] 添加 `private val scrollPositionCache = mutableMapOf<String, ScrollPosition>()` 缓存
- [x] `NavigateTo` 事件处理：保存当前路径的滚动位置到缓存，然后加载目标路径
- [x] `NavigateUp` 事件处理：保存当前路径的滚动位置到缓存，然后加载父目录
- [x] 修改 `loadFiles()` 方法，在加载完成后检查缓存，设置 scrollToIndex/scrollToOffset/scrollRestoreKey 到 UiState

## Task 3: FileListScreen 使用显式 LazyListState 并实现滚动恢复 ✅
在 FileListScreen 中显式持有 LazyListState 和 LazyGridState，并实现保存/恢复滚动位置的交互逻辑。

### Steps
- [x] 导入 `rememberLazyListState` 和 `rememberLazyGridState`
- [x] 创建 `listState` 和 `gridState`，分别由 `rememberLazyListState()` / `rememberLazyGridState()` 创建
- [x] `LazyColumn` 使用 `state = listState` 参数
- [x] `LazyVerticalGrid` 使用 `state = gridState` 参数
- [x] 添加 `LaunchedEffect(uiState.scrollRestoreKey)` 在目录加载完成后将列表滚动到目标位置
- [x] `BackHandler` 在触发 `NavigateUp` 前传递当前滚动位置
- [x] 返回按钮在触发 `NavigateUp` 前传递当前滚动位置
- [x] 文件夹点击事件（列表视图）在触发 `NavigateTo` 前传递当前滚动位置
- [x] 文件夹点击事件（网格视图）在触发 `NavigateTo` 前传递当前滚动位置

# Task Dependencies
- Task 2 → depends on Task 1
- Task 3 → depends on Task 2
