package com.caiolandau.devigetredditclient.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

// Simply RxJava Scheduler Provider
class SchedulerProvider {
    fun mainThread() = AndroidSchedulers.mainThread()
    fun io() = Schedulers.io()
}