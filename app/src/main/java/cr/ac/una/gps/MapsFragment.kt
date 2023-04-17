package cr.ac.una.gps

import android.Manifest
import android.app.AlertDialog
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.media.audiofx.BassBoost
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions




class MapsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var maps: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        // Crear instancia de SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // Obtén una referencia al botón de zoom in
        val btnZoomIn = view.findViewById<Button>(R.id.btnZoomIn)

        // Agrega un listener al botón de zoom in
        btnZoomIn.setOnClickListener {
            // Obtén una referencia al mapa
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { googleMap ->
                // Incrementa el nivel de zoom
                val currentZoom = googleMap.cameraPosition.zoom
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
            }
        }

        // Obtén una referencia al botón de zoom out
        val btnZoomOut = view.findViewById<Button>(R.id.btnZoomOut)

        // Agrega un listener al botón de zoom out
        btnZoomOut.setOnClickListener {
            // Obtén una referencia al mapa
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { googleMap ->
                // Decrementa el nivel de zoom
                val currentZoom = googleMap.cameraPosition.zoom
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
            }
        }

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para ser usado.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync{
            maps = it
            getLocation()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun getLocation() {
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Pedir permisos de ubicación
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)

        } else {
            // Obtener la ubicación actual y mostrarla en el mapa
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (maps != null) {
                    // Ubicación obtenida con éxito
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        addMarkerToMap(currentLatLng, textoMarcador)
                        maps.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
            }
        }
    }

    private fun addMarkerToMap(latLng: LatLng, title: String?) {
        maps.addMarker(MarkerOptions().position(latLng).title(title))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    getLocation()
                }
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
            }


        }
    }
}
