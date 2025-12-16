# Home Screen UI - Visual Layout Overview

```
┌─────────────────────────────────────────────────────┐
│                 Love Diary                          │
│                 恋爱日记                             │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  HERO CARD WITH AVATARS                             │
│  ┌───────────────────────────────────────────────┐  │
│  │  👤              Day 365             👤       │  │
│  │  Avatar       与 XX 的第 365 天      Avatar   │  │
│  │                                               │  │
│  │         [  今天：2024-12-16（周一） ]         │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  MOOD TIMELINE CARD                                 │
│  ┌───────────────────────────────────────────────┐  │
│  │  今天感觉如何？                                │  │
│  │  点击表情即可切换心情                          │  │
│  │                                               │  │
│  │  MOOD SELECTOR (6 moods, 3 per row)          │  │
│  │  ┌──────┐ ┌──────┐ ┌──────┐                  │  │
│  │  │  😊  │ │  💗  │ │  🙂  │                  │  │
│  │  │ 开心 │ │ 满足 │ │ 正常 │                  │  │
│  │  └──────┘ └──────┘ └──────┘                  │  │
│  │  ┌──────┐ ┌──────┐ ┌──────┐                  │  │
│  │  │  😔  │ │  😡  │ │  ✏️  │                  │  │
│  │  │ 失落 │ │ 生气 │ │ 其它 │                  │  │
│  │  └──────┘ └──────┘ └──────┘                  │  │
│  │                                               │  │
│  │  "开心收到啦，我也在屏幕这头偷偷笑～"          │  │
│  │                                               │  │
│  │  NOTE INPUT SECTION                           │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │ 今天的心情描述（可选）                   │  │
│  │  │                                         │  │
│  │  │ [写下一句话...]                         │  │
│  │  │                                         │  │
│  │  │              [取消] [保存记录]          │  │
│  │  └─────────────────────────────────────────┘  │  │
│  │                                               │  │
│  │  PHOTO UPLOAD PLACEHOLDER                     │  │
│  │  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │  │
│  │  │           📷                           │  │  │
│  │  │    添加照片记录这一刻                  │  │  │
│  │  │        点击上传                        │  │  │
│  │  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │  │
│  │                                               │  │
│  │  最近心情                                      │  │
│  │  ┌───────────────────────────────────────┐    │  │
│  │  │ 😊 💗 🙂 😔 😡 ✏️ 😊 💗 🙂 😔  [更多→]│    │  │
│  │  └───────────────────────────────────────┘    │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  MOOD QUOTE CARD                                    │
│  ┌───────────────────────────────────────────────┐  │
│  │  "   心情寄语                                 │  │
│  │                                               │  │
│  │      开心收到啦，我也在屏幕这头偷偷笑～       │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  STATS ROW                                          │
│  ┌───────────────────────────────────────────────┐  │
│  │  记录统计                                      │  │
│  │  你的心情变化                                  │  │
│  │                                               │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  │  │
│  │  │ 总记录       📊 │  │ 连续打卡     🔥 │  │  │
│  │  │                  │  │                  │  │  │
│  │  │   365 天         │  │   30 天         │  │  │
│  │  └──────────────────┘  └──────────────────┘  │  │
│  │                                               │  │
│  │  ┌────────────────────────────────────────┐  │  │
│  │  │ 最近30天                           😊  │  │  │
│  │  │                                        │  │  │
│  │  │   开心                                 │  │  │
│  │  │   最常心情                             │  │  │
│  │  └────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  STATISTICS SECTION (existing)                      │
│  [Charts and detailed statistics...]                │
└─────────────────────────────────────────────────────┘

═════════════════════════════════════════════════════
     BOTTOM NAVIGATION BAR
═════════════════════════════════════════════════════
  🏠 Pink        ✓ Gray         👤 Gray
   今天           打卡            我的
  (selected)   (unselected)   (unselected)
═════════════════════════════════════════════════════
```

## Component Descriptions

### 1. Hero Card
**Visual**: Large card at top with two circular avatars flanking centered text
- **Left Avatar**: User's avatar placeholder (gradient circle with person icon)
- **Center**: "Day X" in large, bold, primary-colored text
- **Subtitle**: "与 [couple name] 的第 X 天"
- **Date Badge**: Pill-shaped badge with today's date
- **Right Avatar**: Partner's avatar placeholder

### 2. Mood Selector
**Visual**: 3x2 grid of circular mood buttons
- **Layout**: FlowRow, 3 items per row
- **Each Button**: 
  - Circular shape (76dp height)
  - Large emoji (28sp)
  - Label below emoji
  - Selected: primary container background + scale up
  - Unselected: surface variant background

### 3. Mood Prompt
**Visual**: Single line of feedback text
- Shows personalized message based on selected mood
- Gray color, medium body text
- Changes when mood selection changes

### 4. Note Input
**Visual**: Outlined text field with header and action buttons
- **Header**: "今天的心情描述（可选）"
- **Field**: Multi-line input (min 96dp height)
- **Placeholder**: "写下一句话，直接保存在今天的记录里"
- **Actions**: "取消" and "保存记录" buttons aligned right

### 5. Photo Placeholder
**Visual**: Dashed border box with upload prompt
- **Size**: 160dp height, full width
- **Border**: Dashed outline (2dp), light color
- **Background**: Very light surface variant
- **Content**: 
  - Camera icon (48dp)
  - "添加照片记录这一刻" text
  - "点击上传" subtitle

### 6. Recent Moods
**Visual**: Horizontal scrollable row of mood emojis
- **Container**: Card with padding
- **Moods**: Last 10 entries as emoji icons (36dp each)
- **Spacing**: 8dp between emojis
- **Action**: "更多" button with chevron at right
- **Background**: Each emoji in rounded square (12dp corners)

### 7. Mood Quote Card
**Visual**: Card with quote icon and feedback text
- **Icon**: Quote marks (32dp) on left, tertiary color
- **Header**: "心情寄语" in small label
- **Text**: Feedback message in body medium
- **Layout**: Horizontal row with icon | text column

### 8. Stats Row
**Visual**: Three elevated cards in grid layout
- **Row 1**: Two cards side-by-side
  - **Card 1**: Total records with 📊 icon
  - **Card 2**: Continuous streak with 🔥 icon
- **Row 2**: One full-width card
  - **Card 3**: Favorite mood (last 30 days) with mood emoji

**Each Card**:
- Elevated with 2dp shadow
- Rounded corners (16dp)
- Icon top-right
- Title in small label
- Value in large title, primary color
- Optional subtitle in small body

### 9. Bottom Navigation
**Visual**: Material 3 NavigationBar at bottom
- **Height**: Standard M3 height (80dp)
- **Items**: Three navigation items
  - **今天** (Home): Filled when selected, pink/primary color
  - **打卡** (Check): Outlined when not selected
  - **我的** (Person): Outlined when not selected
- **Selection**: Pink/primary indicator and filled icon

## Color Scheme

### Primary Colors
- **Primary**: #B9806E (Rose-gold)
- **Primary Container**: #EFE2DA (Light rose)
- **On Primary**: #2B120B (Dark brown)

### Surface Colors
- **Background**: #FAFAFA (Off-white)
- **Surface**: #FFFFFF (White)
- **Surface Variant**: #F4F0EB (Light beige)

### Text Colors
- **On Surface**: #1F1410 (Almost black)
- **On Surface Variant**: #5C514A (Gray-brown)

## Spacing System

- **Screen Padding**: 16dp (left/right margins)
- **Card Padding**: 16dp (internal padding)
- **Section Spacing**: 12dp (between elements)
- **Large Spacing**: 24dp (between major sections)
- **Item Spacing**: 8dp (between small items)

## Typography Scale

- **Display Small**: Day counter (large prominent text)
- **Title Large**: Section headers, important values
- **Title Medium**: Card titles, mood names
- **Body Medium**: Regular text, descriptions
- **Body Small**: Subtitles, helper text
- **Label Large**: Button text, navigation labels
- **Label Medium**: Small labels, card subtitles

## Accessibility Features

✅ All interactive elements have content descriptions
✅ Minimum touch targets: 48dp
✅ High contrast ratios for text
✅ Semantic structure for screen readers
✅ Clear visual feedback on interactions
✅ Keyboard navigation support (inherited from Compose)

## Animation & Interaction

- **Mood Selection**: Scale animation (1.0 → 1.2)
- **Button Press**: Scale down (1.0 → 0.98)
- **Expand/Collapse**: Fade + expand vertically
- **Navigation**: Slide transition between tabs
- **Stats**: Static (consider adding counter animation)

## Responsive Behavior

- **Small Screens**: Mood selector wraps to 2 rows of 3
- **Large Screens**: More space between cards
- **Landscape**: Layout adjusts, maintains proportions
- **Tablet**: Wider constraints, centered content

---

This visual layout provides a complete picture of the redesigned Home screen, showing how all components work together to create a cohesive, accessible, and beautiful user experience.
