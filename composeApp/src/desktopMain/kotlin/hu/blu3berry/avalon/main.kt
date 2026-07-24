package hu.blu3berry.avalon

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import hu.blu3berry.avalon.di.initKoin

fun main() {
    initKoin()
    application {
        Window(onCloseRequest = ::exitApplication, title = "Avalon") {
            App()
        }
    }
}
