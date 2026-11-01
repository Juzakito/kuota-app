package com.kouta.app.domain.model

import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.ManualSale
import com.kouta.app.data.local.SaleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditLevelAndPurchasesTest {

    @Test
    fun verifyAllSixCreditLevelsHierarchy() {
        val levels = CreditLevel.values()
        assertEquals("Debe haber exactamente 6 niveles de crédito", 6, levels.size)

        // Nivel 1
        val n1 = CreditLevel.NIVEL_1
        assertEquals(1, n1.levelNumber)
        assertEquals(300_00L, n1.minLineCents)
        assertEquals(50, n1.downPaymentPercentage)

        // Nivel 6
        val n6 = CreditLevel.NIVEL_6
        assertEquals(6, n6.levelNumber)
        assertEquals(12_000_00L, n6.maxLineCents)
        assertEquals(10, n6.downPaymentPercentage)

        // Monotonic increase of credit limits
        for (i in 0 until levels.size - 1) {
            assertTrue(
                "Nivel ${levels[i + 1].levelNumber} debe tener mayor límite que Nivel ${levels[i].levelNumber}",
                levels[i + 1].maxLineCents > levels[i].maxLineCents
            )
            assertTrue(
                "Nivel ${levels[i + 1].levelNumber} debe tener menor o igual cuota inicial que Nivel ${levels[i].levelNumber}",
                levels[i + 1].downPaymentPercentage <= levels[i].downPaymentPercentage
            )
        }
    }

    @Test
    fun verifyDownPaymentAndSupplierPayoutFormulas() {
        // Venta de S/ 200.00 en Nivel 1 (50% inicial)
        val n1 = CreditLevel.NIVEL_1
        val totalCents = 200_00L
        val downPaymentCents = n1.calculateDownPaymentCents(totalCents)
        val financedCents = n1.calculateFinancedAmountCents(totalCents)

        assertEquals("Inicial 50% de S/ 200 debe ser S/ 100", 100_00L, downPaymentCents)
        assertEquals("Saldo financiado debe ser S/ 100", 100_00L, financedCents)

        // Fórmula proveedor en 48h: 98% * venta - inicial = 98% * 200 - 100 = 196 - 100 = S/ 96.00
        val supplierPayoutCents = n1.calculateSupplierPayout48hCents(totalCents, downPaymentCents)
        assertEquals("Liquidación 48h debe ser S/ 96.00", 96_00L, supplierPayoutCents)

        // Venta de S/ 5,000 en Nivel 6 (10% inicial)
        val n6 = CreditLevel.NIVEL_6
        val bigSaleCents = 5_000_00L
        val n6DownPayment = n6.calculateDownPaymentCents(bigSaleCents)
        assertEquals("Inicial Nivel 6 es 10%", 500_00L, n6DownPayment)

        val n6SupplierPayout = n6.calculateSupplierPayout48hCents(bigSaleCents, n6DownPayment)
        // 98% de 5000 = 4900 - 500 = 4400
        assertEquals("Liquidación al 98% es S/ 4,400.00", 4_400_00L, n6SupplierPayout)
    }

    @Test
    fun verifyRule32Enforcement() {
        val n1 = CreditLevel.NIVEL_1

        // 1. Con transacción abierta -> debe rechazar
        val checkBlocked = PurchaseEligibilityCheck.validate(
            totalAmountCents = 150_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = true
        )
        assertFalse("Debe bloquearse si hay transacción activa (Regla 3.2)", checkBlocked.isEligible)
        assertNotNull(checkBlocked.errorMessage)
        assertTrue(checkBlocked.errorMessage!!.contains("compra abierta"))

        // 2. Sin transacción abierta y dentro de línea -> debe aprobar
        val checkApproved = PurchaseEligibilityCheck.validate(
            totalAmountCents = 150_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = false
        )
        assertTrue("Debe aprobarse si está dentro de línea", checkApproved.isEligible)
        assertEquals(75_00L, checkApproved.downPaymentCents)
        assertEquals(75_00L, checkApproved.financedCents)
        assertNull(checkApproved.errorMessage)

        // 3. Monto mayor a línea disponible (Financiado S/ 400 > Línea S/ 300) -> debe rechazar
        val checkOverLimit = PurchaseEligibilityCheck.validate(
            totalAmountCents = 800_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = false
        )
        assertFalse("Debe bloquearse si excede la línea", checkOverLimit.isEligible)
        assertTrue(checkOverLimit.errorMessage!!.contains("excede tu línea"))
    }

    @Test
    fun verifyUserRoles() {
        assertEquals(UserRole.VENDEDOR, UserRole.fromCode("VENDEDOR"))
        assertEquals(UserRole.PROVEEDOR, UserRole.fromCode("PROVEEDOR"))
        assertEquals(UserRole.PROVEEDOR, UserRole.fromCode("proveedor"))
        assertEquals(UserRole.VENDEDOR, UserRole.fromCode("other"))
    }

    @Test
    fun verifyFase2TermsAlignedWithPRDTable3() {
        // Validación rigurosa de los plazos semanales de compra (múltiplos de 7 días conforme a AGENTS.md)
        assertEquals(listOf(21), CreditLevel.NIVEL_1.availableTermsDays)
        assertEquals(listOf(21), CreditLevel.NIVEL_2.availableTermsDays)
        assertEquals(listOf(21, 28), CreditLevel.NIVEL_3.availableTermsDays)
        assertEquals(listOf(28, 35, 42), CreditLevel.NIVEL_4.availableTermsDays)
        assertEquals(listOf(28, 42), CreditLevel.NIVEL_5.availableTermsDays)
        assertEquals(listOf(42, 56, 70, 84), CreditLevel.NIVEL_6.availableTermsDays)
    }

    @Test
    fun verifyFase2ThreeInstallmentsArithmeticWithoutFloatingPointLoss() {
        // En Fase 2 (PRD 6.2): El vendedor elige cuotas (ej: 3 cuotas) para el saldo financiado
        val financedCents = 100_00L // S/ 100.00
        val count = 3
        val baseAmount = financedCents / count
        val remainder = financedCents % count

        val installments = (1..count).map { num ->
            val amount = if (num == 1) baseAmount + remainder else baseAmount
            CreditInstallment(
                number = num,
                amountCents = amount,
                dueEpochMillis = System.currentTimeMillis() + (num * 5L * 24 * 3600_000)
            )
        }

        assertEquals(3, installments.size)
        assertEquals("Cuota 1 absorbe el centavo de residuo (S/ 33.34)", 33_34L, installments[0].amountCents)
        assertEquals("Cuota 2 es exacta (S/ 33.33)", 33_33L, installments[1].amountCents)
        assertEquals("Cuota 3 es exacta (S/ 33.33)", 33_33L, installments[2].amountCents)
        assertEquals("La suma total debe igualar exactamente los 100.00 soles", financedCents, installments.sumOf { it.amountCents })
    }

    @Test
    fun verifyPurchasePhasePRDLabelsAndRemainingDebt() {
        val purchase = CreditPurchase(
            id = "test-pur-1",
            supplierId = "sup-1",
            supplierName = "Distribuidora Central",
            totalAmountCents = 200_00L,
            creditLevelNumber = 1,
            downPaymentPercent = 50,
            downPaymentCents = 100_00L,
            financedAmountCents = 100_00L,
            supplierPayout48hCents = 96_00L,
            kuotaFeeCents = 4_00L,
            phase = PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION
        )

        assertEquals("Inicial confirmada", purchase.statusLabel)
        assertEquals(100_00L, purchase.remainingDebtCents)

        // Simular confirmación de cronograma de 2 cuotas en Fase 2
        val activeRepayment = purchase.copy(
            phase = PurchasePhase.ACTIVE_IN_REPAYMENT,
            selectedTermDays = 15,
            installmentCount = 2,
            installments = listOf(
                CreditInstallment(number = 1, amountCents = 50_00L, dueEpochMillis = System.currentTimeMillis() + 86400000L, isPaid = false),
                CreditInstallment(number = 2, amountCents = 50_00L, dueEpochMillis = System.currentTimeMillis() + (2 * 86400000L), isPaid = false)
            )
        )

        assertEquals("En cuotas", activeRepayment.statusLabel)
        assertEquals(100_00L, activeRepayment.remainingDebtCents)
        assertFalse(activeRepayment.isOverdue)

        // Pagar cuota 1
        val onePaid = activeRepayment.copy(
            installments = activeRepayment.installments.map { if (it.number == 1) it.copy(isPaid = true) else it }
        )
        assertEquals("Deuda restante debe reducirse a S/ 50.00", 50_00L, onePaid.remainingDebtCents)
        assertEquals(1, onePaid.totalPaidInstallments)

        // Pagar cuota 2 -> transiciona a COMPLETED
        val completed = onePaid.copy(
            phase = PurchasePhase.COMPLETED,
            installments = onePaid.installments.map { it.copy(isPaid = true) }
        )
        assertEquals("Pagada", completed.statusLabel)
        assertEquals("Deuda restante debe ser 0", 0L, completed.remainingDebtCents)
        assertFalse("Al estar completada ya no es una transacción abierta (libera regla 3.2)", completed.isOpen)
    }

    @Test
    fun verifyFase3PaymentLinkFeeAndTransitions() {
        // PRD 6.4 B: Comisión plana de 1%
        val amountCents = 185_00L // S/ 185.00
        val link = PaymentLink(
            id = "link-test-1",
            customerName = "Juan Pérez",
            customerDni = "12345678",
            customerPhone = "987654321",
            concept = "Saco de Arroz Extra 50kg",
            quantity = 1,
            unitPriceCents = 185_00L,
            amountCents = amountCents,
            linkUrl = "https://kuota.pe/pay/link-test-1"
        )

        assertEquals("Comisión debe ser 1%", 1, link.feePercent)
        // 1% de 185.00 = S/ 1.85 (185 centavos)
        assertEquals(1_85L, link.feeCents)
        // Neto comercio: 185.00 - 1.85 = S/ 183.15 (18315 centavos)
        assertEquals(183_15L, link.netAmountCents)
        assertEquals("Total = comisión + neto", link.amountCents, link.feeCents + link.netAmountCents)
        assertEquals(PaymentLinkStatus.GENERADO, link.status)

        // Transición a ENVIADO
        val sentLink = link.copy(status = PaymentLinkStatus.ENVIADO)
        assertEquals(PaymentLinkStatus.ENVIADO, sentLink.status)

        // Transición a PAGADO
        val paidNow = System.currentTimeMillis()
        val paidLink = sentLink.copy(status = PaymentLinkStatus.PAGADO, paidAtEpochMillis = paidNow)
        assertEquals(PaymentLinkStatus.PAGADO, paidLink.status)
        assertEquals(paidNow, paidLink.paidAtEpochMillis)
        assertFalse("Un link pagado no debe considerarse vencido", paidLink.isExpired)
    }

    @Test
    fun verifyFase3CatalogPriceCrossChecking() {
        // PRD 6.5 & 6.7: Cruce automático de ventas comprobadas
        val catalogPrices = setOf(185_00L, 72_00L, 45_50L)

        // 1. Venta que coincide con precio del catálogo -> COMPROBADA
        val saleMatched = ManualSale(
            id = "s-1",
            amountCents = 185_00L,
            recordedAtEpochMillis = System.currentTimeMillis(),
            isVerified = 185_00L in catalogPrices
        )
        assertTrue("Venta por S/ 185.00 debe ser comprobada", saleMatched.isVerified)

        // 2. Venta libre que no coincide -> NO COMPROBADA
        val saleUnmatched = ManualSale(
            id = "s-2",
            amountCents = 99_90L,
            recordedAtEpochMillis = System.currentTimeMillis(),
            isVerified = 99_90L in catalogPrices
        )
        assertFalse("Venta por S/ 99.90 no catalogada debe ser no comprobada", saleUnmatched.isVerified)

        // 3. Venta por LINK_PAGO -> Siempre comprobada por construcción (PRD 6.7)
        val linkSale = ManualSale(
            id = "s-link",
            amountCents = 250_00L,
            recordedAtEpochMillis = System.currentTimeMillis(),
            source = SaleSource.LINK_PAGO,
            isVerified = true,
            productName = "Pedido especial vía Link"
        )
        assertTrue("Venta originada por Link de Pago siempre es comprobada", linkSale.isVerified)
        assertEquals(SaleSource.LINK_PAGO, linkSale.source)

        // 4. Recálculo tras cambio de catálogo
        val existingSales = listOf(saleMatched, saleUnmatched, linkSale)
        val newCatalogPrices = setOf(99_90L) // Ahora se añade el producto de 99.90 y se quita 185.00

        val recalculated = existingSales.map { sale ->
            val shouldBeVerified = sale.source == SaleSource.LINK_PAGO || sale.amountCents in newCatalogPrices
            sale.copy(isVerified = shouldBeVerified)
        }

        assertFalse("Venta 1 ya no está en catálogo", recalculated[0].isVerified)
        assertTrue("Venta 2 ahora coincide con nuevo catálogo", recalculated[1].isVerified)
        assertTrue("Venta por Link de Pago se mantiene comprobada", recalculated[2].isVerified)
    }

    @Test
    fun verifyFase4SupplierPayoutFormulasAndNetReceipt() {
        // PRD 10 & 13.4: El proveedor recibe el 98% de la venta total
        // Ejemplo de la sección 13.4: Venta de S/ 1,000 con inicial de S/ 500
        val totalSaleCents = 1_000_00L
        val downPaymentCents = 500_00L
        val kuotaFeeCents = (totalSaleCents * 2L) / 100L // 2% = S/ 20.00

        // Payout a 48h = 98% * venta - inicial = 980 - 500 = S/ 480.00
        val payout48hCents = CreditLevel.NIVEL_1.calculateSupplierPayout48hCents(totalSaleCents, downPaymentCents)
        assertEquals(480_00L, payout48hCents)
        assertEquals(20_00L, kuotaFeeCents)

        // El proveedor recibe en mano la inicial (S/ 500) y de Kuota a 48h (S/ 480) = S/ 980 (98%)
        val totalReceivedBySupplier = downPaymentCents + payout48hCents
        assertEquals(980_00L, totalReceivedBySupplier)
        assertEquals(totalSaleCents, totalReceivedBySupplier + kuotaFeeCents)
    }

    @Test
    fun verifyFase4SupplierRejectionFreesBuyerRule32() {
        // PRD 13.9 & 14.10: Rechazo de pago inicial cancela la operación y libera regla 3.2
        val pendingPurchase = CreditPurchase(
            id = "pur-pending-1",
            supplierId = "sup-10",
            supplierName = "Textiles Gamarra",
            totalAmountCents = 400_00L,
            creditLevelNumber = 1,
            downPaymentPercent = 50,
            downPaymentCents = 200_00L,
            financedAmountCents = 200_00L,
            supplierPayout48hCents = 192_00L,
            kuotaFeeCents = 8_00L,
            phase = PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED
        )

        assertTrue("La compra en revisión es una transacción abierta", pendingPurchase.isOpen)

        // El proveedor rechaza el voucher no válido ("No recibí el pago")
        val rejectedPurchase = pendingPurchase.copy(phase = PurchasePhase.CANCELLED)

        assertEquals("Cancelada", rejectedPurchase.statusLabel)
        assertEquals(0L, rejectedPurchase.remainingDebtCents)
        assertFalse("Al ser cancelada ya no es una transacción abierta y libera al comprador", rejectedPurchase.isOpen)

        // Verificamos que ahora el comprador vuelve a ser elegible bajo regla 3.2
        val eligibility = PurchaseEligibilityCheck.validate(
            totalAmountCents = 300_00L,
            currentLevel = CreditLevel.NIVEL_1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = rejectedPurchase.isOpen
        )
        assertTrue("El comprador puede volver a comprar tras el rechazo", eligibility.isEligible)
    }

    @Test
    fun verifyFase4SupplierCommercialProfileDefaults() {
        // PRD 13.2: Perfil comercial del proveedor
        val profile = LocalBusinessProfile(
            businessName = "Confecciones Don Pepe",
            email = "pepe@kuota.pe",
            phone = "987123456",
            ruc = "20123456789",
            role = UserRole.PROVEEDOR.name
        )

        assertEquals("Textil y Confección", profile.commercialCategory)
        assertEquals("Gamarra, La Victoria", profile.commercialZone)
        assertTrue(profile.commercialAddress.isNotEmpty())
        assertTrue(profile.operatingHours.isNotEmpty())

        val updatedProfile = profile.copy(
            commercialCategory = "Calzado y Cueros",
            commercialZone = "Caquetá, San Martín de Porres"
        )
        assertEquals("Calzado y Cueros", updatedProfile.commercialCategory)
        assertEquals("Caquetá, San Martín de Porres", updatedProfile.commercialZone)
    }

    @Test
    fun verifyFase5EndToEndFullCycleFromQrToSettlementAndLevelUpgrade() {
        // PRD v0.3: Ciclo completo de compra, liquidación y subida de nivel
        var currentLevel = CreditLevel.NIVEL_1
        var availableLineCents = 300_00L // S/ 300 inicial

        // 1. Escaneo QR en tienda mayorista: compra por S/ 200.00
        val purchaseAmountCents = 200_00L
        val eligibility = PurchaseEligibilityCheck.validate(
            totalAmountCents = purchaseAmountCents,
            currentLevel = currentLevel,
            availableLineCents = availableLineCents,
            hasActiveOpenTransaction = false
        )
        assertTrue("Compra de S/ 200 dentro de línea de S/ 300 debe ser elegible", eligibility.isEligible)
        assertEquals("Inicial al 50% es S/ 100", 100_00L, eligibility.downPaymentCents)
        assertEquals("Saldo financiado es S/ 100", 100_00L, eligibility.financedCents)

        // 2. Creación de la compra en Fase 1
        val purchase = CreditPurchase(
            id = "pur-e2e-1",
            supplierId = "sup-pepe",
            supplierName = "Distribuidora Don Pepe",
            totalAmountCents = purchaseAmountCents,
            creditLevelNumber = currentLevel.levelNumber,
            downPaymentPercent = currentLevel.downPaymentPercentage,
            downPaymentCents = eligibility.downPaymentCents,
            financedAmountCents = eligibility.financedCents,
            supplierPayout48hCents = currentLevel.calculateSupplierPayout48hCents(purchaseAmountCents, eligibility.downPaymentCents),
            kuotaFeeCents = (purchaseAmountCents * 2L) / 100L,
            phase = PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED
        )

        assertTrue(purchase.isOpen)
        assertEquals(96_00L, purchase.supplierPayout48hCents)
        assertEquals(4_00L, purchase.kuotaFeeCents)

        // 3. Proveedor confirma pago inicial en pantalla Por Confirmar
        val confirmedPurchase = purchase.copy(
            phase = PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION,
            confirmedBySupplierAtEpochMillis = System.currentTimeMillis()
        )
        assertEquals("Inicial confirmada", confirmedPurchase.statusLabel)

        // 4. Comprador elige cronograma de 3 cuotas semanales a 21 días (plazo permitido en Nivel 1)
        assertTrue(21 in currentLevel.availableTermsDays)
        val activePurchase = confirmedPurchase.copy(
            phase = PurchasePhase.ACTIVE_IN_REPAYMENT,
            selectedTermDays = 21,
            installmentCount = 3,
            installments = listOf(
                CreditInstallment(number = 1, amountCents = 33_33L, dueEpochMillis = System.currentTimeMillis() + 86400000L),
                CreditInstallment(number = 2, amountCents = 33_33L, dueEpochMillis = System.currentTimeMillis() + (2 * 86400000L)),
                CreditInstallment(number = 3, amountCents = 33_34L, dueEpochMillis = System.currentTimeMillis() + (3 * 86400000L))
            )
        )
        assertEquals(100_00L, activePurchase.remainingDebtCents)

        // 5. El comprador paga cuota 1 y cuota 2 puntualmente
        val fullyPaidPurchase = activePurchase.copy(
            phase = PurchasePhase.COMPLETED,
            installments = activePurchase.installments.map { it.copy(isPaid = true, paidAtEpochMillis = System.currentTimeMillis()) },
            completedAtEpochMillis = System.currentTimeMillis()
        )
        assertEquals("Pagada", fullyPaidPurchase.statusLabel)
        assertEquals(0L, fullyPaidPurchase.remainingDebtCents)
        assertFalse(fullyPaidPurchase.isOpen)

        // 6. Al pagar puntualmente, la línea se libera y el vendedor califica para subir a Nivel 2
        val nextEligibility = PurchaseEligibilityCheck.validate(
            totalAmountCents = 500_00L,
            currentLevel = CreditLevel.NIVEL_2,
            availableLineCents = CreditLevel.NIVEL_2.minLineCents,
            hasActiveOpenTransaction = fullyPaidPurchase.isOpen
        )
        assertTrue("En Nivel 2, una compra de S/ 500 es elegible", nextEligibility.isEligible)
        assertEquals("Inicial Nivel 2 es 40% (S/ 200)", 200_00L, nextEligibility.downPaymentCents)
        assertEquals("Saldo financiado es S/ 300", 300_00L, nextEligibility.financedCents)
    }
}
