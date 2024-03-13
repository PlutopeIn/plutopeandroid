package com.app.plutope.model

class CommonOptionModel(var name: String, var id: String, var isSelected: Boolean = false) {
}

enum class SecurityOption(val value: String) {
    PASSCODE("Passcode"),
    PASSCODEBIOMATRIC("Passcode/Touch ID/Face ID")

    /* PASSCODE(context.getString(R.string.passcode)),
     PASSCODEBIOMATRIC(context.getString(R.string.passcode_touch_id_face_id))
 */
}