package com.harsh.worksphere.employee.dashboard

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

private const val GEOFENCE_RADIUS = 100.0

class LocationVerificationDialog(
    private val fragment: Fragment,
    private val site: SiteModel,
    private val onVerified: (siteId: String, siteName: String) -> Unit,
    private val onShiftInvalid: (message: String) -> Unit,
    private val onDismissed: () -> Unit
) {

    private var dialog: Dialog? = null
    private var googleMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var userLatLng: LatLng? = null
    private var isVerified = false

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var btnClose: ImageButton
    private lateinit var stateLoading: LinearLayout
    private lateinit var stateInRange: MaterialCardView
    private lateinit var stateOutRange: MaterialCardView
    private lateinit var tvDistanceInRange: TextView
    private lateinit var tvDistanceOutRange: TextView
    private lateinit var tvError: TextView
    private lateinit var tvSiteName: TextView
    private lateinit var tvSiteAddress: TextView
    private lateinit var btnOpenMaps: ImageButton
    private lateinit var btnChecking: MaterialButton
    private lateinit var btnProceed: MaterialButton
    private lateinit var btnRetry: MaterialButton

    fun show() {
        val context = fragment.requireContext()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_location_verification, null)

        dialog = Dialog(context).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (fragment.resources.displayMetrics.widthPixels * 0.92).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
            setOnDismissListener {
                cleanupMap()
                if (!isVerified) onDismissed()
            }
        }

        initViews(dialogView)
        setupMapView()
        populateSiteInfo()
        setupClickListeners()
        fetchLocation()

        dialog?.show()
    }

    private fun initViews(view: View) {
        btnClose = view.findViewById(R.id.btn_close)
        stateLoading = view.findViewById(R.id.state_loading)
        stateInRange = view.findViewById(R.id.state_in_range)
        stateOutRange = view.findViewById(R.id.state_out_range)
        tvDistanceInRange = view.findViewById(R.id.tv_distance_in_range)
        tvDistanceOutRange = view.findViewById(R.id.tv_distance_out_range)
        tvError = view.findViewById(R.id.tv_error)
        tvSiteName = view.findViewById(R.id.tv_site_name)
        tvSiteAddress = view.findViewById(R.id.tv_site_address)
        btnOpenMaps = view.findViewById(R.id.btn_open_maps)
        mapView = view.findViewById(R.id.map_view)
        btnChecking = view.findViewById(R.id.btn_checking)
        btnProceed = view.findViewById(R.id.btn_proceed)
        btnRetry = view.findViewById(R.id.btn_retry)
    }

    private fun setupMapView() {
        mapView?.onCreate(null)
        mapView?.onResume()
        mapView?.getMapAsync { map ->
            googleMap = map
            configureMap(map)
            userLatLng?.let { addUserMarkerAndAdjustCamera(map, it) }
        }
    }

    private fun populateSiteInfo() {
        tvSiteName.text = site.siteName
        if (site.location.address.isNotEmpty()) {
            tvSiteAddress.text = site.location.address
            tvSiteAddress.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener { dismiss() }
        btnRetry.setOnClickListener { fetchLocation() }
        btnProceed.setOnClickListener { proceedToLogVisit() }

        btnOpenMaps.setOnClickListener {
            val uri = Uri.parse(
                "geo:${site.location.latitude},${site.location.longitude}" +
                        "?q=${site.location.latitude},${site.location.longitude}"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                fragment.startActivity(intent)
            } catch (_: Exception) {
                val fallback = Uri.parse(
                    "https://www.google.com/maps?q=${site.location.latitude},${site.location.longitude}"
                )
                fragment.startActivity(Intent(Intent.ACTION_VIEW, fallback))
            }
        }
    }

    // ── Map ───────────────────────────────────────────────────────────────────
    private fun configureMap(map: GoogleMap) {
        map.uiSettings.apply {
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
            isZoomControlsEnabled = false
            isCompassEnabled = false
            isMapToolbarEnabled = false
        }

        val sitePos = LatLng(site.location.latitude, site.location.longitude)

        map.addMarker(MarkerOptions().position(sitePos).title("Site"))

        map.addCircle(
            CircleOptions()
                .center(sitePos)
                .radius(GEOFENCE_RADIUS)
                .fillColor(0x222E7D32.toInt())
                .strokeColor(0xFF2E7D32.toInt())
                .strokeWidth(3f)
        )

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sitePos, 10f))
    }

    private fun addUserMarkerAndAdjustCamera(map: GoogleMap, userPos: LatLng) {
        val sitePos = LatLng(site.location.latitude, site.location.longitude)

        map.addMarker(
            MarkerOptions()
                .position(userPos)
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        try {
            val bounds = LatLngBounds.Builder()
                .include(sitePos)
                .include(userPos)
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (_: Exception) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(sitePos, 15f))
        }
    }

    // ── Location Fetching ─────────────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        showLoadingState()
        fragment.lifecycleScope.launch {
            try {
                val location = getCurrentLocation()
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    userLatLng = latLng

                    googleMap?.let { map ->
                        map.clear()
                        configureMap(map)
                        addUserMarkerAndAdjustCamera(map, latLng)
                    }

                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        site.location.latitude, site.location.longitude,
                        results
                    )
                    val distance = results[0]

                    if (distance <= GEOFENCE_RADIUS) {
                        showInRange(distance)
                    } else {
                        showOutOfRange(distance)
                    }
                } else {
                    showOutOfRange(-1f)
                    setError("Could not get your location. Please enable GPS and try again.")
                }
            } catch (e: Exception) {
                showOutOfRange(-1f)
                setError("Location error: ${e.localizedMessage}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }

    // ── State Display ─────────────────────────────────────────────────────────
    private fun showLoadingState() {
        stateLoading.visibility = View.VISIBLE
        stateInRange.visibility = View.GONE
        stateOutRange.visibility = View.GONE
        tvError.visibility = View.GONE
        btnChecking.visibility = View.VISIBLE
        btnProceed.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showInRange(distance: Float) {
        stateLoading.visibility = View.GONE
        stateInRange.visibility = View.VISIBLE
        stateOutRange.visibility = View.GONE
        tvDistanceInRange.text = "${distance.toInt()}m"
        tvError.visibility = View.GONE
        btnChecking.visibility = View.GONE
        btnProceed.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
    }

    private fun showOutOfRange(distance: Float) {
        stateLoading.visibility = View.GONE
        stateInRange.visibility = View.GONE
        stateOutRange.visibility = View.VISIBLE
        tvDistanceOutRange.text = if (distance < 0) "N/A" else "${distance.toInt()}m"
        tvError.visibility = View.GONE
        btnChecking.visibility = View.GONE
        btnProceed.visibility = View.GONE
        btnRetry.visibility = View.VISIBLE
    }

    private fun setError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    // ── Proceed to Log Visit ──────────────────────────────────────────────────
    private fun proceedToLogVisit() {
        val shiftFrom = site.visitTimeFrom
        val shiftTo = site.visitTimeTo

        if (shiftFrom.isNotEmpty() && shiftTo.isNotEmpty()) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            try {
                val fromTime = timeFormat.parse(shiftFrom)
                val toTime = timeFormat.parse(shiftTo)
                val now = Calendar.getInstance()
                val currentTimeOnly = timeFormat.parse(timeFormat.format(now.time))

                if (fromTime != null && toTime != null && currentTimeOnly != null) {
                    if (currentTimeOnly.before(fromTime) || currentTimeOnly.after(toTime)) {
                        onShiftInvalid("Cannot log visit outside shift hours ($shiftFrom - $shiftTo)")
                        dismiss()
                        return
                    }
                }
            } catch (_: Exception) {
            }
        }

        isVerified = true
        onVerified(site.siteId, site.siteName)
        dismiss()
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────
    private fun cleanupMap() {
        mapView?.onPause()
        mapView?.onDestroy()
        mapView = null
        googleMap = null
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
