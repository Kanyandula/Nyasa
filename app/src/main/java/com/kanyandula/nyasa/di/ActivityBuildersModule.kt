package com.kanyandula.nyasa.di


import com.kanyandula.nyasa.di.auth.AuthFragmentBuildersModule
import com.kanyandula.nyasa.di.auth.AuthModule
import com.kanyandula.nyasa.di.auth.AuthScope
import com.kanyandula.nyasa.di.auth.AuthViewModelModule
import com.kanyandula.nyasa.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

}