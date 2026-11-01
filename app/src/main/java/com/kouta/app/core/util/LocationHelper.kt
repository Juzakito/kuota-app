package com.kouta.app.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class DetectedLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val district: String = "",
    val province: String = "",
    val department: String = ""
)

sealed interface LocationResult {
    data class Success(val location: DetectedLocation) : LocationResult
    data object PermissionDenied : LocationResult
    data object GpsDisabled : LocationResult
    data class Error(val message: String) : LocationResult
}

interface LocationTracker {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): LocationResult
}

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationTracker {

    override fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.PermissionDenied
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (!isGpsEnabled && !isNetworkEnabled) {
            return LocationResult.GpsDisabled
        }

        return withContext(Dispatchers.IO) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()

                // Intentar obtener la ubicación actual de alta precisión
                var rawLocation: Location? = try {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                } catch (e: Exception) {
                    null
                }

                // Fallback a última ubicación conocida de Fused Client
                if (rawLocation == null) {
                    rawLocation = try {
                        fusedClient.lastLocation.await()
                    } catch (e: Exception) {
                        null
                    }
                }

                // Fallback al LocationManager nativo del sistema
                if (rawLocation == null && locationManager != null) {
                    rawLocation = try {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (rawLocation == null) {
                    return@withContext LocationResult.Error(
                        "No se pudo obtener la posición del sensor GPS. Asegúrate de tener visibilidad o Wi-Fi activo."
                    )
                }

                val lat = rawLocation.latitude
                val lng = rawLocation.longitude

                // Geocodificación inversa
                val detected = reverseGeocode(lat, lng)
                LocationResult.Success(detected)
            } catch (e: Exception) {
                LocationResult.Error(e.localizedMessage ?: "Error desconocido al capturar ubicación.")
            }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): DetectedLocation {
        return try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("es-PE"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val addr = addresses.firstOrNull()
                            cont.resume(addr?.toDetectedLocation(latitude, longitude) ?: fallbackDetected(latitude, longitude))
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(fallbackDetected(latitude, longitude))
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.toDetectedLocation(latitude, longitude) ?: fallbackDetected(latitude, longitude)
            }
        } catch (e: Exception) {
            fallbackDetected(latitude, longitude)
        }
    }

    private fun fallbackDetected(latitude: Double, longitude: Double): DetectedLocation {
        return DetectedLocation(
            latitude = latitude,
            longitude = longitude,
            address = "",
            district = "",
            province = "Lima",
            department = "Lima"
        )
    }

    private fun Address.toDetectedLocation(lat: Double, lng: Double): DetectedLocation {
        val streetName = thoroughfare
        val streetNum = subThoroughfare
        val fullStreet = when {
            !streetName.isNullOrBlank() && !streetNum.isNullOrBlank() -> "$streetName $streetNum"
            !streetName.isNullOrBlank() -> streetName
            else -> getAddressLine(0)?.split(",")?.firstOrNull()?.trim() ?: ""
        }
        val dist = subLocality ?: locality ?: ""
        val prov = subAdminArea ?: "Lima"
        val dept = adminArea ?: "Lima"

        return DetectedLocation(
            latitude = lat,
            longitude = lng,
            address = fullStreet,
            district = dist,
            province = prov,
            department = dept
        )
    }
}
