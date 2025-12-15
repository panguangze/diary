# 全局改造规则（必须遵守）— 清新极简 · 青绿 · 浅色为主

适用范围：本仓库所有 Compose UI 页面与组件。

## 1) 视觉原则
1. 主色（青绿）仅用于强调：Primary Button、选中态、关键 icon、链接/高亮文字  
2. 背景/卡片以白与极浅灰为主：少阴影，多边框（`outlineVariant`）  
3. 文字层级用排版与留白实现，不用花色与大阴影

## 2) 代码原则
1. 文本全部使用 `MaterialTheme.typography.*`  
   - 禁止 `fontSize = xx.sp` 随意硬编码（除非是非常特殊的图表标注且经过评审）
2. 间距/圆角/尺寸必须来自 tokens：`Dimens` / `ShapeTokens` / `ColorTokens`
3. 页面标题统一由 `AppScaffold` 的 `TopAppBar` 提供  
   - 页面内部不要再放一个“超大标题 Text”
4. 卡片统一使用 `AppCard`（默认 0 elevation + 1dp outlineVariant 边框）
5. 分区标题统一使用 `SectionHeader`
6. 空/错/加载统一使用 `StateViews`：`LoadingState` / `EmptyState` / `ErrorState`

## 3) 交付/验收（每个页面改完必须做）
- 截图（浅色模式）：
  - 初始态
  - 空态/错态/加载态（如适用）
- 自测：
  - 主色是否克制
  - 是否存在突兀的字号/间距/阴影
- 可回滚：每个 commit 可独立 revert 且编译通过