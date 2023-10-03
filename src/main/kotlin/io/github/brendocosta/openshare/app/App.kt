package io.github.brendocosta.openshare.app

import android.app.Application
import android.content.Context

public final class App: android.app.Application {

    companion object {

        private lateinit var instance: App
        public fun getInstance(): App { return instance }

    }

    public lateinit var context: Context

    init { instance = this }
    constructor(): super()

    public fun getAppName(): String {

        return this.context.getApplicationInfo().loadLabel(this.context.packageManager).toString()

    }

    public override fun onCreate() {

        super.onCreate()
        this.context = this.applicationContext

    }

    public override fun onLowMemory() {

	    super.onLowMemory()

	}

}