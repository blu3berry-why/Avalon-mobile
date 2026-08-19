package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.core.data.di.CoreDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

/**
 * The app's Koin graph. Every `@KoinViewModel` under this package is picked up by the scan, and
 * the Koin compiler plugin resolves each constructor at build time (`compileSafety` in
 * `build.gradle.kts`), so a view model asking for something nothing provides fails the build.
 *
 * `CoreDataModule` is included rather than loaded at runtime so the plugin can check across the
 * module boundary. Only `platformCoreDataModule` stays DSL — expect/actual bindings annotations
 * cannot express — and `initKoin` loads it alongside.
 */
@Module(includes = [CoreDataModule::class])
@ComponentScan("hu.blu3berry.avalon")
@KoinApplication
class AvalonApp
