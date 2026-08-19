package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.auth.LoginViewModel
import hu.blu3berry.avalon.auth.RegisterViewModel
import hu.blu3berry.avalon.core.data.di.coreDataModule
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    viewModel { (sessionExpired: Boolean) ->
        LoginViewModel(authRepository = get(), sessionExpired = sessionExpired)
    }
    viewModelOf(::RegisterViewModel)
}

// Single Koin entry point for all platforms.
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreDataModule, appModule)
    }
}
