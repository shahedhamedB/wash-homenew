package com.washathomes.views.introduction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.Language
import com.washathomes.R

class LanguagesAdapter(private val context: Context, private val arrayList: java.util.ArrayList<Language?>) : BaseAdapter() {
    private lateinit var title: TextView
    private lateinit var icon: ImageView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.language_spinner_item, parent, false)
        title = convertView.findViewById(R.id.title)
        icon = convertView.findViewById(R.id.icon)
        title.text = arrayList[position]?.name
        Glide.with(context).load(arrayList[position]?.icons).into(icon)
        return convertView
    }
}