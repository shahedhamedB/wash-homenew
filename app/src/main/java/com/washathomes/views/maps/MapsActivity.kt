package com.washathomes.views.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.washathomes.R
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
@AndroidEntryPoint
class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var markerOptions: MarkerOptions? = null
    private var requestingLocationUpdates = false
    private var mAddress = ""
    private var markerPosition: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //        Places.initialize(getApplicationContext(), getBaseContext().getString(R.string.google_places_key), Locale.US);
        val actionBar = actionBar
        actionBar?.hide()

//        if(Helpers.getLocale().equals("ar")){
//            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//        }else {
//            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
//        }
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_places_key), Locale.US)
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

//        ((EditText)autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(14.0f);
        //autocompleteFragment.setHint("اختر موقعك..");

        // Specify the types of place data to return.
        autocompleteFragment!!.setPlaceFields(
            Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (place.latLng != null) {
                    placeMarkerOnMap(place.latLng)
                    markerPosition = place.latLng
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("onError", "An error occurred: $status")
            }
        })
        val submitAddressMaterialButton: MaterialButton =
            findViewById(R.id.submitAddressMaterialButton)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    Toast.makeText(this@MapsActivity, "Error", Toast.LENGTH_SHORT).show()
                    return
                }
                requestingLocationUpdates = false
                stopLocationUpdates()
                for (location in locationResult.locations) {
                    // Update UI with location data
                    val latLng = LatLng(location.latitude, location.longitude)
                    requestingLocationUpdates = false
                    placeMarkerOnMap(latLng)
                    markerPosition = latLng
                    break
                }
            }
        }
        submitAddressMaterialButton.setOnClickListener { v: View? -> attemptSaveAddress() }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions = MarkerOptions()
        markerOptions!!.draggable(true)
        mMap!!.setOnMapClickListener { latLng: LatLng ->
            markerOptions!!.position(latLng)
            mMap!!.clear()
            mMap!!.addMarker(markerOptions!!)
            displayAddressInfo(latLng)
        }
        mMap!!.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                displayAddressInfo(marker.position)
            }
        })
        requestingLocationUpdates = true
        checkLocationPermission()
    }

    public override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    public override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    private fun placeMarkerOnMap(latLng: LatLng) {
        markerOptions!!.position(latLng)
        mMap!!.clear()
        mMap!!.addMarker(markerOptions!!)
        displayAddressInfo(latLng)
        val cameraPosition = CameraPosition.Builder()
            .target(latLng) // Sets the center of the map to Mountain View
            .zoom(17f) // Sets the zoom
            .build() // Creates a CameraPosition from the builder
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            returnCancelledResult()
        } else {
            location
        }
    }// Logic to handle location object

    // Got last known location. In some rare situations this can be null.
    @get:RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private val location: Unit
        private get() {
            fusedLocationClient!!.lastLocation
                .addOnSuccessListener(
                    this
                ) { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        val latLng =
                            LatLng(location.latitude, location.longitude)
                        requestingLocationUpdates = false
                        placeMarkerOnMap(latLng)
                        markerPosition = latLng
                    } else {
                        requestLocationUpdates()
                    }
                }
        }

    private fun requestLocationUpdates() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(createLocationRequest())
        builder.setAlwaysShow(true)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(
            this
        ) { locationSettingsResponse: LocationSettingsResponse? ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            startLocationUpdates()
        }
        task.addOnFailureListener(
            this
        ) { e: Exception? ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: SendIntentException) {
                    returnCancelledResult()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            returnCancelledResult()
        } else {
            fusedLocationClient!!.requestLocationUpdates(
                createLocationRequest(),
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 1
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        } else {
            setCurrentLocationOnMap()
        }
    }

    private fun getAddress(latLng: LatLng): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address?>? = null
        return try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses[0]
        } catch (e: IOException) {
            null
        }
    }

    private fun displayAddressInfo(latLng: LatLng) {
        val address = getAddress(latLng)
        markerPosition = latLng
        if (address == null) return
        mAddress = address.getAddressLine(0)
    }

    private fun attemptSaveAddress() {
        var isValid = true
        val latitude = java.lang.Double.toString(markerPosition!!.latitude)
        val longitude = java.lang.Double.toString(markerPosition!!.longitude)
        if (mAddress.isEmpty()) {
            isValid = false
        }
        if (latitude.isEmpty()) {
            isValid = false
        }
        if (longitude.isEmpty()) {
            isValid = false
        }
        if (!isValid) {
            Toast.makeText(this, "Please choose a more accurate address", Toast.LENGTH_SHORT).show()
            return
        }
        val returnIntent = Intent()
        returnIntent.putExtra("address", mAddress)
        returnIntent.putExtra("latitude", latitude)
        returnIntent.putExtra("longitude", longitude)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                setCurrentLocationOnMap()
            } else {
                returnCancelledResult()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates()
            } else {
                returnCancelledResult()
            }
        }
    }

    private fun returnCancelledResult() {
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)
        finish()
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2
    }
}