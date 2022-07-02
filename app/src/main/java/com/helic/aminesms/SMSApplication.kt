package com.helic.aminesms

import android.app.Application
import com.helic.aminesms.utils.Constants.PROJECT_KEY_QONVERSION
import com.qonversion.android.sdk.Qonversion
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SMSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Qonversion.setDebugMode()
        Qonversion.launch(this, PROJECT_KEY_QONVERSION, false)
    }
}