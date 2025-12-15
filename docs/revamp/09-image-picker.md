# ImagePicker 组件逐段落修改指令（统一形状/占位，并提供更稳的失败兜底）

文件：
- `app/src/main/java/com/love/diary/presentation/components/ImagePicker.kt`

目标：
- 组件样式对齐全局极简（圆角、边框、占位）
- 图片加载失败不至于完全空白（最低体验稳定性）

---

## Commit IMG-1 — `ui(image-picker): align shapes/colors with tokens & AppCard`
### 段落 1：预览容器
- 定位：`Card(shape = RoundedCornerShape(12.dp)) { Image(...) }`
- 修改：
  - 使用 `AppCard` 或至少 shape=tokens（16dp）
  - 避免额外 elevation
  - 需要裁剪：保留 `ContentScale.Crop`

### 段落 2：无图占位区
- 定位：`Surface(color = surfaceVariant, shape = RoundedCornerShape(12.dp))`
- 修改：
  - shape 用 tokens
  - icon tint 用 `onSurfaceVariant`
  - 文案 typography.bodyMedium + `onSurfaceVariant`
  - 维持 16:9 比例

#### 验收
- 有图/无图两种状态视觉统一
- 组件看起来像系统控件而不是“自画 UI”

---

## Commit IMG-2（可选） — `fix(image-picker): show placeholder on image load failure`
### 段落 3：Coil 加载失败兜底
- 改造方式（任选其一）：
  - 使用 `AsyncImage` 并提供 `error` 占位
  - 或继续 painter，但监听 state（如你愿意引入更明确的状态处理）
- 失败时展示：
  - 占位 icon + “图片加载失败”
  - 保持可点击重新选择

#### 验收
- 错误 uri 不会导致空白无反馈