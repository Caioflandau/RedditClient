package com.caiolandau.devigetredditclient.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

// Simply RxJava Scheduler Provider
class SchedulerProvider {
    fun mainThread() = AndroidSchedulers.mainThread()
}