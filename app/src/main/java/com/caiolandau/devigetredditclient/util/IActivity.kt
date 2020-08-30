package com.caiolandau.devigetredditclient.util

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.FragmentManager

/**
 * A simple Activity interface for Android's Activity
 * This is used in the activity wrapper to allow testing activities without Robolectric/AndroidX test
 */
interface IActivity {
    val context: Context
    fun getIntent(): Intent
    fun <T : View> findViewById(@IdRes id: Int): T
    fun getSupportActionBar(): ActionBar?
    fun getSupportFragmentManager(): FragmentManager
    fun navigateUpTo(upIntent: Intent): Boolean
}