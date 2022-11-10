package com.washathomes.views.main.courier.more.orderhistory.adapters

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
import com.washathomes.views.main.courier.more.orderhistory.CourierViewItemsFragment

class ItemsAdapter(
    var courierViewItemsFragment: CourierViewItemsFragment,
    var cartItems: ArrayList<OrderItem>
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
        val cartItem = cartItems[position]

        holder.title.text = cartItem.title+" ("+cartItem.quantity+")"
        if (cartItem.image.isNotEmpty()){
            Glide.with(context!!).load(cartItem.image).into(holder.itemIcon)
        }
        holder.price.text = ""+cartItem.price

        var services = ""
        for (service in cartItem.servcie){
            services = services + service.title+", "
        }

        holder.services.text = services

    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.review_item_name)
        var services: TextView = itemView.findViewById(R.id.review_item_services)
        var itemIcon: ImageView = itemView.findViewById(R.id.item_icon)
        var price: TextView = itemView.findViewById(R.id.review_item_price)
        var viewNotes: LinearLayoutCompat = itemView.findViewById(R.id.view_items_notes)
    }

}