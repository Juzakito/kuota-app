package com.kouta.app.data.local

import android.content.Context
import android.util.AtomicFile
import com.kouta.app.domain.model.MerchantProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class ProductCatalogPayload(
    val products: List<MerchantProduct> = emptyList(),
    val updatedAtEpochMillis: Long = 0L
)

/**
 * Almacenamiento local persistente para el catálogo "Mis productos" del vendedor.
 * Permite el cruce automático de montos para el estado "Comprobada / No comprobada" (PRD 6.5 y 6.7).
 */
class ProductCatalogStore(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    private val atomicFile = AtomicFile(file)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _productsFlow = MutableStateFlow<List<MerchantProduct>>(emptyList())
    val productsFlow: StateFlow<List<MerchantProduct>> = _productsFlow.asStateFlow()

    init {
        loadFromDisk()
    }

    @Synchronized
    fun getProducts(): List<MerchantProduct> {
        return _productsFlow.value
    }

    fun getAll(): List<MerchantProduct> = getProducts()
    fun add(name: String, priceCents: Long, category: String = "", sku: String? = null) = addProduct(name, priceCents, category, sku)
    fun update(product: MerchantProduct) = updateProduct(product)
    fun delete(productId: String) = deleteProduct(productId)

    @Synchronized
    fun addProduct(name: String, priceCents: Long, category: String = "General", sku: String? = null): MerchantProduct {
        val current = _productsFlow.value.toMutableList()
        val newProduct = MerchantProduct(
            name = name.trim(),
            priceCents = priceCents,
            category = category.trim().ifBlank { "General" },
            sku = sku?.trim()?.ifBlank { null }
        )
        current.add(0, newProduct)
        saveToDisk(current)
        _productsFlow.value = current
        return newProduct
    }

    @Synchronized
    fun updateProduct(product: MerchantProduct): Boolean {
        val current = _productsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == product.id }
        if (index == -1) return false
        current[index] = product.copy(updatedAtEpochMillis = System.currentTimeMillis())
        saveToDisk(current)
        _productsFlow.value = current
        return true
    }

    @Synchronized
    fun deleteProduct(productId: String): Boolean {
        val current = _productsFlow.value.toMutableList()
        val removed = current.removeAll { it.id == productId }
        if (removed) {
            saveToDisk(current)
            _productsFlow.value = current
        }
        return removed
    }

    /**
     * Regla 6.7: Una venta es COMPROBADA cuando su monto coincide exactamente con el precio
     * de algún producto registrado en "Mis productos".
     */
    @Synchronized
    fun isSaleVerified(saleAmountCents: Long): Boolean {
        if (saleAmountCents <= 0L) return false
        return _productsFlow.value.any { it.priceCents == saleAmountCents }
    }

    @Synchronized
    fun findMatchingProduct(saleAmountCents: Long): MerchantProduct? {
        return _productsFlow.value.firstOrNull { it.priceCents == saleAmountCents }
    }

    private fun loadFromDisk() {
        if (!file.exists()) {
            _productsFlow.value = defaultProducts()
            saveToDisk(_productsFlow.value)
            return
        }
        try {
            val bytes = atomicFile.readFully()
            val text = bytes.toString(Charsets.UTF_8)
            val payload = json.decodeFromString<ProductCatalogPayload>(text)
            _productsFlow.value = payload.products
        } catch (_: Exception) {
            _productsFlow.value = defaultProducts()
        }
    }

    private fun saveToDisk(products: List<MerchantProduct>) {
        val payload = ProductCatalogPayload(
            products = products,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        val text = json.encodeToString(payload)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = atomicFile.startWrite()
            stream.write(text.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            if (stream != null) atomicFile.failWrite(stream)
        }
    }

    /**
     * Catálogo demo inicial para vendedores que inician por primera vez.
     */
    private fun defaultProducts(): List<MerchantProduct> = listOf(
        MerchantProduct(
            name = "Saco de Arroz Extra 50kg",
            priceCents = 185_00L, // S/ 185.00
            category = "Abarrotes"
        ),
        MerchantProduct(
            name = "Caja de Aceite Vegetal x 12 un",
            priceCents = 95_00L,  // S/ 95.00
            category = "Abarrotes"
        ),
        MerchantProduct(
            name = "Fardo de Azúcar Rubia 50kg",
            priceCents = 160_00L, // S/ 160.00
            category = "Abarrotes"
        )
    )

    companion object {
        private const val FILE_NAME = "kuota_merchant_products.json"
    }
}
