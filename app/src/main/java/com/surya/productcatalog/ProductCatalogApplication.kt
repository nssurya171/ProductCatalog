package com.surya.productcatalog

import android.app.Application

/** Created once when the app process starts; owns the AppContainer for the whole app. */
class ProductCatalogApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
