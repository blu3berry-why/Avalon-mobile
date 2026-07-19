package hu.blu3berry.avalon.core.data.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
