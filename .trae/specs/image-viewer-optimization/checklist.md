# Checklist

## ImageLoader 优化配置
- [x] `ImageLoaderModule.kt` 通过 Hilt 提供 `@Singleton ImageLoader`
- [x] 磁盘缓存配置为 512MB，目录为 `cacheDir/image_cache`
- [x] `allowHardware(false)` 避免手势操作 crash
- [x] `crossfade(true)` 平滑过渡动画
- [x] `memoryCachePolicy(CachePolicy.ENABLED)` 启用内存缓存

## ImageViewerScreen 优化
- [x] HorizontalPager 设置 `beyondBoundsPageCount = 2`
- [x] 当前页变化时预加载前后 3 页图片到 Coil 内存缓存
- [x] 快速滑动时暂停预加载请求
- [x] AsyncImage 显式传入自定义 `imageLoader` 参数
- [x] 当前页图片使用 `Priority.HIGH`

## BookViewerScreen 优化
- [x] HorizontalPager 设置 `beyondBoundsPageCount = 2`
- [x] 当前页变化时预加载前后 3 页图片
- [x] AsyncImage 显式传入自定义 `imageLoader` 参数
- [x] 快速滑动时暂停预加载请求

## 行为验证
- [x] 1000+ 图片滑动卡顿明显改善
- [x] 前后翻页图片加载无明显白屏或占位闪烁
- [x] 手势缩放 / 双击放大功能正常（无 crash）
- [x] 增强功能（AutoFix）仍正常显示
- [x] 书架书籍浏览同样流畅
