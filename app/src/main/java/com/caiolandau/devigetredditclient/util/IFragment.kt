package com.caiolandau.devigetredditclient.util

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.viewmodel.PostDetailViewModel

interface IFragment<T, VM: ViewModel> {
    val ctx: Context?
    val args: Bundle?
    val viewLifecycleOwner: LifecycleOwner
    fun getViewModel(data: T): VM
}