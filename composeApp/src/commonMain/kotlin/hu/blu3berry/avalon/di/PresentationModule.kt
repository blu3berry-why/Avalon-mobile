package hu.blu3berry.avalon.di

import hu.blu3berry.avalon.auth.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::AuthViewModel)
}
