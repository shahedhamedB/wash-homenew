package com.washathomes.views.main.washee.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.Ad
import com.washathomes.R
import com.washathomes.views.main.washee.WasheeMainActivity

class ImagesAdapter(private val ads: ArrayList<Ad>, private val washeeMainActivity: WasheeMainActivity) :
    PagerAdapter() {
    override fun getCount(): Int {
        return ads.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =
            washeeMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.image_slider_layout, container, false)
        container.addView(view)

        val img: ImageView = view.findViewById(R.id.icon)
        Glide.with(washeeMainActivity).load(ads[position].image).into(img)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}
