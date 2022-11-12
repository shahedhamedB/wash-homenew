package com.washathomes.views.registration.washerregistration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.washathomes.apputils.modules.AccountTypeIntro
import com.washathomes.R

class IntroAdapter(contents: ArrayList<AccountTypeIntro>, washerRegistrationActivity: WasherRegistrationActivity) :
    PagerAdapter() {
    private val contents: ArrayList<AccountTypeIntro>
    private val washerRegistrationActivity: WasherRegistrationActivity
    override fun getCount(): Int {
        return contents.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =
            washerRegistrationActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.intro_slider_layout, container, false)
        container.addView(view)
        val background = view.findViewById<ImageView>(R.id.slider_background)
        val title = view.findViewById<TextView>(R.id.title)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val description = view.findViewById<TextView>(R.id.description)

        Glide.with(view).load(contents[position].image).into(background)
        Glide.with(view).load(contents[position].icons).into(icon)
        title.text = contents[position].title
        description.text = contents[position].description
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    init {
        this.contents = contents
        this.washerRegistrationActivity = washerRegistrationActivity
    }

}
