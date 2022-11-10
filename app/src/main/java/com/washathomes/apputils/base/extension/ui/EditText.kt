package com.washathomes.base.extension.ui

import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

fun EditText.getString(): String {
    return this.text.trim().toString()
}

fun EditText.isEmpty(): Boolean {
    return this.text.toString().isEmpty()
}

fun EditText.errorStateChangeListener(inputLayout: TextInputLayout) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (!inputLayout.error.isNullOrEmpty())
                inputLayout.error = null
        }

    })
}

fun EditText.setInputTypeAsPassword() {
    this.transformationMethod = PasswordTransformationMethod()
}