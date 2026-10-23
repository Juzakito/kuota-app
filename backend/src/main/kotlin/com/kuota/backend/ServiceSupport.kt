package com.kuota.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal val storeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}

internal fun <T> readJsonFile(file: Path, serializer: KSerializer<T>): T? {
    if (!Files.exists(file)) return null
    return runCatching { storeJson.decodeFromString(serializer, Files.readString(file)) }.getOrNull()
}

internal fun <T> writeJsonFileAtomically(file: Path, serializer: KSerializer<T>, value: T) {
    file.parent?.let { Files.createDirectories(it) }
    val temporary = file.resolveSibling("${file.fileName}.tmp")
    Files.writeString(temporary, storeJson.encodeToString(serializer, value))
    runCatching {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }.getOrElse {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING)
    }
}

/** Devuelve los claims del usuario si envió un Bearer JWT válido, o null si no está autenticado. */
internal fun ApplicationCall.optionalUser(): JwtUserClaims? {
    val header = request.headers["Authorization"].orEmpty()
    return header.takeIf { it.startsWith("Bearer ") }
        ?.substringAfter("Bearer ")
        ?.let(JwtConfig::verifyToken)
}

/** Responde 401 y devuelve null si falta el Bearer JWT o es inválido. */
internal suspend fun ApplicationCall.requireUser(): JwtUserClaims? {
    val claims = optionalUser()
    if (claims == null) {
        respond(
            HttpStatusCode.Unauthorized,
            apiError("AUTH_TOKEN_INVALID", "Inicia sesión nuevamente para continuar.")
        )
    }
    return claims
}

internal fun maskTail(value: String, visible: Int = 4): String =
    if (value.length <= visible) "****" else "****" + value.takeLast(visible)

internal fun env(name: String): String? = System.getenv(name)?.trim()?.takeIf(String::isNotEmpty)
