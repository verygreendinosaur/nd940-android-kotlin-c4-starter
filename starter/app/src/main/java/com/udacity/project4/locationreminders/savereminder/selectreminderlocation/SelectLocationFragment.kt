package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import androidx.lifecycle.Observer
import java.math.RoundingMode
import java.text.DecimalFormat


class SelectLocationFragment : BaseFragment() {

    // Properties

    override val primaryViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    // Lifecycle / Overrides

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = primaryViewModel

        startObservers()
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            primaryViewModel.selectMapType.setValue(GoogleMap.MAP_TYPE_NORMAL)
            true
        }
        R.id.hybrid_map -> {
            primaryViewModel.selectMapType.setValue(GoogleMap.MAP_TYPE_HYBRID)
            true
        }
        R.id.satellite_map -> {
            primaryViewModel.selectMapType.setValue(GoogleMap.MAP_TYPE_SATELLITE)
            true
        }
        R.id.terrain_map -> {
            primaryViewModel.selectMapType.setValue(GoogleMap.MAP_TYPE_TERRAIN)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Helper methods

    private fun startObservers() {
        // User selects a location on map
        primaryViewModel.selectLocation.observe(viewLifecycleOwner, Observer {
            onLocationSelected(it.latitude, it.longitude)
        })
    }

    private fun onLocationSelected(lat: Double, long: Double) {
        // Set lat, long, and text
        val format = DecimalFormat("#.####")
        format.roundingMode = RoundingMode.CEILING

        primaryViewModel.latitude.setValue(lat)
        primaryViewModel.longitude.setValue(long)
        primaryViewModel.reminderSelectedLocationStr.setValue("${format.format(lat)} , ${format.format(long)}")

        // Navigate back to previous fragment
        primaryViewModel.navigationCommand.value = NavigationCommand.Back
    }

}
