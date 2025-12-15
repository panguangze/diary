# HistoryScreen 逐段落修改指令（去“假TopBar卡片”）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/history/HistoryScreen.kt`

目标：
- 标题与筛选进入 TopAppBar
- 页面内容顶部只保留轻量 summary（记录天数）
- 列表项统一 AppCard，状态统一 StateViews

---

## Commit HIS-1 — `ui(history): move title/actions to TopAppBar; replace header card with SectionHeader`
### 段落 1：移除顶部 Card Header
- 定位：`Card(... primaryContainer) { Row { Text("心情日记") ... IconButton(Filter) } }`
- 修改：
  - 删除该 Card 头部实现
  - 标题交给 `AppScaffold(title="心情日记")`
  - Filter action：
    - 放到 AppScaffold actions（TopAppBar 右侧 IconButton）

### 段落 2：新增 summary 区
- 在页面内容顶部添加：
  - `SectionHeader(title="概览", subtitle="共 X 天记录")` 或仅 subtitle
- 保持文案意义不变

### 段落 3：状态统一
- `isLoading` → `LoadingState()`
- `moodRecords.isEmpty()` → `EmptyState(icon=History, title="还没有记录", subtitle="去首页记录今天的心情吧")`

#### 验收
- 顶部干净一致（TopAppBar）
- 空/加载态统一风格

---

## Commit HIS-2 — `ui(history): unify list items with AppCard + typography`
### 段落 4：`MoodHistoryList` item 样式统一
- 定位：`LazyColumn` 内 items 的布局（Row/Card 等）
- 修改：
  - item 外层用 `AppCard`
  - 日期/星期/心情文字使用 typography
  - 备注/描述两行以内，ellipsis，`onSurfaceVariant`
  - 使用 `Modifier.clickable` 保留点击

#### 验收
- 列表规整，信息层级清晰