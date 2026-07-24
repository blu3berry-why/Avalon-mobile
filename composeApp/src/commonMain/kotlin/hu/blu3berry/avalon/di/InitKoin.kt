package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.core.data.di.coreDataModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// Single Koin entry point for all platforms. `coreDataModule` configures the generated Api
// singletons eagerly, so nothing may reach the network before this has run.
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreDataModule, presentationModule)
    }
}
