# 书架功能 Spec

## Why
当前 TagFile 仅支持通用文件浏览和管理，缺乏面向图片类内容的书籍化组织功能。用户需要一个"书架"模块，能够将指定文件夹下仅包含图片的子文件夹自动识别为"书籍"，并以大图封面方式展示，支持搜索、每日推荐和阅读统计。

## What Changes
- 新增 BookEntity Room 数据库实体和 BookDao，存储书籍信息
- 新增 PreferencesManager 中书架文件夹路径配置
- 新增 ShelfScreen（书架主页）：搜索栏 + 每日推荐 + 自动检索 + 书籍大图网格
- 新增 ShelfViewModel / ShelfUiState / ShelfEvent 遵循现有 MVVM 模式
- 新增 BookViewerScreen（书籍内图片浏览）
- 新增 BookViewerViewModel
- 修改 AppDatabase 增加 BookEntity 和 BookDao
- 修改 DatabaseModule 提供 BookDao
- 修改 NavGraph 增加书架和书籍浏览路由
- 修改 HomeScreen 增加书架入口
- 修改 SettingsScreen 增加书架文件夹路径设置

## Impact
- Affected specs: tagfile-app
- Affected code:
  - `data/local/entity/BookEntity.kt`（新增）
  - `data/local/dao/BookDao.kt`（新增）
  - `data/local/AppDatabase.kt`（修改）
  - `di/DatabaseModule.kt`（修改）
  - `data/preferences/PreferencesManager.kt`（修改）
  - `domain/model/Book.kt`（新增）
  - `domain/repository/ShelfRepository.kt`（新增）
  - `data/repository/ShelfRepositoryImpl.kt`（新增）
  - `ui/shelf/ShelfScreen.kt`（新增）
  - `ui/shelf/ShelfViewModel.kt`（新增）
  - `ui/shelf/ShelfUiState.kt`（新增）
  - `ui/bookviewer/BookViewerScreen.kt`（新增）
  - `ui/bookviewer/BookViewerViewModel.kt`（新增）
  - `ui/bookviewer/BookViewerUiState.kt`（新增）
  - `navigation/NavGraph.kt`（修改）
  - `ui/home/HomeScreen.kt`（修改）
  - `ui/settings/SettingsScreen.kt`（修改）
  - `ui/settings/SettingsViewModel.kt`（修改）
  - `ui/settings/SettingsUiState.kt`（修改）
  - `di/RepositoryModule.kt`（修改）

## ADDED Requirements

### Requirement: 书架数据库
系统 SHALL 提供 BookEntity（书籍表）和 BookDao，存储书籍的核心信息。

#### BookEntity 字段
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 书籍唯一 ID |
| title | String | 书名（文件夹名称） |
| author | String? | 作者（从文件夹名 []或【】中提取，无则为 null） |
| tags | String | 标签（文件夹下所有标签名以 / 连接） |
| coverPath | String | 封面路径（默认第一张图片） |
| folderPath | String | 书籍文件夹路径 |
| pageCount | Int | 图片数量 |
| viewCount | Int | 浏览次数，默认 0 |
| totalDuration | Long | 累计阅读时长（毫秒），默认 0 |
| lastReadTime | Long | 最后阅读时间戳 |
| createdAt | Long | 创建时间戳 |

#### Scenario: 自动检索创建书籍
- **WHEN** 用户点击"自动检索新书籍"
- **THEN** 系统扫描书架文件夹路径下所有子文件夹，对每个只包含图片的子文件夹创建一条书籍记录

#### Scenario: 作者提取
- **WHEN** 文件夹名为 `[山田太郎] 我的漫画` 或 `【张三】摄影集`
- **THEN** author 字段提取为 `山田太郎` 或 `张三`
- **WHEN** 文件夹名无 [] 或 【】
- **THEN** author 字段为 null

### Requirement: 书架主页 UI
系统 SHALL 提供书架主页，顶部搜索栏，下方每日推荐区域，再下方自动检索按钮，最后为书籍大图网格。

#### Scenario: 搜索栏
- **WHEN** 用户进入书架页面
- **THEN** 顶部显示搜索栏，支持按作者、标签、名称三种模式搜索，支持模糊搜索

#### Scenario: 每日推荐
- **WHEN** 用户进入书架页面
- **THEN** 从书籍数据库中随机选择 3 部（不足 3 部则全选），以横向滚动卡片展示

#### Scenario: 自动检索按钮
- **WHEN** 用户点击"自动检索新书籍"
- **THEN** 系统扫描书架文件夹，为符合规则的子文件夹创建书籍记录

#### Scenario: 书籍大图网格
- **WHEN** 书籍列表中有数据
- **THEN** 每行展示 3 本书，使用大图封面（fillMaxWidth 自适应列宽），布局均匀，显示封面图、书名、作者（如有）

### Requirement: 书籍阅读与统计
系统 SHALL 提供书籍内图片浏览功能，并追踪浏览统计。

#### Scenario: 打开书籍
- **WHEN** 用户点击书籍封面
- **THEN** 进入 BookViewerScreen，显示书籍内所有图片，viewCount +1，更新 lastReadTime

#### Scenario: 阅读时长统计
- **WHEN** 用户在 BookViewerScreen 中浏览
- **THEN** 系统追踪停留时长，返回时更新 totalDuration

### Requirement: 书架文件夹路径配置
系统 SHALL 在设置页面提供书架文件夹路径配置入口。

#### Scenario: 配置书架路径
- **WHEN** 用户在设置中选择书架文件夹
- **THEN** 路径保存到 PreferencesManager，书架模块基于该路径检索书籍

## MODIFIED Requirements

### Requirement: 导航路由（修改）
系统 SHALL 新增书架（`shelf`）和书籍浏览（`book_viewer/{bookId}`）两条路由。

### Requirement: 主页入口（修改）
系统 SHALL 在 HomeScreen 中新增书架卡片入口。

### Requirement: 设置页面（修改）
系统 SHALL 在 SettingsScreen 中新增书架文件夹路径设置项。
