package com.msusman.splinedemo

import android.app.Application
import com.msusman.splinedemo.spline.SplineEngine
import design.spline.runtime.RustBridge
import timber.log.Timber

class App : Application() {
    lateinit var splineEngine: SplineEngine

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Timber.plant(Timber.DebugTree())

        if (RustBridge.context == null) {
            RustBridge.context = this.applicationContext
            // should only be called once
            RustBridge.engineInit(this.applicationContext)
        }
        splineEngine = SplineEngine(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        splineEngine.destroy()
    }
}