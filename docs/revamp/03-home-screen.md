# HomeScreen 逐段落修改指令（门面页优先）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/home/HomeScreen.kt`

目标：
- 第一屏“干净、层级清晰、主操作明确”
- 卡片、分区标题、输入区域统一极简风

---

## Commit HOME-1 — `ui(home): replace ad-hoc cards with AppCard + SectionHeader`
### 段落 1：删除/降级页面内部大标题
- 定位：首页内部是否有 `Text(... headlineLarge/24.sp/Bold ...)` 这类“页面标题”
- 修改：页面标题由 TopAppBar 提供；内部若需要说明用 `SectionHeader(subtitle=...)`

### 段落 2：分区标题统一
- 定位：关键字 `Text("最近心情")`、`Text("...")` 分组标题
- 修改：替换为 `SectionHeader(title=..., subtitle=可选)`

### 段落 3：卡片统一 AppCard
- 定位：`Card(`，尤其是 `RecentMoodsList` 里的 `Card`
- 修改：替换为 `AppCard(modifier=fillMaxWidth()) { ... }`，移除 elevation/border 硬编码

### 段落 4：输入区（`MoodNoteInput`）
- 定位：`@Composable private fun MoodNoteInput(...)`
- 修改：
  - 外层背景若用自定义颜色（如 `BackgroundSubtle`），优先改为 `surfaceVariant` 或用 `AppCard`
  - `OutlinedTextField`：
    - shape 用 tokens
    - placeholder 与 supporting/error 文案用 typography + onSurfaceVariant/error
  - 按钮主次：
    - 主动作：Filled/FilledTonal（按你全局）
    - 次动作：TextButton

#### 验收
- 卡片一致、留白一致
- 首页信息层级清晰，主操作显眼但不刺眼

---

## Commit HOME-2 — `ui(home): normalize typography, secondary colors, spacing tokens`
### 段落 5：消灭硬编码字号/粗体
- 定位：`fontSize =`、`FontWeight.Bold` 大量出现处
- 修改：使用 `MaterialTheme.typography` 对应层级替换

### 段落 6：统一次级文字颜色
- 修改：说明文字统一 `onSurfaceVariant`

### 段落 7：统一间距
- 定位：`.padding(16.dp)`、`Spacer(8.dp)` 等
- 修改：替换为 tokens；列表/区块间距用 `Arrangement.spacedBy(...)`

#### 验收
- 视觉节奏统一（不挤不空）