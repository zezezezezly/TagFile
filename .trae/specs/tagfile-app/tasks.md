# Tasks

## Task 1: 创建 Android 项目工程结构 ✅
使用 Kotlin + Jetpack Compose 搭建项目骨架，配置 Gradle 依赖和 Hilt DI。

### Steps
- [x] 创建标准 Android 项目目录结构（app 模块）
- [x] 配置 build.gradle（project-level 和 app-level），添加所有依赖
- [x] 创建 Application 类（App.kt），添加 @HiltAndroidApp 注解
- [x] 创建 MainActivity.kt，添加 @AndroidEntryPoint 注解，使用 setContent + Compose 宿主
- [x] 创建 AndroidManifest.xml，声明存储权限
- [x] 创建 Hilt DI 模块骨架（DatabaseModule、RepositoryModule、UseCaseModule）
- [x] 验证：确保项目结构完整

## Task 2: 实现数据层 - Room 数据库与 DAO ✅
定义数据库实体、DAO 接口和 AppDatabase。

### Steps
- [x] 创建 TagEntity（id, name, color, icon, sortOrder, createdAt）
- [x] 创建 FileTagCrossRefEntity（filePath, tagId, isInherited），复合主键 (filePath, tagId)
- [x] 创建 TagDao：insert/update/delete/getAll/getById
- [x] 创建 FileTagDao：支持 AND/OR 标签查询、路径更新、批量操作
- [x] 创建 AppDatabase（@Database，包含上述 Entity 和 DAO）
- [x] 在 DatabaseModule 中提供 AppDatabase、TagDao、FileTagDao 实例

## Task 3: 实现领域层 - Domain Models 与 UseCase ✅
定义纯 Kotlin 的领域模型、Repository 接口和 UseCase。

### Steps
- [x] 创建 domain/model/Tag.kt、FileItem.kt、FileType.kt、SearchFilter.kt
- [x] 创建 domain/repository/FileRepository.kt 接口
- [x] 创建 domain/repository/TagRepository.kt 接口
- [x] 创建 domain/repository/SearchRepository.kt 接口
- [x] 创建 domain/usecase/BrowseFilesUseCase.kt
- [x] 创建 domain/usecase/ManageTagsUseCase.kt
- [x] 创建 domain/usecase/SearchFilesUseCase.kt
- [x] 创建 domain/usecase/FileOperationsUseCase.kt

## Task 4: 实现数据层 - Repository 实现与文件系统 ✅
实现 Domain 层的 Repository 接口，封装 Room 操作和文件系统操作。

### Steps
- [x] 创建 data/mapper/EntityMapper.kt
- [x] 实现 TagRepositoryImpl：对接 TagDao
- [x] 实现 FileRepositoryImpl：封装 java.io.File 操作
- [x] 创建 data/filesystem/FileSystemManager.kt
- [x] 创建 data/filesystem/FileScanner.kt
- [x] 创建 data/filesystem/ThumbnailProvider.kt
- [x] 实现 SearchRepositoryImpl
- [x] 在 RepositoryModule 中绑定所有接口到实现

## Task 5: 实现主题系统与通用 UI 组件 ✅
构建 Material 3 主题和可复用的 UI 组件。

### Steps
- [x] 创建 ui/theme/Color.kt + Type.kt + Theme.kt
- [x] 创建 ui/common/TagChip.kt
- [x] 创建 ui/common/FileItemCard.kt（含 FileGridItem）
- [x] 创建 ui/common/SearchBar.kt
- [x] 创建 ui/common/SortBottomSheet.kt
- [x] 创建 ui/common/LoadingIndicator.kt
- [x] 创建 ui/common/PermissionDialog.kt

## Task 6: 实现文件浏览页面 ✅
构建应用主页面——文件目录浏览。

### Steps
- [x] 创建 ui/filelist/FileListUiState.kt
- [x] 创建 ui/filelist/FileListViewModel.kt
- [x] 创建 ui/filelist/FileListScreen.kt
- [x] 集成权限检查
- [x] 集成 FileItemCard + TagChip

## Task 7: 实现标签管理页面 ✅
构建标签的创建、编辑、删除和排序功能。

### Steps
- [x] 创建 ui/tagmanager/TagManagerUiState.kt
- [x] 创建 ui/tagmanager/TagManagerViewModel.kt
- [x] 创建 ui/tagmanager/TagEditorDialog.kt
- [x] 创建 ui/tagmanager/TagManagerScreen.kt

## Task 8: 实现标签关联文件查看页面 ✅
点击标签后展示该标签关联的所有文件。

### Steps
- [x] 创建 ui/taggedfiles/TaggedFilesUiState.kt
- [x] 创建 ui/taggedfiles/TaggedFilesViewModel.kt
- [x] 创建 ui/taggedfiles/TaggedFilesScreen.kt

## Task 9: 实现搜索与筛选页面 ✅
构建全局搜索和组合筛选功能。

### Steps
- [x] 创建 ui/search/SearchUiState.kt
- [x] 创建 ui/search/SearchViewModel.kt（防抖搜索、标签AND/OR、类型筛选）
- [x] 创建 ui/search/SearchScreen.kt
- [x] 支持搜索结果中打标签操作

## Task 10: 实现分类聚合页面 ✅
按文件类型和标签分类聚合展示。

### Steps
- [x] 创建 ui/category/CategoryUiState.kt
- [x] 创建 ui/category/CategoryViewModel.kt
- [x] 创建 ui/category/CategoryScreen.kt

## Task 11: 实现文件操作功能 ✅
构建复制/移动/删除/重命名/分享等文件操作。

### Steps
- [x] 批量操作底部操作栏（标签、重命名、分享、删除）
- [x] 实现删除功能（确认弹窗 + 回收站 + 数据库清理）
- [x] 实现重命名功能（弹窗 + 数据库路径同步）
- [x] 实现复制/移动功能（目标目录 + 异步执行）
- [x] 实现分享（Intent.ACTION_SEND）和打开方式

## Task 12: 实现设置页面 ✅
构建设置功能，含主题切换和数据导入导出。

### Steps
- [x] 创建 ui/settings/SettingsUiState.kt
- [x] 创建 ui/settings/SettingsViewModel.kt
- [x] 创建 ui/settings/SettingsScreen.kt
- [x] 实现 JSON 导出
- [x] 实现 JSON 导入（含文件选择器）

## Task 13: 实现导航架构 ✅
使用 Navigation Compose 连接所有页面。

### Steps
- [x] 创建 navigation/NavGraph.kt（6条路由）
- [x] 配置底部导航栏（5个Tab：文件/分类/搜索/标签/设置）
- [x] 实现页面间参数传递（tagId）
- [x] 更新 MainActivity.kt（Scaffold + NavGraph + BottomNav）

## Task 14: 集成测试与收尾 ✅
确保所有模块正常协作。

### Steps
- [x] 添加 FileProvider 配置（AndroidManifest + file_paths.xml）
- [x] 修复 MainActivity Scaffold padding 传递
- [x] 验证所有 Hilt DI 绑定完整性
- [x] 验证所有 Navigation 路由一致性
- [x] 权限流程完善（MANAGE_EXTERNAL_STORAGE）

# Task Dependencies
- Task 2 → depends on Task 1
- Task 3 → depends on Task 1
- Task 4 → depends on Task 2, 3
- Task 5 → depends on Task 1
- Task 6-12 → depends on Task 4, 5
- Task 13 → depends on Task 6-12
- Task 14 → depends on Task 13
