# FirstRunScreen（首次运行）逐段落修改指令（清新极简）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/setup/FirstRunScreen.kt`

目标：
- 去装饰、降阴影、统一排版与间距
- 日期选择改为“只读输入框 + trailing icon”极简模式
- 小屏可滚动、键盘不挡主要按钮（基础版即可）

---

## Commit F1 — `ui(first-run): minimal header + form container via AppCard`
### 段落 1：页面从“垂直居中”改为“上对齐 + 可滚动”
- 定位：`Column(... verticalArrangement = Arrangement.Center ...)`
- 修改：
  - `verticalArrangement` 改为 `Arrangement.Top`
  - `modifier` 增加 `verticalScroll(rememberScrollState())`
  - 页面 padding 统一 `16.dp`（或 tokens），顶部额外留白 `24.dp`

### 段落 2：标题去 emoji + 增加副标题
- 定位：`Text(text = "欢迎使用恋爱日记 💕")`
- 修改：
  - 主标题文案：`欢迎使用恋爱日记`
  - style：`MaterialTheme.typography.headlineMedium`（或 titleLarge）
  - 颜色：`onBackground` 或默认
- 新增副标题一行：
  - 文案：`先完成基础信息，之后就可以开始记录与打卡。`
  - style：`bodyMedium`
  - 颜色：`onSurfaceVariant`
  - 对齐：可居中

### 段落 3：表单容器改为极简 AppCard（去掉厚重 elevation）
- 定位：`Card(... elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) ...)`
- 修改：
  - 替换为 `AppCard { Column(...) { ... } }`
  - 或：将 elevation 改 0，并添加 1dp border（outlineVariant）
- 表单内部：
  - `Column(verticalArrangement = Arrangement.spacedBy(12.dp))`（或 tokens）
  - 统一 TextField shape 与 padding

### 段落 4：新增底部全宽主按钮“开始使用/完成设置”
- 位置：表单卡片下方（同一 Column 内）
- `enabled`：至少 `yourName/partnerName` 非空（按你业务定），其余可选
- onClick：复用现存保存逻辑 + `onSetupComplete()`

#### 验收
- 小屏可滚动看到按钮
- 无厚重阴影，整体留白统一
- 保存与跳转不回归

---

## Commit F2 — `ui(first-run): date field as read-only textfield with trailing icon`
### 段落 5：移除日期旁边的“选择日期”按钮
- 定位：日期 Row：`OutlinedTextField(...) + Button("选择日期")`
- 修改：
  - 删除右侧 Button
  - TextField 改为：
    - `readOnly = true`
    - `trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, ...) } }`
  - 推荐：给 TextField 外层 `Modifier.clickable { showDatePicker = true }`（整块可点）

#### 验收
- 日期输入行不拥挤
- 点击任意区域或 icon 能打开 DatePicker
- DatePickerDialog 逻辑保持不变

---

## Commit F3（可选） — `ui(first-run): IME actions + basic validation`
- 为输入框加 `ImeAction.Next/Done` 与 FocusRequester
- 点击提交但必填为空时显示轻量错误提示（不要常驻红字）

#### 验收
- 键盘体验顺滑
- 必填引导明确