package com.love.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.love.diary.navigation.Screen
import com.love.diary.presentation.screens.home.HomeScreen
import com.love.diary.presentation.screens.settings.SettingsScreen
import com.love.diary.presentation.screens.setup.FirstRunScreen
import com.love.diary.presentation.viewmodel.HomeViewModel
import com.love.diary.habit.HabitListScreen
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens
import com.love.diary.ui.theme.LoveDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.love.diary.data.repository.AppRepository

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var importLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化导入文件选择器
        importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 这里可以处理导入结果，如果需要的话
        }

        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    // 检查是否需要首次运行设置
    var isFirstRun by remember { mutableStateOf(true) } // 将在LaunchedEffect中更新
    var isLoading by remember { mutableStateOf(true) }   // 将在LaunchedEffect中更新
    var darkMode by remember { mutableStateOf<Boolean?>(null) } // Dark mode setting
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val repository = homeViewModel.repository // 获取repository实例

    LaunchedEffect(Unit) {
        // 检查是否是首次运行
        val firstRun = homeViewModel.isFirstRun()
        isFirstRun = firstRun
        
        // Load dark mode setting
        val config = repository.getAppConfig()
        darkMode = config?.darkMode
        
        isLoading = false
    }
    
    // Observe config changes for dark mode
    LaunchedEffect(Unit) {
        repository.getAppConfigFlow().collect { config ->
            darkMode = config?.darkMode
        }
    }
    
    // Apply theme based on dark mode setting
    LoveDiaryTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
        MainAppContent(
            isLoading = isLoading,
            isFirstRun = isFirstRun,
            repository = repository,
            onSetupComplete = { isFirstRun = false },
            selectedTab = selectedTab,
            onTabSelected = { index ->
                selectedTab = index
                when (index) {
                    0 -> navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    1 -> navController.navigate(Screen.Habits.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    2 -> navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            navController = navController
        )
    }
}

@Composable
fun MainAppContent(
    isLoading: Boolean,
    isFirstRun: Boolean,
    repository: AppRepository,
    onSetupComplete: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: androidx.navigation.NavHostController
) {
    if (isLoading) {
        // 加载中显示
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (isFirstRun) {
        // 首次运行显示设置页
        FirstRunScreen(
            repository = repository,
            onSetupComplete = onSetupComplete,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 正常显示应用
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    AppScaffold(title = "恋爱日记") { inner ->
                        HomeScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(inner)
                        )
                    }
                }

                composable(Screen.Habits.route) {
                    AppScaffold(title = "打卡") { inner ->
                        HabitListScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(inner)
                        )
                    }
                }

                composable(Screen.Settings.route) {
                    AppScaffold(title = "设置") { inner ->
                        SettingsScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(inner)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val primaryPink = Color(0xFFFF557F)
    val items = listOf(
        Screen.Home,
        Screen.Habits,
        Screen.Settings
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, screen ->
                NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    if (selectedTab == index) {
                        Icon(screen.selectedIcon, contentDescription = "${screen.title}，已选中")
                    } else {
                        Icon(screen.unselectedIcon, contentDescription = "${screen.title}，未选中")
                    }
                },
                label = { Text(screen.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryPink,
                    selectedTextColor = primaryPink,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
