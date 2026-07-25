package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.core.data.di.platformCoreDataModule
import org.koin.plugin.module.dsl.startKoin
import org.koin.dsl.KoinAppDeclaration

// Single Koin entry point for all platforms. The annotated graph comes from AvalonApp; only the
// expect/actual platform bindings are loaded as a DSL module. `CoreDataModule` configures the
// generated Api singletons eagerly, so nothing may reach the network before this has run.
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin<AvalonApp> {
        config?.invoke(this)
        modules(platformCoreDataModule)
    }
}
