package la.shiro.batterylog.stressers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import la.shiro.batterylog.config.PI_50
import la.shiro.batterylog.config.TAG

class LocationStresser(context: Context) : Stresser(context) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        Log.i(
            TAG,
            locationManager.getProviders(false).joinToString(prefix = "Found Location Providers: ")
        )
    }

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            impossibleUIUpdateOnMain(location.latitude == PI_50)
        }

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun permissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        else
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
    }

    override fun start() {
        super.start()

        assert(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
        assert(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0.0f,
            locationListener
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0.0f,
            locationListener
        )
    }

    override fun stop() {
        super.stop()
        locationManager.removeUpdates(locationListener)
    }
}
