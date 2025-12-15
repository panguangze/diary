# MainActivity / 导航骨架：逐段落修改指令

文件：
- `app/src/main/java/com/love/diary/MainActivity.kt`

目标：
- 保持底部栏不动
- 让每个 Tab 页统一使用 `AppScaffold`（TopAppBar 标题一致）
- 页面内部不再重复标题

## Commit NAV-1 — `refactor(nav): wrap tab destinations with AppScaffold`
### 段落 1：定位三个 Tab destination
- 定位：`NavHost { composable(Screen.Home.route) { ... } ... }`

### 段落 2：为每个 Tab 包裹 AppScaffold
- 修改（伪代码）：
  - `composable(Screen.Home.route) { AppScaffold(title="恋爱日记") { HomeScreen(...) } }`
  - `composable(Screen.Habits.route) { AppScaffold(title="打卡") { HabitListScreen(...) } }`
  - `composable(Screen.Settings.route) { AppScaffold(title="设置") { SettingsScreen(...) } }`
- 如果 Screen 已经接收 `modifier`：保持 `modifier = Modifier.fillMaxSize()` 传入

### 段落 3：actions 的放置策略（可选）
- 如果某页面需要筛选/新增：
  - 优先放 TopAppBar actions（icon button）
  - 不要放在页面 header Row 里

## 验收
- 切换 tab：TopAppBar 标题正确
- 页面内部原有大标题若仍存在，后续在各页面文件中移除
- 业务逻辑不变，编译通过