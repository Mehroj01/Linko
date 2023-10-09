package com.neb.linko.ui.store

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import com.neb.linko.R
import com.neb.linko.databinding.ActivityMapBinding
import com.neb.linko.models.StoreModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var mapBinding: ActivityMapBinding
    lateinit var mMap: GoogleMap
    var store: StoreModel? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var dialogForMap: DialogForMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(mapBinding.root)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        store = Gson().fromJson(intent.getStringExtra("location"), StoreModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapBinding.myLocationBtn.setOnClickListener {
            myLocation()
        }

    }

    fun openMap(latitude: Double, longitude: Double) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude&mode=l"))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    private fun myLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            var locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000
            locationRequest.fastestInterval = 2000

            var builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            var result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

            result.addOnCompleteListener(OnCompleteListener {
                try {
                    var response = it.getResult(ApiException::class.java)
                    val locationResult = fusedLocationProviderClient.lastLocation
                    locationResult.addOnCompleteListener { task ->
                        val lastKnownLocation = task.result

                        if (lastKnownLocation != null) {
                            try {
                                val mark =
                                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                                mMap.addMarker(MarkerOptions().position(mark).title("My Location"))
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
                            } catch (e: Exception) {
                                Toast.makeText(this, "Location not specified", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Internet disconnected", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                var resolvableApiException = e as ResolvableApiException
                                resolvableApiException.startResolutionForResult(this, 100)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    e.printStackTrace()
                }
            })
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (store != null && store?.storeLocations != null) {
            store?.storeLocations?.forEach {
                try {
                    val mark = LatLng(it.latitude!!, it.longitude!!)
                    googleMap.addMarker(MarkerOptions().position(mark).title(it.name ?: ""))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
                } catch (e: Exception) {
                    Toast.makeText(this, "information not available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val position = marker.position
            showDialog(position.latitude, position.longitude, marker.title ?: "Linko")
            true
        }
    }

    private fun showDialog(lat: Double, lon: Double, markerTitle: String) {
        dialogForMap = DialogForMap(markerTitle, this, object : DialogForMap.MapClick {
            override fun skip() {
                dialogForMap.dialogs.onBackPressed()
            }

            override fun openMap() {
                dialogForMap.dialogs.onBackPressed()
                openMap(lat, lon)
            }
        })
        dialogForMap.networkDialog()
    }

}