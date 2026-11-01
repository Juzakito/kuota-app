package com.kuota.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.theme.KuotaColors

enum class KuotaNavTab(val title: String, val icon: ImageVector) {
    Home("Inicio", Icons.Default.Home),
    Sales("Ventas", Icons.AutoMirrored.Filled.TrendingUp),
    Installments("Cuotas", Icons.AutoMirrored.Filled.ReceiptLong),
    Profile("Perfil", Icons.Default.Person)
}

/**
 * Barra de navegación inferior oficial de Kuota App según PRD §8.2 y DESIGN.md.
 * - 4 destinos principales: Inicio, Ventas, Cuotas, Perfil.
 * - Botón central QR plano: Diseñado para compra o cobro rápido en menos de 60 segundos.
 * - Cero degradados ni sombras duras.
 */
@Composable
fun KuotaBottomNav(
    selectedTab: KuotaNavTab,
    onTabSelected: (KuotaNavTab) -> Unit,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(KuotaColors.Paper),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = KuotaColors.Paper,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Inicio
                NavItem(
                    tab = KuotaNavTab.Home,
                    isSelected = selectedTab == KuotaNavTab.Home,
                    onClick = { onTabSelected(KuotaNavTab.Home) }
                )

                // Tab 2: Ventas
                NavItem(
                    tab = KuotaNavTab.Sales,
                    isSelected = selectedTab == KuotaNavTab.Sales,
                    onClick = { onTabSelected(KuotaNavTab.Sales) }
                )

                // Espacio para el botón central QR
                Spacer(modifier = Modifier.size(56.dp))

                // Tab 3: Cuotas
                NavItem(
                    tab = KuotaNavTab.Installments,
                    isSelected = selectedTab == KuotaNavTab.Installments,
                    onClick = { onTabSelected(KuotaNavTab.Installments) }
                )

                // Tab 4: Perfil
                NavItem(
                    tab = KuotaNavTab.Profile,
                    isSelected = selectedTab == KuotaNavTab.Profile,
                    onClick = { onTabSelected(KuotaNavTab.Profile) }
                )
            }
        }

        // Botón central QR plano y prominente
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(KuotaColors.PersianBlue)
                .clickable(
                    role = Role.Button,
                    onClick = onQrClick
                )
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Escanear código QR",
                tint = KuotaColors.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: KuotaNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Pebble
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.title,
            color = tint,
            fontSize = 11.sp,
            fontWeight = fontWeight
        )
    }
}
