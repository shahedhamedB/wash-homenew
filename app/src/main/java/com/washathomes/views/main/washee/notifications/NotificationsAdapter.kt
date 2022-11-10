package com.washathomes.views.main.washee.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.washathomes.apputils.modules.Notification
import com.washathomes.R

class NotificationsAdapter(
    var washeeNotificationsFragment: WasheeNotificationsFragment,
    var notificatios: ArrayList<Notification>
) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val notification = notificatios[position]

        holder.title.text = notification.title
        holder.description.text = notification.title
        holder.title.text = notification.title

        holder.delete.setOnClickListener { washeeNotificationsFragment.deleteNotification(notification) }

        holder.itemView.setOnClickListener { washeeNotificationsFragment.readNotification(notification) }
    }

    override fun getItemCount(): Int {
        return notificatios.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.notification_header)
        var description: TextView = itemView.findViewById(R.id.notification_desc)
        var time: TextView = itemView.findViewById(R.id.notification_clock)
        var delete: ImageView = itemView.findViewById(R.id.delete)
    }

}