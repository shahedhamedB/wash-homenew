package com.washathomes.apputils.base.extension.ui

import com.afollestad.materialdialogs.MaterialDialog
import com.washathomes.R

fun MaterialDialog.generateErrorDialog(
    dialogMessage: String,
    positiveBlock: () -> Unit,
    negativeBlock: () -> Unit,
    isCancelable: Boolean = false
) {
    show {
        message(text = dialogMessage)
        cancelable(isCancelable)
        positiveButton(R.string.try_again) {
            positiveBlock()
        }
        negativeButton(R.string.close_button) {
            negativeBlock()
        }
    }
}

fun MaterialDialog.generateActionDialog(dialogMessage: String) {
    show {
        message(text = dialogMessage)
        positiveButton(R.string.ok)
    }
}

fun MaterialDialog.generateActionDialog(title: String, dialogMessage: String) {
    show {
        title(text = title)
        message(text = dialogMessage)
        positiveButton(R.string.ok)
    }
}

fun MaterialDialog.generateActionDialog(dialogMessage: String, block: () -> Unit) {
    show {
        message(text = dialogMessage)
        positiveButton(R.string.ok) {
            block()
        }
    }.cancelable(false)
}

fun MaterialDialog.generateAdviceDialog(
    dialogMessage: String,
    positiveBlock: () -> Unit,
    isCancelable: Boolean = false
) {
    show {
        message(text = dialogMessage)
        cancelable(isCancelable)
        positiveButton(R.string.ok) {
            positiveBlock()
        }
        negativeButton(R.string.close_button) {
            this.dismiss()
        }
    }
}

fun MaterialDialog.generateAdviceDialogWithMessage(
    title: String,
    dialogMessage: String,
    positiveBlock: () -> Unit,
    isCancelable: Boolean = false
) {
    show {
        title(text = title)
        message(text = dialogMessage)
        cancelable(isCancelable)
        positiveButton(R.string.ok) {
            positiveBlock()
        }
        negativeButton(R.string.close_button) {
            this.dismiss()
        }
    }
}


