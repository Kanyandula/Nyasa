package com.kanyandula.nyasa.di.auth

import com.kanyandula.nyasa.ui.auth.ForgotPasswordFragment
import com.kanyandula.nyasa.ui.auth.LauncherFragment
import com.kanyandula.nyasa.ui.auth.LoginFragment
import com.kanyandula.nyasa.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}