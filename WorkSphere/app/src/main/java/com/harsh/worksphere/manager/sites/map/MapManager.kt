package com.harsh.worksphere.manager.sites

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

/**
 * Callback interface for map events
 */
interface MapManagerCallback {
    fun onLocationSelected(latitude: Double, longitude: Double, address: String)
    fun onMapReady()
    fun onError(message: String)
}

/**
 * Data class for location info
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

/**
 * MapManager - Pure map logic, NO touch handling (handled by MapTouchWrapper)
 */
class MapManager(
    private val context: Context,
    private val callback: MapManagerCallback
) : OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var mapFragment: SupportMapFragment? = null

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var selectedLocation: LocationData? = null
    private var isMapReady = false

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val TAG = "MapManager"
        private const val LOCATION_UPDATE_INTERVAL = 10000L
        private const val FASTEST_UPDATE_INTERVAL = 5000L
    }

    /**
     * Initialize with map fragment only
     */
    fun initialize(mapFragment: SupportMapFragment) {
        this.mapFragment = mapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true

        configureMap()
        callback.onMapReady()
    }

    private fun configureMap() {
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMyLocationButtonEnabled = hasLocationPermission()

            if (hasLocationPermission()) {
                try {
                    isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception", e)
                }
            }

            setOnMapClickListener { latLng ->
                selectLocation(latLng)
            }

            setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDrag(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    selectLocation(marker.position)
                }
            })
        }
    }

    fun selectLocation(latLng: LatLng) {
        placeMarker(latLng)
        moveCamera(latLng)
        reverseGeocode(latLng)
    }

    fun placeMarker(latLng: LatLng, title: String = "Selected Location") {
        currentMarker?.remove()
        currentMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true)
        )
    }

    fun moveCamera(latLng: LatLng, zoom: Float = DEFAULT_ZOOM, animate: Boolean = true) {
        val update = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        if (animate) {
            googleMap?.animateCamera(update)
        } else {
            googleMap?.moveCamera(update)
        }
    }

    private fun reverseGeocode(latLng: LatLng) {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                    processAddress(addresses, latLng)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                processAddress(addresses ?: emptyList(), latLng)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Geocoder error", e)
            selectedLocation = LocationData(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                address = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            )
            notifyCallback()
        }
    }

    private fun processAddress(addresses: List<Address>, latLng: LatLng) {
        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            val fullAddress = address.getAddressLine(0) ?: buildAddressFromParts(address)

            selectedLocation = LocationData(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                address = fullAddress,
                city = address.locality,
                state = address.adminArea,
                country = address.countryName,
                postalCode = address.postalCode
            )
        } else {
            selectedLocation = LocationData(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                address = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            )
        }
        notifyCallback()
    }

    private fun buildAddressFromParts(address: Address): String {
        val parts = mutableListOf<String>()
        address.subLocality?.let { parts.add(it) }
        address.locality?.let { parts.add(it) }
        address.adminArea?.let { parts.add(it) }
        address.postalCode?.let { parts.add(it) }
        address.countryName?.let { parts.add(it) }
        return parts.joinToString(", ")
    }

    private fun notifyCallback() {
        selectedLocation?.let {
            callback.onLocationSelected(it.latitude, it.longitude, it.address)
        }
    }

    fun getCurrentLocation(centerMap: Boolean = true, selectAsLocation: Boolean = false) {
        if (!hasLocationPermission()) {
            callback.onError("Location permission not granted")
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        if (centerMap) {
                            moveCamera(latLng, animate = false)
                        }
                        if (selectAsLocation) {
                            selectLocation(latLng)
                        }
                    } ?: callback.onError("Please turn on your location")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Location error", e)
                    callback.onError("Failed to get location")
                }
        } catch (e: SecurityException) {
            callback.onError("Location permission error")
        }
    }

    fun searchLocation(query: String, onResult: (Boolean) -> Unit = {}) {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(query, 1) { addresses ->
                    handleSearchResult(addresses, onResult)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                handleSearchResult(addresses ?: emptyList(), onResult)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Search error", e)
            onResult(false)
        }
    }

    private fun handleSearchResult(addresses: List<Address>, onResult: (Boolean) -> Unit) {
        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)
            selectLocation(latLng)
            onResult(true)
        } else {
            onResult(false)
        }
    }

    fun clearSelection() {
        currentMarker?.remove()
        currentMarker = null
        selectedLocation = null
    }

    fun isReady(): Boolean = isMapReady
    fun getSelectedLocation(): LocationData? = selectedLocation

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun setSelectedLocation(latitude: Double, longitude: Double, address: String) {
        if (!isReady()) {
            Log.w(TAG, "Map not ready yet")
            return
        }

        selectedLocation = LocationData(
            latitude = latitude,
            longitude = longitude,
            address = address
        )

        val latLng = LatLng(latitude, longitude)

        // Update marker
        currentMarker?.let {
            it.position = latLng
        } ?: run {
            currentMarker = googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .draggable(true)
            )
        }

        // Move camera
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
    }
    fun setInteractionEnabled(enabled: Boolean) {
        googleMap?.uiSettings?.apply {
            isScrollGesturesEnabled = enabled
            isZoomGesturesEnabled = enabled
            isTiltGesturesEnabled = enabled
            isRotateGesturesEnabled = enabled
            isMyLocationButtonEnabled = enabled && hasLocationPermission()
        }

        if (!enabled) {
            googleMap?.setOnMapClickListener(null)
            googleMap?.setOnMarkerDragListener(null)
        } else {
            googleMap?.setOnMapClickListener { latLng -> selectLocation(latLng) }
            googleMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDrag(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    selectLocation(marker.position)
                }
            })
        }
    }
    fun cleanup() {
        currentMarker = null
        selectedLocation = null
        googleMap = null
        isMapReady = false
    }
}