# SettingsScreen 逐段落修改指令（你已有基础，统一到极简体系）

文件：
- `app/src/main/java/com/love/diary/presentation/screens/settings/SettingsScreen.kt`

目标：
- SettingsCard/Item 对齐 AppCard 体系
- 分组清晰、留白一致、icon 与文字层级克制

---

## Commit SET-1 — `ui(settings): align SettingsCard with AppCard tokens`
### 段落 1：SettingsCard 改造
- 定位：`@Composable fun SettingsCard(...) { Card(...) }`
- 修改：
  - 用 `AppCard` 替代 `Card`
  - 去掉 `surfaceColorAtElevation(3.dp)` 风格（极简不靠 elevation）
  - 标题 Text：
    - style 用 `titleMedium`
    - 颜色建议 `onSurface`（不要整片 primary）
    - 需要强调可用 `fontWeight = Medium`

#### 验收
- SettingsCard 与其他页面卡片一致：边框+圆角一致、阴影极少

### 段落 2：SettingsItem / SwitchSettingsItem / ThemeSettingsItem
- 修改：
  - subtitle 统一 `onSurfaceVariant`
  - icon tint 默认 `onSurfaceVariant`，仅关键状态用 primary
  - 行 padding 用 tokens

---

## Commit SET-2 — `ui(settings): group sections with SectionHeader + consistent spacing`
### 段落 3：分组标题统一 SectionHeader
- 定位：设置页分组处（可能是多个 SettingsCard 前的 Text）
- 修改：替换 `SectionHeader` 并规整分组间距（16/24）

### 段落 4：布局结构建议
- `LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = spacedBy(16.dp))`
- 每个 SettingsCard 占一项

#### 验收
- 滚动时分组清晰，留白一致