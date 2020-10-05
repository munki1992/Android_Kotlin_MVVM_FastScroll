package com.munki.android_kotlin_mvvm_fastscroll.ui.main

import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.RecyclerView
import com.munki.android_kotlin_mvvm_fastscroll.GlobalApplication
import com.munki.android_kotlin_mvvm_fastscroll.ui.base.BaseViewModel

/**
 * MainViewModel
 * @author 나비이쁜이
 * @since 2020.10.05
 */
class MainViewModel internal constructor(application : GlobalApplication, var wordList : ObservableArrayList<String>) : BaseViewModel<MainNavigator>(application) {

    // - Adapter
    var adapter: MainAdapter = MainAdapter(getApplication())

    /************************************************************************************************************************************************/

    /* Listener Databinding */

    companion object {

        // [Binding] setAdapter
        @JvmStatic
        @BindingAdapter("setWordListAdapter")
        fun bindWordListAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
            recyclerView.adapter = adapter
        }

        // [Binding] setItem
        @JvmStatic
        @BindingAdapter("setWordListItem")
        fun bindWordListItem(recyclerView: RecyclerView, dataList: ObservableArrayList<String?>) {
            (recyclerView.adapter as MainAdapter?)?.setItem(dataList)
        }
    }
}