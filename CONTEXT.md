# TagFile — 领域术语与设计语言

## 领域术语

| 术语 | 定义 |
|------|------|
| **文件 (File)** | 设备存储中的文件或文件夹，通过文件浏览器访问 |
| **标签 (Tag)** | 用户自定义的彩色标记，用于分类和过滤文件 |
| **书籍 (Book)** | 书架中的图像集合（文件夹），包含封面、标题、作者等元数据 |
| **书架 (Shelf)** | 管理图片类书籍的模块，支持排序、搜索、评分 |
| **作者 (Author)** | 书籍的创作者，支持文件夹命名约定 `[作者名] 书名` 自动推断 |
| **作者继承 (Author Inheritance)** | 子目录中的书籍自动继承上层 `[作者名]` 目录的作者属性 |
| **滤镜 (Filter)** | 画质增强的预设参数组合（强度、锐度、降噪等） |
| **搜索模式 (SearchMode)** | 搜索维度：AUTHOR(作者)、TAGS(标签)、TITLE(名称) |
| **排序模式 (SortMode)** | SCORE(评分)、AUTHOR(作者)、VIEW_COUNT(浏览)、TITLE(名称)、PAGE_COUNT(页数) |

---

## 设计语言 — 玻璃态 (Glassmorphism) + 湖蓝暖灰

### 配色体系

| 角色 | 亮色 | 深色 |
|------|------|------|
| Primary (主色) | `#00897B` 湖蓝 | `#4DB6AC` |
| Primary Variant | `#00695C` | — |
| Background (背景) | `#F5F0EB` 暖灰 | `#1A1D1F` |
| Surface (表面) | `#F8F4F0` | `#1E2123` |
| Surface Variant | `#E8E4DE` | `#25282A` |
| On Background | `#1B1C1E` | `#E8E6E3` |
| On Surface Variant | `#65635F` | `#A9A6A0` |
| Error | `#C62828` | `#EF5350` |

### 圆角规范（分级）

| 层级 | 半径 | 适用组件 |
|------|------|----------|
| 大 (large/extraLarge) | 16dp | 卡片、对话框、BottomSheet |
| 中 (medium) | 12dp | 按钮、输入框、Chip |
| 小 (small/extraSmall) | 8dp | 缩略图、标签 |

### 字体层级

15 级 Material 3 Typography：
- `displayLarge` (57sp) — 页面主标题
- `displayMedium` (45sp) — 次要标题
- `displaySmall` (36sp)
- `headlineLarge` (32sp) — 页面标题
- `headlineMedium` (28sp)
- `headlineSmall` (24sp)
- `titleLarge` (22sp Medium) — 卡片标题
- `titleMedium` (16sp Medium)
- `titleSmall` (14sp SemiBold) — 分类标题
- `bodyLarge` (16sp) — 正文
- `bodyMedium` (14sp)
- `bodySmall` (12sp)
- `labelLarge` (14sp Medium)
- `labelMedium` (12sp Medium)
- `labelSmall` (11sp Medium)

### 阴影与层级

- 卡片：1-2dp elevation，微妙阴影
- 浮层（如排序面板）：4dp elevation
- 深色模式：通过亮度差区分层级

### 动画规范

| 场景 | 动画 | 参数 |
|------|------|------|
| 页面切换 | 共享轴过渡 (Shared Axis) | 350ms tween, 右侧滑入/左滑返回 |
| 列表入场 | 交错淡入上浮 (Staggered Fade+Slide) | 40ms/项, spring(0.7f), 20dp |
| 展开/收起 | expandVertically + fadeIn/Out | 默认 Material 参数 |
| 沉浸模式切换 | fadeIn/Out | ImageViewer |
| 内容展开 | animateContentSize | BookDetail 简介 |

### 按钮体系

| 层级 | 样式 | 用途 |
|------|------|------|
| Primary | `Button` (实心填充 `RoundedCornerShape(12.dp)`) | 主操作（"开始阅读"、"浏览书架"） |
| Secondary | `FilledTonalButton` | 次要操作（"选择标签"） |
| Tertiary | `TextButton` | 辅助操作（"编辑"、"保存"、"取消"） |

### 标签色彩（12色，降饱和20%）

`#CC5252`, `#C8507A`, `#8A4090`, `#6E4BA0`, `#4554A0`, `#357DC9`, `#208EC8`, `#189991`, `#00786B`, `#509E50`, `#80AD52`, `#F07040`

### 对话框

- 不使用硬编码背景色，跟随 `MaterialTheme.colorScheme.surface`
- 确认/取消使用 TextButton

### 个性化

- 壁纸：支持自定义背景图片 + 透明度滑块
- 文字描边：带颜色选择器
- 已移除：自定义文字颜色、自定义图标颜色（由新配色主题统一管理）

### 加载态

- 使用骨架屏 Shimmer 动画，替代全屏 CircularProgressIndicator（推荐，渐进迁移）

### 玻璃态

- 覆盖范围：卡片、对话框、BottomSheet 半透明模糊（85% alpha）
- TopAppBar 保持不透明
- Android 12+ 使用 `RenderEffect.createBlurEffect()` 原生模糊，低版本回退半透明无模糊
- 颜色：`MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)`
- 圆角：卡片 16dp，弹窗 16dp，BottomSheet 顶部 16dp

#### 弹窗组件

| 组件 | 封装 | 模糊技术 |
|------|------|----------|
| GlassDialog | `ui/common/GlassDialog.kt` | Android 12+ `FLAG_BLUR_BEHIND` + `setBlurBehindRadius(30)`；低版本半透明回退 |
| GlassBottomSheet | `ui/common/GlassBottomSheet.kt` | `containerColor = surface.copy(alpha=0.85f)`，顶部圆角 16dp |
| GlassCard | `ui/common/BlurredSurface.kt` | `RenderEffect` 或半透明回退 |
| BlurredSurface | `ui/common/BlurredSurface.kt` | 底层玻璃态容器盒 |

- GlassDialog API 透明封装 `AlertDialog` 参数（`onDismissRequest` / `title` / `text` / `confirmButton` / `dismissButton`）
- GlassBottomSheet API 透明封装 `ModalBottomSheet` 参数（`onDismissRequest` / `content`）
- 弹窗模糊半径 30，确保弹窗背后内容清晰可辨但不分散注意力

---

## 新功能术语

| 术语 | 定义 |
|------|------|
| **批量操作 (Multi-select)** | 长按进入多选模式，可全选、批量打标签、批量删除 |
| **复制/移动 (Copy/Move)** | 移动/复制文件到任意目录，使用 SAF 目录选择器 |
| **未分类 (Untagged)** | 没有任何标签的文件，分类页提供入口 |
| **标签分组 (Tag Groups)** | 标签归组管理，可按组筛选 |
| **阅读进度 (Reading Progress)** | 自动保存翻页进度，下次打开自动跳转 |
| **阅读时长 (Read Duration)** | 每本书累计在前台阅读的时间（毫秒），仅在应用前台时计时，后台不计入 |
| **阅读历史 (Reading History)** | 所有已阅读书籍的独立页面，按 lastReadTime 降序排列，展示封面、书名、作者、阅读时长、最后阅读时间（相对时间） |
| **手动选择封面 (Manual Cover Selection)** | 从书籍文件夹中选择图片作为封面 |
| **仪表盘 (Dashboard)** | 首页顶部统计卡片（总文件/标签/书籍/回收站） |
| **快捷访问 (Quick Access)** | 用户自定义常用文件夹/标签/书籍快捷方式，点击直达 |
| **回收站 (Trash Bin)** | 删除的文件放入回收站，30 天后自动清理，支持恢复/彻底删除 |
| **压缩包预览 (Archive Preview)** | 支持 ZIP/RAR/7Z 预览内部文件，可单独解压 |
| **应用快捷方式 (App Shortcut)** | 首页可以添加自定义快捷方式，直达目标页面 |