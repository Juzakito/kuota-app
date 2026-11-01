package com.kouta.app.feature.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kouta.app.di.AppEntryPoint
import com.kuota.core.model.ProviderProfile
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.EmptyState
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.flowOf

@Composable
fun MarketplaceScreen(
    onSelectSupplierForPurchase: (ProviderProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }
    val supplierRepo = entryPoint?.supplierRepository()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Textil", "Calzado", "Abarrotes", "Ferretería", "Tecnología")

    val providers by (supplierRepo?.observePublicProviders(selectedCategory, searchQuery) ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    // Si la base en Firestore está vacía en fase de inicio, proveer directorio inicial
    val displayProviders = if (providers.isNotEmpty()) {
        providers
    } else {
        val defaultList = listOf(
            ProviderProfile(
                providerUid = "SUPP-001",
                businessName = "Distribuidora San Juan MYPE",
                category = "Abarrotes",
                address = "Jr. Huánuco 1240",
                district = "La Victoria",
                phone = "987654321",
                isVerified = true
            ),
            ProviderProfile(
                providerUid = "SUPP-002",
                businessName = "Textiles Gamarra S.A.C.",
                category = "Textil",
                address = "Gamarra 850",
                district = "La Victoria",
                phone = "987112233",
                isVerified = true
            ),
            ProviderProfile(
                providerUid = "SUPP-003",
                businessName = "Calzados El Porvenir",
                category = "Calzado",
                address = "Av. España 410",
                district = "Trujillo",
                phone = "944556677",
                isVerified = true
            ),
            ProviderProfile(
                providerUid = "SUPP-004",
                businessName = "Ferretería & Aceros Perú",
                category = "Ferretería",
                address = "Av. Argentina 1890",
                district = "Cercado de Lima",
                phone = "911223344",
                isVerified = true
            )
        )
        defaultList.filter {
            (selectedCategory == "Todos" || it.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || it.businessName.contains(searchQuery, ignoreCase = true) || it.district.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.directory_title),
            color = KuotaColors.ForestInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Encuentra distribuidores autorizados para comprar mercadería al crédito con Kuota.",
            color = KuotaColors.Charcoal,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.directory_search_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = KuotaColors.ForestInk)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KuotaColors.ForestInk,
                focusedLabelColor = KuotaColors.ForestInk
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filtros en chips pill por rubro comercial
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                ChipPill(
                    label = cat,
                    isSelected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de proveedores
        if (displayProviders.isEmpty()) {
            EmptyState(
                title = "No encontramos proveedores",
                description = "Prueba buscando con otro término o seleccionando otra categoría.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayProviders, key = { it.providerUid }) { provider ->
                    ProviderItemCard(
                        provider = provider,
                        onSelect = { onSelectSupplierForPurchase(provider) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderItemCard(
    provider: ProviderProfile,
    onSelect: () -> Unit
) {
    KuotaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KuotaColors.LimeVoltage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = KuotaColors.ForestInk,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.businessName,
                                color = KuotaColors.ForestInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (provider.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verificado",
                                    tint = KuotaColors.ForestInk,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = provider.category,
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                    }
                }

                ChipPill(
                    label = provider.district,
                    isSelected = false,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = KuotaColors.Charcoal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${provider.address}, ${provider.district}",
                    color = KuotaColors.Charcoal,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PillButton(
                text = stringResource(R.string.directory_btn_buy),
                onClick = onSelect,
                variant = PillButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
