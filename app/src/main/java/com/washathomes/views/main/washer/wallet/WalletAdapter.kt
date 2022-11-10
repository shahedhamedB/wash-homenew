package com.washathomes.views.main.washer.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.TransList
import com.washathomes.R

class WalletAdapter(
    var transList: ArrayList<TransList>
) :
    RecyclerView.Adapter<WalletAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_incoming_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = transList[position]

        holder.amount.text = item.amount
        holder.orderNo.text = "#"+item.order_id
        holder.date.text = item.date
    }

    override fun getItemCount(): Int {
        return transList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var amount: TextView = itemView.findViewById(R.id.tv_amount)
        var orderNo: TextView = itemView.findViewById(R.id.tv_order_no)
        var date: TextView = itemView.findViewById(R.id.tv_date)
    }

}