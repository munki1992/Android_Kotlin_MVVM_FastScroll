package com.munki.android_kotlin_mvvm_fastscroll.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dagger.android.AndroidInjection

/**
 * (MVVM) BaseActivity
 * B = databinding / M = viewModel
 * @author 나비이쁜이
 * @since 2020.10.05
 */
abstract class BaseActivity<B: ViewDataBinding?, M : BaseViewModel<*>?> : AppCompatActivity() {

    /**
     * Current DataBinding & ViewModel
     */
    var mViewDataBinding: B ? = null
    private var mViewModel: M ? = null

    /**
     * 레이아웃으로 정의한 variable - name을 BR class에서 가져옵니다.
     * @return variable id
     */
    abstract val bindingVariable: Int

    /**
     * 상속받는 현재 resoucre id를 가져옵니다.
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * 상속받는 현재 ViewModel을 가져옵니다.
     */
    abstract val viewModel: M

    /**
     * 상속받는 현재 ui의 DataBinding을 적용합니다.
     */
    private fun performDataBinding() {
        // Activity의 ViewDatabinding을 가져옵니다.
        mViewDataBinding = DataBindingUtil.setContentView<B>(this, layoutId)

        /**
         * (true) View Model이 존재하는 경우 getViewModel()
         * (false) View Model이 존재하지 않는 경우 mViewModel
         */
        mViewModel = if (mViewModel == null) viewModel else mViewModel

        // Binding Model를 적용
        mViewDataBinding!!.setVariable(bindingVariable, mViewModel)

        // Binding을 즉시 실행
        mViewDataBinding!!.executePendingBindings()
    }

    /************************************************************************************************************************************************/

    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Dagger Injection init
        AndroidInjection.inject(this)

        // super onCreate
        super.onCreate(savedInstanceState)

        // Databinding
        performDataBinding()
    }

    /**
     * init
     */
    protected open fun init() {}
}