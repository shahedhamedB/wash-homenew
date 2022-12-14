package com.washathomes.base.ui

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

abstract class ViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

    protected val context: Context = itemView.context
    protected val resources: Resources = itemView.resources

    abstract fun bind(data: T)

    override val containerView: View = itemView
}