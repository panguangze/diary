# Love Diary（panguangze/diary）UI 视觉升级执行文档（风格 A：清爽克制）

> 适用范围：Jetpack Compose + Material3  
> 目标：统一字体/颜色/间距/组件风格，让 UI 更“克制、耐看、协调”，并重点提升打卡页、首页、统计页、设置页的质感。  
> 代码结构依据：  
> - `app/src/main/java/com/love/diary/ui/theme/*`  
> - `app/src/main/java/com/love/diary/presentation/components/DesignSystem.kt`  
> - `app/src/main/java/com/love/diary/presentation/screens/*`

---

## 0. 核心结论（必须先做的 3 件事）

1. **Theme.kt 修复 Typography 未生效**  
   当前 `Theme.kt` 里使用了 `typography = MaterialTheme.typography`，导致 `Type.kt` 定义不会生效。  
   ✅ 必须改为 `typography = Typography`（你自定义的那套）。

2. **Type.kt 补齐完整 Typography tokens**  
   当前只改了 `bodyLarge=16sp`，不够支撑全局层级。  
   ✅ 必须把 title / body / label / display 等都系统化。

3. **DesignSystem.AppCard 去掉默认描边**  
   当前 `AppCard` 默认 `border=outlineVariant 1dp`，会让全 App “线框感过重”，不高级。  
   ✅ 默认无描边，仅在特定场景按需加描边。

> 推荐执行顺序：Theme/Type → DesignSystem → CheckInDashboard → Home → Statistics → Settings → FirstRun/History（收尾）

---

## 1) 全局设计规范（所有 agent 共用）

### 1.1 字体规范（Material3 Typography 映射）

目标：中文清晰、层级稳定、克制高级。

| 场景 | Token | 字号 | 字重 | 行高 | 备注 |
|---|---|---:|---:|---:|---|
| 页面标题（TopBar/页面） | `titleLarge` | 20sp | 600 | 28sp | 稳重清晰 |
| 卡片/区块标题 | `titleMedium` | 16sp | 600 | 22sp | 卡片标题 |
| 列表项标题 | `titleSmall` | 14sp | 600 | 20sp | 列表标题 |
| 正文 | `bodyLarge` | 14sp | 400 | 20sp | 主体阅读 |
| 次正文 | `bodyMedium` | 13sp | 400 | 18sp | 次信息 |
| 辅助/meta | `bodySmall` | 12sp | 400 | 16sp | 时间/说明 |
| 按钮/Chip | `labelLarge` | 13sp | 500 | - | 交互文案 |
| 小标签/Badge | `labelMedium` | 12sp | 500 | - | 胶囊标签 |
| 关键数字（第N天） | `displaySmall` | 32sp | 700 | 40sp | 少量使用 |

**规则：**
- 标题不要再额外手写 `fontWeight = Bold/Medium` 覆盖 token（会破坏一致性）。
- 强调尽量用留白/位置/颜色实现，不要全靠粗体。

---

### 1.2 间距与圆角（使用现有 Dimens/ShapeTokens）

- `Dimens.ScreenPadding = 16dp`
- `Dimens.CardPadding = 16dp`
- `Dimens.SectionSpacing = 12dp`
- `Dimens.LargeSpacing = 24dp`

圆角：
- `ShapeTokens.Card = 16dp`
- `ShapeTokens.Field = 12dp`
- ✅ 建议新增：`ShapeTokens.Pill = RoundedCornerShape(999.dp)`（用于 Badge/Segmented/Chip）

---

### 1.3 色彩方向（风格 A：清爽克制）

- 页面背景：淡粉白（如 `#FFF7F9`），不要“明显粉底”
- 卡片层级：优先用 `surfaceVariant / surfaceContainerLow`（如果未定义 container，则用 `surfaceVariant`）
- `outlineVariant`：只用于输入框、分隔线、必要时的细描边
- 红色（error）仅用于错误/未完成的 **淡底 badge**，避免大红文本

---

## 2) Theme 与 Type（P0 必做）

### 2.1 `ui/theme/Theme.kt`

**现状问题：**  
- `typography = MaterialTheme.typography` 导致自定义字体不生效。

**要求：**  
- ✅ `typography = Typography`

**建议补齐的色彩字段（提高界面一致性与可控性）：**
- `surfaceVariant`
- `onSurfaceVariant`
- `outline`
- `outlineVariant`
- `errorContainer`
- `onErrorContainer`

---

### 2.2 `ui/theme/Type.kt`

**现状问题：**  
- 仅定义 `bodyLarge=16sp`，其余用默认 token，层级不稳。

**要求：**  
按 1.1 表格补齐 token，并将 `bodyLarge` 改为 **14sp**（更克制耐看，16 容易笨重）。

---

### 2.3 `ui/theme/Color.kt`

**现状问题：**  
- 仍是模板 Purple/Pink，易误导团队。

**二选一处理：**
- 方案 A（推荐）：替换为真实恋爱粉色板 + 注释用途  
- 方案 B：保留但标注“模板/废弃”，避免继续引用

---

## 3) DesignSystem（P1：统一组件，直接决定“协调感”）

文件：`presentation/components/DesignSystem.kt`

### 3.1 `AppCard`（强制统一）

**现状：** 默认描边 + 0 阴影 → 线框感强、偏“表格感”。

**改造规范：**
- 默认：**无描边**
- 背景：优先 `surfaceContainerLow`（或 `surfaceVariant`）
- 可选参数：`bordered: Boolean = false`
- elevation：默认 0dp（风格A克制），必要时可轻微 1dp

### 3.2 新增通用组件（建议）

1) `AppBadge` / `StatusBadge`（胶囊标签）  
用于：连续记录、已完成/未完成、类型标签等。  
- shape：`ShapeTokens.Pill`
- text：`labelMedium`
- 颜色：淡底（primaryContainer / errorContainer）+ 深字（on*Container）

2) `AppSegmentedTabs`（替代 FilterChip 组）  
用于：
- 统计页：最近7/30/90/全年
- 设置页：跟随系统/浅色/深色

样式：
- 外容器：淡底 + 胶囊圆角
- 选中项：白底或淡色底（primary 8~12%）
- 文案：`labelLarge`

3) `SectionHeader` subtitle 统一  
当前 subtitle 用 `bodyMedium`（13sp）略抢。  
- ✅ 建议改为 `bodySmall`（12sp）+ `onSurfaceVariant`

---

## 4) 页面级改造清单（按文件给 agent）

---

### 4.1 `HomeScreen.kt`（首页）

#### 现状问题
1) RelationshipCard：  
- “第N天”使用 `headlineMedium`（过大且不稳）  
- “=xx天”也用 primary（两行都抢）

2) TodayOverviewBar：  
- “连续记录：xx天”直接 primary 文本，显硬

3) 风格混杂：OutlinedButton/Surface(bordered)/AppCard 混用

#### 目标改造点（可执行）

**A) RelationshipCard**
- “第 ${dayIndex} 天”：改为 `displaySmall`（32sp/700）+ primary
- “= ${dayDisplay}”：降级为 `bodySmall` + `onSurfaceVariant`（不要 primary）
- 顶部「我们」已经在一起：`bodySmall` + `onSurfaceVariant`
- 100天提示条：保留 primaryContainer，但文字用 `bodySmall/bodyMedium`，减少额外 Bold

**信息层级：**
- 强：第N天（displaySmall）
- 中：标题（bodySmall 灰）
- 弱：=xx天（bodySmall 灰）

**B) TodayOverviewBar**
- 左侧日期：用 `bodyMedium/bodyLarge`（不要 titleMedium）
- 右侧 streak：改成 `StatusBadge`（胶囊），淡底 + labelMedium

**C) MoodNoteViewer / MoodNoteInput**
- 维持“输入框有边界”的原则（outlineVariant），但标题统一 `bodySmall` 灰
- 正文用 `bodyLarge`

**D) MoodSelectorRow（6个心情按钮）**
- 未选中：surfaceVariant 淡底，无描边
- 选中：primaryContainer 淡底 + 轻描边
- 文案：labelMedium（12sp/500）

**E) 删除多余手写 `fontWeight`**
- 标题依赖 token，不额外 Bold 覆盖，提升一致性。

---

### 4.2 `StatisticsScreen.kt`（统计）

#### 现状问题
- TimeRangeSelector 使用 FilterChip：像“功能按钮”，不够克制统一  
- StatItem value 用 headlineMedium + Bold：太大且不协调  
- 进度条细硬，缺少统一圆角/高度

#### 目标改造点

**A) TimeRangeSelector**
- 替换 FilterChip → `AppSegmentedTabs`
- 文案：labelLarge 13/500
- 统一高度 40–44dp

**B) OverviewCard / StatItem**
- 标题“统计概览”：`titleMedium`
- value：建议 22–24sp / 700（可新增 token 或使用合适 headlineSmall）
- label：`bodySmall` 灰
- icon tint：`onSurfaceVariant`（除非卡片底色是 *Container）

**C) MoodDistributionItem**
- LinearProgressIndicator：高度 6dp、圆角/圆头（Round）
- “x次(%)”：bodySmall 灰
- emoji 固定 18–20sp，避免用 titleLarge.fontSize（不稳定）

---

### 4.3 `SettingsScreen.kt`（设置）

#### 现状问题
- 主题设置使用 FilterChip
- subtitle 使用 bodyMedium（略大）
- Divider 多且明显 → 表格感

#### 目标改造点
**A) ThemeSettingsItem**
- FilterChip → `AppSegmentedTabs`

**B) SettingsItem / SwitchSettingsItem**
- title：bodyLarge（14sp）
- subtitle：bodySmall（12sp）+ onSurfaceVariant
- Divider：颜色更淡（outlineVariant + alpha降低），或减少使用

---

### 4.4 `FirstRunScreen.kt`（首次设置）

#### 现状问题
- 标题 headlineMedium 偏“demo”
- 文案层级不够稳

#### 目标改造点
- 标题：titleLarge（20/600）
- 说明文字：bodySmall 灰
- 卡片标题“让我们开始记录吧”：titleMedium
- 保持留白：CardPadding=16、SectionSpacing=12

---

### 4.5 `HistoryScreen.kt`（历史）

#### 现状优点
- 已使用 AppScaffold/SectionHeader/AppCard，结构好

#### 需要优化
- 日期行不要手写 Medium；使用 titleSmall 或 bodyLarge token
- MoodTypeBadge 建议统一为 `ShapeTokens.Pill` 或 12dp
- badge 文字统一 `labelMedium`

---

### 4.6 `CheckInDashboardScreen.kt`（打卡）——重点重构

#### 现状问题
- 两个 LazyColumn + 第二个固定 200dp → 滚动与信息架构不合理
- 类型卡片“标题+强 Button” → 信息密度低、按钮墙
- 记录列表像 debug 输出

#### 目标结构（必须）
改为：**一个 LazyColumn**（唯一滚动容器）+ sections：

1) `SectionHeader`：打卡事项（subtitle：管理你的习惯与今日完成度）  
2) Grid（两列）：打卡类型卡片  
3) `SectionHeader`：最近记录  
4) 列表：记录 items

**类型卡片（两列 Grid）规范：**
- 标题：titleMedium
- 副标题：bodySmall 灰（如“记录今日完成度”）
- CTA：`FilledTonalButton`（小尺寸）文案“打卡”（避免强 primary Button）
- 高度：88–104dp
- padding：16dp

**记录 item 规范：**
- 第一行：name（titleSmall） + date（bodySmall 灰，右对齐）
- 第二行：类型 badge（淡底）
- 第三行：note（bodySmall 灰，可选）

---

## 5) 风格A一致性专项：减少混用组件造成“不协调”

当前混用：
- Card / ElevatedCard / OutlinedCard / Surface(bordered) / FilterChip / OutlinedButton

**统一策略：**
- 内容容器：统一 `AppCard`
- 分段选择：统一 `AppSegmentedTabs`（替换 FilterChip 组）
- 次级按钮：优先 `FilledTonalButton` / `TextButton`
- OutlinedButton：仅用于“更多/弱动作”场景

---

## 6) Top 6 快速收益（建议优先级）

1) Theme.kt：`typography = Typography`（立即全局生效）  
2) AppCard：默认去描边（立刻变轻盈）  
3) 首页：第N天用 displaySmall；“=xx天”降级灰  
4) streak 改 badge（别用 primary 文本硬顶）  
5) Statistics/Settings：统一 segmented（替换 FilterChip）  
6) CheckInDashboard：单 LazyColumn + grid（打卡页产品化）

---

## 7) 给 Copilot agent 的任务拆分模板（可复制）

### Task A（P0）：Theme/Type
- Fix `Theme.kt` to use `typography = Typography` and add missing color scheme fields (surfaceVariant, onSurfaceVariant, outline, outlineVariant, errorContainer...).
- Replace `Type.kt` with full typography tokens (titleLarge 20/600, titleMedium 16/600, bodyLarge 14/400, bodySmall 12/400, labelLarge 13/500, displaySmall 32/700…).

### Task B（P1）：DesignSystem
- Update `AppCard` default: remove default border, use surfaceVariant/surfaceContainerLow, optional bordered flag.
- Add `ShapeTokens.Pill`.
- Add `AppSegmentedTabs` to replace FilterChip groups.
- Add `StatusBadge` for streak/status/type tags.
- Update `SectionHeader` subtitle to use bodySmall.

### Task C（P1）：CheckInDashboard
- Convert two LazyColumns into a single LazyColumn with sections.
- Create a 2-column grid for check-in types; use tonal buttons.
- Redesign record list items with proper typography hierarchy and badges.

### Task D（P2）：Home
- RelationshipCard: use displaySmall for day index; demote “=xx天” to bodySmall gray.
- TodayOverviewBar: streak as badge; date uses bodyMedium/bodyLarge.
- Mood selector: chip-like style, selected uses primaryContainer.

### Task E（P2）：Statistics
- Replace FilterChip time selector with AppSegmentedTabs.
- StatItem: value typography 22–24sp bold; label bodySmall gray; icon tint onSurfaceVariant.
- Progress bars height 6dp, rounded caps.

### Task F（P2）：Settings
- Replace theme FilterChip group with AppSegmentedTabs.
- Subtitles -> bodySmall; reduce divider prominence.

---

## 8) 验收标准（Review Checklist）

### 字体
- 页面标题=20/600；卡片标题=16/600；正文=14/400；辅助=12/400  
- 行高正确（正文 20，辅助 16）  
- 关键数字只在 KPI 用（displaySmall 32/700）

### 颜色
- 背景淡粉白、卡片轻层级  
- 红色仅用于错误/未完成的淡底 badge  
- outlineVariant 只用于必要分隔/输入边界

### 组件一致性
- 卡片圆角统一 16dp  
- Badge/Segmented/Chip 统一胶囊  
- 统计/设置的选择控件风格一致

### 打卡页
- 单一滚动容器  
- 类型区 grid + tonal CTA  
- 记录列表信息层级清楚（name/date/type/note）

---