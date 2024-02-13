import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {
    private const val PERMISSION_REQUEST_CODE = 101

    interface PermissionCallback {
        fun onPermissionsGranted(permissions: List<String>)
        fun onPermissionsDenied(permissions: List<String>)
    }

    // Function to request multiple permissions
    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // All permissions are already granted
            callback.onPermissionsGranted(listOf(*permissions))
        }
    }

    // Handle permission request result (call this from onRequestPermissionsResult)
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        callback: PermissionCallback
    ) {
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()

        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i])
            } else {
                deniedPermissions.add(permissions[i])
            }
        }

        if (grantedPermissions.isNotEmpty()) {
            callback.onPermissionsGranted(grantedPermissions)
        }

        if (deniedPermissions.isNotEmpty()) {
            callback.onPermissionsDenied(deniedPermissions)
        }
    }
}