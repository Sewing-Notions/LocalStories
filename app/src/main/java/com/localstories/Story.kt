package com.localstories

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    val storyId: String,
    val title: String,
    val description: String,
    val dateOfFact: String, // Use String for now, can convert to Date later
    val photoPath: String,
    val locationId: String,
    val userId: String,
    val author: String
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }
}
