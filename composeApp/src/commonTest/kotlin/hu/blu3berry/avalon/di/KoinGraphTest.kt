package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.auth.AuthViewModel
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.session.SessionManager
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * The compiler plugin proves the graph resolves; this proves it actually starts — the eager
 * `ApiConfigurator` in `coreDataModule` runs during `startKoin`, so a broken data-layer binding
 * fails here rather than on the first screen.
 */
class KoinGraphTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun `initKoin starts a graph that can build the auth screen's dependencies`() {
        initKoin()

        val koin = KoinPlatformTools.defaultContext().get()
        assertNotNull(koin.get<AuthRepository>())
        assertNotNull(koin.get<SessionManager>())
        assertNotNull(koin.get<AuthViewModel>())
    }
}
