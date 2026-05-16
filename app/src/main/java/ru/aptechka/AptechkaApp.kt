package ru.aptechka

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.aptechka.di.appModules

class AptechkaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AptechkaApp)
            modules(appModules)
        }
    }
}
