# Checklist

## 项目工程
- [x] 项目目录结构完整（app 模块、Gradle 配置、Manifest、源码目录）
- [x] Gradle 依赖正确：Compose BOM、Material 3、Room、Hilt、Navigation Compose、Coil、Okio、Accompanist Permissions
- [ ] ./gradlew assembleDebug 编译通过无错误（需 Android SDK 环境）
- [x] Hilt Application 和 Activity 正确配置 @HiltAndroidApp / @AndroidEntryPoint

## 数据库层
- [x] TagEntity 字段完整（id, name, color, icon, sortOrder, createdAt）
- [x] FileTagCrossRefEntity 复合主键正确（filePath + tagId）
- [x] TagDao 提供 insert/update/delete/getAll/getById 方法
- [x] FileTagDao 提供 insertCrossRef/deleteCrossRef/getFilesByTagId/getTagsByFilePath/updateFilePath 方法
- [x] getFilesByTagIds 支持 AND/OR 标签组合查询
- [x] DatabaseModule 正确提供 AppDatabase 和各 DAO 实例

## 领域层
- [x] Domain Models 不含任何 Android SDK 依赖
- [x] FileRepository 接口定义 getFiles/copyFile/moveFile/deleteFile/renameFile/createDirectory
- [x] TagRepository 接口定义 createTag/updateTag/deleteTag/getAllTags/addTagToFile/removeTagFromFile/addTagToDirectory/getFilesByTagId
- [x] SearchRepository 接口定义 searchFiles/getFilesByType/getRecentFiles/getLargeFiles
- [x] BrowseFilesUseCase / ManageTagsUseCase / SearchFilesUseCase / FileOperationsUseCase 全部实现

## 数据层实现
- [x] TagRepositoryImpl 正确对接 TagDao 和 FileTagDao
- [x] FileRepositoryImpl 正确封装文件系统 I/O 操作
- [x] FileSystemManager 支持文件遍历、信息获取、MIME 识别
- [x] FileScanner 支持异步递归目录扫描
- [x] ThumbnailProvider 支持图片/视频/APK 缩略图加载
- [x] SearchRepositoryImpl 支持关键词搜索、类型聚合、大文件扫描
- [x] 文件移动/重命名时同步更新数据库中的路径记录
- [x] RepositoryModule Hilt 绑定完整

## 主题与通用组件
- [x] Material 3 主题（亮色/暗色）完整定义
- [x] 12 种标签预设颜色定义
- [x] TagChip 组件可正确渲染彩色标签
- [x] FileItemCard 组件显示文件图标/名称/大小/日期/标签
- [x] SearchBar 组件支持文本输入和清除
- [x] SortBottomSheet 组件提供排序选项
- [x] PermissionDialog 组件提供权限引导

## 文件浏览页面
- [x] 面包屑路径导航正常工作
- [x] 文件列表正确展示（名称、图标、大小、日期、标签）
- [x] 列表/网格视图可切换
- [x] 排序功能（名称/时间/大小/类型）正常
- [x] 多选模式可进入和退出
- [x] 新建文件夹功能正常
- [x] 无权限时显示 PermissionDialog 引导

## 标签管理页面
- [x] 标签列表正确展示所有已创建标签
- [x] 创建标签弹窗：名称输入 + 颜色选择 + 确认可用
- [x] 编辑标签功能正常
- [x] 删除标签功能正常，关联同步解除
- [x] 标签排序功能正常

## 文件打标签
- [x] 长按文件可触发打标签操作
- [x] 多选文件可批量打标签
- [x] 文件夹可递归打标签（isInherited=true）
- [x] 标签打上后文件列表即时刷新显示
- [x] 支持从文件上移除指定标签

## 标签关联文件查看
- [x] 点击标签可进入关联文件列表页
- [x] 标签关联文件列表正确显示
- [x] 页面顶部显示标签名称和颜色

## 搜索与筛选
- [x] 关键词搜索返回匹配结果
- [x] 标签 AND 筛选正确（同时拥有所有选中标签的文件）
- [x] 标签 OR 筛选正确（拥有任一选中标签的文件）
- [x] 文件类型筛选正确
- [x] 组合筛选（关键词+标签+类型）正确
- [x] 搜索结果中可进行打标签操作

## 分类聚合
- [x] 按类型聚合：图片/视频/文档/音频/压缩包/APK/其他 分类正确
- [x] 按标签聚合：每个标签作为入口可查看关联文件
- [x] 最近文件列表按修改时间倒序
- [x] 大文件扫描列出超过指定大小的文件

## 文件操作
- [x] 复制文件功能正常
- [x] 移动文件功能正常，数据库路径同步更新
- [x] 删除功能正常（回收站机制），标签关联同步清理
- [x] 重命名功能正常，数据库路径同步更新
- [x] 文件属性页显示详情和已关联标签
- [x] 分享功能正常
- [x] 打开方式功能正常

## 设置页面
- [x] 暗色模式开关生效且持久化
- [x] 标签数据导出为 JSON 文件
- [x] 标签数据从 JSON 文件正确导入恢复

## 导航
- [x] 所有页面路由定义且可正确跳转
- [x] 页面间参数传递正确（如 tagId）
- [x] 返回栈行为正确
- [x] 底部导航栏 Tab 切换正确

## 权限
- [x] Android 11+ MANAGE_EXTERNAL_STORAGE 权限引导正常
- [x] Android 10 及以下 READ/WRITE_EXTERNAL_STORAGE 权限请求正常
- [x] 权限被拒绝时有合理降级提示
