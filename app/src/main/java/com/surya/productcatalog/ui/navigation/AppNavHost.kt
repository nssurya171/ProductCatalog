package com.surya.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.surya.productcatalog.ui.cart.CartScreen
import com.surya.productcatalog.ui.productdetail.ProductDetailScreen
import com.surya.productcatalog.ui.productlist.ProductListScreen

/** Route names for every screen in the app. */
private object Routes {
    const val PRODUCT_LIST = "products"
    const val ARG_PRODUCT_ID = "productId"
    const val PRODUCT_DETAIL = "products/{$ARG_PRODUCT_ID}"
    const val CART = "cart"

    fun productDetail(productId: Int) = "products/$productId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PRODUCT_LIST) {

        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onProductClick = { productId ->
                    // launchSingleTop: a quick double tap won't open the screen twice.
                    navController.navigate(Routes.productDetail(productId)) {
                        launchSingleTop = true
                    }
                },
                onCartClick = {
                    navController.navigate(Routes.CART) { launchSingleTop = true }
                },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.IntType }),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt(Routes.ARG_PRODUCT_ID) ?: return@composable
            ProductDetailScreen(
                productId = productId,
                // navigateUp never pops the start screen, so a double tap on Back
                // can't leave the app on a blank screen.
                onBack = { navController.navigateUp() },
            )
        }

        composable(Routes.CART) {
            CartScreen(onBack = { navController.navigateUp() })
        }
    }
}
