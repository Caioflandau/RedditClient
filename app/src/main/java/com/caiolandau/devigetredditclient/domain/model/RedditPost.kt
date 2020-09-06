package com.caiolandau.devigetredditclient.domain.model

import android.os.Parcel
import android.os.Parcelable

class RedditPost(
    val id: String,
    val name: String,
    val title: String,
    val author: String,
    val selfText: String?,
    val permalink: String,
    val entryDate: String,
    val url: String?,
    val thumbnailUrl: String?,
    val numOfComments: Int,
    var isRead: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(selfText)
        parcel.writeString(permalink)
        parcel.writeString(entryDate)
        parcel.writeString(url)
        parcel.writeString(thumbnailUrl)
        parcel.writeInt(numOfComments)
        parcel.writeByte(if (isRead) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedditPost

        if (id != other.id) return false
        if (name != other.name) return false
        if (title != other.title) return false
        if (author != other.author) return false
        if (selfText != other.selfText) return false
        if (permalink != other.permalink) return false
        if (entryDate != other.entryDate) return false
        if (url != other.url) return false
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (numOfComments != other.numOfComments) return false
        if (isRead != other.isRead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + (selfText?.hashCode() ?: 0)
        result = 31 * result + permalink.hashCode()
        result = 31 * result + entryDate.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (thumbnailUrl?.hashCode() ?: 0)
        result = 31 * result + numOfComments
        result = 31 * result + isRead.hashCode()
        return result
    }


    companion object CREATOR : Parcelable.Creator<RedditPost> {
        override fun createFromParcel(parcel: Parcel): RedditPost {
            return RedditPost(parcel)
        }

        override fun newArray(size: Int): Array<RedditPost?> {
            return arrayOfNulls(size)
        }
    }

}
