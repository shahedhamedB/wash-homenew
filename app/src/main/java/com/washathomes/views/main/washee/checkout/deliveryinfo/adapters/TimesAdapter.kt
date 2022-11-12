package com.washathomes.views.main.washee.checkout.deliveryinfo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.washathomes.R

class TimesAdapter(private val context: Context, private val arrayList: java.util.ArrayList<String>) : BaseAdapter() {
    private lateinit var title: TextView
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
        convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        title = convertView.findViewById(R.id.title)
        title.text = arrayList[position]
        return convertView
    }
}