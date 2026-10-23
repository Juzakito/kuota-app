package com.kouta.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.kouta.app.core.designsystem.theme.KoutaTheme
import com.kouta.app.core.designsystem.theme.ThemeController
import com.kouta.app.core.designsystem.theme.ThemeMode
import com.kouta.app.core.security.InactivityLockManager
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.core.security.ScreenSecurityManager
import com.kouta.app.core.util.NotificationHelper
import com.kouta.app.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val pendingDeepLink = mutableStateOf<String?>(null)

    private val allowedDeepLinks = setOf(
        "kuota://home",
        "kuota://sales",
        "kuota://financing",
        "kuota://payments",
        "kuota://kyc",
        "kuota://profile",
        "kuota://support",
        "kuota://notifications"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PinSecurityManager.initialize(this)
        enableEdgeToEdge()
        ThemeController.load(this)
        if (savedInstanceState == null) consumeDeepLink(intent)

        setContent {
            val themeMode by ThemeController.mode.collectAsState()
            val pinState by PinSecurityManager.stateFlow.collectAsState()
            val isSensitiveActive by ScreenSecurityManager.isSensitiveScreenActive.collectAsState()

            // Aplica FLAG_SECURE dinámicamente si la app está bloqueada o en pantallas de alta confidencialidad
            LaunchedEffect(pinState.isLocked, pinState.isConfigured, isSensitiveActive) {
                val shouldProtect = (pinState.isConfigured && pinState.isLocked) || isSensitiveActive
                ScreenSecurityManager.applyPolicy(this@MainActivity, shouldProtect)
            }

            // Comprobador periódico de inactividad de sesión (cada 15 segundos)
            LaunchedEffect(pinState.isLocked, pinState.isConfigured) {
                while (pinState.isConfigured && !pinState.isLocked) {
                    delay(15_000L)
                    InactivityLockManager.checkInactivity(this@MainActivity)
                }
            }

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            KoutaTheme(darkTheme = darkTheme) {
                MainScreen(
                    deepLink = pendingDeepLink.value,
                    onDeepLinkHandled = { pendingDeepLink.value = null }
                )
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        InactivityLockManager.notifyUserActivity()
    }

    override fun onStart() {
        super.onStart()
        val state = PinSecurityManager.onAppForegrounded(this)
        ScreenSecurityManager.applyPolicy(this, state.isConfigured && state.isLocked)
    }

    override fun onStop() {
        super.onStop()
        // Si no es una rotación o cambio de configuración, bloquea inmediatamente al pasar a segundo plano
        if (!isChangingConfigurations) {
            if (PinSecurityManager.isPinConfigured(this)) {
                ScreenSecurityManager.applyPolicy(this, true)
                PinSecurityManager.onAppBackgrounded(this)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeDeepLink(intent)
    }

    private fun consumeDeepLink(intent: Intent?) {
        val link = intent?.getStringExtra(NotificationHelper.EXTRA_DEEP_LINK) ?: return
        intent.removeExtra(NotificationHelper.EXTRA_DEEP_LINK)
        val sanitized = link.trim().lowercase().substringBefore("?")
        if (sanitized in allowedDeepLinks) {
            pendingDeepLink.value = link
        }
    }
}
