package com.washathomes.views.main.washee.checkout.basket.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.CartItem
import com.washathomes.R
import com.washathomes.views.main.washee.checkout.basket.BasketFragment

class CartItemsAdapter(
    var basketFragment: BasketFragment,
    var cartItems: ArrayList<CartItem>
) :
    RecyclerView.Adapter<CartItemsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.basket_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val cartItem = cartItems[position]

        holder.title.text = cartItem.title
        if (cartItem.image.isNotEmpty()){
            Glide.with(context!!).load(cartItem.image).into(holder.itemIcon)
        }
        holder.price.text = ""+cartItem.price
        holder.quantity.text = "x"+cartItem.quantity

        var services = ""
        for (service in cartItem.servcie){
            services = services + service.title+", "
        }

        holder.services.text = services

        holder.increaseQnt.setOnClickListener {
            val qnt = cartItem.quantity.toInt()+1
            basketFragment.updateQuantity(cartItem.id, qnt.toString())
        }
        holder.decreaseQnt.setOnClickListener {
            var qnt = cartItem.quantity.toInt()
            if (qnt != 1){
                qnt--
                basketFragment.updateQuantity(cartItem.id, qnt.toString())
            }
        }
        holder.deleteItem.setOnClickListener {
            basketFragment.deleteCartItemDialog(cartItem.id)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.basket_item_name)
        var services: TextView = itemView.findViewById(R.id.basket_item_services)
        var quantity: TextView = itemView.findViewById(R.id.basket_item_quantity)
        var decreaseQnt: ImageView = itemView.findViewById(R.id.basket_item_decrease)
        var increaseQnt: ImageView = itemView.findViewById(R.id.basket_item_increase)
        var deleteItem: ImageView = itemView.findViewById(R.id.delete_icon)
        var itemIcon: ImageView = itemView.findViewById(R.id.item_icon)
        var price: TextView = itemView.findViewById(R.id.basket_item_price)
    }

}