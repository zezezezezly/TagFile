# 图片查看器滑动性能优化 Spec

## Why
ImageViewerScreen 在浏览大量图片（1000+）时，从上一张滑到下一张的临界点有明显卡顿。根因是使用 Coil 默认 ImageLoader 单例（无定制缓存策略），且 HorizontalPager 无预加载机制，导致滑动时主线程需等待图片解码。

## What Changes
- 新增 Hilt DI 模块提供定制的 Coil `ImageLoader` 单例（增大磁盘/内存缓存、启用硬件位图）
- `ImageViewerScreen` HorizontalPager 配置预加载 + 滑动时暂停图片请求
- `ImageViewerScreen` 使用 `ImageLoader.preload()` 预解码前后多张图片
- `ImageViewerScreen` AsyncImage 指定共享的优化 ImageLoader 实例
- `BookViewerScreen` 同样接入优化 ImageLoader

## Impact
- Affected specs: bookshelf
- Affected code:
  - `di/ImageLoaderModule.kt`（新增）
  - `ui/imageviewer/ImageViewerScreen.kt`（修改）
  - `ui/bookviewer/BookViewerScreen.kt`（修改）
  - 其他使用 AsyncImage 的文件（ShelfScreen / BookDetailScreen / FileItemCard 等）按需修改

## ADDED Requirements

### Requirement: Coil ImageLoader 全局优化配置
系统 SHALL 通过 Hilt 提供一个 `@Singleton` 的定制 `ImageLoader` 实例，替代 Coil 默认单例。

#### Scenario: 缓存配置
- **WHEN** 应用启动
- **THEN** ImageLoader 使用 `diskCache(DiskCache.Builder().directory(cacheDir).maxSizeBytes(512 * 1024 * 1024).build())` 配置磁盘缓存
- **AND** `memoryCachePolicy(CachePolicy.ENABLED)` 启用内存缓存
- **AND** Coil 2.x 默认内存缓存约为可用内存的 15%

#### Scenario: 解码优化
- **WHEN** 加载本地文件图片
- **THEN** ImageLoader 配置 `allowHardware(false)` 以确保兼容 BitmapFactory 和手势操作（硬件位图不支持 Canvas 操作，可能导致手势缩放等场景 crash）

### Requirement: HorizontalPager 预加载
系统 SHALL 在 ImageViewerScreen 的 HorizontalPager 中配置预加载页面数量，并在当前页变化时预解码前后图片。

#### Scenario: beyondBoundsPageCount
- **WHEN** 用户浏览图片
- **THEN** HorizontalPager 设置 `beyondBoundsPageCount = 2`，Compose 框架预组合前后 2 页的 Composable

#### Scenario: 主动预加载
- **WHEN** 当前页变化
- **THEN** `ImageLoader.enqueue(ImageRequest)` 预加载前后 3 页的图片到内存缓存，滑动时图片已在缓存中

#### Scenario: 快速滑动暂停
- **WHEN** pagerState.isScrollInProgress 为 true
- **THEN** 暂停新的预加载请求，等待滑动停止后再恢复，避免主线程竞争

## MODIFIED Requirements

### Requirement: ImageViewerScreen（修改）
系统 SHALL 在 AsyncImage 中传入全局优化的 `imageLoader` 参数。

### Requirement: BookViewerScreen（修改）
系统 SHALL 同样配置 beyondBoundsPageCount 和预加载策略。
