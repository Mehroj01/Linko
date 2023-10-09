package com.neb.linko.businessUi.storeDetails

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
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
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.databinding.ActivityAddLocationBinding
import com.neb.linko.models.StoreLocation
import javax.inject.Inject

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    @Inject
    lateinit var businessViewModel: BusinessViewModel

    lateinit var addLocationBinding: ActivityAddLocationBinding
    lateinit var mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var dialogAddLocation: DialogAddLocation
    var storeLocation: StoreLocation? = null
    lateinit var locations: ArrayList<StoreLocation?>
    var status = false
    var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addLocationBinding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(addLocationBinding.root)
        App.appComponent.addStoreLocationActivity(this)

        locations = ArrayList()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        addLocationBinding.myLocationBtn.setOnClickListener {
            myLocation()
        }

        addLocationBinding.saveBtn.setOnClickListener {
            Toast.makeText(
                this,
                "${storeLocation?.latitude} ${storeLocation?.longitude}",
                Toast.LENGTH_SHORT
            ).show()
            if (storeLocation?.latitude != null && storeLocation?.longitude != null) {
                showDialog(storeLocation?.name ?: "")
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (status) {
                    storeLocation = null
                    position = -1
                    status = false
                    addLocationBinding.saveBtn.text = "Save"
                    readMap()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener { marker ->
            for (i in 0 until locations.size) {
                if (locations[i]?.name == marker.title && locations[i]?.longitude == marker.position.longitude && locations[i]?.latitude == marker.position.latitude) {
                    storeLocation = null
                    position = -1
                    status = false
                    addLocationBinding.saveBtn.text = "Save"
                    showEditAndRemoveDialog(i)
                    break
                }
            }
            true
        }

        addLocationBinding.progress.visibility = View.VISIBLE
        businessViewModel.getMyStore(this).observe(this, Observer {
            if (it != null) {
                locations.clear()
                locations.addAll(it.storeLocations ?: ArrayList())
                readMap()
                if (locations.isNotEmpty()) {
                    val mark = LatLng(locations.last()?.latitude!!, locations.last()?.longitude!!)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
                }
            }
            addLocationBinding.progress.visibility = View.INVISIBLE
        })


        mMap.setOnMapClickListener {
            storeLocation = StoreLocation()
            readMap()
            try {
                val mark =
                    LatLng(it.latitude, it.longitude)
                if (status) {
                    storeLocation?.name = locations[position]?.name
                }
                storeLocation?.latitude = it.latitude
                storeLocation?.longitude = it.longitude
                mMap.addMarker(MarkerOptions().position(mark).title("Location"))
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
            } catch (e: Exception) {
                Toast.makeText(this, "Location not specified", Toast.LENGTH_SHORT)
                    .show()
                storeLocation = null
            }
        }
    }

    private fun showEditAndRemoveDialog(i: Int) {
        AlertDialog.Builder(this)
            .setTitle("Select option")
            .setPositiveButton("Remove") { _, _ ->
                readMap()
                locations.removeAt(i)
                editLocation()
            }
            .setNegativeButton("Change") { _, _ ->
                readMap()
                storeLocation = locations[i]
                position = i
                status = true
                addLocationBinding.saveBtn.text = "Change"
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun readMap() {
        mMap.clear()
        locations.forEach {
            try {
                if (it != null) {
                    val mark = LatLng(it.latitude!!, it.longitude!!)
                    mMap.addMarker(MarkerOptions().position(mark).title(it.name ?: ""))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
                }
            } catch (e: Exception) {
                Toast.makeText(this, "information not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialog(editName: String) {
        dialogAddLocation =
            DialogAddLocation(this, editName, status, object : DialogAddLocation.AddLocationClick {
                override fun skip() {
                    dialogAddLocation.dialogs.onBackPressed()
                }

                override fun save(name: String) {
                    dialogAddLocation.dialogs.onBackPressed()
                    storeLocation?.name = name
                    if (status) {
                        locations[position] = storeLocation
                    } else {
                        locations.add(storeLocation)
                    }
                    editLocation()
                }
            })
        dialogAddLocation.networkDialog()
    }

    private fun editLocation() {
        addLocationBinding.progress.visibility = View.VISIBLE
        businessViewModel.addLocation(locations).observe(this@AddLocationActivity,
            Observer {
                addLocationBinding.progress.visibility = View.INVISIBLE
                if (it) {
                    Toast.makeText(
                        this@AddLocationActivity,
                        "Success",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                } else {
                    Toast.makeText(
                        this@AddLocationActivity,
                        "Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }
            })
    }

    private fun myLocation() {
        readMap()
        storeLocation = StoreLocation()
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
                                storeLocation?.latitude = lastKnownLocation.latitude
                                storeLocation?.longitude = lastKnownLocation.longitude
                                mMap.addMarker(MarkerOptions().position(mark).title("My Location"))
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(mark))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 14f))
                            } catch (e: Exception) {
                                storeLocation = null
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

}