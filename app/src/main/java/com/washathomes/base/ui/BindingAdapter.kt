package com.washathomes.base.ui

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.washathomes.R


class BindingAdapter {

    object ImageUrlBindingAdapter {

        @BindingAdapter("imageUrl")
        @JvmStatic
        fun setImageView(view: ImageView, imageUrl: String?) {
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(view.context)
                    .load(imageUrl)
                    .priority(Priority.IMMEDIATE)
                    .placeholder(R.color.black)
                    .into(view)
                    .waitForLayout()
            }
        }
    }

    object RoundImageUrlBindingAdapter {

        @BindingAdapter("roundImageUrl")
        @JvmStatic
        fun setImageView(view: ImageView, imageUrl: String?) {
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(view.context)
                    .load(imageUrl)
                    .priority(Priority.IMMEDIATE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)

            }
        }
    }



    object VisibilityBindingAdapter {
        @BindingAdapter("is_visible")
        @JvmStatic
        fun setVisibility(view: View, value: Boolean) {
            view.visibility = if (value) View.VISIBLE else View.GONE
        }
    }
}

@BindingAdapter("isBold")
fun setBold(view: TextView, isBold: Boolean) {
    if (isBold) {
        view.setTypeface(null, Typeface.BOLD)
    } else {
        view.setTypeface(null, Typeface.NORMAL)
    }
}


@BindingAdapter("src")
fun setResource(view: ImageView, resource: Int) {
    view.setImageResource(resource)
}

@BindingAdapter("htmlText")
fun convertHtmlToStr(view: TextView, html: String?) {
    view.text = html?.let { Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY) }
}



@BindingAdapter("layout_constraintWidth_percent")
fun widthPercent(view: View, size: Float?) {
    (view.layoutParams as? ConstraintLayout.LayoutParams)?.matchConstraintPercentWidth = size ?: 1f
    view.requestLayout()
}