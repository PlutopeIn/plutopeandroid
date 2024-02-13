package com.app.plutope.browser.browserModel

import android.os.Parcel
import android.os.Parcelable

class DApp : Parcelable {
    @JvmField
    var name: String?

    @JvmField
    var url: String?
    var category: String? = null

    @JvmField
    var description: String? = null
    var isAdded = false

    constructor(name: String?, url: String?) {
        this.name = name
        this.url = url
    }

    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        url = `in`.readString()
        category = `in`.readString()
        description = `in`.readString()
        isAdded = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(url)
        dest.writeString(category)
        dest.writeString(description)
        dest.writeByte((if (isAdded) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is DApp) return false
        val dApp = o
        return if (url == null || name == null || dApp.name == null || dApp.url == null) false else name == dApp.name && url == dApp.url
    }

    override fun hashCode(): Int {
        val a = arrayOf<Any?>(name, url)
        var result = 1
        for (element in a) result = 31 * result + (element?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DApp?> = object : Parcelable.Creator<DApp?> {
            override fun createFromParcel(`in`: Parcel): DApp? {
                return DApp(`in`)
            }

            override fun newArray(size: Int): Array<DApp?> {
                return arrayOfNulls(size)
            }
        }
    }
}
