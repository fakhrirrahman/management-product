package com.example.managementproduct

import android.app.Application
import com.example.managementproduct.data.AppContainer
import com.example.managementproduct.data.DefaultAppContainer

class ManagementProductApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
