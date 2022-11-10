package com.washathomes.views.main.washer.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.PendingOrderObj
import com.washathomes.R
import com.washathomes.views.main.washer.home.WasherHomeFragment

class PendingOrdersItemsAdapter(
    var washerHomeFragment: WasherHomeFragment,
    var activeOrders: ArrayList<PendingOrderObj>
) :
    RecyclerView.Adapter<PendingOrdersItemsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.pending_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val activeOrder = activeOrders[position]

        holder.time.text = activeOrder.remain_min + " " + context!!.resources.getString(R.string.mins_left)
        holder.id.text = "#"+activeOrder.id
        holder.price.text = ""+activeOrder.total_amount

        holder.itemView.setOnClickListener { washerHomeFragment.pendingOrderDetails(activeOrder) }
    }

    override fun getItemCount(): Int {
        return activeOrders.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView = itemView.findViewById(R.id.time_left)
        var id: TextView = itemView.findViewById(R.id.order_no)
        var price: TextView = itemView.findViewById(R.id.order_price)
    }

}