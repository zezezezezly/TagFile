# TagFile

Android 标签化文件管理器 —— 用标签和书架重新定义文件管理

![Version](https://img.shields.io/badge/Version-1.0.1-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-orange)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 概览

TagFile 是一款基于标签化管理的 Android 文件管理器，以 **湖蓝暖灰** 配色和 **玻璃态模糊** 为设计语言，突破了传统目录树的管理模式。核心思路是：为文件/文件夹打彩色标签，通过标签多对多映射实现灵活分类；同时将图片文件夹自动识别为"书籍"，提供封面式的阅读管理体验。

---

## 核心功能

### 标签管理

- **打标签**：为任意文件或文件夹打上 12 种彩色标签，支持长按单选和批量多选后批量打标签/取消标签
- **标签分组**：标签支持分组管理，可展开/折叠分组，支持重命名、删除、合并分组，以及批量转移未分组标签到指定分组
- **标签聚合**：每个标签相当于一个"虚拟文件夹"，点击即可查看所有关联文件
- **文件夹递归**：对文件夹打标签时，其内部所有文件自动继承该标签（`isInherited` 标记）
- **标签排序**：支持默认排序、按颜色排序、按文件数量排序

### 文件浏览

- **目录导航**：面包屑路径支持快速跳转任意层级，返回上级目录时自动恢复滚动位置
- **视图切换**：列表视图 / 网格视图自由切换
- **排序**：按名称、修改时间、大小、类型排序
- **缩略图**：支持图片、视频、APK 缩略图预览
- **多选模式**：长按进入多选，支持全选/取消、批量打标签、批量删除、批量移动
- **文件操作**：重命名、复制、移动、删除 → 回收站
- **新建文件夹**

### 回收站

- 删除的文件进入回收站（`TrashEntity`），记录原始路径、删除时间、文件大小
- 支持恢复原路径或彻底删除
- 回收站可在设置中手动清理

### 搜索与筛选

- **关键词搜索**：文件名实时搜索，300ms 防抖
- **标签筛选**：多标签 AND/OR 组合模式
- **类型筛选**：图片 / 视频 / 文档 / 音频 / 压缩包 / APK / 其他
- **组合筛选**：关键词 + 标签 + 类型三者叠加
- 搜索结果中可直接打标签

### 分类聚合

- **按类型分**：图片、视频、文档、音频、压缩包、APK、其他
- **按标签分**：每个标签作为虚拟文件夹入口
- **最近文件**：按修改时间倒序排列
- **大文件扫描**：超过指定大小的文件列表
- **未标签文件**：查看所有未分配任何标签的文件，支持批量打标签

### 书架

- **自动识别**：将仅含图片的子文件夹自动识别为"书籍"，支持 `[作者名] 书名` 和 `【作者名】书名` 命名约定自动提取作者
- **书籍字段**：封面、标题、作者、标签、页数、当前页码、阅读进度、浏览计数、阅读时长、评分（0-10）、简介
- **书架主页**：搜索栏（按作者/标签/名称三种模式）、每日随机推荐、书籍网格（每行 3 本玻璃态卡片）
- **书籍浏览**：HorizontalPager 翻页，自动保存阅读进度，前台计时阅读时长，支持沉浸模式（自动隐藏 UI）
- **书籍详情**：评分编辑器、简介编辑器、作者编辑（带实时预览）、标签管理、手动选择封面
- **书籍列表**：按作者分组展开/折叠，按评分/浏览数/标题/页数排序
- **作者导航**：点击作者名跳转至该作者全部作品
- **数据修复**：一键检测并修复封面丢失、页数不一致、重复记录等问题

### 阅读统计与历史

- **阅读历史**：按最后阅读时间降序排列，展示封面、书名、作者、阅读时长、相对时间（"刚刚" / "5 分钟前" / "昨天 14:30" / "6月5日"）
- **阅读统计**：总阅读时长、已读书籍数、活跃天数、Top N 最常阅读排行榜

### 图像增强

- **Anime4K 引擎**：基于 OpenGL ES 2.0 的 GPU 加速图像增强（EglCore → GlProgram → GlShaders → GpuProcessor → Anime4KProcessor）
- **7 项可调参数**：强度、锐化、降噪、线条加深、对比度、饱和度、放大系数
- **对比预览**：增强前后 split 对比
- **滤镜预设库**：创建、编辑、启用自己的滤镜预设（`FilterPresetEntity`），支持连续增强模式

### 图片查看器

- 定制 Coil ImageLoader（`@Singleton`）：512MB 磁盘缓存 + 内存缓存，跨页面共享
- HorizontalPager 预加载（`beyondBoundsPageCount = 2`），快速滑动时智能暂停预加载
- 当前页 `Priority.HIGH`，关闭硬件位图避免手势缩放崩溃
- 路由从 JSON 数组改为传文件夹路径，避免 URL 长度限制

### 个性化

- 暗色/亮色模式切换，全局即时生效
- 自定义壁纸背景 + 透明度滑块（壁纸表面半透明叠加，适配玻璃态主题）
- 文字描边（开关 + 颜色选择器），增强壁纸上的可读性

### 数据导入/导出

- 标签数据 JSON 导出/导入
- 数据库整体导出/导入（替换模式与合并模式），包含标签、书籍、文件索引、回收站等完整数据

### 首页仪表盘

- 统计卡片：标签数、书籍数、回收站数
- 书架快捷入口
- 最近阅读的书籍

---

## 设计语言

### 配色

| 角色 | 亮色 | 深色 |
|------|------|------|
| Primary | `#00897B` 湖蓝 | `#4DB6AC` |
| Background | `#F5F0EB` 暖灰 | `#1A1D1F` |
| Surface | `#F8F4F0` | `#1E2123` |
| Surface Variant | `#E8E4DE` | `#25282A` |

### 玻璃态

卡片、对话框、BottomSheet 统一使用半透明玻璃态（`surface.copy(alpha = 0.85f)`）。Android 12+ 使用 `FLAG_BLUR_BEHIND` + `RenderEffect` 原生模糊，低版本回退半透明。

| 组件 | 文件 |
|------|------|
| `GlassDialog` | `ui/common/GlassDialog.kt` |
| `GlassBottomSheet` | `ui/common/GlassBottomSheet.kt` |
| `GlassCard` / `BlurredSurface` | `ui/common/BlurredSurface.kt` |

### 标签色彩（12 色，降饱和 20%）

`#CC5252` `#C8507A` `#8A4090` `#6E4BA0` `#4554A0` `#357DC9` `#208EC8` `#189991` `#00786B` `#509E50` `#80AD52` `#F07040`

### 动画

| 场景 | 动画 | 参数 |
|------|------|------|
| 页面切换 | 共享轴过渡 | 350ms tween, 右滑入/左滑返回 |
| 列表入场 | 交错淡入上浮 | 40ms/项, spring(0.7f) |
| 骨架屏 | Shimmer 渐变动画 | 1200ms 循环 |
| 展开/收起 | `expandVertically` + `fadeIn/Out` | Material 默认 |

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 1.9.22 |
| UI | Jetpack Compose + Material 3 | BOM 2024.06.00 |
| 架构 | MVVM + Clean Architecture | — |
| 依赖注入 | Hilt | 2.50 |
| 数据库 | Room | 2.6.1 |
| 导航 | Navigation Compose | 2.7.6 |
| 图片加载 | Coil | 2.5.0 |
| GPU 增强 | OpenGL ES | 2.0 |
| 序列化 | kotlinx-serialization | 1.6.2 |
| 协程 | kotlinx-coroutines | 1.7.3 |
| 文件 I/O | Okio | 3.7.0 |
| JDK | 17 | — |
| Min SDK | 26 (Android 8.0) | — |
| Target SDK | 34 (Android 14) | — |

---

## 架构

```
ui/                  → Compose Screens + ViewModels
domain/              → 纯 Kotlin 领域层（Model, Repository 接口, UseCase）
data/                → 数据层（Room, FileSystem, Preferences, Repository 实现）
di/                  → Hilt 依赖注入模块
navigation/          → NavGraph 路由定义
enhance/             → 图像增强独立模块（data/domain/ui 三层）
```

### 数据模型

| Entity | 表名 | 说明 |
|--------|------|------|
| `TagEntity` | `tags` | 标签（name, color, icon, groupName, sortOrder, createdAt） |
| `BookEntity` | `books` | 书籍（title, author, coverPath, pageCount, score, readDuration 等 14 字段） |
| `FileIndexEntity` | `file_index` | 文件索引（path, name, size, modifiedAt, type, isDirectory） |
| `FileTagCrossRefEntity` | `file_tag_cross_ref` | 文件-标签关联（filePath, tagId, isInherited） |
| `FilterPresetEntity` | `filter_presets` | 图像增强滤镜预设 |
| `TrashEntity` | `trash` | 回收站（originalPath, trashPath, deletedAt, fileSize） |

### DAO 层

| DAO | 方法数 | 说明 |
|-----|--------|------|
| `TagDao` | 10+ | 增删改查、分组管理（renameGroup/clearGroup/mergeGroups/moveUngroupedToGroup） |
| `BookDao` | 10+ | 增删改查、按作者/标签/标题搜索、统计查询 |
| `FileIndexDao` | 8+ | 增删改查、按路径/类型/关键词查询、获取计数 |
| `FileTagDao` | 6+ | 交叉引用管理、按标签查文件、按文件查标签 |
| `FilterPresetDao` | 4 | 滤镜预设增删改查 |
| `TrashDao` | 5 | 回收站增删查、按时间清理 |

### Repository 接口

| 接口 | 实现 | 说明 |
|------|------|------|
| `TagRepository` | `TagRepositoryImpl` | 标签 CRUD、分组管理、文件关联 |
| `ShelfRepository` | `ShelfRepositoryImpl` | 书籍管理、数据修复、阅读统计 |
| `SearchRepository` | `SearchRepositoryImpl` | 搜索、筛选、未标签文件 |
| `FileRepository` | `FileRepositoryImpl` | 文件系统操作、文件索引 |

### 偏好设置

| 类 | 职责 |
|----|------|
| `AppearancePreferences` | 暗色模式、壁纸状态、文字描边 |
| `EnhancePreferences` | 图像增强参数 |
| `ShelfPreferences` | 书架根目录路径、每日推荐日期 |
| `WallpaperPreferences` | 壁纸路径、透明度 |

---

## 项目结构

```
TagFile/
├── app/
│   └── src/main/java/com/tagfile/app/
│       ├── App.kt                           # @HiltAndroidApp 入口
│       ├── MainActivity.kt                  # 单 Activity 入口
│       │
│       ├── data/                            # 数据层
│       │   ├── export/
│       │   │   └── DatabaseExport.kt        # 数据库导入/导出模型
│       │   ├── filesystem/
│       │   │   ├── FileSystemManager.kt     # 文件 CRUD 操作
│       │   │   ├── FileScanner.kt           # 文件扫描
│       │   │   ├── FileIndexer.kt           # 文件索引
│       │   │   └── ThumbnailProvider.kt     # 缩略图提供
│       │   ├── local/
│       │   │   ├── AppDatabase.kt           # Room 数据库 (v8)
│       │   │   ├── dao/
│       │   │   │   ├── TagDao.kt
│       │   │   │   ├── BookDao.kt
│       │   │   │   ├── FileIndexDao.kt
│       │   │   │   ├── FileTagDao.kt
│       │   │   │   ├── FilterPresetDao.kt
│       │   │   │   └── TrashDao.kt
│       │   │   └── entity/
│       │   │       ├── TagEntity.kt
│       │   │       ├── BookEntity.kt
│       │   │       ├── FileIndexEntity.kt
│       │   │       ├── FileTagCrossRefEntity.kt
│       │   │       ├── FilterPresetEntity.kt
│       │   │       └── TrashEntity.kt
│       │   ├── mapper/
│       │   │   └── EntityMapper.kt          # Entity ↔ Domain 映射
│       │   ├── preferences/
│       │   │   ├── AppearancePreferences.kt
│       │   │   ├── EnhancePreferences.kt
│       │   │   ├── ShelfPreferences.kt
│       │   │   └── WallpaperPreferences.kt
│       │   └── repository/
│       │       ├── TagRepositoryImpl.kt
│       │       ├── ShelfRepositoryImpl.kt
│       │       ├── SearchRepositoryImpl.kt
│       │       └── FileRepositoryImpl.kt
│       │
│       ├── domain/                          # 领域层（纯 Kotlin）
│       │   ├── model/
│       │   │   ├── Book.kt                  # 14 字段
│       │   │   ├── Tag.kt                   # 7 字段
│       │   │   ├── FileItem.kt
│       │   │   ├── FileType.kt
│       │   │   ├── FileTagCrossRef.kt
│       │   │   ├── SearchFilter.kt
│       │   │   └── RepairResult.kt
│       │   ├── repository/
│       │   │   ├── TagRepository.kt
│       │   │   ├── ShelfRepository.kt
│       │   │   ├── SearchRepository.kt
│       │   │   └── FileRepository.kt
│       │   └── usecase/
│       │       ├── BrowseFilesUseCase.kt
│       │       ├── SearchFilesUseCase.kt
│       │       └── FileOperationsUseCase.kt
│       │
│       ├── di/                              # Hilt DI 模块
│       │   ├── DatabaseModule.kt
│       │   ├── RepositoryModule.kt
│       │   ├── ImageLoaderModule.kt
│       │   ├── UseCaseModule.kt
│       │   └── ImageViewerEntryPoint.kt     # @EntryPoint 获取 ImageLoader
│       │
│       ├── navigation/
│       │   └── NavGraph.kt                  # 20+ 路由定义
│       │
│       ├── enhance/                         # 图像增强独立模块
│       │   ├── data/
│       │   │   ├── processor/
│       │   │   │   ├── Anime4KProcessor.kt
│       │   │   │   └── gl/
│       │   │   │       ├── EglCore.kt
│       │   │   │       ├── GlProgram.kt
│       │   │   │       ├── GlShaders.kt
│       │   │   │       └── GpuProcessor.kt
│       │   │   └── repository/
│       │   │       └── FilterPresetRepository.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   └── EnhanceParams.kt
│       │   │   └── usecase/
│       │   │       └── EnhanceImageUseCase.kt
│       │   └── ui/
│       │       ├── EnhanceScreen.kt
│       │       ├── EnhanceViewModel.kt
│       │       ├── EnhanceUiState.kt
│       │       ├── FilterLibraryScreen.kt
│       │       ├── FilterLibraryViewModel.kt
│       │       ├── FilterSettingsScreen.kt
│       │       └── FilterSettingsViewModel.kt
│       │
│       └── ui/                              # 界面层（26 个 Screen + ViewModel）
│           ├── home/
│           │   ├── HomeScreen.kt            # 仪表盘 + 快捷入口
│           │   └── HomeViewModel.kt
│           ├── filelist/
│           │   ├── FileListScreen.kt        # 文件浏览器
│           │   ├── FileListViewModel.kt
│           │   └── FileListUiState.kt
│           ├── category/
│           │   ├── CategoryScreen.kt        # 分类聚合
│           │   ├── CategoryViewModel.kt
│           │   └── CategoryUiState.kt
│           ├── search/
│           │   ├── SearchScreen.kt          # 搜索
│           │   ├── SearchViewModel.kt
│           │   └── SearchUiState.kt
│           ├── typefiles/
│           │   ├── TypeFilesScreen.kt       # 按类型浏览
│           │   ├── TypeFilesViewModel.kt
│           │   └── TypeFilesUiState.kt
│           ├── taggedfiles/
│           │   ├── TaggedFilesScreen.kt     # 标签关联文件
│           │   ├── TaggedFilesViewModel.kt
│           │   └── TaggedFilesUiState.kt
│           ├── untagged/
│           │   ├── UntaggedFilesScreen.kt   # 未标签文件
│           │   └── UntaggedFilesViewModel.kt
│           ├── tagmanager/
│           │   ├── TagManagerScreen.kt      # 标签管理 + 分组管理
│           │   ├── TagManagerViewModel.kt
│           │   ├── TagManagerUiState.kt
│           │   └── TagEditorDialog.kt
│           ├── shelf/
│           │   ├── ShelfScreen.kt           # 书架主页
│           │   ├── ShelfViewModel.kt
│           │   ├── ShelfUiState.kt
│           │   ├── BookDetailScreen.kt      # 书籍详情
│           │   ├── BookDetailViewModel.kt
│           │   ├── BookListScreen.kt        # 书籍列表
│           │   ├── BookListViewModel.kt
│           │   └── BookListUiState.kt
│           ├── bookviewer/
│           │   ├── BookViewerScreen.kt      # 书籍翻页浏览
│           │   ├── BookViewerViewModel.kt
│           │   └── BookViewerUiState.kt
│           ├── history/
│           │   ├── ReadingHistoryScreen.kt  # 阅读历史
│           │   └── ReadingHistoryViewModel.kt
│           ├── statistics/
│           │   ├── ReadingStatisticsScreen.kt  # 阅读统计
│           │   └── ReadingStatisticsViewModel.kt
│           ├── imageviewer/
│           │   ├── ImageViewerScreen.kt     # 图片查看器
│           │   └── ImageViewerViewModel.kt
│           ├── trash/
│           │   ├── TrashScreen.kt           # 回收站
│           │   └── TrashViewModel.kt
│           ├── settings/
│           │   ├── SettingsScreen.kt        # 设置主页
│           │   ├── SettingsViewModel.kt
│           │   ├── SettingsUiState.kt
│           │   ├── PersonalizationScreen.kt # 个性化
│           │   ├── PersonalizationViewModel.kt
│           │   ├── ShelfSettingsScreen.kt   # 书架设置
│           │   └── PsColorPicker.kt         # 取色器组件
│           ├── common/                      # 通用组件
│           │   ├── FileItemCard.kt
│           │   ├── TagChip.kt
│           │   ├── SearchBar.kt
│           │   ├── SortBottomSheet.kt
│           │   ├── AnimatedListItems.kt     # 交错入场动画
│           │   ├── Skeleton.kt              # Shimmer 骨架屏
│           │   ├── BlurredSurface.kt        # 玻璃态容器
│           │   ├── GlassBottomSheet.kt
│           │   ├── GlassDialog.kt
│           │   ├── LoadingIndicator.kt
│           │   └── PermissionDialog.kt
│           └── theme/
│               ├── Color.kt                 # 配色定义 + 12 标签色
│               ├── Type.kt                  # 15 级 Material 3 字体
│               ├── Theme.kt                 # 主题（亮/暗 + 壁纸 + 描边）
│               └── WallpaperBackground.kt   # 壁纸背景组件
│
├── build.gradle.kts
├── app/build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── CONTEXT.md                               # 领域术语 + 设计语言
└── CHANGELOG.md                             # 版本历史
```

---

## 构建

```bash
./gradlew assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

> 需要 JDK 17 + Android SDK（compileSdk 34）

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.1 | 2026-06 | 偏好设置重构、书籍数据修复、作者管理、Material 3 主题焕新、玻璃态 UI、回收站、阅读统计、标签分组管理 |
| v1.0.0 | 2026-06 | 首次发布 |

详见 [CHANGELOG.md](./CHANGELOG.md)

---

## License

MIT