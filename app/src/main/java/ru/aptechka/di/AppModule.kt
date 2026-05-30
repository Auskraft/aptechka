package ru.aptechka.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.aptechka.data.CatalogSeeder
import ru.aptechka.data.db.AptechkaDatabase
import ru.aptechka.data.repository.CatalogRepository
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.KitRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.ui.screens.add.AddDrugViewModel
import ru.aptechka.ui.screens.expiry.ExpiryViewModel
import ru.aptechka.ui.screens.kits.KitDetailViewModel
import ru.aptechka.ui.screens.kits.KitsViewModel
import ru.aptechka.ui.screens.meddetail.MedDetailViewModel
import ru.aptechka.ui.screens.scanner.ScannerViewModel
import ru.aptechka.ui.screens.shopping.ShoppingViewModel

val databaseModule = module {
    single { AptechkaDatabase.getDatabase(androidContext()) }
    single { get<AptechkaDatabase>().kitDao() }
    single { get<AptechkaDatabase>().userDrugDao() }
    single { get<AptechkaDatabase>().drugBatchDao() }
    single { get<AptechkaDatabase>().shoppingDao() }
    single { get<AptechkaDatabase>().reminderDao() }
    single { get<AptechkaDatabase>().catalogDrugDao() }
}

val repositoryModule = module {
    single { KitRepository(get()) }
    single { DrugRepository(get(), get()) }
    single { ShoppingRepository(get()) }
    single { CatalogRepository(get()) }
    single { CatalogSeeder(androidContext(), get()) }
}

val viewModelModule = module {
    viewModel { KitsViewModel(get(), get()) }
    viewModel { (kitId: Long) -> KitDetailViewModel(kitId, get(), get(), get()) }
    viewModel { (kitId: Long) -> AddDrugViewModel(kitId, get(), get()) }
    viewModel { (drugId: Long) -> MedDetailViewModel(drugId, get(), get()) }
    viewModel { ExpiryViewModel(get(), get(), get()) }
    viewModel { ShoppingViewModel(get()) }
    viewModel { ScannerViewModel(get()) }
}

val appModules = listOf(databaseModule, repositoryModule, viewModelModule)
