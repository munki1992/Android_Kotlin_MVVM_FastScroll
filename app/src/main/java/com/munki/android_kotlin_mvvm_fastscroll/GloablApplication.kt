package com.munki.android_kotlin_mvvm_fastscroll

import android.content.Context
import androidx.multidex.MultiDex
import com.munki.android_kotlin_mvvm_fastscroll.di.component.ApplicationComponent
import com.munki.android_kotlin_mvvm_fastscroll.di.component.DaggerApplicationComponent
import dagger.android.HasAndroidInjector
import dagger.android.support.DaggerApplication

/**
 * Activity에서 공통적으로 적용되는 상위 MultiDexApplication
 * @author 나비이쁜이
 * @since 2020.10.05
 */
class GlobalApplication : DaggerApplication(), HasAndroidInjector {

    /**
     * ContributesAndroidInjector를 사용하기 위한 Injector
     */
    override fun applicationInjector(): ApplicationComponent? {
        return DaggerApplicationComponent.builder().application(this).build()
    }

    /**
     * attachBaseContext
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        // MultiDex init
        MultiDex.install(this)
    }
}