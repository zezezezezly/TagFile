# Tasks

## Task 1: 创建 Room 数据库层 — BookEntity 和 BookDao ✅
创建书籍数据库实体和 DAO 接口。

### Steps
- [x] 创建 `data/local/entity/BookEntity.kt`：包含 id, title, author, tags, coverPath, folderPath, pageCount, viewCount, totalDuration, lastReadTime, createdAt 字段
- [x] 创建 `data/local/dao/BookDao.kt`
- [x] insert / insertAll / update / deleteById / getAll / getById / getByFolderPath
- [x] searchByTitle / searchByAuthor / searchByTags 三种模糊搜索
- [x] incrementViewCount / addDuration / updateLastReadTime / count 统计方法

## Task 2: 修改 AppDatabase 和 DatabaseModule ✅

### Steps
- [x] 修改 `AppDatabase.kt`：entities 新增 `BookEntity::class`，version 升至 4，新增 `abstract fun bookDao(): BookDao`
- [x] 修改 `DatabaseModule.kt`：添加 `provideBookDao()` 方法

## Task 3: 创建领域层 — Book 模型和 ShelfRepository ✅

### Steps
- [x] 创建 `domain/model/Book.kt`
- [x] 创建 `domain/repository/ShelfRepository.kt`：定义接口 + SearchMode 枚举

## Task 4: 创建数据层 — ShelfRepositoryImpl 和 EntityMapper ✅

### Steps
- [x] `EntityMapper.kt` 添加 BookEntity ↔ Book 映射
- [x] 创建 `ShelfRepositoryImpl.kt`：实现 scanAndAddBooks（含作者提取 + 纯图片检测）
- [x] `RepositoryModule.kt` 绑定 ShelfRepository → ShelfRepositoryImpl

## Task 5: 创建书架主页 UI ✅

### Steps
- [x] 创建 `ShelfUiState.kt`
- [x] 创建 `ShelfViewModel.kt`：加载书籍、随机推荐、扫描、搜索
- [x] 创建 `ShelfScreen.kt`：搜索栏 + FilterChip 模式切换 + 每日推荐 LazyRow + 检索按钮 + GridCells.Fixed(3) 书籍网格

## Task 6: 创建书籍浏览页 ✅

### Steps
- [x] 创建 `BookViewerUiState.kt`
- [x] 创建 `BookViewerViewModel.kt`：SavedStateHandle 接收 bookId，加载图片，计数+计时
- [x] 创建 `BookViewerScreen.kt`：HorizontalPager + AsyncImage + 页码指示器

## Task 7: 配置书架文件夹路径偏好 ✅

### Steps
- [x] `PreferencesManager.kt` 添加 `shelfFolderPath`
- [x] `SettingsUiState.kt` 添加字段 + 事件
- [x] `SettingsViewModel.kt` 添加事件处理
- [x] `SettingsScreen.kt` 添加书架文件夹设置项

## Task 8: 更新导航路由和主页入口 ✅

### Steps
- [x] `NavGraph.kt` 新增 Routes.SHELF / Routes.BOOK_VIEWER / bookViewer() 方法 + 两个 composable 路由
- [x] `HomeScreen.kt` 新增书架入口（Icons.Default.MenuBook）+ onNavigateToShelf 回调

# Task Dependencies
- Task 2 → depends on Task 1
- Task 4 → depends on Task 1, 3
- Task 5 → depends on Task 4
- Task 6 → depends on Task 4
- Task 7 → no dependencies (可并行)
- Task 8 → depends on Task 5, 6, 7
