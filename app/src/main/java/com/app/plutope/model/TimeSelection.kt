package com.app.plutope.model

import androidx.annotation.Keep

@Keep
data class TimeSelection(val time: String, var isSelected: Boolean = false, val interval: String)