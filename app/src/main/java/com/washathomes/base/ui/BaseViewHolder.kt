package com.washathomes.base.ui

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder<T : ViewDataBinding, V>(val binding: T) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: V?, adapter: BaseAdapter<T, V>) {
        adapter.bindView(binding, item)
        if (adapter.onClickListener != null) {
            itemView.setOnClickListener {
                if (adapterPosition >= 0 && adapter.items?.size ?: 0 > adapterPosition)
                    adapter.onClickListener?.invoke(item)
                else
                    return@setOnClickListener
            }
        }
    }

}