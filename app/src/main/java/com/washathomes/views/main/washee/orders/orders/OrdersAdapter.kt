package com.washathomes.views.main.washee.orders.orders

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.WasheeActiveOrder
import com.washathomes.R

class OrdersAdapter(
    var ordersFragment: OrdersFragment,
    var orders: ArrayList<WasheeActiveOrder>
) :
    RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {
    var context: Context? = null
    var hasServices = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.order_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val order = orders[position]

        holder.date.text = order.date
        holder.orderNo.text = "#"+order.id
        holder.price.text = ""+order.total_amount
        holder.orderStatus.text = order.status_title

        if (order.status == "0" || order.status == "1"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.mid_grey))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.mid_grey))
            Glide.with(context!!).load(R.drawable.order_status_bullet_grey).into(holder.bulletIndicator)
        }else if (order.status == "8"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.bright_green))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.bright_green))
            Glide.with(context!!).load(R.drawable.order_status_bullet_green).into(holder.bulletIndicator)
        }else if (order.status == "2" || order.status == "7"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.blue))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.blue))
//            Glide.with(context!!).load(R.drawable.order_status_bullet_blue).into(holder.bulletIndicator)
        }else{
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.red))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.red))
            Glide.with(context!!).load(R.drawable.order_status_bullet_red).into(holder.bulletIndicator)
        }

        holder.itemView.setOnClickListener { ordersFragment.orderDetails(order) }

    }

    override fun getItemCount(): Int {
        return orders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var orderNo: TextView = itemView.findViewById(R.id.order_no)
        var price: TextView = itemView.findViewById(R.id.order_price)
        var date: TextView = itemView.findViewById(R.id.order_date)
        var orderState: TextView = itemView.findViewById(R.id.order_state)
        var orderStatus: TextView = itemView.findViewById(R.id.order_status)
        var leftIndicator:View = itemView.findViewById(R.id.order_left_indicator)
        var bulletIndicator: ImageView = itemView.findViewById(R.id.order_bullet_indicator)
    }

}