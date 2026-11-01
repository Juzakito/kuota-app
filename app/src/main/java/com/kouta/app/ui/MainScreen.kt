package com.kouta.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.PurchasePhase
import com.kouta.app.domain.model.UserRole
import com.kouta.app.feature.financing.TradeScreenNavState
import com.kouta.app.feature.security.PinLockBarrierScreen
import com.kouta.app.feature.security.PinSetupBarrierScreen
import com.kouta.app.navigation.KoutaNavHost
import com.kouta.app.navigation.NavDestination
import com.kouta.app.navigation.navigateToTopLevel
import com.kouta.app.di.AppEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(deepLink: String? = null, onDeepLinkHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pinState by PinSecurityManager.stateFlow.collectAsState()
    val revision by KuotaEvents.revision.collectAsState()

    val appEntryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }
    val userSummary by (appEntryPoint?.userRepository()?.observeUserSummary()
        ?: kotlinx.coroutines.flow.flowOf(null))
        .collectAsState(initial = null)

    var currentProfile by remember { mutableStateOf<LocalBusinessProfile?>(null) }
    var pendingBadgeCount by remember { mutableIntStateOf(0) }
    var showKuotearActionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(store, revision, currentRoute) {
        currentProfile = withContext(Dispatchers.IO) {
            runCatching { store.readBusinessProfile() }.getOrNull()
        }
        pendingBadgeCount = withContext(Dispatchers.IO) {
            runCatching {
                creditStore.getAll().count {
                    it.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED || it.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING
                }
            }.getOrDefault(0)
        }
    }

    val isProtectedRoute = currentRoute != null && currentRoute != NavDestination.Onboarding.route
    val requiresPinSetup = isProtectedRoute && !pinState.isConfigured
    val requiresPinUnlock = isProtectedRoute && pinState.isConfigured && pinState.isLocked
    val isSecurityBarrierActive = requiresPinSetup || requiresPinUnlock

    val isProveedor = (userSummary?.role?.equals("PROVEEDOR", ignoreCase = true) == true) ||
        (currentProfile?.role?.equals(UserRole.PROVEEDOR.name, ignoreCase = true) == true)

    val topLevelRoutes = if (isProveedor) {
        setOf(
            NavDestination.SupplierHome.route,
            NavDestination.SupplierPending.route,
            NavDestination.SupplierCharge.route,
            NavDestination.SupplierSales.route
        )
    } else {
        setOf(
            NavDestination.Home.route,
            NavDestination.Sales.route,
            NavDestination.Kuotear.route,
            NavDestination.CreditLine.route,
            NavDestination.Account.route
        )
    }
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
                    if (isProveedor) {
                        SupplierBottomBar(
                            currentRoute = currentRoute,
                            pendingBadgeCount = pendingBadgeCount,
                            onHome = { navigate(NavDestination.SupplierHome.route) },
                            onPending = { navigate(NavDestination.SupplierPending.route) },
                            onCharge = { navigate(NavDestination.SupplierCharge.route) },
                            onSales = { navigate(NavDestination.SupplierSales.route) }
                        )
                    } else {
                        VendedorBottomBar(
                            currentRoute = currentRoute,
                            onHome = { navigate(NavDestination.Home.route) },
                            onSales = { navigate(NavDestination.Sales.route) },
                            onKuotear = { showKuotearActionSheet = true },
                            onCreditLine = { navigate(NavDestination.CreditLine.route) },
                            onAccount = { navigate(NavDestination.Account.route) }
                        )
                    }
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

        if (showKuotearActionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showKuotearActionSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Kuotear",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Haz que tus ventas financien tus compras y crecimiento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Opción 1: Comprar a crédito
                    KuotearSheetActionCard(
                        title = "Comprar a crédito",
                        subtitle = "Escanea el QR de tu proveedor y paga en cuotas semanales",
                        badge = "Hasta tu línea disponible",
                        badgeColor = Color(0xFF10B981),
                        icon = Icons.Default.Storefront,
                        onClick = {
                            showKuotearActionSheet = false
                            TradeScreenNavState.openBuy()
                            navigate(NavDestination.Kuotear.route)
                        }
                    )

                    // Opción 2: Cobrar con link o QR
                    KuotearSheetActionCard(
                        title = "Cobrar con link o QR",
                        subtitle = "Cobra a tus clientes por Yape, Plin o tarjeta en minutos",
                        badge = "Comisión 1%",
                        badgeColor = Color(0xFF2457F0),
                        icon = Icons.Default.Link,
                        onClick = {
                            showKuotearActionSheet = false
                            TradeScreenNavState.openLink()
                            navigate(NavDestination.Kuotear.route)
                        }
                    )

                    // Opción 3: Registrar venta
                    KuotearSheetActionCard(
                        title = "Registrar venta",
                        subtitle = "Anota ventas en efectivo o POS para aumentar tu nivel de crédito",
                        badge = "Sube tu línea",
                        badgeColor = Color(0xFFF5A524),
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        onClick = {
                            showKuotearActionSheet = false
                            navigate(NavDestination.Sales.route)
                        }
                    )
                }
            }
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
private fun VendedorBottomBar(
    currentRoute: String?,
    onHome: () -> Unit,
    onSales: () -> Unit,
    onKuotear: () -> Unit,
    onCreditLine: () -> Unit,
    onAccount: () -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = barShape, clip = false)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = barShape
            )
            .background(MaterialTheme.colorScheme.surface, barShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .heightIn(min = 72.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "Inicio",
                icon = Icons.Default.Home,
                selected = currentRoute == NavDestination.Home.route,
                onClick = onHome,
                modifier = Modifier.weight(1f)
            )
            BottomBarItem(
                label = "Ventas",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                selected = currentRoute == NavDestination.Sales.route,
                onClick = onSales,
                modifier = Modifier.weight(1f)
            )
            CenterActionButton(
                label = "Kuotear",
                selected = currentRoute == NavDestination.Kuotear.route,
                onClick = onKuotear,
                modifier = Modifier.weight(1.25f)
            )
            BottomBarItem(
                label = "Línea",
                icon = Icons.Default.CreditCard,
                selected = currentRoute == NavDestination.CreditLine.route,
                onClick = onCreditLine,
                modifier = Modifier.weight(1f)
            )
            BottomBarItem(
                label = "Cuenta",
                icon = Icons.Default.Person,
                selected = currentRoute == NavDestination.Account.route,
                onClick = onAccount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SupplierBottomBar(
    currentRoute: String?,
    pendingBadgeCount: Int,
    onHome: () -> Unit,
    onPending: () -> Unit,
    onCharge: () -> Unit,
    onSales: () -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = barShape, clip = false)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = barShape
            )
            .background(MaterialTheme.colorScheme.surface, barShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .heightIn(min = 72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                label = "Inicio",
                icon = Icons.Default.Home,
                selected = currentRoute == NavDestination.SupplierHome.route,
                onClick = onHome,
                modifier = Modifier.weight(1f)
            )
            BottomBarItem(
                label = "Por confirmar",
                icon = Icons.Default.PendingActions,
                selected = currentRoute == NavDestination.SupplierPending.route,
                badgeCount = pendingBadgeCount,
                onClick = onPending,
                modifier = Modifier.weight(1f)
            )
            CenterActionButton(
                label = "Cobrar",
                selected = currentRoute == NavDestination.SupplierCharge.route,
                onClick = onCharge,
                modifier = Modifier.weight(1.2f)
            )
            BottomBarItem(
                label = "Ventas",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                selected = currentRoute == NavDestination.SupplierSales.route,
                onClick = onSales,
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
    badgeCount: Int = 0,
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
    val iconScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            selected -> 1.04f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = KuotaMotion.resolveDurationMs(140, animatorScale)),
        label = "bottomTabIconScale_$label"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("$badgeCount")
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(iconScale)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = (-0.1).sp
            ),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint,
            maxLines = 1
        )
    }
}

@Composable
private fun CenterActionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val animatorScale = rememberSystemAnimatorScale()
    val duration = KuotaMotion.resolveDurationMs(180, animatorScale)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            selected -> 1.03f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = KuotaMotion.resolveDurationMs(150, animatorScale)),
        label = "centerActionScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = duration),
        label = "centerActionLabelColor"
    )

    // Gradiente en azul de marca conforme a la paleta de 5 colores
    val subtleBrandGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF1A3BB0), // Persian Blue
            Color(0xFF0B2545)  // Prussian Blue
        )
    )

    val displayLabel = when (label) {
        "Comprar / Vender", "Comprar y vender" -> "Comprar\ny vender"
        else -> label
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    KuotaMotion.performKeyTapHaptic(view)
                    onClick()
                }
            )
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(52.dp)
                .scale(buttonScale)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(subtleBrandGradient, CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = displayLabel.replace("\n", " "),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (displayLabel.contains("\n") || displayLabel.length > 8) 10.sp else 11.sp,
                lineHeight = 11.5.sp,
                letterSpacing = (-0.2).sp
            ),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = labelColor,
            maxLines = 2
        )
    }
}

@Composable
private fun KuotearSheetActionCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            badge,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

