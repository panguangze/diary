package com.love.diary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.love.diary.navigation.Screen
import com.love.diary.presentation.screens.home.HomeScreen
import com.love.diary.presentation.screens.settings.SettingsScreen
import com.love.diary.presentation.screens.setup.FirstRunScreen
import com.love.diary.presentation.viewmodel.HomeViewModel
import com.love.diary.presentation.screens.CheckInDashboardScreen
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.ui.theme.LoveDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.love.diary.data.backup.DataBackupManager
import com.love.diary.data.repository.AppRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var backupManager: DataBackupManager
    
    private lateinit var importLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化导入文件选择器
        importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 这里可以处理导入结果，如果需要的话
        }

        setContent {
            MainApp(backupManager = backupManager)
        }
    }
}

@Composable
fun MainApp(backupManager: DataBackupManager) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    // 检查是否需要首次运行设置
    var isFirstRun by remember { mutableStateOf(true) } // 将在LaunchedEffect中更新
    var isLoading by remember { mutableStateOf(true) }   // 将在LaunchedEffect中更新
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val repository = homeViewModel.repository // 获取repository实例

    LaunchedEffect(Unit) {
        // 检查是否是首次运行
        val firstRun = homeViewModel.isFirstRun()
        isFirstRun = firstRun
        
        isLoading = false
    }
    
    // Always use light theme (Requirement 4: remove theme selection)
    LoveDiaryTheme(darkTheme = false) {
        MainAppContent(
            isLoading = isLoading,
            isFirstRun = isFirstRun,
            repository = repository,
            backupManager = backupManager,
            onSetupComplete = { isFirstRun = false },
            onNavigateToFirstRun = { isFirstRun = true },
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
    backupManager: DataBackupManager,
    onSetupComplete: () -> Unit,
    onNavigateToFirstRun: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: androidx.navigation.NavHostController
) {
    // Get current route for back button handling
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
    val isOnHomeScreen = currentRoute?.destination?.route == Screen.Home.route
    
    // State for double-back-to-exit
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = navController.context
    val coroutineScope = rememberCoroutineScope()
    
    // Handle back button press - but not during first run
    BackHandler(enabled = !isFirstRun) {
        if (isOnHomeScreen) {
            // If on Home screen, use double-press to exit
            if (backPressedOnce) {
                // Second press - exit app
                (context as? ComponentActivity)?.finish()
            } else {
                // First press - show message and set flag
                backPressedOnce = true
                android.widget.Toast.makeText(
                    context,
                    "再按一次退出应用",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Reset flag after 2 seconds
                coroutineScope.launch {
                    kotlinx.coroutines.delay(2000)
                    backPressedOnce = false
                }
            }
        } else {
            // If not on Home screen, navigate to Home
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
            onTabSelected(0) // Update selected tab to Home
        }
    }
    
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
            backupManager = backupManager,
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
                    AppScaffold(
                        title = "恋爱日记",
                        showTopBar = false,
                        backgroundBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFAFAFC), Color(0xFFF5F5F8))
                        )
                    ) { inner ->
                        HomeScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(inner)
                        )
                    }
                }

                composable(Screen.Habits.route) {
                    CheckInDashboardScreen()
                }

                composable(Screen.Settings.route) {
                    AppScaffold(title = "设置") { inner ->
                        SettingsScreen(
                            onNavigateToFirstRun = onNavigateToFirstRun,
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
    val unselectedColor = Color(0xFF999999)
    val items = listOf(
        Screen.Home,
        Screen.Habits,
        Screen.Settings
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.height(56.dp)
    ) {
        items.forEachIndexed { index, screen ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        tint = if (isSelected) Color(0xFFFF6B81) else unselectedColor,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent,
                    selectedTextColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color.Transparent
                ),
                alwaysShowLabel = false
            )
        }
    }
}
