package com.caiolandau.devigetredditclient.home.model

import android.os.Parcel
import android.os.Parcelable
import java.net.URL

class RedditPost(
    val title: String,
    val author: String,
    val entryDate: String,
    val thumbnailUrl: URL?,
    val numOfComments: Int,
    val isRead: Boolean
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString() ?: "",
        source.readSerializable() as URL?,
        source.readInt(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(author)
        writeString(entryDate)
        writeSerializable(thumbnailUrl)
        writeInt(numOfComments)
        writeInt((if (isRead) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RedditPost> = object : Parcelable.Creator<RedditPost> {
            override fun createFromParcel(source: Parcel): RedditPost = RedditPost(source)
            override fun newArray(size: Int): Array<RedditPost?> = arrayOfNulls(size)
        }
    }
}
