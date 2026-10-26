package org.example.project

import android.app.Application
import org.multipaz.context.initializeApplication

class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeApplication(applicationContext)
    }
}
