package hu.blu3berry.avalon.core.data.di

// BuildKonfig's package comes from the module path via `Project.pathToPackageName()`.
import hu.blu3berry.avalon_core_data.BuildKonfig
import hu.blu3berry.avalon.core.data.network.AvalonJson
import hu.blu3berry.avalon.core.data.network.createHttpClient
import hu.blu3berry.avalon.core.data.repository.AuthRepositoryImpl
import hu.blu3berry.avalon.core.data.repository.GameRepositoryImpl
import hu.blu3berry.avalon.core.data.repository.LobbyRepositoryImpl
import hu.blu3berry.avalon.core.data.repository.UserRepositoryImpl
import hu.blu3berry.avalon.core.data.session.SessionManagerImpl
import hu.blu3berry.avalon.core.data.storage.SecureSettingsFactory
import hu.blu3berry.avalon.core.data.storage.SecureSettingsTokenStorage
import hu.blu3berry.avalon.core.data.storage.TokenStorage
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.repository.GameRepository
import hu.blu3berry.avalon.core.domain.repository.LobbyRepository
import hu.blu3berry.avalon.core.domain.repository.UserRepository
import hu.blu3berry.avalon.core.domain.session.SessionManager
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.Url
import org.koin.core.module.Module
import org.koin.dsl.module
import hu.blu3berry.avalon.core.data.generated.auth.Api as AuthApi
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * Per-platform data bindings (HttpClientEngine factory + Android Context for encrypted prefs).
 * `includes()` into [coreDataModule] so consumers load a single cross-feature module.
 */
expect val platformCoreDataModule: Module

val coreDataModule: Module = module {
    includes(platformCoreDataModule)

    single<TokenStorage> { SecureSettingsTokenStorage(get<SecureSettingsFactory>().create()) }
    single<SessionManager> { SessionManagerImpl() }

    // The kmpgen `Api` objects are process-wide singletons that build their own client, so
    // they are configured once here rather than injected. The game API and ForwardAuth are
    // separate deployments with separate base URLs, hence two `Api` objects.
    single<ApiConfigurator> {
        ApiConfigurator(
            engine = get(),
            tokenStorage = get(),
        ).also { it.configure() }
    }

    single<AuthRepository> {
        get<ApiConfigurator>()
        AuthRepositoryImpl(tokenStorage = get(), sessionManager = get())
    }
    single<UserRepository> {
        get<ApiConfigurator>()
        UserRepositoryImpl(sessionManager = get())
    }
    single<LobbyRepository> {
        get<ApiConfigurator>()
        LobbyRepositoryImpl(sessionManager = get())
    }
    single<GameRepository> {
        get<ApiConfigurator>()
        GameRepositoryImpl(sessionManager = get())
    }
}

/**
 * Points both generated `Api` singletons at their deployment and at Avalon's client config.
 * Injected (and thereby run) by every repository binding — the generated `Api` objects are
 * unusable until this has happened, and Koin resolving it once is cheaper than each
 * repository re-checking.
 */
class ApiConfigurator(
    private val engine: HttpClientEngine,
    private val tokenStorage: TokenStorage,
) {
    fun configure() {
        GameApi.baseUrl = Url(BuildKonfig.GAME_BASE_URL)
        GameApi.updateClient(json = AvalonJson, createHttpClient = ::buildClient)

        AuthApi.baseUrl = Url(BuildKonfig.AUTH_BASE_URL)
        AuthApi.updateClient(json = AvalonJson, createHttpClient = ::buildClient)
    }

    private fun buildClient(decorator: HttpClientConfig<*>.() -> Unit) =
        createHttpClient(
            engine = engine,
            tokenStorage = tokenStorage,
            decorator = decorator,
        )
}
