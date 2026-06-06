# TagFile 标签化文件管理器 Spec

## Why
传统安卓文件管理器只支持按目录树浏览文件，缺乏灵活的标签化管理能力。用户需要一个能够给文件和文件夹打标签、通过标签快速分类和检索文件的文件管理工具。

## What Changes
- 从零构建一个完整的安卓文件管理应用
- 实现标签-文件多对多映射的核心数据模型
- 提供文件浏览、标签管理、搜索筛选、分类聚合、文件操作等完整功能
- 采用 MVVM + Clean Architecture 三层架构
- 技术栈: Kotlin + Jetpack Compose + Room + Hilt + Coroutines/Flow

## Impact
- Affected specs: 无（全新项目）
- Affected code: 全新项目，所有代码从零编写

## ADDED Requirements

### Requirement: 项目工程搭建
系统 SHALL 使用 Kotlin + Jetpack Compose + Material 3 构建安卓项目，集成 Hilt 依赖注入、Room 数据库、Navigation Compose 导航、Coil 图片加载和 Okio 文件 I/O。

#### Scenario: 项目创建成功
- **WHEN** 项目通过 Gradle 构建
- **THEN** 编译通过且所有依赖正确解析

### Requirement: 数据库与数据模型
系统 SHALL 使用 Room 数据库存储标签实体（TagEntity）和文件-标签关联实体（FileTagCrossRefEntity），支持多对多关系查询。

#### Scenario: 标签 CRUD
- **WHEN** 用户创建/编辑/删除标签
- **THEN** 数据库同步更新，UI 实时反映变化

#### Scenario: 文件-标签关联
- **WHEN** 用户给文件打标签或移除标签
- **THEN** 关联表正确更新，支持通过标签反向查询所有关联文件路径

### Requirement: 领域层（Domain Layer）
系统 SHALL 提供纯 Kotlin 的领域层，包含 Tag、FileItem、SearchFilter 等数据模型，以及 FileRepository、TagRepository、SearchRepository 接口和对应 UseCase（BrowseFilesUseCase、ManageTagsUseCase、SearchFilesUseCase、FileOperationsUseCase）。

#### Scenario: 领域层无 Android 依赖
- **WHEN** 导入 domain 模块或进行单元测试
- **THEN** 不需要 Android SDK 即可编译和运行

### Requirement: 数据层（Data Layer）
系统 SHALL 实现领域层接口，包括 Room DAO 操作（TagDao、FileTagDao）、文件系统操作封装（FileSystemManager）、文件扫描（FileScanner）和缩略图提供（ThumbnailProvider）。

#### Scenario: 文件浏览
- **WHEN** 导航到指定目录路径
- **THEN** 返回该目录下所有文件和子目录列表，包含名称、大小、修改时间、MIME 类型信息

#### Scenario: 文件操作
- **WHEN** 执行复制/移动/删除/重命名操作
- **THEN** 文件系统正确响应，关联标签路径同步更新

### Requirement: 主题与通用 UI 组件
系统 SHALL 使用 Material 3 主题系统，支持亮色/暗色模式切换，并构建可复用的通用组件：FileItemCard（文件/文件夹列表项）、TagChip（彩色标签芯片）、SearchBar（搜索栏）、SortBottomSheet（排序弹窗）。

#### Scenario: 主题切换
- **WHEN** 用户切换暗色模式
- **THEN** 全局 UI 即时切换为暗色主题

### Requirement: 文件浏览页面
系统 SHALL 提供文件浏览页面（FileListScreen），支持目录导航、面包屑路径、列表/网格视图切换、排序（名称/时间/大小/类型）、文件信息展示（含已关联标签 Chip）和缩略图预览。

#### Scenario: 目录导航
- **WHEN** 用户点击文件夹
- **THEN** 进入子目录，顶部面包屑显示当前路径

#### Scenario: 排序切换
- **WHEN** 用户选择排序方式
- **THEN** 文件列表按指定规则重新排列

### Requirement: 标签管理页面
系统 SHALL 提供标签管理页面（TagManagerScreen），支持创建标签（名称+颜色选择）、编辑标签、删除标签（级联解除关联）、标签排序。

#### Scenario: 创建标签
- **WHEN** 用户输入标签名称并选择颜色后确认
- **THEN** 新标签出现在标签列表中

#### Scenario: 删除标签
- **WHEN** 用户确认删除标签
- **THEN** 标签被移除，所有文件上的该标签关联同步解除

### Requirement: 文件打标签功能
系统 SHALL 支持用户长按或多选文件后批量打标签，也支持对文件夹递归打标签。

#### Scenario: 多选文件打标签
- **WHEN** 用户多选若干文件并选择标签
- **THEN** 选中文件全部关联到该标签，文件列表刷新显示新标签 Chip

#### Scenario: 文件夹递归打标签
- **WHEN** 用户给文件夹打标签
- **THEN** 该文件夹内所有文件（含子目录）全部关联此标签，标记 isInherited=true

### Requirement: 标签关联文件查看
系统 SHALL 提供标签关联文件查看页面（TaggedFilesScreen），点击标签后展示该标签下所有文件。

#### Scenario: 查看标签文件
- **WHEN** 用户点击某个标签
- **THEN** 展示该标签关联的所有文件列表

### Requirement: 搜索与筛选
系统 SHALL 提供全局搜索功能，支持按文件名关键词、标签（AND/OR 模式）、文件类型、大小范围和日期范围组合筛选。

#### Scenario: 关键词搜索
- **WHEN** 用户输入文件名关键词
- **THEN** 实时显示匹配的文件列表

#### Scenario: 多标签 AND 筛选
- **WHEN** 用户选择多个标签并启用 AND 模式
- **THEN** 仅显示同时拥有所有选中标签的文件

### Requirement: 分类聚合页面
系统 SHALL 提供分类聚合页面（CategoryScreen），按文件类型（图片/视频/文档/音频/压缩包/APK/其他）聚合展示，支持按标签虚拟文件夹查看、最近文件列表和大文件扫描。

#### Scenario: 按类型浏览
- **WHEN** 用户选择"图片"分类
- **THEN** 展示所有图片文件

### Requirement: 文件操作功能
系统 SHALL 支持文件复制、移动、删除（含回收站机制）、重命名（同步更新标签关联）、属性查看、分享和打开方式。

#### Scenario: 重命名同步标签
- **WHEN** 用户重命名或移动文件
- **THEN** 数据库中该文件的路径记录同步更新

### Requirement: 设置页面
系统 SHALL 提供设置页面，支持暗色模式切换、标签数据导出/导入（JSON 格式）和文件变化监听开关。

#### Scenario: 导出标签数据
- **WHEN** 用户触发导出
- **THEN** 生成包含所有标签及映射关系的 JSON 文件

#### Scenario: 导入标签数据
- **WHEN** 用户选择 JSON 文件导入
- **THEN** 标签和映射关系恢复到数据库

### Requirement: 导航架构
系统 SHALL 使用 Navigation Compose 实现单 Activity 架构，包含以下路由：文件浏览、标签管理、标签关联文件、搜索、分类聚合、设置。

#### Scenario: 页面间导航
- **WHEN** 用户从文件浏览页导航到标签管理页
- **THEN** 正确入栈，返回键可回到上一页

### Requirement: 权限管理
系统 SHALL 在 Android 11+ 上引导用户开启"所有文件访问权限"（MANAGE_EXTERNAL_STORAGE），在低版本上请求 READ/WRITE_EXTERNAL_STORAGE。

#### Scenario: 首次启动权限请求
- **WHEN** 应用首次启动
- **THEN** 显示权限说明并引导用户授权
