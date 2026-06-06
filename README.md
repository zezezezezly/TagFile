# TagFile

Android 标签化文件管理器 | Android Tag-based File Manager

![TagFile](https://img.shields.io/badge/Version-1.0.0-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-green)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 特性 | Features

### 标签化管理 | Tag-based Management
- 为任意文件/文件夹打上彩色标签，实现多对多映射
- Tag any file or folder with colored tags, supporting many-to-many relationships
- 支持长按单选或批量多选后批量打标签
- Support long-press single selection or batch tagging
- 文件夹递归打标签：自动继承标签到子目录所有文件
- Recursive tagging: inherit tags to all files within a folder

### 文件浏览 | File Browsing
- 目录导航、面包屑路径
- Directory navigation with breadcrumb path
- 列表视图 / 网格视图切换
- List view / Grid view toggle
- 排序：按名称 / 时间 / 大小 / 类型
- Sort by: name / date / size / type
- 文件缩略图预览（图片、视频、APK）
- File thumbnails (images, videos, APK)

### 书架模块 | Bookshelf Module
- 将仅含图片的子文件夹自动识别为"书籍"，以大图封面展示
- Automatically detect image-only folders as "books" with cover display
- 从文件夹名中提取作者（支持 `[]` 和 `【】` 格式）
- Extract author from folder name (supports `[]` and `【】` formats)
- 每日随机推荐
- Daily random recommendations
- 阅读统计：浏览次数、累计阅读时长
- Reading statistics: view count, total reading duration

### 图像增强 | Image Enhancement
- Anime4K 风格 GPU 加速图像增强（OpenGL ES 2.0）
- Anime4K-style GPU-accelerated image enhancement (OpenGL ES 2.0)
- 7 项可调参数：强度、锐化、降噪、线条加深、对比度、饱和度、放大系数
- 7 adjustable parameters: strength, sharpness, denoise, line darkening, contrast, saturation, upscale factor
- 滤镜预设库：创建、编辑、启用管理
- Filter preset library: create, edit, enable management
- 增强前后对比预览
- Before/after comparison preview

### 搜索与筛选 | Search & Filter
- 文件名关键词实时搜索（防抖）
- Real-time keyword search with debounce
- 多标签 AND/OR 组合筛选
- Multi-tag AND/OR filter combination
- 按文件类型筛选：图片 / 视频 / 文档 / 音频 / 压缩包 / APK / 其他
- Filter by type: image / video / document / audio / archive / APK / other
- 组合筛选：关键词 + 标签 + 类型
- Combined filter: keyword + tags + type

### 分类聚合 | Categorization
- 按文件类型聚合浏览
- Browse by file type
- 按标签聚合（虚拟文件夹）
- Browse by tag (virtual folders)
- 最近文件列表
- Recent files list
- 大文件扫描
- Large file scanner

### 个性化 | Personalization
- 暗色 / 亮色模式切换
- Dark / Light mode toggle
- 壁纸背景（自定义图片 + 透明度）
- Wallpaper background (custom image + opacity)
- 自定义文字颜色 / 图标颜色
- Custom text color / icon color
- 文字描边开关与颜色
- Text stroke toggle and color

---

## 技术栈 | Tech Stack

| 类别 | Category | 技术 | Technology |
|------|----------|------|------------|
| 语言 | Language | Kotlin 1.9.22 | Kotlin 1.9.22 |
| UI | UI | Jetpack Compose + Material 3 | Jetpack Compose + Material 3 |
| 架构 | Architecture | MVVM + Clean Architecture | MVVM + Clean Architecture |
| 依赖注入 | DI | Hilt 2.50 | Hilt 2.50 |
| 数据库 | Database | Room 2.6.1 | Room 2.6.1 |
| 导航 | Navigation | Navigation Compose 2.7.6 | Navigation Compose 2.7.6 |
| 图片加载 | Image Loading | Coil 2.5.0 | Coil 2.5.0 |
| GPU 增强 | GPU Enhance | OpenGL ES 2.0 | OpenGL ES 2.0 |
| 序列化 | Serialization | kotlinx-serialization 1.6.2 | kotlinx-serialization 1.6.2 |
| 文件 I/O | File I/O | Okio 3.7.0 | Okio 3.7.0 |

---

## 项目结构 | Project Structure

```
TagFile/
├── app/
│   └── src/main/java/com/tagfile/app/
│       ├── data/                    # 数据层 Data Layer
│       │   ├── filesystem/         # 文件系统操作（FileSystemManager, FileScanner, ThumbnailProvider）
│       │   ├── local/              # Room 数据库（AppDatabase, DAOs, Entities）
│       │   ├── mapper/             # Entity ↔ Domain Model 映射
│       │   ├── preferences/        # SharedPreferences 管理（PreferencesManager）
│       │   ├── repository/         # Repository 接口实现
│       │   ├── enhance/            # 图像增强模块
│       │   │   ├── data/           # 处理器（Anime4K, GlProgram, GlShaders, GpuProcessor）
│       │   │   ├── domain/         # 领域模型（EnhanceParams）和用例
│       │   │   └── ui/             # 增强 UI（EnhanceScreen, FilterLibrary, FilterSettings）
│       │   └── repository/
│       ├── domain/                  # 领域层 Domain Layer（纯 Kotlin，无 Android 依赖）
│       │   ├── model/               # 数据模型（Book, FileItem, FileType, SearchFilter, Tag）
│       │   ├── repository/          # Repository 接口定义
│       │   └── usecase/            # 用例（BrowseFiles, FileOperations, ManageTags, SearchFiles）
│       ├── di/                      # Hilt DI 模块
│       ├── navigation/              # Navigation Compose 导航图
│       ├── ui/                      # UI 层（所有 Compose Screen & ViewModel）
│       │   ├── bookviewer/          # 书籍浏览
│       │   ├── category/            # 分类聚合
│       │   ├── common/              # 通用组件（FileItemCard, TagChip, SearchBar, SortBottomSheet）
│       │   ├── enhance/              # 增强 UI（重导出）
│       │   ├── filelist/            # 文件浏览
│       │   ├── home/                # 主页
│       │   ├── imageviewer/          # 图片查看器
│       │   ├── search/               # 搜索
│       │   ├── settings/             # 设置 & 个性化
│       │   ├── shelf/               # 书架 & 书籍列表 & 详情
│       │   ├── taggedfiles/          # 标签关联文件
│       │   ├── tagmanager/           # 标签管理
│       │   ├── theme/                # Material 3 主题
│       │   └── typefiles/            # 按类型浏览
│       ├── App.kt                    # Hilt Application
│       └── MainActivity.kt           # 单 Activity 入口
├── build.gradle.kts                  # 项目级构建配置
├── app/build.gradle.kts              # 模块级构建配置
└── gradle.properties                 # Gradle 配置
```

---

## 构建 | Build

```bash
# 克隆项目后，在项目根目录执行
./gradlew assembleDebug

# APK 输出位置
app/build/outputs/apk/debug/app-debug.apk
```

> **Note**: 需要 Android SDK 环境（compileSdk 34）

---

## 版本历史 | Changelog

| 版本 | Version | 说明 | Description |
|------|---------|------|-------------|
| v1.0.0 | 2026-06 | 首次正式发布 - TagFile 标签化文件管理器 | Initial Release - TagFile Tag-based File Manager |

详细更新日志请参阅 [CHANGELOG.md](./CHANGELOG.md)

---

## License

MIT License
