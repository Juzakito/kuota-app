package com.kouta.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.core.util.NotificationHelper
import com.kouta.app.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val pendingDeepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PinSecurityManager.initialize(this)
        enableEdgeToEdge()
        ThemeController.load(this)
        if (savedInstanceState == null) consumeDeepLink(intent)
        setContent {
            val themeMode by ThemeController.mode.collectAsState()
            val pinState by PinSecurityManager.stateFlow.collectAsState()

            // Protege la ventana en multitarea / capturas mientras la app esté bloqueada por PIN
            LaunchedEffect(pinState.isLocked, pinState.isConfigured) {
                if (pinState.isConfigured && pinState.isLocked) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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

    override fun onStart() {
        super.onStart()
        val state = PinSecurityManager.onAppForegrounded(this)
        if (state.isConfigured && state.isLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Si no es una rotación o cambio de configuración, bloquea inmediatamente al pasar a segundo plano
        if (!isChangingConfigurations) {
            if (PinSecurityManager.isPinConfigured(this)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
        // Solo enlaces internos conocidos; cualquier otro valor se ignora.
        if (link.startsWith("kuota://")) pendingDeepLink.value = link
    }
}
