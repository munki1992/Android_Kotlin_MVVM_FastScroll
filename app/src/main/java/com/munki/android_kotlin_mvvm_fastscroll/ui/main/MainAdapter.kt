package com.munki.android_kotlin_mvvm_fastscroll.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import com.munki.android_kotlin_mvvm_fastscroll.R
import com.munki.android_kotlin_mvvm_fastscroll.databinding.ItemRecyclerviewBinding
import com.munki.android_kotlin_mvvm_fastscroll.ui.base.BaseViewHolder
import com.munki.android_kotlin_mvvm_fastscroll.ui.custom.RecyclerFastScroller

/**
 * MainAdapter
 * @author 나비이쁜이
 * @since 2020.10.05
 */
class MainAdapter internal constructor (private val mContext: Context) : RecyclerFastScroller.KoreanIndexerRecyclerAdapter<BaseViewHolder<String?>>(), RecyclerFastScroller.FastScrollable {

    // - Word List
    private var dataList: ObservableArrayList<String?>? = null

    /**
     * onCreateViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview, parent, false))
    }

    /**
     * onBindViewHolder
     */
    override fun onBindViewHolder(holder : BaseViewHolder<String?>, position: Int) {
        holder.bind(dataList!![holder.layoutPosition], position)
    }

    /**
     * Item Count
     */
    override fun getItemCount() : Int {
        return if (dataList != null) dataList!!.size else 0
    }

    /************************************************************************************************************************************************/

    /**
     * Set Word
     */
    fun setItem(words: ObservableArrayList<String?>?) {
        if (dataList != null) return

        dataList = words
        notifyDataSetChanged()
    }

    /************************************************************************************************************************************************/

    /**
     * setBubbleText
     */
    override
    fun setBubbleText(position: Int) : String? {
        return dataList!![position]
    }

    /************************************************************************************************************************************************/

    /**
     * ViewHolder
     */
    inner class ViewHolder constructor(view: View) : BaseViewHolder<String?>(view) {

        /**
         * Databinding
         */
        private var mBinding: ItemRecyclerviewBinding? = DataBindingUtil.bind(view)

        /**
         * Bind
         */
        override fun bind(itemVo: String?, position: Int?) {
            mBinding!!.textView.text = itemVo
        }
    }
}