package com.munki.android_kotlin_mvvm_fastscroll.di.builder

import com.munki.android_kotlin_mvvm_fastscroll.ui.main.MainActivity
import com.munki.android_kotlin_mvvm_fastscroll.ui.main.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * (Dagger) Module - 생성 공급자
 * @author 나비이쁜이
 * @since 2020.10.05
 */
@Module
abstract class ActivityBuilder {

    /**
     * Module 지정(Component 위치 지정) -> Inject 위치 지정
     */

    // MainActivity
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindMainActivity(): MainActivity?
}