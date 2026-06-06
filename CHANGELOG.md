# Changelog

All notable changes to this project will be documented in this file.

## [v1.0.0] - 2026-06-02

### First Release

**TagFile v1.0.0** - Android 标签化文件管理器首次正式发布。

---

### Added

#### 核心文件管理
- 文件浏览页面（FileListScreen）：目录导航、面包屑路径、列表/网格视图切换
- 排序功能：按名称 / 时间 / 大小 / 类型
- 文件缩略图预览（图片、视频、APK）
- 多选模式：长按进入多选，批量打标签、重命名、分享、删除
- 文件夹递归打标签：自动继承标签到子目录所有文件（isInherited=true）
- 新建文件夹功能
- 文件操作：复制、移动、删除（含回收站机制）、重命名、属性查看、分享、打开方式
- 文件移动/重命名时数据库路径同步更新

#### 标签管理
- 标签管理页面（TagManagerScreen）：创建、编辑、删除（级联解除关联）、排序
- 12 种预设标签颜色
- 标签关联文件查看页面（TaggedFilesScreen）
- 标签数据 JSON 导出/导入

#### 搜索与筛选
- 全局搜索：文件名关键词实时搜索（防抖 300ms）
- 多标签 AND/OR 模式筛选
- 文件类型筛选
- 组合筛选（关键词 + 标签 + 类型）
- 搜索结果中可直接打标签

#### 分类聚合
- 按文件类型聚合：图片/视频/文档/音频/压缩包/APK/其他
- 按标签聚合（虚拟文件夹）
- 最近文件列表（按修改时间倒序）
- 大文件扫描（超过指定大小的文件列表）

#### 书架模块 (Bookshelf)
- BookEntity（11 个字段）：id, title, author, tags, coverPath, folderPath, pageCount, viewCount, totalDuration, lastReadTime, createdAt
- BookDao：CRUD + 搜索（按标题/作者/标签）+ 统计方法
- 书架主页（ShelfScreen）：搜索栏（作者/标签/名称三种模式）+ 每日随机推荐（横向滚动）+ 自动检索按钮 + 书籍大图网格（每行 3 本）
- 书籍浏览页（BookViewerScreen）：HorizontalPager 翻页 + 浏览计数 + 阅读时长统计
- 书籍详情页（BookDetailScreen）与书籍列表页（BookListScreen）
- 作者自动提取：从文件夹名的 `[]` 或 `【】` 中解析
- 书架文件夹路径配置：设置页可选择书架根目录路径
- 数据库版本升级至 4

#### 图像增强引擎 (Enhance)
- Anime4K 风格 GPU 加速图像处理（OpenGL ES 2.0）
- 完整处理管线：EglCore / GlProgram / GlShaders / GpuProcessor / Anime4KProcessor
- 7 项可调参数：强度（strength）、锐化（sharpness）、降噪（denoise）、线条加深（lineDarkening）、对比度（contrast）、饱和度（saturation）、放大系数（upscaleFactor）
- 增强前后对比预览（Compare 模式）
- 增强结果保存
- 连续增强模式（设置中可开关）
- 滤镜预设库（FilterPresetEntity + FilterPresetDao）：创建、编辑、启用管理
- 滤镜设置页（FilterSettingsScreen）
- 数据库版本升级至 5

#### 图片查看器优化
- 定制 Coil ImageLoader（@Singleton，通过 Hilt 注入）：512MB 磁盘缓存（`cacheDir/image_cache`），启用内存缓存
- HorizontalPager 预加载：`beyondBoundsPageCount = 2`，主动预加载前后 3 页图片
- 快速滑动智能暂停：`isScrollInProgress` 检测 + 150ms 延迟
- 当前页图片使用 `Priority.HIGH`
- 关闭硬件位图（`allowHardware(false)`）避免手势缩放 crash
- 图片查看器与书架浏览器同步接入优化 ImageLoader

#### 滚动位置恢复
- FileListViewModel 维护路径到滚动位置的缓存映射
- 进入子目录前保存滚动位置，返回时自动恢复
- 同时支持 LazyColumn 和 LazyVerticalGrid 滚动恢复

#### 个性化设置
- 暗色/亮色模式切换（全局即时生效）
- 壁纸背景（自定义图片路径 + 透明度调节）
- 自定义文字颜色（PsColorPicker 取色器）
- 自定义图标颜色
- 文字描边开关与描边颜色自定义

#### 导航架构
- 单 Activity 架构（MainActivity）
- Navigation Compose：20 条路由定义
- 底部导航栏 5 个 Tab：文件 / 分类 / 搜索 / 标签 / 设置
- 页面间参数传递（tagId、fileType、bookId、filterId、query/mode）

#### 权限管理
- Android 11+：MANAGE_EXTERNAL_STORAGE 全文件访问权限引导
- Android 10 及以下：动态请求 READ/WRITE_EXTERNAL_STORAGE
- 权限拒绝降级提示

---

### Technical Stack Additions

- Kotlin 1.9.22
- Jetpack Compose BOM 2024.06.00
- Material 3
- Navigation Compose 2.7.6
- Room 2.6.1
- Hilt 2.50
- Coil 2.5.0
- Okio 3.7.0
- kotlinx-serialization-json 1.6.2
- kotlinx-coroutines-android 1.7.3
- KSP 1.9.22-1.0.17

---

### Min Requirements

- Min SDK: 26 (Android 8.0 Oreo)
- Target SDK: 34 (Android 14)
- Compile SDK: 34
- Java/Kotlin: JDK 17
