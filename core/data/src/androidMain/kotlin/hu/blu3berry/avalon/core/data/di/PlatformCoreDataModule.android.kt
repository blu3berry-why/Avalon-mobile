package hu.blu3berry.avalon.core.data.di

import hu.blu3berry.avalon.core.data.network.provideHttpClientEngineFactory
import hu.blu3berry.avalon.core.data.storage.SecureSettingsFactory
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { provideHttpClientEngineFactory().create() }
    single<SecureSettingsFactory> { SecureSettingsFactory(androidContext()) }
}
