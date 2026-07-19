package hu.blu3berry.avalon.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// Single Koin entry point for all platforms. Modules wired in later phases.
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules()
    }
}
