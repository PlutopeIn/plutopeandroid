package com.app.plutope.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("Contacts")
data class ContactModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    var name: String,
    val address: String,
    var type: ContactType = ContactType.ITEM
):Parcelable

enum class ContactType {
    HEADER, ITEM
}