package com.arny.mvpclean.utils

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


fun <T> mainThreadObservable(observable: Observable<T>): Observable<T> {
    return mainThreadObservable(observable, Schedulers.io())
}

fun <T> mainThreadObservable(observable: Observable<T>, scheduler: Scheduler): Observable<T> {
    return observable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(observable: Single<T>): Single<T> {
    return mainThreadObservable(Schedulers.io(), observable)
}

fun <T> mainThreadObservable(scheduler: Scheduler, observable: Single<T>): Single<T> {
    return observable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(scheduler: Scheduler, observable: Observable<T>): Observable<T> {
    return observable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(flowable: Flowable<T>): Flowable<T> {
    return flowable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> mainThreadObservable(scheduler: Scheduler, flowable: Flowable<T>): Flowable<T> {
    return flowable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread())
}