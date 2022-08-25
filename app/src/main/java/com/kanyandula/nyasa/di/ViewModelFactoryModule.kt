package com.kanyandula.nyasa.di

import androidx.lifecycle.ViewModelProvider
import com.kanyandula.nyasa.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}