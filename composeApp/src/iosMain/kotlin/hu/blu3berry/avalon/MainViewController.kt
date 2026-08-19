package hu.blu3berry.avalon

import androidx.compose.ui.window.ComposeUIViewController
import hu.blu3berry.avalon.di.initKoin
import org.koin.mp.KoinPlatformTools

// iOS has no Application/main to start Koin from — the Swift side only asks for this
// controller, so the graph is started here, guarded because a second controller would
// otherwise re-run startKoin and throw.
fun MainViewController() = ComposeUIViewController {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) initKoin()
    App()
}
