package com.kanyandula.nyasa.di.auth

import androidx.lifecycle.ViewModel
import com.kanyandula.nyasa.di.ViewModelKey
import com.kanyandula.nyasa.ui.auth.AuthViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}