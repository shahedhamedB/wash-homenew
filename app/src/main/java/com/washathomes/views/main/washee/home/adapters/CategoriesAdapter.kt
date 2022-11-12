package com.washathomes.views.main.washee.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.Category
import com.washathomes.R
import com.washathomes.views.main.washee.home.WasheeHomeFragment

class CategoriesAdapter(
    var washeeHomeFragment: WasheeHomeFragment,
    var categories: ArrayList<Category>,
    var selectedPosition: Int
) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.category_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]

        holder.title.text = category.title

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }

        if (position == selectedPosition){
            holder.title.setTextColor(context!!.resources.getColor(R.color.blue))
            holder.line.visibility = View.VISIBLE
            washeeHomeFragment.selectedCategory = category.id
            washeeHomeFragment.getCategoryItems(category.id)
        }else{
            holder.title.setTextColor(context!!.resources.getColor(R.color.mid_grey))
            holder.line.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var line: View = itemView.findViewById(R.id.line)
    }

}