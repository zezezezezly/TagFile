# 文件浏览页面滚动位置恢复 Spec

## Why
当前 FileListScreen 在进入子文件夹后按返回键回到上级目录时，LazyColumn / LazyVerticalGrid 的滚动位置会重置到顶部。用户需要重新手动滚动到之前浏览的位置，不符合浏览习惯。应当在返回上级目录时自动恢复之前的滚动位置。

## What Changes
- FileListViewModel 中维护一个以目录路径为 key 的滚动位置缓存（firstVisibleItemIndex + firstVisibleItemScrollOffset）
- 进入子目录时保存当前目录的滚动位置；返回上级目录时恢复之前缓存的滚动位置
- FileListScreen 中使用 `rememberLazyListState()` 显式持有列表滚动状态，并通过 LaunchedEffect 在目录加载完成后恢复滚动位置
- 同时处理网格视图（LazyVerticalGrid）的滚动位置保存与恢复

## Impact
- Affected specs: tagfile-app（文件浏览页面相关功能增强）
- Affected code:
  - `app/src/main/java/com/tagfile/app/ui/filelist/FileListScreen.kt`
  - `app/src/main/java/com/tagfile/app/ui/filelist/FileListViewModel.kt`
  - `app/src/main/java/com/tagfile/app/ui/filelist/FileListUiState.kt`

## ADDED Requirements

### Requirement: 目录滚动位置缓存
系统 SHALL 在 FileListViewModel 中维护每个访问过的目录路径的滚动位置信息（首可见项索引和首可见项偏移量）。

#### Scenario: 进入子目录前保存滚动位置
- **WHEN** 用户在当前目录下滚动到某个位置后点击进入子目录
- **THEN** 当前目录的滚动位置（index + offset）被保存到 ViewModel 的缓存中

#### Scenario: 返回上级目录恢复滚动位置
- **WHEN** 用户从子目录按返回键回到上级目录
- **THEN** 文件列表自动滚动到之前缓存的滚动位置

### Requirement: 列表状态显式管理
系统 SHALL 在 FileListScreen 中使用 `rememberLazyListState()` 显式持有 LazyColumn 和 LazyVerticalGrid 的滚动状态，并通过 LaunchedEffect 在文件加载完成后将状态滚动到目标位置。

#### Scenario: 首次进入目录
- **WHEN** 用户首次进入某个目录
- **THEN** 列表从顶部开始显示（index=0, offset=0）

#### Scenario: 重新进入已访问目录
- **WHEN** 用户通过返回操作重新进入之前访问过的目录
- **THEN** 列表恢复到上次离开时的滚动位置

## MODIFIED Requirements

无。
