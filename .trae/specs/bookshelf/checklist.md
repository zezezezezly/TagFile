# Checklist

## 数据库层
- [x] `BookEntity` 包含所有 11 个字段（id, title, author, tags, coverPath, folderPath, pageCount, viewCount, totalDuration, lastReadTime, createdAt）
- [x] `BookDao` 提供插入、更新、删除、全量查询、按 ID 查询、按路径查询方法
- [x] `BookDao` 提供 searchByTitle / searchByAuthor / searchByTags 三种模糊搜索
- [x] `BookDao` 提供 incrementViewCount / addDuration / updateLastReadTime 统计方法
- [x] `AppDatabase` entities 包含 BookEntity，version 升级至 4
- [x] `DatabaseModule` 提供 `bookDao()` 实例

## 领域层
- [x] `Book` 领域模型为纯 Kotlin data class，不含 Android 依赖
- [x] `ShelfRepository` 接口定义完整（getAllBooks, scanAndAddBooks, getBookById, 统计方法, 搜索方法, 随机推荐, 获取书籍图片）

## 数据层
- [x] `EntityMapper` 包含 BookEntity ↔ Book 双向映射
- [x] `ShelfRepositoryImpl` 实现所有接口方法
- [x] `scanAndAddBooks()` 正确识别仅含图片的子文件夹
- [x] `scanAndAddBooks()` 正确提取 [] 和 【】 中的作者名
- [x] `RepositoryModule` 绑定 ShelfRepository → ShelfRepositoryImpl

## 书架主页 UI
- [x] 顶部搜索栏支持输入和清除
- [x] 搜索模式切换（FilterChip：作者/标签/名称）
- [x] 每日推荐区展示随机 3 本书籍封面（LazyRow）
- [x] 自动检索新书籍按钮可用，扫描后更新列表
- [x] 书籍网格每行 3 本，封面大图 + 书名 + 作者布局均匀

## 书籍浏览页
- [x] 点击书籍封面进入 BookViewerScreen
- [x] HorizontalPager 支持左右翻页浏览所有图片
- [x] 进入时 viewCount +1，lastReadTime 更新
- [x] 退出时累计阅读时长写入数据库
- [x] TopAppBar 显示书名和返回按钮

## 设置
- [x] 设置页有书架文件夹路径设置项
- [x] 可选择文件夹并保存到 PreferencesManager
- [x] 路径变化后书架模块可用

## 导航
- [x] `shelf` 路由可正常跳转
- [x] `book_viewer/{bookId}` 路由接收参数正确
- [x] 主页"书架"入口可点击跳转

## 行为验证
- [x] 首次进入书架页面，无书籍时显示空状态
- [x] 配置书架路径后点击自动检索，正确发现所有书籍
- [x] 作者提取正确（方括号/圆括号/无括号）
- [x] 搜索（作者/标签/名称）结果正确
- [x] 阅读后浏览次数和时长正确累计
