package hu.blu3berry.avalon.core.data.di

import hu.blu3berry.avalon.core.data.network.createHttpClient
import hu.blu3berry.avalon.core.data.session.SessionManagerImpl
import hu.blu3berry.avalon.core.data.storage.SecureSettingsFactory
import hu.blu3berry.avalon.core.data.storage.SecureSettingsTokenStorage
import hu.blu3berry.avalon.core.data.storage.TokenStorage
import hu.blu3berry.avalon.core.domain.session.SessionManager
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Per-platform data bindings (HttpClientEngine factory + Android Context for encrypted prefs).
 * `includes()` into [coreDataModule] so consumers load a single cross-feature module.
 */
expect val platformCoreDataModule: Module

val coreDataModule: Module = module {
    includes(platformCoreDataModule)

    single<TokenStorage> { SecureSettingsTokenStorage(get<SecureSettingsFactory>().create()) }
    single<SessionManager> { SessionManagerImpl() }
    single<HttpClient> {
        createHttpClient(
            engine = get(),
            tokenStorage = get(),
            sessionManager = get(),
        )
    }
}
