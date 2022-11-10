package com.washathomes.base.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : ViewDataBinding, V>(@LayoutRes val layoutResId: Int) : RecyclerView.Adapter<BaseViewHolder<T, V>>() {

    private lateinit var layoutInflater: LayoutInflater
    var binding: T? = null

    var items: List<V>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onClickListener: ((item: V?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, V> {
        layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, layoutResId, parent, false)
        return BaseViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, V>, position: Int) {
        holder.bind(items?.get(position), this)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    abstract fun bindView(binding: T, item: V?)

}
