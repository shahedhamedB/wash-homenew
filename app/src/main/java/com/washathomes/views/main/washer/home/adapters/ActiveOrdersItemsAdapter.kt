package com.washathomes.views.main.washer.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.ActiveOrderObj
import com.washathomes.R
import com.washathomes.views.main.washer.home.WasherHomeFragment

class ActiveOrdersItemsAdapter(
    var washerHomeFragment: WasherHomeFragment,
    var activeOrders: ArrayList<ActiveOrderObj>
) :
    RecyclerView.Adapter<ActiveOrdersItemsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.active_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val activeOrder = activeOrders[position]

        holder.time.text = activeOrder.time
        holder.id.text = "#"+activeOrder.id
        holder.date.text = activeOrder.date
        holder.price.text = ""+activeOrder.total_amount
        holder.orderStatus.text = activeOrder.status_title

        if (activeOrder.status == "0" || activeOrder.status == "1"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.mid_grey))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.mid_grey))
            Glide.with(context!!).load(R.drawable.order_status_bullet_grey).into(holder.bulletIndicator)
        }else if (activeOrder.status == "8"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.bright_green))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.bright_green))
            Glide.with(context!!).load(R.drawable.order_status_bullet_green).into(holder.bulletIndicator)
        }else if (activeOrder.status == "2" || activeOrder.status == "7"){
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.blue))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.blue))
//            Glide.with(context!!).load(R.drawable.order_status_bullet_blue).into(holder.bulletIndicator)
        }else{
            holder.orderState.setTextColor(context!!.resources.getColor(R.color.red))
            holder.leftIndicator.setBackgroundColor(context!!.resources.getColor(R.color.red))
            Glide.with(context!!).load(R.drawable.order_status_bullet_red).into(holder.bulletIndicator)
        }

        holder.itemView.setOnClickListener { washerHomeFragment.orderDetails(activeOrder) }
    }

    override fun getItemCount(): Int {
        return activeOrders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView = itemView.findViewById(R.id.order_clock)
        var id: TextView = itemView.findViewById(R.id.order_no)
        var orderState: TextView = itemView.findViewById(R.id.order_state)
        var orderStatus: TextView = itemView.findViewById(R.id.order_status)
        var date: TextView = itemView.findViewById(R.id.order_date)
        var price: TextView = itemView.findViewById(R.id.order_price)
        var leftIndicator:View = itemView.findViewById(R.id.order_left_indicator)
        var bulletIndicator:ImageView = itemView.findViewById(R.id.order_bullet_indicator)
    }

}