package com.kouta.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MarketplaceSupplier(
    val id: String,
    val name: String,
    val commercialCategory: String,
    val zone: String,
    val address: String,
    val phone: String,
    val paymentDetails: String,
    val rating: Float = 4.9f,
    val completedTransactions: Int = 120,
    val isVerified: Boolean = true,
    val qrCodePayload: String = ""
) {
    val displaySubtitle: String
        get() = "$commercialCategory · $zone"
}

object SeedSuppliers {
    val items = listOf(
        MarketplaceSupplier(
            id = "sup-gamarra-01",
            name = "Confecciones Don Lucho & Hnos",
            commercialCategory = "Textil & Ropa",
            zone = "La Victoria (Gamarra)",
            address = "Jr. Gamarra 934, Galería Guizado Stand 402",
            phone = "987 123 456",
            paymentDetails = "Yape/Plin: 987123456 (Luis Quispe)",
            rating = 4.95f,
            completedTransactions = 340,
            qrCodePayload = "kuota://pay?supplierId=sup-gamarra-01&name=Confecciones+Don+Lucho"
        ),
        MarketplaceSupplier(
            id = "sup-gamarra-02",
            name = "Telas & Denim San Pedro Mayorista",
            commercialCategory = "Insumos Textiles",
            zone = "La Victoria (Gamarra)",
            address = "Jr. Antonio Bazo 740, La Victoria",
            phone = "981 456 789",
            paymentDetails = "BCP Corriente: 193-4829104-0-12 (Telas San Pedro SAC)",
            rating = 4.88f,
            completedTransactions = 512,
            qrCodePayload = "kuota://pay?supplierId=sup-gamarra-02&name=Telas+San+Pedro"
        ),
        MarketplaceSupplier(
            id = "sup-polvos-01",
            name = "ElectroTech Lima Import",
            commercialCategory = "Tecnología & Móviles",
            zone = "La Victoria (Polvos Azules)",
            address = "Av. José Gálvez 250, Bloque 18 Puesto 12",
            phone = "992 334 112",
            paymentDetails = "Yape: 992334112 / Plin: 992334112",
            rating = 4.92f,
            completedTransactions = 280,
            qrCodePayload = "kuota://pay?supplierId=sup-polvos-01&name=ElectroTech+Lima"
        ),
        MarketplaceSupplier(
            id = "sup-cercado-01",
            name = "Distribuidora Central de Calzado",
            commercialCategory = "Calzado & Cuero",
            zone = "Cercado de Lima",
            address = "Jr. Andahuaylas 815, Mesa Redonda",
            phone = "974 556 221",
            paymentDetails = "Yape: 974556221 (Calzados Central)",
            rating = 4.75f,
            completedTransactions = 195,
            qrCodePayload = "kuota://pay?supplierId=sup-cercado-01&name=Distribuidora+Calzado"
        ),
        MarketplaceSupplier(
            id = "sup-sjl-01",
            name = "Cosméticos & Cuidado Mayorista",
            commercialCategory = "Belleza & Cuidado",
            zone = "San Juan de Lurigancho",
            address = "Av. Próceres de la Independencia 1420",
            phone = "966 889 004",
            paymentDetails = "Plin: 966889004 (Rosa Huamán)",
            rating = 4.90f,
            completedTransactions = 145,
            qrCodePayload = "kuota://pay?supplierId=sup-sjl-01&name=Cosmeticos+Mayorista"
        ),
        MarketplaceSupplier(
            id = "sup-ate-01",
            name = "Empaques & Bolsas Industriales SAC",
            commercialCategory = "Empaques & Papelería",
            zone = "Ate - Ceres",
            address = "Av. Nicolás Ayllón 4200",
            phone = "955 112 334",
            paymentDetails = "Interbank Cta: 200-3001849201",
            rating = 4.85f,
            completedTransactions = 230,
            qrCodePayload = "kuota://pay?supplierId=sup-ate-01&name=Empaques+Industriales"
        )
    )
}
