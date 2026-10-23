package com.kouta.app.core.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Avisa a las pantallas abiertas que llegaron notificaciones o cambió el estado de un reclamo. */
object KuotaEvents {
    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision

    fun bump() {
        _revision.value = _revision.value + 1
    }
}
