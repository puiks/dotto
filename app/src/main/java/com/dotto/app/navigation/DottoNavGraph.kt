package com.dotto.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dotto.app.DottoApp
import com.dotto.app.ui.detail.DetailScreen
import com.dotto.app.ui.detail.DetailViewModel
import com.dotto.app.ui.home.HomeScreen
import com.dotto.app.ui.home.HomeViewModel

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{habitId}"

    fun detail(habitId: Long) = "detail/$habitId"
}

@Composable
fun DottoNavGraph(app: DottoApp) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(app.habitRepository)
            )
            HomeScreen(
                viewModel = viewModel,
                onHabitClick = { habitId ->
                    navController.navigate(Routes.detail(habitId))
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            val viewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(app.habitRepository, habitId)
            )
            DetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
