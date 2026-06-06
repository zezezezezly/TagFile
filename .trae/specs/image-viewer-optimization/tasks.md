# Tasks

## Task 1: 创建 Hilt ImageLoaderModule 提供优化的 Coil ImageLoader ✅
通过 Hilt DI 提供全局共享的定制 ImageLoader 实例。

### Steps
- [x] 创建 `di/ImageLoaderModule.kt`
- [x] `@Provides @Singleton fun provideImageLoader()` 配置 512MB 磁盘缓存
- [x] `allowHardware(false)` 避免手势操作 crash
- [x] `crossfade(true)` 平滑过渡
- [x] `memoryCachePolicy(CachePolicy.ENABLED)` 启用内存缓存

## Task 2: 优化 ImageViewerScreen ✅

### Steps
- [x] `HorizontalPager` 添加 `beyondBoundsPageCount = 2`
- [x] 添加 `LaunchedEffect(pagerState.currentPage)` 预加载前后 3 页图片
- [x] 快速滑动时 `delay(150)` + `isScrollInProgress` 暂停预加载
- [x] `AsyncImage` 显式传入 `imageLoader` 参数
- [x] 当前页使用 `Priority.HIGH`，其他页 `Priority.NORMAL`

## Task 3: 优化 BookViewerScreen ✅

### Steps
- [x] `HorizontalPager` 添加 `beyondBoundsPageCount = 2`
- [x] 添加 `LaunchedEffect(pagerState.currentPage)` 预加载前后 3 页图片
- [x] `AsyncImage` 显式传入 `imageLoader` 参数
- [x] 快速滑动暂停逻辑

# Task Dependencies
- Task 2 depends on Task 1
- Task 3 depends on Task 1（可与 Task 2 并行）
