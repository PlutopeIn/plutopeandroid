package com.app.plutope.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleDriveBackupModel(val fileId:String,val fileName:String,val createdTime:String,var fileContent:String):Parcelable