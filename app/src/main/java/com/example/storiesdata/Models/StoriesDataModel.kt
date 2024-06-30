package com.example.storiesdata.Models

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class StoriesDataModel(
    val title:String, val content:String,
    val imageUrl:String, val storyType:String, var isFav:Boolean,
    val storyID:String,
    val users:Array<String>

):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
       parcel.readString().toString(),
        parcel.createStringArray() ?: arrayOf()




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
