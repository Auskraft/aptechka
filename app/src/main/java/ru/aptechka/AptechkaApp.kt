package ru.aptechka

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import ru.aptechka.data.CatalogSeeder
import ru.aptechka.di.appModules

class AptechkaApp : Application(), KoinComponent {

    private val appScope = CoroutineScope(SupervisorJob())
    private val catalogSeeder: CatalogSeeder by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AptechkaApp)
            modules(appModules)
        }
        appScope.launch { catalogSeeder.seedIfEmpty() }
    }
}
