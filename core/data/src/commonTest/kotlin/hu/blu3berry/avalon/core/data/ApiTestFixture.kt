package hu.blu3berry.avalon.core.data

import hu.blu3berry.avalon.core.data.network.AvalonJson
import hu.blu3berry.avalon.core.data.network.createHttpClient
import hu.blu3berry.avalon.core.data.storage.InMemoryTokenStorage
import hu.blu3berry.avalon.core.data.storage.TokenStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import hu.blu3berry.avalon.core.data.generated.auth.Api as AuthApi
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * kmpgen generates each API as a process-wide `object` whose `client` the generated services
 * read at call time, so pointing a repository at a `MockEngine` means mutating global state.
 * That is the hazard this fixture contains: without it a test that forgets to install an engine
 * silently runs against whichever engine the previous test left behind, and two tests running
 * concurrently overwrite each other's.
 *
 * [runApiTest] is therefore the only supported way to reach a generated API from a test:
 * it holds a lock for the whole test body, so overlapping tests serialize instead of
 * clobbering each other, and it installs a fresh client per test, so nothing leaks forward.
 */
private val apiMutex = Mutex()

/**
 * Runs [body] with both generated API singletons pointed at [engine].
 *
 * Serialized against every other `runApiTest` in the process. Both APIs are installed, not just
 * the one under test, so a repository that reaches for the wrong deployment fails on the
 * assertion rather than on a stale client.
 */
fun runApiTest(
    engine: HttpClientEngine,
    tokenStorage: TokenStorage = InMemoryTokenStorage(),
    body: suspend TestScope.() -> Unit,
): TestResult = runTest {
    apiMutex.withLock {
        installMockEngine(engine, tokenStorage)
        try {
            body()
        } finally {
            // The next runApiTest builds its own; closing here keeps engines from piling up
            // for the lifetime of the test JVM.
            GameApi.client.close()
            AuthApi.client.close()
        }
    }
}

private fun installMockEngine(engine: HttpClientEngine, tokenStorage: TokenStorage) {
    listOf(GameApi, AuthApi).forEach { api ->
        api.baseUrl = Url("http://avalon.test/")
        api.updateClient(
            json = AvalonJson,
            createHttpClient = { decorator ->
                createHttpClient(
                    engine = engine,
                    tokenStorage = tokenStorage,
                    decorator = decorator,
                )
            },
        )
    }
}
