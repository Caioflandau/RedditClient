package com.caiolandau.devigetredditclient.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.MediaStore

class LocalImageSaver {

    fun saveImageToGallery(
        context: Context,
        filename: String,
        drawable: Drawable
    ) {
        val resolver = context.applicationContext?.contentResolver ?: return
        val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        }
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageContentUri = resolver.insert(imageCollection, imageDetails) ?: return
        val out = resolver.openOutputStream(imageContentUri, "w")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out?.close()
    }

}
