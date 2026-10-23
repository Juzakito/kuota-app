package com.kouta.app.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kouta.app.core.designsystem.components.SecurityNotice
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.TokenManager
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.RequestOtpBody
import com.kouta.app.data.remote.VerifyOtpBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun unreachableMessage(serverUrl: String) =
    "No pudimos conectar con el servidor de Kuota ($serverUrl). Revisa tu conexión o la dirección del servidor en Notificaciones › Avanzado."

/** Vincula el teléfono con el backend vía OTP para enviar reclamos y recibir avisos del servidor. */
@Composable
fun AccountLinkDialog(initialPhone: String, onDismiss: () -> Unit, onLinked: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf(initialPhone.filter(Char::isDigit).takeLast(9)) }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var demoCode by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun describe(outcome: ApiOutcome<*>): String = when (outcome) {
        is ApiOutcome.Unreachable -> unreachableMessage(outcome.serverUrl)
        is ApiOutcome.Rejected -> outcome.message
        ApiOutcome.SessionExpired -> "El código no es válido o venció. Solicita uno nuevo."
        is ApiOutcome.Ok -> ""
    }

    fun requestCode() {
        busy = true
        error = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) { KuotaApiClient.call(context) { requestOtp(RequestOtpBody(phone)) } }
            busy = false
            if (outcome is ApiOutcome.Ok) {
                codeSent = true
                demoCode = outcome.value.otpCodeDebug
            } else {
                error = describe(outcome)
            }
        }
    }

    fun verifyCode() {
        busy = true
        error = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) { KuotaApiClient.call(context) { verifyOtp(VerifyOtpBody(phone, code)) } }
            if (outcome is ApiOutcome.Ok) {
                withContext(Dispatchers.IO) {
                    TokenManager(context).saveToken(outcome.value.accessToken)
                    AppSettings(context).apply {
                        registeredFingerprint = null
                        linkedPhone = phone
                    }
                    runCatching { KuotaSync.runOnce(context) }
                }
                KuotaSync.enqueue(context)
                busy = false
                onLinked()
            } else {
                busy = false
                error = describe(outcome)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Vincular tu cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Con tu cuenta vinculada enviamos tus reclamos al equipo de atención y te avisamos de tus cuotas aunque la app esté cerrada.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 9) phone = value; error = null },
                    label = { Text("Celular") },
                    prefix = { Text("+51  ") },
                    singleLine = true,
                    enabled = !codeSent && !busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                if (codeSent) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 6) code = value; error = null },
                        label = { Text("Código de 6 dígitos") },
                        singleLine = true,
                        enabled = !busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    demoCode?.let {
                        Text("Modo demostración: tu código es $it", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { codeSent = false; code = "" }, enabled = !busy) { Text("Cambiar número") }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                SecurityNotice()
            }
        },
        confirmButton = {
            if (codeSent) {
                Button(onClick = ::verifyCode, enabled = code.length == 6 && !busy) { Text(if (busy) "Verificando…" else "Verificar") }
            } else {
                Button(onClick = ::requestCode, enabled = phone.length == 9 && !busy) { Text(if (busy) "Enviando…" else "Enviar código") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Ahora no") } }
    )
}

