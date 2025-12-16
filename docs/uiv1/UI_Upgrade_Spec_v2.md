# Product Requirement Document: Love Diary UI 2.0 (Code-Ready Spec)

> **Context**: User Interface overhaul for `panguangze/diary` (Android/Compose).
> **Goal**: Create a cohesive, "Warm & Clean" Design System (Material3). Avoid "spreadsheet-like" sterility; prioritize emotional connection and usability.
> **Tech Stack**: Jetpack Compose, Material3, Kotlin.

---

## 1. Core Design Philosophy (The "North Star")

*   **Visual Style**: "Warm Minimalism" (清爽温润).
    *   **Light Mode**: Soft Warm White background (`#FFF7F9` or similar surface), avoiding harsh pure white.
    *   **Dark Mode**: Deep Warm Grey (`#1A1115` or `#121212`), preserving eye comfort at night.
    *   **Hierarchy**: Use spacing and typography over bold borders.
*   **Interaction**:
    *   **Touch Targets**: Minimum 48dp for all interactive elements.
    *   **Feedback**: Ripple effects on all cards; micro-animations on mood selection.
*   **Critical Rule**: **Do NOT use hardcoded colors or sizes.** Always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.

---

## 2. Technical Foundation (P0 - Immediate Fixes)

### 2.1 Theme Configuration (`ui/theme/Theme.kt`)
*   **Action**: Bind the custom typography object.
    *   *Current*: `typography = MaterialTheme.typography` (Wrong)
    *   *Fix*: `typography = Typography` (Correct)
*   **Color Scheme Expansion**:
    *   Explicitly define `surfaceVariant`, `onSurfaceVariant`, `outline`, `outlineVariant`, `errorContainer`, `onErrorContainer` for **both** Light and Dark schemes.
    *   **Dark Mode Requirement**: Ensure strictly high contrast text (e.g., `onSurface` should be off-white, not gray) against dark backgrounds.

### 2.2 Typography System (`ui/theme/Type.kt`)
Define the complete scale. **Do not bold manually in UI code**; use these tokens:

| Token | Size | Weight | LineHeight | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `displaySmall` | 32sp | Bold (700) | 40sp | Key metrics (Days Counter) |
| `titleLarge` | 22sp | SemiBold (600)| 28sp | Screen Titles (TopAppBar) |
| `titleMedium` | 16sp | SemiBold (600)| 24sp | Card Titles, Section Headers |
| `titleSmall` | 14sp | Medium (500) | 20sp | List Item Titles |
| `bodyLarge` | 16sp | Regular (400) | 24sp | Main Content, Inputs |
| `bodyMedium` | 14sp | Regular (400) | 20sp | Secondary Content |
| `bodySmall` | 12sp | Regular (400) | 16sp | Metadata, Timestamps, Captions |
| `labelLarge` | 14sp | Medium (500) | 20sp | Buttons, Tabs |
| `labelMedium` | 12sp | Medium (500) | 16sp | Chips, Badges |

### 2.3 Shape System
*   `ShapeTokens.Card`: `RoundedCornerShape(16.dp)` (Global standard for cards)
*   `ShapeTokens.Field`: `RoundedCornerShape(12.dp)` (Inputs)
*   `ShapeTokens.Pill`: `CircleShape` (Badges, Chips, Segmented Buttons)

---

## 3. Component Library (`presentation/components/DesignSystem.kt`)

### 3.1 `AppCard` (The Container)
*   **Default State**: No border, `surfaceContainerLow` (or `surfaceVariant`) background.
*   **Elevation**: Default `0.dp`.
*   **Border**: Optional param `bordered: Boolean = false`. If true, use `outlineVariant` (1dp).
*   **Interaction**: Must support `onClick` param (making the whole card clickable).

### 3.2 `AppSegmentedTabs` (New Component)
*   *Replaces `FilterChip` groups for "Single Select" scenarios.*
*   **Appearance**: Pill-shaped container, contrasting indicator for selected item.
*   **Use Cases**: Time Range (Stats), Theme Select (Settings).

### 3.3 `StatusBadge` (New Component)
*   **Appearance**: Small Capsule (Pill shape).
*   **Colors**: Pastel background (`primaryContainer` or `errorContainer`) with dark text (`onContainer`).
*   **Text**: `labelMedium`.

---

## 4. Screen-by-Screen Implementation Guide

### 4.1 Home Screen (`HomeScreen.kt`)
**Goal**: Maximize emotional connection.

1.  **RelationshipCard**:
    *   **Bg**: Allow for an Image Background slot (overlay with black alpha gradient). Fallback to `primaryContainer` gradient.
    *   **Counter**: Use `displaySmall` (32sp).
    *   **Text**: "We have been together for" -> `bodySmall` (Opacity 0.8).
    *   **Secondary Info**: Move exact dates/details to a less prominent spot (bottom right, `bodySmall`).
2.  **Mood Input Area**:
    *   **Header**: "How are you today?" (`titleMedium`).
    *   **Selector**: Row of Emojis.
        *   *Unselected*: Grayscale/Low Alpha.
        *   *Selected*: Full Color, Scale 1.2x, Background `primaryContainer` circle.
3.  **TodayOverviewBar**:
    *   **Streak**: Use `StatusBadge` (e.g., "🔥 12 Days"). Do not use plain colored text.

### 4.2 Check-In Dashboard (`CheckInDashboardScreen.kt`)
**Goal**: Frictionless entry.

1.  **Layout Refactor**:
    *   **Structure**: **Single `LazyColumn`**. No nested scrolling.
    *   **Top Section**: "Quick Actions" or "Today's Status".
    *   **Middle Section**: Grid (2 columns) for "Check-In Types".
    *   **Bottom Section**: Recent History list.
2.  **Type Cards (Grid Items)**:
    *   **Interaction**: The **Entire Card** is the button. Do not place a small "Check" button inside.
    *   **Visual**: Icon (Top Left) + Title (Bottom Left).
    *   **Feedback**: Ripple on touch.
3.  **History Items**:
    *   **Typography**: Name (`titleSmall`) + Time (`bodySmall`, aligned end).
    *   **Style**: Minimalist row, optional light divider.

### 4.3 Statistics Screen (`StatisticsScreen.kt`)
**Goal**: Data storytelling, not just charts.

1.  **Controls**: Replace `FilterChip` with `AppSegmentedTabs` (Week/Month/Year).
2.  **Stat Cards**:
    *   **Big Number**: `headlineMedium` (or 24sp Bold).
    *   **Label**: `bodySmall` (onSurfaceVariant).
    *   **Insight**: Add a text slot for insights (e.g., "Most frequent mood: Happy").
3.  **Progress Bars**:
    *   Height: 8dp.
    *   Ends: `StrokeCap.Round`.
    *   Track Color: `surfaceVariant`.

### 4.4 Settings (`SettingsScreen.kt`)
**Goal**: Grouping and clarity.

1.  **Headers**: Use `titleSmall` + `primary` color for section headers (General, Appearance, Data).
2.  **Items**: Standardize height (56dp min).
3.  **Theme Selector**: Use `AppSegmentedTabs` (System / Light / Dark).

---

## 5. Execution Roadmap for Agent

Please execute the following tasks in order. **Stop and ask for review after each Phase.**

### Phase 1: Foundation (Theme & Design System)
1.  **Refactor `Theme.kt`**: Bind Typography, define Color Scheme (Light/Dark).
2.  **Refactor `Type.kt`**: Implement the full token table (2.2).
3.  **Create `DesignSystem.kt`**: Implement `AppCard` (clickable, borderless default), `StatusBadge`, and `AppSegmentedTabs`.

### Phase 2: Core UX (Home & Check-In)
4.  **Refactor `HomeScreen.kt`**: Update RelationshipCard (visuals + text hierarchy) and MoodSelector (interaction).
5.  **Refactor `CheckInDashboardScreen.kt`**: Merge LazyColumns, implement Grid layout for types, make cards fully clickable.

### Phase 3: Secondary Screens (Stats & Settings)
6.  **Refactor `StatisticsScreen.kt`**: Update selector controls and chart styling.
7.  **Refactor `SettingsScreen.kt`**: Standardize list items and headers.

### Phase 4: Polish
8.  **Empty States**: Add placeholder UI for empty lists in History/Home.
9.  **Dark Mode Check**: Verify contrast ratios in dark mode preview.