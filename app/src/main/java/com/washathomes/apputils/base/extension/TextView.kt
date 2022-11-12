package com.washathomes.base.extension

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

fun TextView.setClickableHighLightedText(
    textToHighlight: String,
    onClickListener: View.OnClickListener?
) {
    val tvt = text.toString()
    var ofe = tvt.indexOf(textToHighlight, 0)
    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(p0: View) {
            onClickListener?.onClick(p0)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = android.graphics.Color.parseColor("#2E86FC")
        }
    }
    val wordToSpan = android.text.SpannableString(text)
    var ofs = 0
    while (ofs < tvt.length && ofe != -1) {
        ofe = tvt.indexOf(textToHighlight, ofs)
        if (ofe == -1) break else {
            wordToSpan.setSpan(
                clickableSpan,
                ofe,
                ofe + textToHighlight.length,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setText(wordToSpan, android.widget.TextView.BufferType.SPANNABLE)
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }
        ofs = ofe + 1
    }
}
