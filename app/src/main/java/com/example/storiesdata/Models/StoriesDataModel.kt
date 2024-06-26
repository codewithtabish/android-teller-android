package com.example.storiesdata.Models

import android.os.Parcel
import android.os.Parcelable

data class StoriesDataModel(val title:String,val content:String,
    val imageUrl:String,val storyType:String):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(imageUrl)
        parcel.writeString(storyType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoriesDataModel> {
        override fun createFromParcel(parcel: Parcel): StoriesDataModel {
            return StoriesDataModel(parcel)
        }

        override fun newArray(size: Int): Array<StoriesDataModel?> {
            return arrayOfNulls(size)
        }
    }
}
