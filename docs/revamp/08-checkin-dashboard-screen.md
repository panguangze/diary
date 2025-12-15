# CheckInDashboardScreen 逐段落修改指令（若用户可见：快速去原型感）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/CheckInDashboardScreen.kt`

目标：
- 页面标题归 TopAppBar
- 列表卡片去 elevation，统一 AppCard
- 排版更克制，减少“厚重卡片堆叠”

---

## Commit CHECKIN-1 — `ui(checkin): adopt AppScaffold and remove local big title`
### 段落 1：删除页面内部大标题
- 定位：`Text("打卡应用", style = headlineMedium)`
- 修改：
  - 标题交给 TopAppBar（AppScaffold title=`打卡`/`打卡应用`）
  - 删除该 Text 或降级为 SectionHeader subtitle

#### 验收
- 标题只出现一次，布局更清爽

---

## Commit CHECKIN-2 — `ui(checkin): replace elevation cards with AppCard + spacedBy`
### 段落 2：列表 items 的 Card
- 定位：`Card(... elevation = CardDefaults.cardElevation(defaultElevation = 4.dp))`
- 修改：
  - 替换为 `AppCard`
  - `LazyColumn` 增加 `verticalArrangement = spacedBy(12/16dp)`
  - 删除每项手动 padding/Spacer（能统一则统一）

#### 验收
- 页面“变新”：边框扁平 + 留白一致