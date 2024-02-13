package com.app.plutope.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHelper private constructor(
    private var callback: PermissionHelperCallback?
) {


    companion object {
        private var permissionHelper: PermissionHelper? = null

        private var context: Context? = null
        fun getInstance(fragment: Fragment, callback: PermissionHelperCallback): PermissionHelper {

            context = fragment.requireContext()

            if (permissionHelper == null)
                permissionHelper = PermissionHelper(callback)
            return permissionHelper!!
        }

        fun getInstance(fragment: Activity, callback: PermissionHelperCallback): PermissionHelper {
            context = fragment

            if (permissionHelper == null)
                permissionHelper = PermissionHelper(callback)
            return permissionHelper!!
        }


    }

    fun checkPermission(permissionType: PermissionType) {
        val permissionToAsk = mutableListOf<String>()
        val grantedPermissions = mutableListOf<String>()
        permissionType.getPermissionList().iterator().forEachRemaining {
            if (ContextCompat.checkSelfPermission(context!!, it)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionToAsk.add(it)
            } else {
                grantedPermissions.add(it)
            }
        }
        if (permissionToAsk.isNotEmpty())
            callback?.requestPermissionsFor(permissionToAsk.toTypedArray())
        else {
            callback?.onPermissionGranted(grantedPermissions.toTypedArray())
        }

    }

    fun requestRationalPermissions(
        permissionToAsk: Array<String>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        requestPermissionLauncher.launch(permissionToAsk)
    }

    fun requestPermissions(
        permissionToAsk: Array<String>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        activity: Activity
    ) {
        val rationalList = mutableListOf<String>()
        permissionToAsk.forEach {
            if (shouldShowRequestPermissionRationale(activity, it)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // Display a SnackBar with a button to request the missing permission.
                rationalList.add(it)
            }
        }
        if (rationalList.isNotEmpty()) {
            // You can directly ask for the permission.
            callback?.showRationalFor(rationalList.toTypedArray())
        } else {
            requestPermissionLauncher.launch(permissionToAsk)
        }

    }

    fun setResult(permissions: Map<String, Boolean>) {
        val grantedPermissions = mutableListOf<String>()
        val rejectedPermission = mutableListOf<String>()
        permissions.forEach {
            if (it.value) {
                grantedPermissions.add(it.key)
            } else {
                rejectedPermission.add(it.key)
            }
        }
        if (rejectedPermission.isNotEmpty()) {
            callback?.onPermissionRejected(rejectedPermission.toTypedArray())
        } else {
            callback?.onPermissionGranted(grantedPermissions.toTypedArray())
        }

    }
    /*
        private val requestPermissionLauncher =
            fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->

                val grantedPermissions = mutableListOf<String>()
                val rejectedPermission = mutableListOf<String>()
                permissions.forEach {
                    if (it.value) {
                        grantedPermissions.add(it.key)
                    } else {
                        rejectedPermission.add(it.key)
                    }
                }
                if (rejectedPermission.isNotEmpty()) {
                    callback?.onPermissionRejected(rejectedPermission.toTypedArray())
                } else {
                    callback?.onPermissionGranted(grantedPermissions.toTypedArray())
                }
            }*/
}

interface PermissionHelperCallback {
    fun onPermissionGranted(grantedPermissions: Array<String>)
    fun requestPermissionsFor(permissionToAsk: Array<String>)
    fun showRationalFor(permissionToAsk: Array<String>)
    fun onPermissionRejected(rejectedPermission: Array<String>)

}

enum class PermissionType(private var permissions: Array<String>) {
    CAMERA(arrayOf(Manifest.permission.CAMERA)),

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    NOTIFICATION(arrayOf(Manifest.permission.POST_NOTIFICATIONS)),
    LOCATION(
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ),

    @RequiresApi(Build.VERSION_CODES.Q)
    BACKGROUND_LOCATION(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)),
    AUDIO(arrayOf(Manifest.permission.RECORD_AUDIO)),
    FILE_STORAGE(
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    );

    fun getPermissionList() = permissions
}