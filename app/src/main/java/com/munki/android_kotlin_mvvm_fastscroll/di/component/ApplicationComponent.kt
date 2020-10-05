package com.munki.android_kotlin_mvvm_fastscroll.di.component

import com.munki.android_kotlin_mvvm_fastscroll.GlobalApplication
import com.munki.android_kotlin_mvvm_fastscroll.di.builder.ActivityBuilder
import com.munki.android_kotlin_mvvm_fastscroll.di.module.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Dagger를 사용하기 위한 Application 최상단 init를 위한 Component
 * @author 나비이쁜이
 * @since 2020.10.05
 */
@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuilder::class])
interface ApplicationComponent : AndroidInjector<GlobalApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: GlobalApplication): Builder
        fun build(): ApplicationComponent
    }

    override fun inject(app: GlobalApplication)
}