package com.washathomes.views.main.washee.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.CategoryItem
import com.washathomes.R
import com.washathomes.views.main.washee.home.WasheeHomeFragment

class CategoryItemsAdapter(
    var washeeHomeFragment: WasheeHomeFragment,
    var categoryItems: ArrayList<CategoryItem>
) :
    RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {
    var context: Context? = null
    var hasServices = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = categoryItems[position]

        var hasServices = false

        holder.title.text = item.title
        if (item.image.isNotEmpty()){
            Glide.with(context!!).load(item.image).into(holder.icon)
        }
        if (item.favorite == "0"){
            Glide.with(context!!).load(R.drawable.ic_baseline_favorite_24).into(holder.fav)
        }else{
            Glide.with(context!!).load(R.drawable.ic_baseline_favorite_24_red).into(holder.fav)
        }
        item.price = 0.0
        for (service in item.items_service){
            if (service.checked){
                item.price += service.price
                hasServices = true
            }
        }

        holder.price.text = ""+item.price

        val servicesAdapter = ServicesAdapter(washeeHomeFragment, item.items_service)
        holder.servicesRV.adapter = servicesAdapter
        holder.servicesRV.layoutManager = GridLayoutManager(context, 3)

        holder.addToBasket.isEnabled = hasServices

        holder.addToBasket.setOnClickListener {
            if (holder.notesEdt.text.isNotEmpty()){
                item.notes = holder.notesEdt.text.toString()
            }else{
                item.notes = ""
            }
            washeeHomeFragment.addToBasket(item)
        }

        holder.fav.setOnClickListener {
            if (item.favorite == "0"){
                washeeHomeFragment.addFavorite(item.id)
            }else{
                washeeHomeFragment.removeFavorite(item.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.item_title)
        var fav: ImageView = itemView.findViewById(R.id.item_favorite)
        var icon: ImageView = itemView.findViewById(R.id.item_icon)
        var servicesRV: RecyclerView = itemView.findViewById(R.id.services_RV)
        var price: TextView = itemView.findViewById(R.id.item_price)
        var addToBasket: TextView = itemView.findViewById(R.id.add_to_basket)
        var notesEdt: EditText = itemView.findViewById(R.id.notes_edt)
    }

}