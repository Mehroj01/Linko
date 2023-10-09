package com.neb.linko

import android.app.Application
import com.neb.linko.di.component.AppComponent
import com.neb.linko.di.component.DaggerAppComponent

class App:Application() {

    companion object{
        lateinit var appComponent:AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().build()
    }

}