package com.washathomes.apputils.base

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class BaseViewModel(
    private val disposable: CompositeDisposable = CompositeDisposable(),
    val stateLiveData: MutableLiveData<State> = MutableLiveData()
) :
    ViewModel() {

    //var stateLiveData: MutableLiveData<State> = MutableLiveData()

    open fun handleIntent(extras: Bundle) {}

    fun Disposable.track() {
        disposable.add(this)
    }

    inline fun <T> Observable<T>.sendRequest(crossinline success: (T) -> Unit) {
        sendRequest(success) {
            Timber.d("BaseViewModel.SendRequest-> Error on request : ${it.message}")
            if (it is CompositeException && it.exceptions.size > 0) {
                it.exceptions.forEach { exception -> Timber.e(exception) }
            }
        }
    }

    inline fun <T> Observable<T>.sendRequest(
        isLogin: Boolean = false,
        stateType: StateType = StateType.DEFAULT,
        requestType: RequestType = RequestType.CUSTOM,
        crossinline successHandler: (T) -> Unit
    ) {
        sendRequest(isLogin, stateType, requestType, successHandler) {
            Timber.e(it)
        }
    }

    inline fun <T> Observable<T>.sendRequest(
        isLogin: Boolean,
        stateType: StateType = StateType.DEFAULT,
        requestType: RequestType = RequestType.CUSTOM,
        crossinline successHandler: (T) -> Unit,
        crossinline errorHandler: (Throwable) -> Unit
    ) {
        if (stateType == StateType.DEFAULT || stateType == StateType.ONLY_START_PROGRESS)
            stateLiveData.value = State.ShowLoading(requestType)
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    successHandler(it)
                    if (!isLogin){
                        if (stateType == StateType.DEFAULT)
                        stateLiveData.value = State.ShowContentTemp(requestType)
                    }

                },
                {
                    Timber.e(it)
                    if (it is WashareServiceException && requestType == RequestType.CUSTOM)
                        errorHandler(it)
                    else
                        stateLiveData.value = State.OnError(it, requestType)
                })
            .track()
    }

    inline fun <T> Observable<T>.sendRequestWithError(
        stateType: StateType = StateType.DEFAULT,
        requestType: RequestType = RequestType.CUSTOM,
        crossinline successHandler: (T) -> Unit,
        crossinline errorHandler: (Throwable) -> Unit
    ) {
        if (stateType == StateType.DEFAULT || stateType == StateType.ONLY_START_PROGRESS)
            stateLiveData.postValue(State.ShowLoading(requestType))
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    successHandler(it)
//                    if (stateType == StateType.DEFAULT)
//                        stateLiveData.value = State.ShowContentTemp(requestType)
                },
                {
                    Timber.e(it)
                    errorHandler(it)
                })
            .track()
    }

    inline fun <T> Observable<T>.sendRequest(
        crossinline success: (T) -> Unit, crossinline error: (Throwable) -> Unit = {}
    ) {
        //stateLiveData.value = State.ShowLoading()
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                success(it)
            },
                {
                    Timber.d("BaseViewModel.SendRequest-> Error on request : ${it.message}")
                    stateLiveData.value = State.OnError(it)
                    error(it)
                }).track()
    }

    fun <T> Observable<T>.dropBreadcrumb(): Observable<T> {
        val breadcrumb = BreadcrumbException()
        return this.onErrorResumeNext { error: Throwable ->
            throw CompositeException(error, breadcrumb)
        }
    }

    class BreadcrumbException : Exception()

    sealed class State {
        data class OnError(val throwable: Throwable, val requestType: RequestType? = null) : State()
        data class ShowLoading(val requestType: RequestType? = null) : State()
        data class ShowContentTemp(val requestType: RequestType? = null) : State()
        object ShowError : State()
        object ShowEmpty : State()
        object ShowContent : State()

    }

    enum class StateType {
        DEFAULT, // All  State Send BaseActivity
        NONE,  // No State  BaseActivity
        ONLY_START_PROGRESS  // Only showLoading send BaseActivity
    }

    enum class RequestType {
        INIT,
        ACTION,
        CUSTOM
    }
}