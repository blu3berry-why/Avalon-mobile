package hu.blu3berry.avalon

import androidx.compose.ui.window.ComposeUIViewController
import hu.blu3berry.avalon.di.initKoin
import platform.UIKit.UIViewController

// iOS entry point — the Swift shell only instantiates this once, so initKoin here is safe.
fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
