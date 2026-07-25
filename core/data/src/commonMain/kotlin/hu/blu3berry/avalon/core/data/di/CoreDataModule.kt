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
import org.koin.core.annotation.Module as KoinModule
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.module.Module
import hu.blu3berry.avalon.core.data.generated.auth.Api as AuthApi
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * Per-platform data bindings: the `HttpClientEngine` factory, and the `SecureSettingsFactory`
 * that needs an Android `Context`. Both are `expect`/`actual`, which annotations cannot express,
 * so this half stays plain DSL and its two types are consumed as [Provided] below. Consumers
 * load it alongside the annotated graph — see `initKoin`.
 */
expect val platformCoreDataModule: Module

/**
 * The data graph. Annotated rather than DSL so the Koin compiler plugin can resolve it at build
 * time together with the presentation graph that consumes it (decision D7 revisited: the plugin
 * needs definitions it can introspect, and `@Provided` covers the expect/actual boundary that
 * pushed the original DSL choice).
 */
@KoinModule
class CoreDataModule {

    @Single
    fun tokenStorage(@Provided factory: SecureSettingsFactory): TokenStorage =
        SecureSettingsTokenStorage(factory.create())

    @Single
    fun sessionManager(): SessionManager = SessionManagerImpl()

    /**
     * The kmpgen `Api` objects are process-wide singletons that build their own client, so they
     * are configured once here rather than injected. The game API and ForwardAuth are separate
     * deployments with separate base URLs, hence two `Api` objects.
     *
     * `createdAtStart` because the generated `Api` objects are unusable until this has run and
     * nothing else forces it: Koin builds it during `startKoin`, before any repository can be
     * resolved. A repository added later inherits the guarantee without opting in.
     */
    @Single(createdAtStart = true)
    fun apiConfigurator(
        @Provided engine: HttpClientEngine,
        tokenStorage: TokenStorage,
    ): ApiConfigurator = ApiConfigurator(engine = engine, tokenStorage = tokenStorage)
        .also { it.configure() }

    @Single
    fun authRepository(tokenStorage: TokenStorage, sessionManager: SessionManager): AuthRepository =
        AuthRepositoryImpl(tokenStorage = tokenStorage, sessionManager = sessionManager)

    @Single
    fun userRepository(sessionManager: SessionManager): UserRepository =
        UserRepositoryImpl(sessionManager = sessionManager)

    @Single
    fun lobbyRepository(sessionManager: SessionManager): LobbyRepository =
        LobbyRepositoryImpl(sessionManager = sessionManager)

    @Single
    fun gameRepository(sessionManager: SessionManager): GameRepository =
        GameRepositoryImpl(sessionManager = sessionManager)
}

/**
 * Points both generated `Api` singletons at their deployment and at Avalon's client config.
 * Bound `createdAtStart` so it has run before any repository can be resolved — the generated
 * `Api` objects are unusable until then.
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
