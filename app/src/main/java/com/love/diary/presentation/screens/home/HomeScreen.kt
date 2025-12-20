/**
 * HomeScreen.kt 文件说明：
 * 这是恋爱日记应用的主页屏幕实现文件
 * 包含了用户界面的主要组件，如心情选择、统计信息、历史记录等功能
 */

// 声明包名，表示此文件属于应用的主页屏幕展示层
package com.love.diary.presentation.screens.home

// 导入必要的库和组件，用于构建UI和处理功能
import android.net.Uri  // 用于处理文件路径和URI
import androidx.activity.compose.rememberLauncherForActivityResult  // 用于启动活动并获取结果
import androidx.activity.result.contract.ActivityResultContracts  // 定义启动活动的结果合同
import androidx.compose.animation.AnimatedVisibility  // 用于控制元素的动画可见性
import androidx.compose.animation.core.animateFloatAsState  // 用于创建浮点数值的动画
import androidx.compose.animation.expandVertically  // 垂直展开动画
import androidx.compose.animation.fadeIn  // 淡入动画
import androidx.compose.foundation.BorderStroke  // 边框绘制
import androidx.compose.foundation.Image  // 图像显示组件
import androidx.compose.foundation.background  // 背景设置
import androidx.compose.foundation.border  // 边框设置
import androidx.compose.foundation.clickable  // 点击交互
import androidx.compose.foundation.interaction.MutableInteractionSource  // 交互源管理
import androidx.compose.foundation.interaction.collectIsPressedAsState  // 收集按压状态

import androidx.compose.foundation.layout.Arrangement  // 布局排列方式
import androidx.compose.foundation.layout.Box  // 盒子布局容器
import androidx.compose.foundation.layout.BoxWithConstraints  // 带约束的盒子布局
import androidx.compose.foundation.layout.Column  // 列式布局容器
import androidx.compose.foundation.layout.ExperimentalLayoutApi  // 实验性布局API
import androidx.compose.foundation.layout.FlowRow  // 流式行布局
import androidx.compose.foundation.layout.IntrinsicSize  // 固有尺寸
import androidx.compose.foundation.layout.PaddingValues  // 内边距值
import androidx.compose.foundation.layout.Row  // 行式布局容器
import androidx.compose.foundation.layout.Spacer  // 空白填充器
import androidx.compose.foundation.layout.defaultMinSize  // 默认最小尺寸
import androidx.compose.foundation.layout.fillMaxHeight  // 填充最大高度
import androidx.compose.foundation.layout.fillMaxSize  // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth  // 填充最大宽度
import androidx.compose.foundation.layout.height  // 高度设置
import androidx.compose.foundation.layout.heightIn  // 高度范围限制
import androidx.compose.foundation.layout.padding  // 内边距设置
import androidx.compose.foundation.layout.size  // 大小设置
import androidx.compose.foundation.layout.width  // 宽度设置
import androidx.compose.foundation.layout.widthIn  // 宽度范围限制
import androidx.compose.foundation.lazy.grid.GridCells  // 网格单元格配置
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid  // 垂直网格列表
import androidx.compose.foundation.lazy.grid.items  // 网格项
import androidx.compose.foundation.rememberScrollState  // 滚动状态记忆
import androidx.compose.foundation.shape.CircleShape  // 圆形形状
import androidx.compose.foundation.shape.RoundedCornerShape  // 圆角矩形形状
import androidx.compose.foundation.text.BasicTextField  // 基础文本输入框
import androidx.compose.foundation.verticalScroll  // 垂直滚动
import androidx.compose.material.icons.Icons  // 材料设计图标
import androidx.compose.material.icons.filled.AddPhotoAlternate  // 添加照片图标
import androidx.compose.material.icons.filled.Celebration  // 庆祝图标
import androidx.compose.material.icons.filled.ChevronRight  // 向右箭头图标
import androidx.compose.material.icons.filled.Close  // 关闭图标
import androidx.compose.material.icons.filled.ContentCopy  // 复制内容图标
import androidx.compose.material.icons.filled.FormatQuote  // 引号图标
import androidx.compose.material.icons.filled.Person  // 人物图标
import androidx.compose.material.icons.filled.Share  // 分享图标
import androidx.compose.material3.AlertDialog  // 对话框组件
import androidx.compose.material3.Button  // 按钮组件
import androidx.compose.material3.ButtonDefaults  // 按钮默认样式
import androidx.compose.material3.Card  // 卡片组件
import androidx.compose.material3.CardDefaults  // 卡片默认样式
import androidx.compose.material3.ElevatedCard  // 抬起的卡片组件
import androidx.compose.material3.ExperimentalMaterial3Api  // 实验性材料设计3 API
import androidx.compose.material3.Divider  // 分割线组件
import androidx.compose.material3.IconButton  // 图标按钮组件
import androidx.compose.material3.FilterChip  // 过滤芯片组件
import androidx.compose.material3.Icon  // 图标组件
import androidx.compose.material3.MaterialTheme  // 材料设计主题
import androidx.compose.material3.ModalBottomSheet  // 模态底部表单
import androidx.compose.material3.OutlinedCard  // 轮廓卡片组件
import androidx.compose.material3.OutlinedButton  // 轮廓按钮组件
import androidx.compose.material3.OutlinedTextField  // 轮廓文本输入框
import androidx.compose.material3.Surface  // 表面组件
import androidx.compose.material3.Text  // 文本组件
import androidx.compose.material3.TextButton  // 文本按钮组件
import androidx.compose.material3.rememberModalBottomSheetState  // 记住底部表单状态
import androidx.compose.material3.surfaceColorAtElevation  // 表面颜色随海拔变化
import androidx.compose.runtime.Composable  // Composable 函数注解
import androidx.compose.runtime.collectAsState  // 收集状态流转换为 State
import androidx.compose.runtime.getValue  // 获取状态值
import androidx.compose.runtime.mutableIntStateOf  // 可变整数状态
import androidx.compose.runtime.mutableStateOf  // 可变状态
import androidx.compose.runtime.remember  // 记住变量
import androidx.compose.runtime.setValue  // 设置状态值
import androidx.compose.runtime.key  // 键值用于重组优化
import androidx.compose.ui.geometry.CornerRadius  // 圆角半径
import androidx.compose.ui.geometry.Offset  // 偏移量
import androidx.compose.ui.Alignment  // 对齐方式
import androidx.compose.ui.Modifier  // 修饰符，用于修改UI元素
import androidx.compose.ui.draw.clip  // 裁剪绘制
import androidx.compose.ui.draw.drawBehind  // 在背景绘制
import androidx.compose.ui.graphics.Brush  // 画刷，用于渐变色等效果
import androidx.compose.ui.graphics.Color  // 颜色定义
import androidx.compose.ui.graphics.PathEffect  // 路径效果
import androidx.compose.ui.graphics.drawscope.DrawScope  // 绘制范围
import androidx.compose.ui.graphics.drawscope.Stroke  // 笔划
import androidx.compose.ui.graphics.graphicsLayer  // 图形层
import androidx.compose.ui.layout.ContentScale  // 内容缩放
import androidx.compose.ui.platform.LocalClipboardManager  // 本地剪贴板管理器
import androidx.compose.ui.platform.LocalContext  // 本地上下文
import androidx.compose.ui.platform.LocalDensity  // 本地密度
import androidx.compose.ui.res.painterResource  // 从资源获取图像绘制器
import androidx.compose.ui.res.stringResource  // 从资源获取字符串
import androidx.compose.ui.semantics.contentDescription  // 语义描述
import androidx.compose.ui.semantics.semantics  // 语义属性
import androidx.compose.ui.text.font.FontWeight  // 字体粗细
import androidx.compose.ui.text.style.TextAlign  // 文本对齐
import androidx.compose.ui.unit.dp  // 密度独立像素单位
import androidx.compose.ui.unit.sp  // 缩放独立像素单位
import androidx.compose.ui.window.Dialog  // 对话框窗口
import androidx.compose.ui.window.DialogProperties  // 对话框属性
import androidx.hilt.navigation.compose.hiltViewModel  // Hilt依赖注入的ViewModel
import coil.compose.AsyncImage  // Coil异步加载图像
import coil.request.ImageRequest  // 图像请求
import com.love.diary.R  // 应用资源访问
import com.love.diary.data.database.entities.DailyMoodEntity  // 日常心情实体
import com.love.diary.data.model.MoodType  // 心情类型枚举
import com.love.diary.presentation.components.AppCard  // 应用卡片组件
import com.love.diary.presentation.components.Dimens  // 尺寸定义组件
import com.love.diary.presentation.components.SectionHeader  // 区域标题组件
import com.love.diary.presentation.components.ShapeTokens  // 形状规范组件
import com.love.diary.presentation.components.StatusBadge  // 状态徽章组件
import com.love.diary.presentation.viewmodel.HistoryViewModel  // 历史视图模型
import com.love.diary.presentation.viewmodel.HomeViewModel  // 主页视图模型
import com.love.diary.presentation.viewmodel.StatisticsViewModel  // 统计视图模型
import com.love.diary.util.ShareHelper  // 分享辅助类
import com.love.diary.presentation.screens.statistics.SimpleTrendChart  // 简单趋势图表
import java.time.LocalDate  // 本地日期
import java.time.format.DateTimeFormatter  // 日期时间格式化器
import java.time.format.TextStyle  // 文本样式
import java.util.Locale  // 语言环境

// 定义一些全局使用的常量和颜色值
private val MoodGridMaxHeight = 240.dp  // 心情网格的最大高度
private val StatsGridMinHeight = 160.dp  // 统计网格的最小高度
private val StatsGridMaxHeight = 320.dp  // 统计网格的最大高度
private const val RecentMoodIconTargetCount = 10  // 最近心情图标的显示数量目标

// 定义应用的主要颜色
private val PrimaryPink = Color(0xFFFF557F)  // 主要粉色
private val AccentYellow = Color(0xFFFFD33D)  // 强调黄色
private val NeutralGray = Color(0xFF888888)  // 中性灰色
private val AccentGreen = Color(0xFF34C759)  // 强调绿色
private val AccentBlue = Color(0xFF007AFF)  // 强调蓝色
private val AccentRed = Color(0xFFFF3B30)  // 强调红色
private val BorderColor = Color(0xFFE5E7EB)  // 边框颜色
private val SubtitleGray = Color(0xFF666666)  // 副标题灰色
private val BodyGray = Color(0xFF333333)  // 正文灰色
private val HeaderTextColor = Color(0xFF2D2D33)  // 标题文字颜色
private val SubTextColor = Color(0xFF999999)  // 次要文字颜色
private val ControlTextColor = Color(0xFF4A4A52)  // 控件文字颜色
private val LightSurfaceColor = Color(0xFFF2F2F5)  // 浅表面颜色
private val UploadBorderColor = Color(0xFFE5E5EA)  // 上传框边框颜色
private val AccentPinkText = Color(0xFFFF7A90)  // 强调粉色文字
private val AccentGradientStart = Color(0xFFFF6B81)  // 渐变强调色开始
private val AccentGradientEnd = Color(0xFFFF476F)  // 渐变强调色结束
private val MoodSelectedStart = Color(0xFFFFE6E8)  // 心情选中状态渐变开始色
private val MoodSelectedEnd = Color(0xFFFFC2C6)  // 心情选中状态渐变结束色

// 获取心情趋势范围选项，用于筛选统计数据的时间范围
private val MoodTrendRangeOptions = StatisticsViewModel.DEFAULT_RANGE_OPTIONS.map { it to it.toRangeLabelRes() }

// 定义心情选项数据类，包含标签和心情类型
private data class MoodOption(
    val label: String,  // 心情标签，如"开心"、"难过"等
    val moodType: MoodType  // 心情类型枚举
)

// 将整数范围值转换为对应的资源字符串ID
private fun Int.toRangeLabelRes(): Int = when (this) {
    StatisticsViewModel.RANGE_WEEK -> R.string.home_mood_trend_range_week  // 一周范围
    StatisticsViewModel.RANGE_MONTH -> R.string.home_mood_trend_range_month  // 一个月范围
    StatisticsViewModel.RANGE_QUARTER -> R.string.home_mood_trend_range_quarter  // 一个季度范围
    else -> R.string.home_mood_trend_range_year  // 一年范围
}

/**
 * HomeScreen 可组合函数 - 主页屏幕的主要UI组件
 * 这是应用的主页，包含情侣信息、心情记录、统计图表等主要功能
 * 
 * @param viewModel HomeViewModel 实例，提供UI状态和业务逻辑
 * @param modifier 修饰符，用于自定义组件的外观和行为
 */
@OptIn(ExperimentalMaterial3Api::class)  // 允许使用实验性API
@Composable  // 标记为可组合函数，用于构建UI
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),  // 使用Hilt依赖注入获取HomeViewModel实例
    modifier: Modifier = Modifier  // 默认修饰符
) {
    // 从ViewModel中收集UI状态，当状态变化时自动重组UI
    val uiState by viewModel.uiState.collectAsState()
    
    // 获取其他ViewModel实例，用于处理历史记录和统计数据
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    
    // 从历史记录ViewModel收集心情记录数据
    val historyRecords by historyViewModel.moodRecords.collectAsState()
    // 从统计ViewModel收集统计UI状态
    val statisticsState by statisticsViewModel.uiState.collectAsState()
    
    // 获取当前上下文和剪贴板管理器，用于分享和复制功能
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // 创建图像选择器，用于选择头像
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()  // 启动获取内容的活动
    ) { uri ->  // 回调函数处理选择的URI
        uri?.let { viewModel.updateAvatar(isPartner = false, uri = it.toString()) }  // 更新头像
    }
    
    // 创建心情图片选择器，用于为心情添加图片
    val moodImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()  // 启动获取内容的活动
    ) { uri ->  // 回调函数处理选择的URI
        viewModel.updateSelectedImage(uri?.toString())  // 更新选择的图片
    }

    // 使用状态变量控制日历对话框的显示/隐藏
    var showCalendarSheet by remember { mutableStateOf(false) }
    // 使用状态变量存储选中的历史记录项
    var selectedHistoryItem by remember { mutableStateOf<DailyMoodEntity?>(null) }

    // 记住模态底部表单状态
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 获取今天的日期字符串
    val todayString = uiState.todayDate.ifBlank { LocalDate.now().toString() }

    // 定义心情选项列表，包含标签和对应的心情类型
    val moodOptions = remember {
        listOf(
            MoodOption("甜蜜", MoodType.SATISFIED),  // 满意/甜蜜心情
            MoodOption("开心", MoodType.HAPPY),     // 开心心情
            MoodOption("正常", MoodType.NORMAL),    // 正常心情
            MoodOption("失落", MoodType.SAD),       // 悲伤心情
            MoodOption("愤怒", MoodType.ANGRY),     // 愤怒心情
            MoodOption("其他", MoodType.OTHER)      // 其他心情
        )
    }

    // 计算最近30天中最常出现的心情类型
    val favoriteMood = historyRecords
        .takeLast(30)  // 获取最近30条记录
        .groupBy { it.moodTypeCode }  // 按心情类型代码分组
        .maxByOrNull { it.value.size }  // 找到数量最多的组
        ?.let { MoodType.fromCode(it.key) }  // 转换为MoodType

    // 定义页面背景渐变色
    val pageBackground = Brush.verticalGradient(listOf(Color(0xFFFAFAFC), Color(0xFFF5F5F8)))
    // 获取默认心情文本资源
    val defaultMoodText = stringResource(id = R.string.home_default_mood_text)

    // 使用Box作为根布局容器
    Box(
        modifier = modifier
            .fillMaxSize()  // 填充整个可用空间
            .background(pageBackground)  // 设置背景渐变色
    ) {
        // 使用Column垂直排列所有UI组件
        Column(
            modifier = Modifier
                .fillMaxSize()  // 填充整个可用空间
                .verticalScroll(rememberScrollState())  // 添加垂直滚动功能
                .padding(horizontal = Dimens.ScreenPadding)  // 设置水平内边距
                .padding(top = 24.dp, bottom = 80.dp),  // 设置上下内边距
            horizontalAlignment = Alignment.CenterHorizontally  // 水平居中对齐
        ) {
            // 今日头部组件 - 显示应用标题
            TodayHeader()

            Spacer(modifier = Modifier.height(20.dp))  // 添加垂直间距

            // 顶部信息卡片 - 显示情侣信息和头像
            TopInfoCardRedesigned(
                title = "${uiState.coupleName ?: \"小明 & 小红\"}的第${if (uiState.dayIndex > 0) uiState.dayIndex else 16}天",  // 恋爱天数标题
                subtitle = "From ${uiState.startDate.ifBlank { \"2025 - 01 - 01\" }} to ${uiState.todayDate.ifBlank { todayString }}",  // 日期范围副标题
                avatarUri = uiState.avatarUri,  // 头像URI
                onAvatarClick = { avatarPicker.launch("image/*") },  // 点击头像选择图片
                onAiClick = { viewModel.showOtherMoodDialog() }  // 点击AI按钮显示对话框
            )

            Spacer(modifier = Modifier.height(16.dp))  // 添加垂直间距

            // 心情记录区域 - 选择心情、添加图片、输入文字
            MoodRecordSection(
                moodOptions = moodOptions,  // 心情选项列表
                selectedMood = uiState.todayMood,  // 当前选择的心情
                inputText = uiState.otherMoodText,  // 输入的文字内容
                selectedImageUri = uiState.selectedImageUri,  // 选择的图片URI
                onMoodSelected = { mood ->  // 心情选择回调
                    val noteToSave = uiState.otherMoodText.ifBlank { null }  // 获取要保存的笔记
                    if (mood != uiState.todayMood) {  // 如果心情发生变化
                        viewModel.updateOtherMoodText("")  // 清空心情文本
                    }
                    viewModel.selectMood(mood, noteToSave)  // 选择心情
                },
                onInputChange = viewModel::updateOtherMoodText,  // 输入文本变化回调
                onPickImage = { moodImagePicker.launch("image/*") },  // 选择图片回调
                onSave = { text -> viewModel.saveDescription(text, defaultMoodText) }  // 保存回调
            )

            Spacer(modifier = Modifier.height(16.dp))  // 添加垂直间距

            // 获取今天的心情记录
            val todayDateString = LocalDate.now().toString()
            val todayMoodRecord = historyRecords.find { it.date == todayDateString }

            // 最近心情统计区域 - 显示最近的心情记录和统计数据
            RecentMoodStatsSection(
                recentMoods = uiState.recentTenMoods,  // 最近10条心情记录
                todayMood = todayMoodRecord,  // 今天的心情记录
                totalRecords = historyRecords.size,  // 总记录数
                streak = uiState.currentStreak,  // 当前连续记录天数
                favoriteMood = favoriteMood,  // 最喜欢的心情类型
                moodQuote = uiState.todayMood?.feedbackText  // 今日心情反馈文本
                    ?: "无论今天心情如何，我都在你身边，爱你每一天。",  // 默认反馈文本
                onMoreClick = { showCalendarSheet = true },  // 点击更多按钮回调
                onMoodClick = { selectedHistoryItem = it }  // 点击心情记录回调
            )

            Spacer(modifier = Modifier.height(16.dp))  // 添加垂直间距

            // 心情趋势预览卡片 - 显示心情统计图表
            MoodTrendPreviewCard(
                uiState = statisticsState,  // 统计UI状态
                onRangeChange = statisticsViewModel::updateTimeRange  // 时间范围变化回调
            )
        }

    // 显示周年庆弹窗（如果需要）
    if (uiState.showAnniversaryPopup) {
        Dialog(onDismissRequest = { viewModel.dismissAnniversaryPopup() }) {  // 点击外部关闭弹窗
            Card(  // 使用卡片组件
                modifier = Modifier
                    .fillMaxWidth()  // 填充最大宽度
                    .padding(24.dp),  // 设置内边距
                shape = RoundedCornerShape(16.dp)  // 设置圆角形状
            ) {
                Column(  // 垂直列布局
                    modifier = Modifier.padding(24.dp),  // 设置内边距
                    horizontalAlignment = Alignment.CenterHorizontally  // 水平居中对齐
                ) {
                    // 庆祝图标
                    Icon(
                        imageVector = Icons.Filled.Celebration,  // 庆祝图标
                        contentDescription = null,  // 无内容描述（装饰性图标）
                        modifier = Modifier.size(48.dp),  // 设置图标大小
                        tint = MaterialTheme.colorScheme.primary  // 使用主题主色
                    )

                    Spacer(modifier = Modifier.height(16.dp))  // 添加垂直间距

                    // 周年庆消息文本
                    Text(
                        text = uiState.anniversaryMessage,  // 周年庆消息
                        style = MaterialTheme.typography.bodyLarge,  // 使用大号正文样式
                        textAlign = TextAlign.Center,  // 居中对齐
                        modifier = Modifier.padding(vertical = 8.dp)  // 设置垂直内边距
                    )

                    Spacer(modifier = Modifier.height(24.dp))  // 添加垂直间距

                    // 按钮行
                    Row(
                        modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                        horizontalArrangement = Arrangement.SpaceEvenly  // 按钮均匀分布
                    ) {
                        // 知道了按钮
                        TextButton(onClick = { viewModel.dismissAnniversaryPopup() }) {
                            Text("知道啦")  // 按钮文本
                        }

                        // 写点想说的话按钮
                        Button(onClick = {
                            viewModel.dismissAnniversaryPopup()  // 关闭弹窗
                            viewModel.showOtherMoodDialog()  // 显示其他心情对话框
                        }) {
                            Text("写点想说的话")  // 按钮文本
                        }
                    }
                }
            }
        }
    }

    // 显示其他心情对话框（如果需要）
    if (uiState.showOtherMoodDialog) {
        AlertDialog(  // 显示警告对话框
            onDismissRequest = { viewModel.closeOtherMoodDialog() },  // 点击外部关闭对话框
            title = { Text("想对我说点什么？") },  // 对话框标题
            text = {  // 对话框内容
                Column {  // 垂直列布局
                    // 说明文本
                    Text(
                        text = "写点今天的心情、想对我说的话，只有我会看。",  // 说明文字
                        style = MaterialTheme.typography.bodyMedium,  // 使用中号正文样式
                        color = MaterialTheme.colorScheme.onSurfaceVariant  // 使用表面变体颜色
                    )

                    Spacer(modifier = Modifier.height(16.dp))  // 添加垂直间距

                    // 文本输入框
                    OutlinedTextField(
                        value = uiState.otherMoodText,  // 输入框的当前值
                        onValueChange = { viewModel.updateOtherMoodText(it) },  // 文本变化回调
                        modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                        placeholder = { Text("比如：今天看到你消息的时候，突然很安心……") },  // 占位符文本
                        maxLines = 5  // 最大5行
                    )
                }
            },
            confirmButton = {  // 确认按钮
                TextButton(onClick = {
                    if (uiState.otherMoodText.isNotBlank()) {  // 如果输入不为空
                        viewModel.saveOtherMood(uiState.otherMoodText)  // 保存其他心情
                    }
                }) {
                    Text("保存")  // 保存按钮文本
                }
            },
            dismissButton = {  // 取消按钮
                TextButton(onClick = { viewModel.closeOtherMoodDialog() }) {  // 取消按钮点击事件
                    Text("取消")  // 取消按钮文本
                }
            }
        )
    }

    // 显示心情日历对话框（如果需要）
    if (showCalendarSheet) {
        MoodCalendarDialog(  // 显示心情日历对话框
            onDismiss = { showCalendarSheet = false },  // 关闭日历对话框
            onDateClick = { date ->  // 日期点击回调
                val record = historyRecords.find { it.date == date }  // 查找对应日期的记录
                if (record != null) {  // 如果找到记录
                    selectedHistoryItem = record  // 设置选中的历史记录
                }
            },
            moodRecords = historyRecords  // 传递心情记录数据
        )
    }

    // 显示历史详情底部表单（如果选中了历史记录）
    selectedHistoryItem?.let { record ->  // 如果有选中的记录
        ModalBottomSheet(  // 显示模态底部表单
            sheetState = detailSheetState,  // 底部表单状态
            onDismissRequest = { selectedHistoryItem = null }  // 关闭底部表单
        ) {
            HistoryDetailSheet(  // 历史详情组件
                record = record,  // 传递选中的记录
                onShare = {  // 分享回调
                    ShareHelper(context).shareMoodAsText(  // 使用分享辅助类分享心情
                        date = record.date,  // 日期
                        moodType = MoodType.fromCode(record.moodTypeCode),  // 心情类型
                        moodText = record.moodText,  // 心情文本
                        dayIndex = record.dayIndex  // 恋爱天数
                    )
                },
                onCopy = {  // 复制回调
                    record.moodText?.let { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it)) }  // 复制到剪贴板
                }
            )
        }
    }

}

}

/**
 * TodayHeader 可组合函数 - 今日头部组件
 * 显示应用标题和装饰性分隔线
 */
@Composable
private fun TodayHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {  // 创建垂直列，内容水平居中
        // 显示应用标题文本
        Text(
            text = "恋爱日记",  // 文本内容
            fontSize = 24.sp,  // 字体大小24sp
            lineHeight = 32.sp,  // 行高32sp
            fontWeight = FontWeight.Bold,  // 粗体字
            color = HeaderTextColor  // 使用标题文本颜色
        )
        Spacer(modifier = Modifier.height(8.dp))  // 添加8dp的垂直间距
        // 创建装饰性分隔线
        Box(
            modifier = Modifier
                .width(80.dp)  // 设置宽度为80dp
                .height(1.dp)  // 设置高度为1dp
                .background(AccentPinkText, shape = RoundedCornerShape(50))  // 设置背景色和圆角形状
        )
    }
}

/**
 * TopInfoCardRedesigned 可组合函数 - 顶部信息卡片组件（重新设计版）
 * 显示情侣名称、恋爱天数、起始日期和头像
 * 
 * @param title 标题文本，显示恋爱天数信息
 * @param subtitle 副标题文本，显示日期范围
 * @param avatarUri 头像图片的URI路径
 * @param onAvatarClick 点击头像时的回调函数
 * @param onAiClick 点击AI按钮时的回调函数
 */
@Composable
private fun TopInfoCardRedesigned(
    title: String,  // 标题文本
    subtitle: String,  // 副标题文本
    avatarUri: String?,  // 头像URI，可为空
    onAvatarClick: () -> Unit,  // 点击头像的回调
    onAiClick: () -> Unit  // 点击AI按钮的回调
) {
    Card(  // 创建卡片组件
        modifier = Modifier
            .fillMaxWidth()  // 填充最大宽度
            .height(80.dp),  // 设置高度为80dp
        shape = RoundedCornerShape(16.dp),  // 设置圆角形状，半径16dp
        colors = CardDefaults.cardColors(containerColor = Color.White),  // 设置卡片颜色为白色
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)  // 设置卡片阴影高度为6dp
    ) {
        Row(  // 创建水平行布局
            modifier = Modifier
                .fillMaxSize()  // 填充父容器大小
                .padding(horizontal = 16.dp),  // 设置水平内边距16dp
            horizontalArrangement = Arrangement.SpaceBetween,  // 水平方向两端对齐，元素间等距分布
            verticalAlignment = Alignment.CenterVertically  // 垂直方向居中对齐
        ) {
            Column(  // 左侧信息列
                modifier = Modifier.weight(1f),  // 设置权重为1，占据剩余空间
                verticalArrangement = Arrangement.spacedBy(4.dp)  // 垂直元素间距4dp
            ) {
                // 显示标题文本
                Text(
                    text = title,  // 标题内容
                    fontSize = 18.sp,  // 字体大小18sp
                    fontWeight = FontWeight.Bold,  // 粗体
                    lineHeight = 24.sp,  // 行高24sp
                    color = HeaderTextColor  // 使用标题文本颜色
                )
                // 显示副标题文本
                Text(
                    text = subtitle,  // 副标题内容
                    fontSize = 12.sp,  // 字体大小12sp
                    fontWeight = FontWeight.Light,  // 细体
                    lineHeight = 16.sp,  // 行高16sp
                    color = SubTextColor  // 使用副文本颜色
                )
            }

            Row(  // 右侧按钮行
                horizontalArrangement = Arrangement.spacedBy(12.dp),  // 水平间距12dp
                verticalAlignment = Alignment.CenterVertically  // 垂直居中对齐
            ) {
                // 头像容器
                Box(
                    modifier = Modifier
                        .size(40.dp)  // 设置大小为40dp
                        .clip(CircleShape)  // 裁剪为圆形
                        .background(AccentPinkText.copy(alpha = 0.12f))  // 设置背景色（半透明粉色）
                        .clickable { onAvatarClick() },  // 添加点击事件
                    contentAlignment = Alignment.Center  // 内容居中对齐
                ) {
                    if (avatarUri != null) {  // 如果头像URI不为空
                        // 显示异步加载的头像图片
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)  // 构建图片请求
                                .data(avatarUri)  // 设置图片数据源
                                .crossfade(true)  // 设置淡入淡出过渡效果
                                .build(),  // 构建请求
                            contentDescription = stringResource(id = R.string.home_avatar_desc),  // 内容描述
                            modifier = Modifier.fillMaxSize(),  // 填充容器大小
                            contentScale = ContentScale.Crop  // 裁剪缩放模式
                        )
                    } else {  // 如果头像URI为空，显示默认图标
                        Icon(
                            imageVector = Icons.Filled.Person,  // 人物图标
                            contentDescription = null,  // 无内容描述
                            tint = AccentPinkText  // 使用强调粉色作为图标颜色
                        )
                    }
                }
            }
        }
    }
}

/**
 * MoodRecordSection 可组合函数 - 心情记录区域组件
 * 提供心情选择、图片上传、文字输入和保存功能
 * 
 * @param moodOptions 心情选项列表，包含标签和心情类型
 * @param selectedMood 当前选中的心情类型
 * @param inputText 输入的文本内容
 * @param selectedImageUri 选中的图片URI（可选）
 * @param onMoodSelected 心情选择回调函数
 * @param onInputChange 文本输入变化回调函数
 * @param onPickImage 选择图片回调函数
 * @param onSave 保存回调函数
 */
@OptIn(ExperimentalLayoutApi::class)  // 允许使用实验性布局API
@Composable
private fun MoodRecordSection(
    moodOptions: List<MoodOption>,  // 心情选项列表
    selectedMood: MoodType?,  // 当前选中的心情类型（可为空）
    inputText: String,  // 输入的文本内容
    selectedImageUri: String? = null,  // 选中的图片URI（默认为空）
    onMoodSelected: (MoodType) -> Unit,  // 心情选择回调
    onInputChange: (String) -> Unit,  // 文本输入变化回调
    onPickImage: () -> Unit,  // 选择图片回调
    onSave: (String) -> Unit  // 保存回调
) {
    Card(  // 创建卡片组件
        modifier = Modifier
            .fillMaxWidth(),  // 填充最大宽度
        shape = RoundedCornerShape(12.dp),  // 设置圆角形状，半径12dp
        border = BorderStroke(1.dp, BorderColor),  // 设置边框，1dp宽，边框颜色
        colors = CardDefaults.cardColors(containerColor = Color.White),  // 设置卡片背景为白色
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)  // 设置无阴影
    ) {
        Column(  // 创建垂直列布局
            modifier = Modifier
                .fillMaxWidth()  // 填充最大宽度
                .padding(16.dp),  // 设置内边距16dp
            verticalArrangement = Arrangement.spacedBy(16.dp)  // 垂直元素间距16dp
        ) {
            // 标题文本
            Text(
                text = "今天感觉如何？",  // 标题内容
                style = MaterialTheme.typography.titleMedium,  // 使用中等标题样式
                fontWeight = FontWeight.Bold  // 粗体
            )

            // --- 核心布局：左侧(3行2列) + 右侧(自动填满高度) ---
            Row(  // 创建水平行布局
                modifier = Modifier
                    .fillMaxWidth()  // 填充最大宽度
                    .height(IntrinsicSize.Min),  // 【关键】让左右高度互相对齐
                horizontalArrangement = Arrangement.spacedBy(12.dp)  // 水平间距12dp
            ) {
                // 1. 左侧：心情选择区域 (权重 1.4)
                Column(  // 创建心情选择列
                    modifier = Modifier
                        .weight(1.4f)  // 设置权重1.4，占据更多宽度
                        .fillMaxHeight(),  // 填充最大高度
                    verticalArrangement = Arrangement.SpaceBetween  // 垂直方向两端对齐，使三行心情垂直均匀分布
                ) {
                    // 将 moodOptions (6个) 分成 3组，每组 2个
                    val rows = moodOptions.chunked(2)  // 将心情选项列表按2个一组分组

                    rows.forEachIndexed { index, rowOptions ->  // 遍历分组后的心情选项
                        Row(  // 创建每行的心情选项
                            modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                            horizontalArrangement = Arrangement.SpaceBetween  // 两个心情在行内左右散开
                        ) {
                            // 如果不足2个（防止越界），用Spacer占位，但通常你是6个
                            for (i in 0 until 2) {  // 循环两次，处理每行最多2个心情选项
                                if (i < rowOptions.size) {  // 如果当前索引小于该行选项数量
                                    val option = rowOptions[i]  // 获取当前心情选项
                                    Box(modifier = Modifier.weight(1f)) {  // 给每个MoodTag分配等宽容器
                                        MoodTag(  // 显示心情标签组件
                                            option = option,  // 传递心情选项
                                            selected = selectedMood == option.moodType,  // 判断是否为选中状态
                                            onClick = { onMoodSelected(option.moodType) }  // 点击回调
                                        )
                                    }
                                } else {  // 如果当前行选项不足2个，用Spacer占位
                                    Spacer(modifier = Modifier.weight(1f))  // 等宽占位
                                }
                                // 中间加个间距，除了最后一个
                                if(i == 0) Spacer(modifier = Modifier.width(8.dp))  // 仅在第一个元素后添加间距
                            }
                        }
                        // 行与行之间加间距（除了最后一行）
                        if (index < rows.size - 1) {  // 如果不是最后一行
                            Spacer(modifier = Modifier.height(12.dp))  // 添加12dp垂直间距
                        }
                    }
                }

                // 2. 右侧：上传图片框 (权重 1.0)
                Box(  // 创建图片上传区域容器
                    modifier = Modifier
                        .weight(1f)  // 占据剩余宽度
                        .fillMaxHeight()  // 【关键】高度拉伸，跟随左侧高度
                        .clip(RoundedCornerShape(8.dp))  // 设置圆角
                        .clickable { onPickImage() }  // 添加点击事件
                        .drawBehind {  // 在背景绘制
                            if (selectedImageUri == null) {  // 如果没有选择图片
                                drawRoundRect(  // 绘制圆角矩形
                                    color = BorderColor,  // 边框颜色
                                    cornerRadius = CornerRadius(8.dp.toPx()),  // 圆角半径
                                    style = Stroke(  // 笔划样式
                                        width = 2.dp.toPx(),  // 线条宽度
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))  // 虚线效果
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center  // 内容居中对齐
                ) {
                    if (selectedImageUri != null) {  // 如果已选择图片
                        // 显示图片（如果有Coil库，建议替换为AsyncImage）
                        // 这里暂时用Icon代替已选状态，你可以换成 Image(painter = rememberImagePainter(selectedImageUri)...)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {  // 垂直列，水平居中
                            Icon(  // 显示已选择图标
                                imageVector = Icons.Default.AddPhotoAlternate,  // 或者用 Image 组件
                                contentDescription = "已选择",  // 内容描述
                                tint = PrimaryPink,  // 使用主要粉色
                                modifier = Modifier.size(32.dp)  // 图标大小32dp
                            )
                            Text("已选择一张图片", fontSize = 10.sp, color = PrimaryPink)  // 显示已选择文本
                        }
                    } else {  // 如果未选择图片
                        // 未选择状态
                        Column(  // 垂直列布局
                            horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中
                            verticalArrangement = Arrangement.Center  // 垂直居中
                        ) {
                            Icon(  // 显示添加图片图标
                                imageVector = Icons.Filled.AddPhotoAlternate,  // 图标
                                contentDescription = null,  // 无内容描述
                                tint = NeutralGray,  // 使用中性灰色
                                modifier = Modifier.size(32.dp)  // 图标大小32dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))  // 添加8dp垂直间距
                            Text(  // 显示添加图片文本
                                text = "添加图片",  // 文本内容
                                fontSize = 14.sp,  // 字体大小14sp
                                color = SubtitleGray  // 使用副标题灰色
                            )
                        }
                    }
                }
            }

            // --- 底部：输入框 + 保存按钮 ---
            Row(  // 创建底部水平行
                modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                verticalAlignment = Alignment.CenterVertically,  // 垂直居中对齐
                horizontalArrangement = Arrangement.spacedBy(8.dp)  // 水平间距8dp
            ) {
                // 输入框
                BasicTextField(  // 基础文本输入框
                    value = inputText,  // 输入框的当前值
                    onValueChange = onInputChange,  // 文本变化回调
                    modifier = Modifier
                        .weight(1f)  // 设置权重，占据剩余空间
                        .height(44.dp)  // 设置高度44dp
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))  // 设置边框和圆角
                        .padding(horizontal = 12.dp),  // 设置水平内边距
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),  // 设置文本样式
                    singleLine = true,  // 单行输入
                    decorationBox = { innerTextField ->  // 装饰框
                        Box(contentAlignment = Alignment.CenterStart) {  // 左对齐
                            if (inputText.isBlank()) {  // 如果输入为空
                                Text("写点什么...", color = SubtitleGray, fontSize = 14.sp)  // 显示占位符文本
                            }
                            innerTextField()  // 显示输入框内容
                        }
                    }
                )

                // 保存按钮
                Button(  // 保存按钮
                    onClick = { onSave(inputText) },  // 点击保存
                    enabled = selectedMood != null,  // 没选心情时禁用
                    shape = RoundedCornerShape(8.dp),  // 设置圆角
                    colors = ButtonDefaults.buttonColors(  // 设置按钮颜色
                        containerColor = PrimaryPink,  // 主要粉色背景
                        disabledContainerColor = PrimaryPink.copy(alpha = 0.5f)  // 禁用时半透明
                    ),
                    modifier = Modifier.height(44.dp),  // 设置高度44dp
                    contentPadding = PaddingValues(horizontal = 20.dp)  // 设置水平内边距
                ) {
                    Text("保存", color = Color.White)  // 按钮文本
                }
            }
        }
    }
}

/**
 * MoodTag 可组合函数 - 心情标签组件
 * 显示单个心情选项，包含图标和文字，支持选中状态
 * 
 * @param option 心情选项数据类，包含标签和心情类型
 * @param selected 是否为选中状态
 * @param onClick 点击回调函数
 */
@Composable
private fun MoodTag(
    option: MoodOption,  // 心情选项
    selected: Boolean,  // 是否选中
    onClick: () -> Unit  // 点击回调
) {
    Surface(  // 表面组件，用于创建可点击的表面
        modifier = Modifier
            .width(80.dp)  // 设置宽度80dp
            .height(40.dp)  // 设置高度40dp
            .clickable { onClick() },  // 添加点击事件
        shape = RoundedCornerShape(8.dp),  // 设置圆角形状
        color = Color.Transparent,  // 设置透明背景色
        tonalElevation = 0.dp,  // 设置色调海拔为0
        shadowElevation = 0.dp,  // 设置阴影海拔为0
        border = null  // 无边框
    ) {
        Box(  // 盒子容器
            modifier = Modifier
                .fillMaxSize()  // 填充最大尺寸
                .background(  // 设置背景
                    brush = if (selected) {  // 根据是否选中设置不同背景色
                        Brush.verticalGradient(listOf(MoodSelectedStart, MoodSelectedEnd))  // 选中状态渐变色
                    } else {
                        Brush.verticalGradient(listOf(LightSurfaceColor, LightSurfaceColor))  // 未选中状态浅色
                    },
                    shape = RoundedCornerShape(8.dp)  // 设置圆角形状
                ),
            contentAlignment = Alignment.Center  // 内容居中对齐
        ) {
            Row(  // 水平行布局，包含图标和文字
                verticalAlignment = Alignment.CenterVertically,  // 垂直居中对齐
                horizontalArrangement = Arrangement.spacedBy(4.dp)  // 水平间距4dp
            ) {
                Image(  // 显示心情图标
                    painter = painterResource(id = option.moodType.getDrawableResourceId()),  // 从资源获取图标绘制器
                    contentDescription = option.label,  // 内容描述为心情标签
                    modifier = Modifier.size(16.dp)  // 设置图标大小16dp
                )
                Text(  // 显示心情标签文字
                    text = option.label,  // 标签文字
                    fontSize = 14.sp,  // 字体大小14sp
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,  // 选中时为中等粗细，否则为正常
                    lineHeight = 20.sp,  // 行高20sp
                    color = if (selected) AccentPinkText else ControlTextColor  // 选中时为强调粉色，否则为控制文本色
                )
            }
        }
    }
}

/**
 * DashedUploadBox 可组合函数 - 虚线边框上传框组件
 * 用于显示上传图片的区域，支持显示已选图片或上传提示
 * 
 * @param imageUri 图片URI路径，为空时显示上传提示
 * @param onClick 点击回调函数
 * @param modifier 修饰符，用于自定义组件外观和行为
 */
@Composable
private fun DashedUploadBox(
    imageUri: String?,  // 图片URI路径
    onClick: () -> Unit,  // 点击回调
    modifier: Modifier = Modifier  // 修饰符
) {
    Box(  // 盒子容器
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))  // 裁剪为圆角矩形
            .clickable { onClick() }  // 添加点击事件
            .drawBehind {  // 在背景绘制虚线边框
                drawRoundRect(  // 绘制圆角矩形
                    color = UploadBorderColor,  // 边框颜色
                    cornerRadius = CornerRadius(8.dp.toPx()),  // 圆角半径
                    style = Stroke(  // 笔划样式
                        width = 2.dp.toPx(),  // 线条宽度
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))  // 虚线效果
                    )
                )
            }
            .padding(16.dp),  // 设置内边距
        contentAlignment = Alignment.Center  // 内容居中对齐
    ) {
        if (imageUri != null) {  // 如果已选择图片
            AsyncImage(  // 异步加载并显示图片
                model = ImageRequest.Builder(LocalContext.current)  // 构建图片请求
                    .data(imageUri)  // 设置图片数据
                    .crossfade(true)  // 设置淡入淡出效果
                    .build(),  // 构建请求
                contentDescription = stringResource(id = R.string.home_upload_image_preview),  // 内容描述
                modifier = Modifier.fillMaxSize(),  // 填充容器大小
                contentScale = ContentScale.Crop  // 裁剪缩放模式
            )
        } else {  // 如果未选择图片
            Column(  // 垂直列布局，显示上传提示
                horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中对齐
                verticalArrangement = Arrangement.spacedBy(8.dp)  // 垂直间距8dp
            ) {
                Icon(  // 显示添加图片图标
                    imageVector = Icons.Filled.AddPhotoAlternate,  // 图标向量
                    contentDescription = null,  // 无内容描述
                    tint = SubTextColor,  // 使用副文本颜色
                    modifier = Modifier.size(32.dp)  // 图标大小32dp
                )
                Text(  // 显示上传提示文字
                    text = stringResource(id = R.string.home_upload_image_hint),  // 从资源获取文本
                    fontSize = 12.sp,  // 字体大小12sp
                    fontWeight = FontWeight.Normal,  // 正常粗细
                    lineHeight = 16.sp,  // 行高16sp
                    color = SubTextColor  // 使用副文本颜色
                )
            }
        }
    }
}

/**
 * RecentMoodStatsSection 可组合函数 - 最近心情统计区域组件
 * 显示最近的心情记录、统计数据和心情寄语
 * 
 * @param recentMoods 最近的心情记录列表
 * @param todayMood 今天的心情记录（可选）
 * @param totalRecords 总记录天数
 * @param streak 连续记录天数
 * @param favoriteMood 最喜欢的心情类型（可选）
 * @param moodQuote 心情寄语文本
 * @param onMoreClick 点击"更多"按钮的回调
 * @param onMoodClick 点击心情图标时的回调
 */
@Composable
private fun RecentMoodStatsSection(
    recentMoods: List<DailyMoodEntity>,  // 最近的心情记录列表
    todayMood: DailyMoodEntity?, // 新增参数：今天的心情
    totalRecords: Int,  // 总记录天数
    streak: Int,  // 连续记录天数
    favoriteMood: MoodType?,  // 最喜欢的心情类型
    moodQuote: String,  // 心情寄语文本
    onMoreClick: () -> Unit,  // 点击"更多"的回调
    onMoodClick: (DailyMoodEntity) -> Unit  // 点击心情的回调
) {
    Card(  // 卡片容器
        modifier = Modifier
            .fillMaxWidth()  // 填充最大宽度
            .heightIn(min = 240.dp),  // 最小高度240dp
        shape = RoundedCornerShape(16.dp),  // 圆角形状
        colors = CardDefaults.cardColors(containerColor = Color.White),  // 白色背景
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)  // 阴影高度6dp
    ) {
        Column(  // 垂直列布局
            modifier = Modifier
                .fillMaxWidth()  // 填充最大宽度
                .padding(16.dp),  // 内边距16dp
            verticalArrangement = Arrangement.spacedBy(12.dp)  // 垂直间距12dp
        ) {
            Row(  // 标题行，包含"最近心情"和"更多"按钮
                modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                horizontalArrangement = Arrangement.SpaceBetween,  // 两端对齐
                verticalAlignment = Alignment.CenterVertically  // 垂直居中
            ) {
                Text(  // "最近心情"标题
                    text = "最近心情",  // 文本内容
                    fontSize = 16.sp,  // 字体大小16sp
                    fontWeight = FontWeight.Bold,  // 粗体
                    lineHeight = 22.sp,  // 行高22sp
                    color = HeaderTextColor  // 标题文本颜色
                )
                Text(  // "更多"按钮
                    text = "更多",  // 文本内容
                    fontSize = 12.sp,  // 字体大小12sp
                    fontWeight = FontWeight.Medium,  // 中等粗细
                    lineHeight = 16.sp,  // 行高16sp
                    color = AccentPinkText,  // 强调粉色
                    modifier = Modifier.clickable { onMoreClick() }  // 添加点击事件
                )
            }

            MoodIconRow(  // 显示心情图标行
                recentMoods = recentMoods,  // 传递最近心情记录
                todayMood = todayMood,  // 传递今天的心情
                onMoodClick = onMoodClick  // 传递点击回调
            )

            Row(  // 统计数据行
                modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                horizontalArrangement = Arrangement.spacedBy(12.dp)  // 水平间距12dp
            ) {
                StatItem(title = "已经记录", value = totalRecords.toString(), unit = "天")  // 总记录天数统计
                StatItem(title = "连续记录", value = streak.toString(), unit = "天")  // 连续记录天数统计
                // 隐藏最近x天的卡片
//                StatItem(
//                    title = "最近30天常见心情",
//                    value = favoriteMood?.displayName ?: "-",
//                    unit = null,
//                    highlight = true
//                )
            }

            Column(  // 心情寄语区域
                verticalArrangement = Arrangement.spacedBy(12.dp)  // 垂直间距12dp
            ) {
                Text(  // "心情寄语"标题
                    text = "心情寄语",  // 文本内容
                    fontSize = 16.sp,  // 字体大小16sp
                    fontWeight = FontWeight.Bold,  // 粗体
                    lineHeight = 22.sp,  // 行高22sp
                    color = HeaderTextColor  // 标题文本颜色
                )
                Text(  // 心情寄语文本
                    text = moodQuote,  // 寄语内容
                    fontSize = 14.sp,  // 字体大小14sp
                    fontWeight = FontWeight.Normal,  // 正常粗细
                    lineHeight = 20.sp,  // 行高20sp
                    color = ControlTextColor  // 控制文本颜色
                )
            }
        }
    }
}

/**
 * MoodTrendPreviewCard 可组合函数 - 心情趋势预览卡片组件
 * 显示心情统计数据的趋势图表和时间范围选择
 * 
 * @param uiState 统计UI状态，包含趋势数据和加载状态
 * @param onRangeChange 时间范围变化回调函数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodTrendPreviewCard(
    uiState: StatisticsViewModel.StatisticsUiState,  // 统计UI状态
    onRangeChange: (Int) -> Unit  // 时间范围变化回调
) {
    Card(  // 卡片容器
        modifier = Modifier
            .fillMaxWidth()  // 填充最大宽度
            .heightIn(min = 220.dp),  // 最小高度220dp
        shape = RoundedCornerShape(16.dp),  // 圆角形状
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),  // 表面颜色
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)  // 阴影高度6dp
    ) {
        Column(  // 垂直列布局
            modifier = Modifier
                .fillMaxWidth()  // 填充最大宽度
                .padding(16.dp),  // 内边距16dp
            verticalArrangement = Arrangement.spacedBy(12.dp)  // 垂直间距12dp
        ) {
            Row(  // 标题和筛选行
                modifier = Modifier.fillMaxWidth(),  // 填充最大宽度
                horizontalArrangement = Arrangement.SpaceBetween,  // 两端对齐
                verticalAlignment = Alignment.CenterVertically  // 垂直居中
            ) {
                Text(  // 趋势标题
                    text = stringResource(R.string.home_mood_trend_title, uiState.selectedDays),  // 从资源获取标题文本并插入天数
                    fontSize = 16.sp,  // 字体大小16sp
                    fontWeight = FontWeight.Bold,  // 粗体
                    lineHeight = 22.sp,  // 行高22sp
                    color = HeaderTextColor  // 标题文本颜色
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {  // 时间范围筛选按钮行
                    MoodTrendRangeOptions.forEach { (days, labelRes) ->  // 遍历时间范围选项
                        key(days) {  // 为每个选项设置唯一键值
                            FilterChip(  // 筛选芯片组件
                                selected = uiState.selectedDays == days,  // 判断是否选中
                                onClick = { onRangeChange(days) },  // 点击回调，改变时间范围
                                label = { Text(stringResource(labelRes)) }  // 显示标签文本
                            )
                        }
                    }
                }
            }

            when (uiState.contentState) {  // 根据内容状态显示不同内容
                StatisticsViewModel.ContentState.LOADING -> {  // 加载状态
                    Text(  // 显示加载文本
                        text = stringResource(R.string.home_mood_trend_loading),  // 从资源获取加载文本
                        fontSize = 14.sp,  // 字体大小14sp
                        fontWeight = FontWeight.Normal,  // 正常粗细
                        lineHeight = 20.sp,  // 行高20sp
                        color = SubtitleGray  // 副标题灰色
                    )
                }

                StatisticsViewModel.ContentState.CONTENT -> {  // 有内容状态
                    if (uiState.moodTrend.isNotEmpty()) {  // 如果趋势数据不为空
                        SimpleTrendChart(trendData = uiState.moodTrend)  // 显示趋势图表
                    } else {  // 如果趋势数据为空
                        Text(  // 显示无数据文本
                            text = stringResource(R.string.home_mood_trend_no_data),  // 从资源获取无数据文本
                            fontSize = 14.sp,  // 字体大小14sp
                            fontWeight = FontWeight.Normal,  // 正常粗细
                            lineHeight = 20.sp,  // 行高20sp
                            color = SubtitleGray  // 副标题灰色
                        )
                    }
                }

                StatisticsViewModel.ContentState.EMPTY -> {  // 空状态
                    Text(  // 显示空状态文本
                        text = stringResource(R.string.home_mood_trend_empty),  // 从资源获取空状态文本
                        fontSize = 14.sp,  // 字体大小14sp
                        fontWeight = FontWeight.Normal,  // 正常粗细
                        lineHeight = 20.sp,  // 行高20sp
                        color = SubtitleGray  // 副标题灰色
                    )
                }

                StatisticsViewModel.ContentState.ERROR -> {  // 错误状态
                    Text(  // 显示错误文本
                        text = uiState.errorMessage ?: stringResource(R.string.home_mood_trend_error_default),  // 显示错误消息或默认错误文本
                        fontSize = 14.sp,  // 字体大小14sp
                        fontWeight = FontWeight.Normal,  // 正常粗细
                        lineHeight = 20.sp,  // 行高20sp
                        color = AccentRed  // 强调红色
                    )
                }
            }
        }
    }
}

/**
 * StatItem 可组合函数 - 统计项组件
 * 显示统计数据项，包含值和标题
 * 
 * @param title 标题文本
 * @param value 值文本
 * @param unit 单位文本（可选）
 * @param highlight 是否高亮显示
 */
@Composable
private fun StatItem(
    title: String,  // 标题文本
    value: String,  // 值文本
    unit: String?,  // 单位文本（可选）
    highlight: Boolean = false  // 是否高亮显示，默认为false
) {
    Column(  // 垂直列布局
        verticalArrangement = Arrangement.spacedBy(4.dp)  // 垂直间距4dp
    ) {
        val valueText = unit?.let { "$value$it" } ?: value  // 如果单位不为空则拼接值和单位，否则只显示值
        val valueColor = if (highlight) AccentPinkText else ControlTextColor  // 根据高亮状态选择颜色
        val valueSize = if (highlight) 18.sp else 16.sp  // 根据高亮状态选择字体大小
        val valueWeight = if (highlight) FontWeight.Bold else FontWeight.Normal  // 根据高亮状态选择字体粗细

        Text(  // 显示数值文本
            text = valueText,  // 值文本（可能包含单位）
            fontSize = valueSize,  // 字体大小
            fontWeight = valueWeight,  // 字体粗细
            lineHeight = if (highlight) 24.sp else 22.sp,  // 行高
            color = valueColor  // 文本颜色
        )
        Text(  // 显示标题文本
            text = title,  // 标题文本
            fontSize = 12.sp,  // 字体大小12sp
            fontWeight = FontWeight.Light,  // 细体
            lineHeight = 16.sp,  // 行高16sp
            color = SubTextColor  // 副文本颜色
        )
    }
}

/**
 * MoodIconRow 可组合函数 - 心情图标行组件
 * 显示最近的心情图标，最多显示10个（如果有今天的心情则显示9个历史+1个今天）
 * 
 * @param recentMoods 最近的心情记录列表
 * @param todayMood 今天的心情记录（可选）
 * @param onMoodClick 点击心情图标时的回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodIconRow(
    recentMoods: List<DailyMoodEntity>,  // 最近的心情记录列表
    todayMood: DailyMoodEntity?, // 新增参数：今天的心情
    onMoodClick: (DailyMoodEntity) -> Unit  // 点击心情的回调
) {
    if (recentMoods.isEmpty() && todayMood == null) {
        Text(
            text = "还没有心情记录，去写下第一条吧",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = SubtitleGray
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 计算可用空间和每个图标所需空间，以确定最多能显示多少个图标
            // 每个图标36dp + 4dp间距，减去今天图标的额外空间
            val hasTodayMood = todayMood != null
            val maxIcons = if (hasTodayMood) 9 else 10 // 如果有今天的心情，最多显示9个历史心情
            val recentMoodsToShow = recentMoods.take(maxIcons)
            
            recentMoodsToShow.forEach { mood ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPinkText.copy(alpha = 0.08f))
                        .clickable { onMoodClick(mood) },
                    contentAlignment = Alignment.Center
                ) {
                    val moodType = MoodType.fromCode(mood.moodTypeCode)
                    Image(
                        painter = painterResource(id = moodType.getDrawableResourceId()),
                        contentDescription = moodType.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 确保今天的心情始终显示在最右边，无论是否已设置
            if (todayMood != null) {
                // 如果今天有心情，则显示具体的心情
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPinkText.copy(alpha = 0.08f))
                        .clickable { onMoodClick(todayMood) },
                    contentAlignment = Alignment.Center
                ) {
                    val moodType = MoodType.fromCode(todayMood.moodTypeCode)
                    Image(
                        painter = painterResource(id = moodType.getDrawableResourceId()),
                        contentDescription = moodType.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // 如果今天没有心情，则显示一个空的占位框
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            BorderStroke(1.dp, AccentPinkText.copy(alpha = 0.12f)),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 空白，无内容
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    dateDisplay: String
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        contentPadding = PaddingValues(Dimens.CardPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            // Avatar row with couple names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left avatar
                AvatarPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "用户头像"
                    }
                )
                
                // Center content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Day ${uiState.dayIndex}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.coupleName?.let { "与 $it 的第 ${uiState.dayIndex} 天" } 
                            ?: "Days Together",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.dayIndex % 100 == 0 && uiState.dayIndex > 0) {
                        StatusBadge(
                            text = "🎉 里程碑",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Right avatar
                AvatarPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "伴侣头像"
                    }
                )
            }
            
            // Date display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = ShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            )
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MoodNoteViewer(
    note: String?,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Field)
            .clickable { onEdit() },
        shape = ShapeTokens.Field,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "今天的心情描述",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = note?.takeIf { it.isNotBlank() } ?: "点击添加一些描述吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("修改")
                }
            }
        }
    }
}

@Composable
private fun TodayOverviewBar(
    dateDisplay: String,
    streak: Int
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        contentPadding = PaddingValues(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateDisplay,
                style = MaterialTheme.typography.titleMedium
            )

            StatusBadge(
                text = "🔥 ${streak} 天",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodTimelineCard(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    onMoodSelected: (MoodType) -> Unit,
    onRecentMoodClick: (DailyMoodEntity) -> Unit,
    onExpandCalendar: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveNote: (String) -> Unit,
    onEnterEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val noteText = uiState.otherMoodText.ifBlank { uiState.todayMoodText.orEmpty() }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            SectionHeader(
                title = "今天感觉如何？",
                subtitle = "点击表情即可切换心情"
            )

            MoodSelectorRow(
                selectedMood = uiState.todayMood,
                onMoodSelected = onMoodSelected
            )

            MoodPromptText(selectedMood = uiState.todayMood)

            val shouldShowEditor = shouldShowDescriptionEditor(uiState)

            if (shouldShowEditor) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    MoodNoteInput(
                        note = noteText,
                        onNoteChange = onNoteChange,
                        onSave = { onSaveNote(noteText) },
                        onCancel = if (!uiState.todayMoodText.isNullOrBlank()) onCancelEdit else null,
                        isSaveEnabled = uiState.todayMood != null,
                        errorMessage = uiState.descriptionError
                    )
                }
            } else {
                MoodNoteViewer(
                    note = uiState.todayMoodText,
                    onEdit = onEnterEdit
                )
            }
            
            // Photo upload placeholder
            if (uiState.todayMood != null) {
                PhotoUploadPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "上传今天的照片"
                    },
                    onClick = {
                        // TODO: Implement photo picker
                    }
                )
            }

            RecentMoodsList(
                recentMoods = uiState.recentTenMoods,
                onMoodClick = onRecentMoodClick,
                onMoreClick = onExpandCalendar
            )
        }
    }
}

private fun shouldShowDescriptionEditor(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState
): Boolean {
    return uiState.isDescriptionEditing ||
        (uiState.todayMood != null && uiState.todayMoodText.isNullOrBlank())
}

@Composable
private fun TodayMoodDisplay(
    mood: MoodType,
    moodText: String?,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = mood.emoji, style = MaterialTheme.typography.displaySmall)
                Column {
                    Text(
                        text = mood.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "今天的记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }

        if (!moodText.isNullOrBlank()) {
            Text(
                text = moodText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodSelectorRow(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit
) {
    // Use FlowRow to wrap moods on smaller screens
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        MoodType.values().forEach { mood ->
            MoodButton(
                mood = mood,
                isSelected = selectedMood == mood,
                onClick = { onMoodSelected(mood) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoodNoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)? = null,
    isSaveEnabled: Boolean = true,
    errorMessage: String? = null,
    saveLabel: String = "保存记录"
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.Field,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "今天的心情描述（可选）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
                placeholder = { Text("写下一句话，直接保存在今天的记录里") },
                shape = ShapeTokens.Field
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                onCancel?.let {
                    TextButton(onClick = it) {
                        Text("取消")
                    }
                }
                TextButton(
                    onClick = onSave,
                    enabled = isSaveEnabled
                ) {
                    Text(saveLabel)
                }
            }
        }
    }
}

@Composable
private fun MoodPromptText(selectedMood: MoodType?) {
    val prompt = selectedMood?.feedbackText ?: "选一个心情，记录今天的状态"

    Text(
        text = prompt,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentMoodsList(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        SectionHeader(title = "最近心情")

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimens.SectionSpacing, vertical = Dimens.SectionSpacing)
        ) {
            if (recentMoods.isEmpty()) {
                Text(
                    text = "还没有心情记录，去写下第一条吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecentMoodIconsRow(
                        recentMoods = recentMoods,
                        onMoodClick = onMoodClick,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SectionSpacing))

                    MoreMoodsButton(
                        onClick = onMoreClick,
                        modifier = Modifier.width(88.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMoodIconsRow(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val iconSize = 36.dp
        val iconSpacing = 8.dp
        val targetCount = minOf(RecentMoodIconTargetCount, recentMoods.size)
        val availablePx = with(density) { maxWidth.toPx() }
        val iconPx = with(density) { iconSize.toPx() }
        val spacingPx = with(density) { iconSpacing.toPx() }
        val maxIconsFit = if (targetCount == 0) {
            0
        } else {
            // Calculate how many fixed-size icon slots fit into the single available row (no wrap)
            ((availablePx + spacingPx) / (iconPx + spacingPx)).toInt().coerceIn(1, targetCount)
        }
        val moodsToShow = recentMoods.take(maxIconsFit)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(iconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            moodsToShow.forEach { moodRecord ->
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onMoodClick(moodRecord) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMoodListItem(
    moodRecord: DailyMoodEntity,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    val date = runCatching { LocalDate.parse(moodRecord.date) }.getOrNull()
    val formattedDate = date?.format(dateFormatter) ?: moodRecord.date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = MoodType.fromCode(moodRecord.moodTypeCode).displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun MoreMoodsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 40.dp),
        shape = ShapeTokens.Field,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = Dimens.SectionSpacing, vertical = Dimens.SectionSpacing / 2),
        interactionSource = interactionSource
    ) {
        Text(
            text = "更多",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayMoodSection(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    hasTodayMood: Boolean,
    commonMoods: List<MoodType>,
    showFullMoodGrid: Boolean,
    onMoreToggle: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今天的心情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (hasTodayMood && uiState.todayMood != null) {
            TodayMoodCard(
                mood = uiState.todayMood,
                moodText = uiState.todayMoodText,
                onEdit = onEdit,
                onShare = onShare
            )
        } else {
            Text(
                text = "选择今天的心情吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonMoods.forEach { mood ->
                    ElevatedCard(
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = mood.emoji, style = MaterialTheme.typography.titleLarge)
                            Text(text = mood.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedCard(
                    onClick = onMoreToggle,
                    modifier = Modifier
                        .width(88.dp)
                        .height(72.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "更多", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(visible = showFullMoodGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = MoodGridMaxHeight),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(MoodType.values()) { mood ->
                        MoodButton(
                            mood = mood,
                            isSelected = uiState.todayMood == mood,
                            onClick = { onMoodSelected(mood) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayMoodCard(
    mood: MoodType,
    moodText: String?,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = mood.emoji, style = MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(text = mood.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "今天的记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (!moodText.isNullOrBlank()) {
                Text(
                    text = moodText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(mood: MoodType) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = mood.feedbackText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailSheet(
    record: DailyMoodEntity,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "记录详情", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = MoodType.fromCode(record.moodTypeCode).emoji, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(text = record.date, style = MaterialTheme.typography.titleMedium)
                Text(text = MoodType.fromCode(record.moodTypeCode).displayName, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (!record.moodText.isNullOrBlank()) {
            OutlinedTextField(
                value = record.moodText ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("一句话") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("分享")
            }

            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = onCopy
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制文本")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MoodStatisticsPreviewSection(
    uiState: StatisticsViewModel.StatisticsUiState,
    onRangeChange: (Int) -> Unit,
    onExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "心情统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onExpand) { Text("更多统计") }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 30, 90, 365).forEach { days ->
                FilterChip(
                    selected = uiState.selectedDays == days,
                    onClick = { onRangeChange(days) },
                    label = {
                        Text(
                            when (days) {
                                7 -> "最近7天"
                                30 -> "最近30天"
                                90 -> "最近90天"
                                else -> "全年"
                            }
                        )
                    }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = StatsGridMinHeight, max = StatsGridMaxHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(
                listOf(
                    "记录天数" to uiState.totalRecords.toString(),
                    "平均心情" to uiState.averageMood,
                    "最常心情" to (uiState.topMood?.displayName ?: "-"),
                    "统计范围" to "最近${uiState.selectedDays}天"
                )
            ) { (title, value) ->
                StatPreviewCard(
                    title = title,
                    value = value
                )
            }
        }
    }
}

@Composable
private fun StatPreviewCard(
    title: String,
    value: String
) {
    val shape = RoundedCornerShape(12.dp)
    ElevatedCard(
        modifier = Modifier
            .heightIn(min = 140.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Text(
                text = "默认展开，数据实时更新",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodCalendarDialog(
    onDismiss: () -> Unit,
    onDateClick: (String) -> Unit,
    moodRecords: List<DailyMoodEntity>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("本月", "年历")

    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(today) }

    // Create a map of date -> mood for quick lookup
    val moodMap = remember(moodRecords) {
        moodRecords.associateBy { it.date }
    }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(top = 64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 360.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "心情日历",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭日历"
                            )
                        }
                    }

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            FilterChip(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                label = { Text(tab) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> MonthCalendarView(
                            currentMonth = currentMonth,
                            onMonthChange = { currentMonth = it },
                            moodMap = moodMap,
                            onDateClick = onDateClick,
                            today = today
                        )
                        1 -> YearCalendarView(
                            year = today.year,
                            moodMap = moodMap,
                            onMonthClick = { month ->
                                currentMonth = LocalDate.of(today.year, month, 1)
                                selectedTab = 0
                            },
                            today = today
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    moodMap: Map<String, DailyMoodEntity>,
    onDateClick: (String) -> Unit,
    today: LocalDate
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月") }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Text("< 上月")
            }
            Text(
                text = currentMonth.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Text("下月 >")
            }
        }

        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid (7x6)
        val firstDayOfMonth = currentMonth.withDayOfMonth(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(48.dp))
            }
            
            // Days of the month
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = currentMonth.withDayOfMonth(day)
                val dateStr = date.toString()
                val moodRecord = moodMap[dateStr]
                val isToday = date == today
                
                CalendarDayCell(
                    day = day,
                    moodRecord = moodRecord,
                    isToday = isToday,
                    onClick = {
                        if (moodRecord != null) {
                            onDateClick(dateStr)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun YearCalendarView(
    year: Int,
    moodMap: Map<String, DailyMoodEntity>,
    onMonthClick: (Int) -> Unit,
    today: LocalDate
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${year}年",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { monthIndex ->
                val month = monthIndex + 1
                MiniMonthGrid(
                    year = year,
                    month = month,
                    moodMap = moodMap,
                    onClick = { onMonthClick(month) },
                    today = today
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDayCell(
    day: Int,
    moodRecord: DailyMoodEntity?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        moodRecord != null -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (moodRecord != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                        fontSize = 16.sp
                    )
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MiniMonthGrid(
    year: Int,
    month: Int,
    moodMap: Map<String, DailyMoodEntity>,
    onClick: () -> Unit,
    today: LocalDate
) {
    val monthName = remember(month) {
        val date = LocalDate.of(year, month, 1)
        date.format(DateTimeFormatter.ofPattern("M月"))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Mini grid showing mood icons
            val firstDay = LocalDate.of(year, month, 1)
            val daysInMonth = firstDay.lengthOfMonth()
            val firstDayOfWeek = firstDay.dayOfWeek.value % 7

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Empty cells
                repeat(firstDayOfWeek) {
                    Box(modifier = Modifier.size(12.dp))
                }

                // Days
                repeat(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = LocalDate.of(year, month, day)
                    val dateStr = date.toString()
                    val moodRecord = moodMap[dateStr]

                    Box(
                        modifier = Modifier.size(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (moodRecord != null) {
                            // Use colored circle instead of tiny emoji for better accessibility
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when (MoodType.fromCode(moodRecord.moodTypeCode)) {
                                            MoodType.HAPPY -> MaterialTheme.colorScheme.primary
                                            MoodType.SATISFIED -> MaterialTheme.colorScheme.tertiary
                                            MoodType.NORMAL -> MaterialTheme.colorScheme.secondary
                                            MoodType.SAD -> MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.7f
                                            )

                                            MoodType.ANGRY -> MaterialTheme.colorScheme.error
                                            MoodType.OTHER -> MaterialTheme.colorScheme.outline
                                        },
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodSelectionSheet(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "改一下心情",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MoodType.values()) { mood ->
                MoodButton(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = when {
        isPressed -> 0.98f
        isSelected -> 1.2f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 140),
        label = "mood_button_scale"
    )

    Card(
        modifier = modifier
            .height(76.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription =
                    "心情-${mood.displayName}${if (isSelected) "，已选择" else ""}"
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 28.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoUploadPlaceholder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(ShapeTokens.Field)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = ShapeTokens.Field
            )
            .clickable { onClick() }
            .semantics {
                contentDescription = "添加照片"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "添加照片记录这一刻",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击上传",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MoodQuoteCard(
    selectedMood: MoodType?,
    modifier: Modifier = Modifier
) {
    if (selectedMood != null) {
        AppCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding),
            contentPadding = PaddingValues(Dimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "心情寄语",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMood.feedbackText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    totalRecords: Int,
    continuousRecords: Int,
    favoriteMood: MoodType?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        SectionHeader(
            title = "记录统计",
            subtitle = "你的心情变化"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            StatsCard(
                title = "总记录",
                value = "$totalRecords 天",
                icon = "📊",
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "连续打卡",
                value = "$continuousRecords 天",
                icon = "🔥",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            // 隐藏最近x天的卡片
            // StatsCard(
            //     title = "最近30天",
            //     value = favoriteMood?.displayName ?: "-",
            //     icon = favoriteMood?.emoji ?: "💭",
            //     subtitle = "最常心情",
            //     modifier = Modifier.weight(1f)
            // )
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        shape = ShapeTokens.Card,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
