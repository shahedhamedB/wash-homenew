package com.washathomes.views.main.washer.home.viewitems

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.OrderItem
import com.washathomes.R

class ItemsAdapter(
    var items: ArrayList<OrderItem>
) :
    RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]

        holder.title.text = item.title+" ("+item.quantity+")"
        if (item.image.isNotEmpty()){
            Glide.with(context!!).load(item.image).into(holder.itemIcon)
        }
        holder.price.text = ""+item.price

        var services = ""
        for (service in item.servcie){
            services = services + service.title+", "
        }

        holder.services.text = services
        holder.notes.text = item.note

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.review_item_name)
        var services: TextView = itemView.findViewById(R.id.review_item_services)
        var itemIcon: ImageView = itemView.findViewById(R.id.item_icon)
        var price: TextView = itemView.findViewById(R.id.review_item_price)
        var notes: TextView = itemView.findViewById(R.id.notes)
    }

}