package com.kouta.app

import android.app.Application
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.core.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KoutaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        KuotaSync.schedulePeriodic(this)
        KuotaSync.enqueue(this)
    }
}
