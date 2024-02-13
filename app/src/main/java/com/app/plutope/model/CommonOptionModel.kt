package com.app.plutope.model

class CommonOptionModel(var name: String, var id: String, var isSelected: Boolean = false) {
}

enum class SecurityOption(val value:String){
    PASSCODE("Passcode"),
    PASSCODEBIOMATRIC("Passcode/Touch ID/Face ID")

}