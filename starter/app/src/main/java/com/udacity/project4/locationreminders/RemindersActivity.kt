package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import kotlinx.android.synthetic.main.activity_reminders.*


class RemindersActivity : AppCompatActivity() {

    // region Properties

    private var isAndroidQOrLater = false

    // endregion

    // region Lifecycle / Overrides

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        isAndroidQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    }

    override fun onStart() {
        super.onStart()
        checkPermissionsGranted(isForegroundLocationPermissionGranted(), isBackgroundLocationPermissionGranted())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val wasResultEmpty = grantResults.isEmpty()
        val wasLocationExplicitlyDenied = grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        val wasBackgroundLocationExplicitlyDeniedBackground = (
                requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
                )
        val wasPermissionDenied = wasResultEmpty || wasLocationExplicitlyDenied || wasBackgroundLocationExplicitlyDeniedBackground
        if (wasPermissionDenied) {
            onPermissionDenied()
        } else {
            onPermissionGranted()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Instead of relying on result code, just check location settings again
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkLocationSettings(false)
        }
    }

    // endregion

    // region Check Location Permissions

    private fun isForegroundLocationPermissionGranted(): Boolean {
        val selfPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return PackageManager.PERMISSION_GRANTED == selfPermission
    }

    private fun isBackgroundLocationPermissionGranted(): Boolean {
        var selfPermission: Int = PackageManager.PERMISSION_GRANTED
        if (isAndroidQOrLater) {
            selfPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        return PackageManager.PERMISSION_GRANTED == selfPermission
    }

    private fun checkPermissionsGranted(isForegroundGranted: Boolean, isBackgroundGranted: Boolean) {
        if (isForegroundGranted && isBackgroundGranted) {
            checkLocationSettings()
        } else {
            requestPermissions(isForegroundLocationPermissionGranted(), isBackgroundLocationPermissionGranted())
        }
    }

    // endregion

    // region Check Location Enabled

    private fun checkLocationSettings(resolve: Boolean = true) {
        // 1. Create a location request/builder
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // 2. Check location settings
        val settingsClient = LocationServices.getSettingsClient(this)
        val task = settingsClient.checkLocationSettings(builder.build())

        // 3. Listen for location settings success or failure
        listenForLocationSettingsFailure(task, resolve)
        listenForLocationSettingsSuccess(task)
    }

    private fun listenForLocationSettingsFailure(task: Task<LocationSettingsResponse>, resolve: Boolean = true) {
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                onResolvableException(exception, resolve)
            } else {
                onNonResolvableException()
            }
        }
    }

    private fun listenForLocationSettingsSuccess(task: Task<LocationSettingsResponse>) {
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                // Do nothing for now. Geofences added later.
            }
        }
    }

    private fun onResolvableException(exception: ResolvableApiException, resolve: Boolean = true) {
        try {
            exception.startResolutionForResult(this, REQUEST_TURN_DEVICE_LOCATION_ON)
        } catch (sendEx: IntentSender.SendIntentException) {
            // Do nothing for now.
        }
    }

    private fun onNonResolvableException() {
        Snackbar.make(findViewById(android.R.id.content), R.string.location_required_error, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {
                    checkLocationSettings()
                }
                .show()
    }

    // endregion

    // region Request Permissions


    private fun requestPermissions(isForegroundGranted: Boolean, isBackgroundGranted: Boolean) {
        if (isForegroundGranted && isBackgroundGranted) {
            return
        }

        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        var requestCode: Int

        if (isAndroidQOrLater) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            requestCode = REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        } else {
            requestCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        ActivityCompat.requestPermissions(
                this@RemindersActivity,
                permissions,
                100
        )
    }

    private fun onPermissionDenied() {
        Snackbar.make(findViewById(android.R.id.content), PERMISSION_DENIED_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
    }

    private fun onPermissionGranted() {
        checkLocationSettings()
    }

    // endregion

    // region Constants

    companion object {
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private const val PERMISSION_DENIED_TEXT = "Location permissions denied. Please enable for best experience."
    }

    // endregion

}
