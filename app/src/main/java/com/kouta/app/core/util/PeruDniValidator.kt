package com.kouta.app.core.util

/**
 * Validador oficial del Documento Nacional de Identidad (DNI) del Perú.
 *
 * Implementa las reglas formales de RENIEC:
 * 1. Longitud exacta de 8 dígitos numéricos.
 * 2. Algoritmo de ponderación módulo 11 para el dígito verificador.
 * 3. Exclusión de números de prueba o secuencias inválidas (ej. "00000000", "12345678", "88888888").
 */
object PeruDniValidator {

    private val PONDERATED_FACTORS = intArrayOf(3, 2, 7, 6, 5, 4, 3, 2)

    // Tablas de correlación oficial RENIEC para el dígito verificador (número y letra)
    private val VERIFICATION_NUMBERS = intArrayOf(6, 7, 8, 9, 0, 1, 1, 2, 3, 4, 5)
    private val VERIFICATION_LETTERS = charArrayOf('K', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J')

    private val BLACKLISTED_TEST_DNIS = setOf(
        "00000000",
        "11111111",
        "22222222",
        "33333333",
        "44444444",
        "55555555",
        "66666666",
        "77777777",
        "88888888",
        "99999999"
    )

    data class DniValidationResult(
        val isValid: Boolean,
        val sanitizedDni: String,
        val expectedCheckDigit: Int?,
        val expectedCheckChar: Char?,
        val errorMessage: String? = null
    )

    /**
     * Valida la estructura básica del DNI peruano de 8 dígitos.
     */
    fun validateDni(rawDni: String): DniValidationResult {
        val sanitized = rawDni.trim().filter { it.isDigit() }

        if (sanitized.length != 8) {
            return DniValidationResult(
                isValid = false,
                sanitizedDni = sanitized,
                expectedCheckDigit = null,
                expectedCheckChar = null,
                errorMessage = "El DNI debe contener exactamente 8 dígitos numéricos."
            )
        }

        if (sanitized in BLACKLISTED_TEST_DNIS) {
            return DniValidationResult(
                isValid = false,
                sanitizedDni = sanitized,
                expectedCheckDigit = null,
                expectedCheckChar = null,
                errorMessage = "El número ingresado corresponde a un DNI de prueba no válido."
            )
        }

        val checkIndex = calculateReniecIndex(sanitized)
        val expectedDigit = VERIFICATION_NUMBERS[checkIndex]
        val expectedChar = VERIFICATION_LETTERS[checkIndex]

        return DniValidationResult(
            isValid = true,
            sanitizedDni = sanitized,
            expectedCheckDigit = expectedDigit,
            expectedCheckChar = expectedChar
        )
    }

    /**
     * Calcula el índice de verificación de RENIEC mediante suma ponderada módulo 11.
     */
    fun calculateReniecIndex(dni8Digits: String): Int {
        var sum = 0
        for (i in 0 until 8) {
            val digit = dni8Digits[i].digitToInt()
            sum += digit * PONDERATED_FACTORS[i]
        }
        val remainder = sum % 11
        val result = 11 - remainder
        return if (result >= 11) 0 else result
    }

    /**
     * Enmascara el DNI para logs o vistas no críticas (cumplimiento Ley N° 29733).
     * Ejemplo: "12345678" -> "****5678"
     */
    fun maskDni(dni: String): String {
        val clean = dni.filter { it.isDigit() }
        return if (clean.length == 8) "****${clean.takeLast(4)}" else "********"
    }
}
