package com.kouta.app.core.designsystem.theme

import android.content.Context
import com.kouta.app.data.local.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeController {
    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode = _mode.asStateFlow()

    fun load(context: Context) {
        val saved = AppSettings(context).themeMode
        _mode.value = ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
    }

    fun set(context: Context, mode: ThemeMode) {
        AppSettings(context).themeMode = mode.name
        _mode.value = mode
    }
}
