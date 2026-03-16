package com.github.junhee8649.cleancalendar

import android.app.Application
import com.github.junhee8649.cleancalendar.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CleanCalendarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CleanCalendarApp)
            modules(AppModule.module)
        }
    }
}
