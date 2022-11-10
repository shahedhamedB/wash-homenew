package com.washathomes.base.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this, Observer { it?.let { action(it) } })
}

inline fun <T> Observable<T>.sendRequest(
    crossinline success: (T) -> Unit, crossinline error: (Throwable) -> Unit = {}
) {
    subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            success(it)
        },
            {
                Timber.d("BaseViewModel.SendRequest-> Error on request : ${it.message}")
                error(it)
            }).dispose()
}