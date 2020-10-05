package com.munki.android_kotlin_mvvm_fastscroll.ui.main

import android.os.Bundle
import com.munki.android_kotlin_mvvm_fastscroll.BR
import com.munki.android_kotlin_mvvm_fastscroll.R
import com.munki.android_kotlin_mvvm_fastscroll.databinding.ActivityMainBinding
import com.munki.android_kotlin_mvvm_fastscroll.ui.base.BaseActivity
import javax.inject.Inject

/**
 * MainActivity
 * @author 나비이쁜이
 * @since 2020.10.05
 */
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(), MainNavigator {

    // - Databinding & Viewmodel
    private var mBinding: ActivityMainBinding ? = null
    @Inject internal lateinit var mViewModel : MainViewModel

    /************************************************************************************************************************************************/

    /**
     * Binding variable
     */
    override val bindingVariable: Int get() = BR.main

    /**
     * Resource Layout
     */
    override val layoutId: Int get() = R.layout.activity_main

    /**
     * ViewModel
     */
    override val viewModel: MainViewModel get() {
        mViewModel.setNavigation(this)
        return mViewModel
    }

    /************************************************************************************************************************************************/

    /**
     * onCreate
     */
    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Databinding & Navigation Binding
        mBinding = mViewDataBinding

        // init
        init()
    }

    /**
     * init
     */
    override fun init() {
        super.init()

        // FastScrollView Setting
        mBinding!!.fastScrollView.setKeywordList(mViewModel.wordList)
        mBinding!!.fastScrollView.setRecyclerView(mBinding!!.rvWord)
    }
}

