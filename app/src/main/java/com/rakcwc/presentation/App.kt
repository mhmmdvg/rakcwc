package com.rakcwc.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.rakcwc.data.remote.local.TokenManager
import com.rakcwc.presentation.ui.components.AppBar
import com.rakcwc.presentation.ui.components.BottomNavigation
import com.rakcwc.presentation.ui.components.NavigationTitle
import com.rakcwc.presentation.ui.screens.authentication.AuthScreen
import com.rakcwc.presentation.ui.screens.home.HomeScreen
import com.rakcwc.presentation.ui.screens.home.HomeViewModel
import com.rakcwc.presentation.ui.screens.products.ProductsScreen
import com.rakcwc.presentation.ui.screens.products.ProductsViewModel
import com.rakcwc.presentation.ui.screens.search.SearchScreen
import com.rakcwc.presentation.ui.screens.search.SearchViewModel
import com.rakcwc.presentation.ui.screens.settings.SettingScreen
import com.rakcwc.presentation.ui.screens.splash.SplashScreen

@Composable
fun App(
    tokenManager: TokenManager,
    startDestination: String,
) {
    val navController = rememberNavController()
    var scrollOffset by remember { mutableIntStateOf(0) }
    var productsScreenTitle by remember { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route
    val maxOffset = 200

    val shouldBottomNav = when (currentRoute) {
        Screen.Authentication.route, Screen.Splash.route -> false
        null -> false
        else -> !currentRoute.startsWith(Screen.Home.route + "/")
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            when (currentRoute) {
                Screen.Home.route -> {
                    AppBar(
                        title = "Home",
                        scrollOffset = scrollOffset,
                        maxOffset = maxOffset
                    )
                }

                Screen.Search.route -> {
                    AppBar(
                        title = "Search",
                        scrollOffset = scrollOffset,
                        maxOffset = maxOffset
                    )
                }

                Screen.CatalogDetail.route -> {
                    AppBar(
                        title = productsScreenTitle,
                        scrollOffset = scrollOffset,
                        maxOffset = maxOffset
                    )
                }

                Screen.Setting.route -> {
                    AppBar(
                        title = "Setting",
                        scrollOffset = 200,
                        maxOffset = maxOffset
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = shouldBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavigation(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                ),
            enterTransition = {
                when {
                    (initialState.destination.route == Screen.Authentication.route &&
                            targetState.destination.route == Screen.Home.route) -> {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(durationMillis = 400)
                        ) + fadeIn(animationSpec = tween(400))
                    }

                    (initialState.destination.route == Screen.Home.route &&
                            targetState.destination.route == Screen.Authentication.route) -> {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(durationMillis = 400)
                        ) + fadeIn(animationSpec = tween(400))
                    }

                    targetState.destination.route == Screen.Home.route || targetState.destination.route == Screen.Search.route || targetState.destination.route == Screen.Setting.route -> fadeIn(
                        animationSpec = tween(300)
                    )

                    else -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(350)
                    )
                }
            },
            exitTransition = {
                when {
                    (initialState.destination.route == Screen.Authentication.route &&
                            targetState.destination.route == Screen.Home.route) -> {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(durationMillis = 400)
                        ) + fadeOut(animationSpec = tween(400))
                    }

                    (initialState.destination.route == Screen.Home.route &&
                            targetState.destination.route == Screen.Authentication.route) -> {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(durationMillis = 400)
                        ) + fadeOut(animationSpec = tween(400))
                    }

                    targetState.destination.route == Screen.Home.route || targetState.destination.route == Screen.Search.route || targetState.destination.route == Screen.Setting.route -> fadeOut(
                        animationSpec = tween(300)
                    )

                    else -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(350)
                    )
                }
            },
            popEnterTransition = {
                // Reverse of exitTransition - slide in from left
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            },
            popExitTransition = {
                // Reverse of enterTransition - slide out to right
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        ) {
            composable("splash") {
                SplashScreen(
                    tokenManager = tokenManager,
                    navController = navController
                )
            }

            composable("authentication") {
                AuthScreen(
                    navController = navController,
                )
            }

            navigation(
                startDestination = Screen.Home.route,
                route = Screen.BottomNav.route
            ) {
                composable("home") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("bottom_nav")
                    }
                    val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)

                    HomeScreen(
                        onScrollOffsetChanged = { offset ->
                            scrollOffset = offset
                        },
                        navigationTitle = {
                            NavigationTitle(
                                title = "Home",
                                navController = navController,
                                scrollOffset = scrollOffset,
                                maxOffset = maxOffset,
                            )
                        },
                        navController = navController,
                        homeVm = homeViewModel
                    )
                }

                composable(
                    route = Screen.CatalogDetail.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry("bottom_nav")
                    }
                    val id = it.arguments?.getString("id") ?: ""
                    val productsViewModel: ProductsViewModel = hiltViewModel(parentEntry)

                    ProductsScreen(
                        onScrollOffsetChanged = { offset ->
                            scrollOffset = offset
                        },
                        navigationTitle = { title ->
                            productsScreenTitle = title
                            NavigationTitle(
                                title = title,
                                navController = navController,
                                scrollOffset = scrollOffset,
                                maxOffset = maxOffset,
                            )
                        },
                        catalogId = id,
                        productVm = productsViewModel
                    )
                }

                composable("search") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("bottom_nav")
                    }
                    val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                    SearchScreen(
                        onScrollOffsetChanged = { offset ->
                            scrollOffset = offset
                        },
                        navigationTitle = {
                            NavigationTitle(
                                title = "Search",
                                navController = navController,
                                scrollOffset = scrollOffset,
                                maxOffset = maxOffset,
                            )
                        },
                        searchVm = searchViewModel
                    )
                }

                composable("setting") {
                    SettingScreen()
                }
            }
        }
    }
}