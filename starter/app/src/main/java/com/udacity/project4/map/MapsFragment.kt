package com.udacity.project4.map

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject

class MapsFragment : Fragment() {

    private val viewModel: SaveReminderViewModel by inject()

    var map: GoogleMap? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        map = googleMap

        setMapInitialPosition(googleMap)
        setMapClickListener(googleMap)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        setupObservers()

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setupObservers() {

        // User selects map type
        viewModel.selectMapType.observe(viewLifecycleOwner, Observer {
            onSelectMapType(it)
        })
    }

    private fun onSelectMapType(type: Int) {
        val map = map ?: return
        map.mapType = type
    }

    private fun setMapInitialPosition(map: GoogleMap) {
        val map = map ?: return

        val home = LatLng(HOME_LATITUDE, HOME_LONGITUDE)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, HOME_ZOOM_LEVEL))
        map.addMarker(
                MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .title(HOME_TITLE)
                        .position(home)
        )
    }

    private fun setMapClickListener(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            viewModel.selectLocation.setValue(latLng)
        }
    }

    companion object {
        const val HOME_LATITUDE = 47.6101
        const val HOME_LONGITUDE = -122.2015
        const val HOME_ZOOM_LEVEL = 15f
        const val HOME_TITLE = "Home - Bellevue"
    }

}