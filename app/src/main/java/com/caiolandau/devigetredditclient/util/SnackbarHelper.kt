package com.caiolandau.devigetredditclient.util

import android.view.View
import androidx.annotation.StringRes
import com.caiolandau.devigetredditclient.R
import com.google.android.material.snackbar.Snackbar

class SnackbarHelper {
    fun showSnackbar(
        view: View,
        @StringRes message: Int,
        length: Int = Snackbar.LENGTH_LONG
    ) =
        Snackbar.make(view, R.string.image_saved_success_message, Snackbar.LENGTH_LONG)
            .show()
}