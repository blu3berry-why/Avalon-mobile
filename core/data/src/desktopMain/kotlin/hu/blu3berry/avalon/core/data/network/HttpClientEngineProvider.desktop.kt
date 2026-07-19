package hu.blu3berry.avalon.core.data.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
