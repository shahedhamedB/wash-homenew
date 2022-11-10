package com.washathomes.views.main.courier.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.ActiveOrder
import com.washathomes.R
import com.washathomes.views.main.courier.home.CourierHomeFragment

class ActiveOrdersAdapter(
    var courierHomeFragment: CourierHomeFragment,
    var activeOrders: ArrayList<ActiveOrder>
) :
    RecyclerView.Adapter<ActiveOrdersAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.active_order_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val activeOrder = activeOrders[position]

        holder.title.text = activeOrder.Date

        val adapter = ActiveOrdersItemsAdapter(courierHomeFragment, activeOrder.Orders)
        holder.orders.adapter = adapter
        holder.orders.layoutManager = LinearLayoutManager(context)

    }

    override fun getItemCount(): Int {
        return activeOrders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.washer_section_header_title)
        var orders: RecyclerView = itemView.findViewById(R.id.orders_RV)
    }

}