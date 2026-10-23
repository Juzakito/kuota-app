package com.kouta.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.feature.security.PinLockBarrierScreen
import com.kouta.app.feature.security.PinSetupBarrierScreen
import com.kouta.app.navigation.KoutaNavHost
import com.kouta.app.navigation.NavDestination
import com.kouta.app.navigation.navigateToTopLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(deepLink: String? = null, onDeepLinkHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pinState by PinSecurityManager.stateFlow.collectAsState()
    val revision by KuotaEvents.revision.collectAsState()

    var currentProfile by remember { mutableStateOf<LocalBusinessProfile?>(null) }

    LaunchedEffect(store, revision, currentRoute) {
        currentProfile = withContext(Dispatchers.IO) {
            runCatching { store.readBusinessProfile() }.getOrNull()
        }
    }

    val isProtectedRoute = currentRoute != null && currentRoute != NavDestination.Onboarding.route
    val requiresPinSetup = isProtectedRoute && !pinState.isConfigured
    val requiresPinUnlock = isProtectedRoute && pinState.isConfigured && pinState.isLocked
    val isSecurityBarrierActive = requiresPinSetup || requiresPinUnlock

    val topLevelRoutes = setOf(
        NavDestination.Home.route,
        NavDestination.Cards.route,
        NavDestination.Financing.route,
        NavDestination.Sales.route,
        NavDestination.Profile.route
    )
    val showBottomNavigation = currentRoute in topLevelRoutes && !isSecurityBarrierActive

    fun navigate(route: String) {
        if (isSecurityBarrierActive || currentRoute == route) return
        navController.navigateToTopLevel(route)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.alpha(if (isSecurityBarrierActive) 0f else 1f),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomNavigation) {
                    KoutaBottomBar(
                        currentRoute = currentRoute,
                        onAccount = { navigate(NavDestination.Home.route) },
                        onCards = { navigate(NavDestination.Cards.route) },
                        onQr = { navigate(NavDestination.Financing.route) },
                        onActivity = { navigate(NavDestination.Sales.route) },
                        onMenu = { navigate(NavDestination.Profile.route) }
                    )
                }
            }
        ) { innerPadding ->
            KoutaNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                deepLink = if (isSecurityBarrierActive) null else deepLink,
                onDeepLinkHandled = onDeepLinkHandled
            )
        }

        if (requiresPinSetup) {
            PinSetupBarrierScreen(
                isRecoveryFlow = false,
                onPinConfigured = {
                    // El estado se actualiza automáticamente en PinSecurityManager.stateFlow
                }
            )
        } else if (requiresPinUnlock) {
            PinLockBarrierScreen(
                profile = currentProfile,
                onUnlocked = {
                    // Regresa inmediatamente a la pantalla anterior conservando todo su estado
                },
                onSwitchUserReset = {
                    navController.navigate(NavDestination.Onboarding.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun KoutaBottomBar(
    currentRoute: String?,
    onAccount: () -> Unit,
    onCards: () -> Unit,
    onQr: () -> Unit,
    onActivity: () -> Unit,
    onMenu: () -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 18.dp, shape = barShape, clip = false)
            .background(MaterialTheme.colorScheme.surface, barShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .heightIn(min = 76.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "Cuenta",
                icon = Icons.Default.AccountBalance,
                selected = currentRoute == NavDestination.Home.route,
                onClick = onAccount,
                modifier = Modifier.weight(1f)
            )
            BottomBarItem(
                label = "Tarjetas",
                icon = Icons.Default.CreditCard,
                selected = currentRoute == NavDestination.Cards.route,
                onClick = onCards,
                modifier = Modifier.weight(1f)
            )
            CenterQrButton(
                selected = currentRoute == NavDestination.Financing.route,
                onClick = onQr,
                modifier = Modifier.weight(1.2f)
            )
            BottomBarItem(
                label = "Actividad",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                selected = currentRoute == NavDestination.Sales.route,
                onClick = onActivity,
                modifier = Modifier.weight(1f)
            )
            BottomBarItem(
                label = "Menú",
                icon = Icons.Default.Menu,
                selected = currentRoute == NavDestination.Profile.route,
                onClick = onMenu,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val animatorScale = rememberSystemAnimatorScale()
    val duration = KuotaMotion.resolveDurationMs(180, animatorScale)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = duration, easing = KuotaMotion.StandardEasing),
        label = "bottomTabTint_$label"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selected) 46.dp else 0.dp,
        animationSpec = tween(durationMillis = duration, easing = KuotaMotion.StandardEasing),
        label = "bottomTabPillWidth_$label"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = duration),
        label = "bottomTabPillAlpha_$label"
    )
    val iconScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            selected -> 1.06f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = KuotaMotion.resolveDurationMs(140, animatorScale)),
        label = "bottomTabIconScale_$label"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = {
                    if (!selected) {
                        KuotaMotion.performKeyTapHaptic(view)
                    }
                    onClick()
                }
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier.height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pillWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .width(pillWidth)
                        .height(28.dp)
                        .alpha(pillAlpha)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier
                    .size(23.dp)
                    .scale(iconScale)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tint
        )
    }
}

@Composable
private fun CenterQrButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val animatorScale = rememberSystemAnimatorScale()
    val duration = KuotaMotion.resolveDurationMs(180, animatorScale)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val kuotaColors = MaterialTheme.kuotaColors

    val buttonScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            selected -> 1.04f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = KuotaMotion.resolveDurationMs(150, animatorScale)),
        label = "centerQrScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = duration),
        label = "centerQrLabelColor"
    )
    val ringColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = duration),
        label = "centerQrRingColor"
    )

    Column(
        modifier = modifier
            .offset(y = (-16).dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    KuotaMotion.performKeyTapHaptic(view)
                    onClick()
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(buttonScale)
                .shadow(if (selected) 14.dp else 10.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(kuotaColors.brandGradient), CircleShape)
                .border(if (selected) 2.dp else 0.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Comprar con QR",
                tint = kuotaColors.onBrand,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            "QR",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
    }
}
