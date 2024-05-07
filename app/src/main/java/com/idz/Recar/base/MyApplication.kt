package com.idz.Recar.base

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    object Globals {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        VollyQueue.getInstance(this.applicationContext)
        Globals.appContext = applicationContext
    }
}