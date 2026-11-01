package com.kouta.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.feature.financing.TradeScreen
import com.kouta.app.feature.home.HomeScreen
import com.kouta.app.feature.installments.MyInstallmentsScreen
import com.kouta.app.feature.legal.LegalScreen
import com.kouta.app.feature.marketplace.MarketplaceScreen
import com.kouta.app.feature.notifications.NotificationsScreen
import com.kouta.app.feature.onboarding.OnboardingScreen
import com.kouta.app.feature.products.MyProductsScreen
import com.kouta.app.feature.profile.ProfileScreen
import com.kouta.app.feature.sales.SalesScreen
import com.kouta.app.feature.supplier.SupplierChargeScreen
import com.kouta.app.feature.supplier.SupplierHomeScreen
import com.kouta.app.feature.supplier.SupplierPendingConfirmationsScreen
import com.kouta.app.feature.supplier.SupplierQrScreen
import com.kouta.app.feature.supplier.SupplierSalesHistoryScreen
import com.kouta.app.feature.support.SupportScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun NavHostController.navigateToTopLevel(route: String) {
    if (currentDestination?.route == route) return
    val rootRoute = if (route.startsWith("supplier")) NavDestination.SupplierHome.route else NavDestination.Home.route
    if (route == rootRoute) {
        val popped = popBackStack(rootRoute, inclusive = false)
        if (!popped) {
            navigate(rootRoute) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        return
    }
    navigate(route) {
        popUpTo(rootRoute) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

@Composable
fun KoutaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    deepLink: String? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        startDestination = if (currentUser == null) {
            NavDestination.Onboarding.route
        } else {
            NavDestination.Home.route
        }
    }

    val resolvedStart = startDestination

    LaunchedEffect(deepLink, resolvedStart) {
        val link = deepLink ?: return@LaunchedEffect
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        val target = when {
            link.startsWith("kuota://compras") || link.startsWith("kuota://financing") -> NavDestination.Installments.route
            link.startsWith("kuota://soporte") || link.startsWith("kuota://reclamos") -> supportRoute(false)
            else -> NavDestination.Notifications.route
        }
        if (current != NavDestination.Onboarding.route && current != target) {
            if (target == NavDestination.Installments.route) {
                navController.navigateToTopLevel(target)
            } else {
                navController.navigate(target) { launchSingleTop = true }
            }
        }
        onDeepLinkHandled()
    }

    if (resolvedStart == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val animatorScale = rememberSystemAnimatorScale()
        val navEnterMs = KuotaMotion.resolveDurationMs(220, animatorScale)
        val navExitMs = KuotaMotion.resolveDurationMs(160, animatorScale)
        NavHost(
            navController = navController,
            startDestination = resolvedStart,
            modifier = modifier,
            enterTransition = { fadeIn(tween(navEnterMs, easing = KuotaMotion.DecelerateEasing)) },
            exitTransition = { fadeOut(tween(navExitMs, easing = KuotaMotion.AccelerateEasing)) }
        ) {
            composable(NavDestination.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        val destination = NavDestination.Home.route
                        startDestination = destination
                        navController.navigate(destination) {
                            popUpTo(NavDestination.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // VENDEDOR ROUTES
            composable(NavDestination.Home.route) {
                HomeScreen(
                    onFinanceClick = {
                        navController.navigate(NavDestination.PurchaseWizard.route) { launchSingleTop = true }
                    },
                    onCardsClick = {
                        navController.navigateToTopLevel(NavDestination.Installments.route)
                    },
                    onActivityClick = {
                        navController.navigateToTopLevel(NavDestination.Finance.route)
                    },
                    onSupportClick = {
                        navController.navigate(supportRoute(false)) { launchSingleTop = true }
                    },
                    onNotificationsClick = {
                        navController.navigate(NavDestination.Notifications.route) { launchSingleTop = true }
                    },
                    onOpenProfile = {
                        navController.navigate(NavDestination.Profile.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavDestination.Marketplace.route) {
                MarketplaceScreen(
                    onSelectSupplierForPurchase = { supplier ->
                        navController.navigate(NavDestination.PurchaseWizard.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavDestination.PurchaseWizard.route) {
                val purchaseViewModel: com.kouta.app.feature.purchase.PurchaseViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                com.kouta.app.feature.purchase.PurchaseFlowScreen(
                    viewModel = purchaseViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onFinishToHome = {
                        navController.navigateToTopLevel(NavDestination.Home.route)
                    }
                )
            }

            composable(NavDestination.Trade.route) {
                TradeScreen(
                    onNavigateToInstallments = {
                        navController.navigateToTopLevel(NavDestination.Installments.route)
                    }
                )
            }

            composable(NavDestination.Finance.route) {
                SalesScreen(
                    onOpenMyProducts = {
                        navController.navigate(NavDestination.MyProducts.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavDestination.Installments.route) {
                com.kouta.app.feature.creditline.CreditLineScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPurchase = {
                        navController.navigate(NavDestination.PurchaseWizard.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavDestination.MyProducts.route) {
                MyProductsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // PROVEEDOR ROUTES
            composable(NavDestination.SupplierHome.route) {
                SupplierHomeScreen(
                    onOpenPending = {
                        navController.navigateToTopLevel(NavDestination.SupplierPending.route)
                    },
                    onOpenCharge = {
                        navController.navigateToTopLevel(NavDestination.SupplierCharge.route)
                    },
                    onOpenSales = {
                        navController.navigateToTopLevel(NavDestination.SupplierSales.route)
                    },
                    onOpenProfile = {
                        navController.navigate(NavDestination.Profile.route) { launchSingleTop = true }
                    },
                    onOpenNotifications = {
                        navController.navigate(NavDestination.Notifications.route) { launchSingleTop = true }
                    },
                    onOpenQr = {
                        navController.navigate(NavDestination.SupplierQr.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavDestination.SupplierQr.route) {
                SupplierQrScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavDestination.SupplierPending.route) {
                SupplierPendingConfirmationsScreen()
            }

            composable(NavDestination.SupplierCharge.route) {
                SupplierChargeScreen()
            }

            composable(NavDestination.SupplierSales.route) {
                SupplierSalesHistoryScreen()
            }

            // COMMON ROUTES
            composable(NavDestination.Profile.route) {
                ProfileScreen(
                    onLocalProfileDeleted = {
                        startDestination = NavDestination.Onboarding.route
                        navController.navigate(NavDestination.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenSupport = { openForm ->
                        navController.navigate(supportRoute(openForm)) { launchSingleTop = true }
                    },
                    onOpenNotifications = {
                        navController.navigate(NavDestination.Notifications.route) { launchSingleTop = true }
                    },
                    onOpenLegal = {
                        navController.navigate(NavDestination.Legal.route) { launchSingleTop = true }
                    },
                    onOpenCards = {
                        navController.navigateToTopLevel(NavDestination.Installments.route)
                    }
                )
            }

            composable(NavDestination.Legal.route) {
                LegalScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = NavDestination.Support.route,
                arguments = listOf(navArgument("form") { type = NavType.BoolType; defaultValue = false })
            ) { entry ->
                SupportScreen(
                    openClaimForm = entry.arguments?.getBoolean("form") ?: false,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavDestination.Notifications.route) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDeepLink = { link ->
                        when {
                            link.startsWith("kuota://compras") || link.startsWith("kuota://financing") ->
                                navController.navigateToTopLevel(NavDestination.Installments.route)
                            link.startsWith("kuota://soporte") || link.startsWith("kuota://reclamos") ->
                                navController.navigate(supportRoute(false)) { launchSingleTop = true }
                            else -> Unit
                        }
                    }
                )
            }
        }
    }
}
