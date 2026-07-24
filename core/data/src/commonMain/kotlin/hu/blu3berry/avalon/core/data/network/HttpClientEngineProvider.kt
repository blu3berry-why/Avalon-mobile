package hu.blu3berry.avalon.core.data.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*>
