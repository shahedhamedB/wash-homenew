package com.washathomes.views.main.washer.more.settings.manageservices

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.washathomes.apputils.modules.Service
import com.washathomes.R

class ServicesAdapter(
    manageServicesFragment: ManageServicesFragment,
    services: ArrayList<Service>
) :
    RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {
    lateinit var manageServicesFragment: ManageServicesFragment
    var selectedPosition = 0
    var context: Context? = null
    lateinit var services: ArrayList<Service>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.service_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val service: Service = services[position]

        if (service.isSelected!!){
            Glide.with(context!!).load(service.icon_selected).into(holder.serviceImage)
        }else{
            Glide.with(context!!).load(service.icon).into(holder.serviceImage)
        }
        holder.serviceTitle.text = service.title
        holder.serviceCV.setOnClickListener {
            if (service.isSelected!!){
                Glide.with(context!!).load(service.icon).into(holder.serviceImage)
                service.isSelected = false
                holder.serviceLayout.setBackgroundColor(context!!.resources.getColor(R.color.light_grey))
            }else{
                service.isSelected = true
                holder.serviceLayout.setBackgroundColor(context!!.resources.getColor(R.color.blue))
                Glide.with(context!!).load(service.icon_selected).into(holder.serviceImage)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return services.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var serviceImage: ImageView
        var serviceTitle: TextView
        var serviceCV: MaterialCardView
        var serviceLayout: ConstraintLayout

        init {
            serviceImage = itemView.findViewById(R.id.service_icon)
            serviceTitle = itemView.findViewById(R.id.service_title)
            serviceCV = itemView.findViewById(R.id.service)
            serviceLayout = itemView.findViewById(R.id.service_layout)
        }
    }

    init {
        this.manageServicesFragment = manageServicesFragment
        this.services = services
    }
}