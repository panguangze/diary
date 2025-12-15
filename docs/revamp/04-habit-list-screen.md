# HabitListScreen 逐段落修改指令（消灭“原型感”）

文件：
- `app/src/main/java/com/love/diary/habit/HabitListScreen.kt`

目标：
- 页面标题只在 TopAppBar
- FAB/新增入口合理
- 列表项卡片统一极简

---

## Commit HABIT-1 — `ui(habits): remove manual header, rely on TopAppBar`
### 段落 1：删除 header Row（标题+FAB）
- 定位：`Row(... Text("打卡事项", fontSize = 24.sp) ... FloatingActionButton ...)`
- 修改：
  - 删除整段 Row header
  - 标题由 AppScaffold 提供（`MainActivity.kt` 已包裹）

### 段落 2：新增入口的归位
二选一：
- A：页面右下角 FAB（推荐）
- B：TopAppBar actions（IconButton Add，更极简）
修改：
- 保留 `showAddHabitDialog` 与 onClick 不变，只调整位置与布局结构

#### 验收
- 页面不再出现重复标题
- 新增按钮不挤占头部

---

## Commit HABIT-2 — `ui(habits): unify list spacing + HabitItemCard to AppCard`
### 段落 3：LazyColumn 间距改为 spacedBy
- 定位：`LazyColumn { items(...) { ... Spacer(8.dp) } }`
- 修改：
  - `LazyColumn(verticalArrangement = Arrangement.spacedBy(...), contentPadding = PaddingValues(...))`
  - 删除每项末尾 Spacer

### 段落 4：`HabitItemCard` 统一卡片与排版
- 定位：`@Composable fun HabitItemCard(...)`
- 修改：
  - 外层容器改 `AppCard`
  - 内部 padding 用 tokens
  - title/subtitle 用 typography
  - 次级文字用 `onSurfaceVariant`
  - 交互按钮样式统一 Material3（避免自定义颜色过多）

#### 验收
- 列表看起来规整，信息层级一致