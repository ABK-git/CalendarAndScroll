package com.example.theplanning1

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

//Realmデータベース
class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}