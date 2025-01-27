package com.example.yogaapp.dataclasses

import android.os.Parcel
import android.os.Parcelable

data class Training(
    var name: String,
    var description: String,
    var poses_by_UUID: MutableList<String> = mutableListOf<String>()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeStringList(poses_by_UUID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Training> {
        override fun createFromParcel(parcel: Parcel): Training {
            return Training(parcel)
        }

        override fun newArray(size: Int): Array<Training?> {
            return arrayOfNulls(size)
        }
    }
}
