package com.caiolandau.devigetredditclient.util

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

interface IFragment<T, VM: ViewModel> {
    val ctx: Context?
    val args: Bundle?
    val viewLifecycleOwner: LifecycleOwner
    fun getViewModel(data: T): VM
}