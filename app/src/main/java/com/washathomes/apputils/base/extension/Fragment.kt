package com.washathomes.base.extension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.washathomes.R
import com.washathomes.base.ui.MultiStateView

fun Fragment.runContextNotNull(block: (Context) -> Unit) {
    this.context?.let {
        block(it)
    }
}

