package com.washathomes.views.main.washee.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.ItemService
import com.washathomes.R
import com.washathomes.views.main.washee.home.WasheeHomeFragment

class ServicesAdapter(
    var washeeHomeFragment: WasheeHomeFragment,
    var services: ArrayList<ItemService>
    ) :
    RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.service_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val service = services[position]

        holder.service.text = service.title

        holder.service.isChecked = service.checked

        holder.service.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                service.checked = true
                holder.service.isChecked = true
            }else{
                service.checked = false
                holder.service.isChecked = false
            }
            notifyDataSetChanged()
            washeeHomeFragment.setCategoryItemsRV()
        }
    }

    override fun getItemCount(): Int {
        return services.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var service: CheckBox = itemView.findViewById(R.id.service_CB)
    }

}