# StatisticsScreen 逐段落修改指令（数据为主，克制呈现）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/statistics/StatisticsScreen.kt`

目标：
- 页面标题归 TopAppBar
- 卡片统一 AppCard（边框+圆角+少阴影）
- Empty/Error/Loading 统一 StateViews
- 图表区域不过度花哨

---

## Commit STAT-1 — `ui(statistics): move page title to TopAppBar; adjust header block`
### 段落 1：页面内容标题块降级
- 定位：`Text("心情统计", style = headlineLarge)` + subtitle
- 修改：
  - 页面 title → TopAppBar（AppScaffold title=`统计` 或 `心情统计`）
  - 内容区保留一个轻量 `SectionHeader(title="最近 X 天", subtitle=...)` 或仅 subtitle
  - 不再使用 `headlineLarge` 作为内容标题

#### 验收
- 第一屏更轻，数据卡片更突出

---

## Commit STAT-2 — `ui(statistics): convert all cards to AppCard; unify padding/spacing`
### 段落 2：TimeRangeSelector 区
- 保持交互不变
- 外围容器统一 padding（tokens）

### 段落 3：Overview/Distribution/Trend/Summary 各卡片
- 定位：`*Card` composable 内部的 `Card(...)`
- 修改：
  - 全部替换为 `AppCard`
  - 移除 elevation
  - 内部 padding 用 tokens
  - 图表区域背景可用 `surfaceVariant`（轻分区）

#### 验收
- 卡片统一，图表不刺眼

---

## Commit STAT-3 — `ui(statistics): replace local empty/error with shared StateViews`
### 段落 4：删除本文件私有 `EmptyState` / `ErrorState`
- 用共享 `EmptyState`/`ErrorState` 替代
- 文案保持原意

#### 验收
- 状态呈现与 History 等页面一致