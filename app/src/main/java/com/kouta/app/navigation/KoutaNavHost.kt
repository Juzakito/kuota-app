package com.kouta.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.kouta.app.feature.financing.FinancingScreen
import com.kouta.app.feature.home.HomeScreen
import com.kouta.app.feature.legal.LegalScreen
import com.kouta.app.feature.notifications.NotificationsScreen
import com.kouta.app.feature.payments.PaymentMethodsScreen
import com.kouta.app.feature.profile.ProfileScreen
import com.kouta.app.feature.sales.SalesScreen
import com.kouta.app.feature.support.SupportScreen
import com.kouta.app.feature.onboarding.OnboardingScreen
import com.kouta.app.data.local.SalesJournalStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun NavHostController.navigateToTopLevel(route: String) {
    if (currentDestination?.route == route) return
    if (route == NavDestination.Home.route) {
        val popped = popBackStack(NavDestination.Home.route, inclusive = false)
        if (!popped) {
            navigate(NavDestination.Home.route) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        return
    }
    navigate(route) {
        popUpTo(NavDestination.Home.route) {
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
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    var startDestination by remember(store) { mutableStateOf<String?>(null) }

    LaunchedEffect(store) {
        val hasLocalProfile = withContext(Dispatchers.IO) {
            runCatching { store.readBusinessProfile() != null }.getOrDefault(false)
        }
        startDestination = if (hasLocalProfile) NavDestination.Home.route else NavDestination.Onboarding.route
    }

    val resolvedStart = startDestination

    LaunchedEffect(deepLink, resolvedStart) {
        val link = deepLink ?: return@LaunchedEffect
        // Espera a que el NavHost tenga grafo antes de consumir el enlace.
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        val target = when {
            link.startsWith("kuota://compras") || link.startsWith("kuota://financing") -> NavDestination.Cards.route
            link.startsWith("kuota://soporte") || link.startsWith("kuota://reclamos") -> supportRoute(false)
            else -> NavDestination.Notifications.route
        }
        // Durante el onboarding se descarta: aún no hay perfil para mostrar estas pantallas.
        if (current != NavDestination.Onboarding.route && current != target) {
            if (target == NavDestination.Cards.route) {
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
                        startDestination = NavDestination.Home.route
                        navController.navigate(NavDestination.Home.route) {
                            popUpTo(NavDestination.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(NavDestination.Home.route) {
                HomeScreen(
                    onFinanceClick = {
                        navController.navigateToTopLevel(NavDestination.Financing.route)
                    },
                    onCardsClick = {
                        navController.navigateToTopLevel(NavDestination.Cards.route)
                    },
                    onActivityClick = {
                        navController.navigateToTopLevel(NavDestination.Sales.route)
                    },
                    onSupportClick = {
                        navController.navigate(supportRoute(false)) { launchSingleTop = true }
                    },
                    onNotificationsClick = {
                        navController.navigate(NavDestination.Notifications.route) { launchSingleTop = true }
                    },
                    onOpenProfile = {
                        navController.navigateToTopLevel(NavDestination.Profile.route)
                    }
                )
            }
            composable(NavDestination.Cards.route) {
                PaymentMethodsScreen(
                    onOpenFinancing = {
                        navController.navigateToTopLevel(NavDestination.Financing.route)
                    }
                )
            }
            composable(NavDestination.Sales.route) {
                SalesScreen()
            }
            composable(NavDestination.Financing.route) {
                FinancingScreen(
                    onOpenCards = {
                        navController.navigateToTopLevel(NavDestination.Cards.route)
                    }
                )
            }
            composable(NavDestination.Profile.route) {
                ProfileScreen(
                    onLocalProfileDeleted = {
                        startDestination = NavDestination.Onboarding.route
                        navController.navigate(NavDestination.Onboarding.route) {
                            popUpTo(NavDestination.Home.route) { inclusive = true }
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
                        navController.navigateToTopLevel(NavDestination.Cards.route)
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
                                navController.navigateToTopLevel(NavDestination.Cards.route)
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
