package hu.blu3berry.avalon

import android.app.Application
import hu.blu3berry.avalon.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AvalonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@AvalonApplication)
            androidLogger()
        }
    }
}
