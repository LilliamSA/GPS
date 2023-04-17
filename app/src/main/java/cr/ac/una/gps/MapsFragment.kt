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
    private var previousLocation: LatLng? = null
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
    private val callback = OnMapReadyCallback { googleMap ->
        // Recuperar el valor guardado en SharedPreferences
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")

        // Crear un marcador en el mapa con el texto recuperado
        val latLng = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(latLng).title(textoMarcador))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))

        // Guardar una referencia al mapa
        maps = googleMap

        // Obtener la ubicación actual y mostrarla en el mapa
        getLocation()

        // Mover la cámara del mapa a la última ubicación guardada
        moveCameraToLastLocation()
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
        // Obtén una referencia al botón de actualización
        val btnactualizar = view.findViewById<Button>(R.id.btnActualizar)

        // Agrega un listener al botón de actualización
        btnactualizar.setOnClickListener {
            getLocation()
        }

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para ser usado.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private fun saveLocation(location: Location?) {
        location?.let {
            val editor = sharedPreferences.edit()
            editor.putFloat("lastLat", location.latitude.toFloat())
            editor.putFloat("lastLng", location.longitude.toFloat())
            editor.apply()
        }
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

                        // Guardar la ubicación actual
                        saveLocation(location)
                    }
                }
            }
        }
    }
    private fun moveCameraToLastLocation() {
        val lastLat = sharedPreferences.getFloat("lastLat", 0f).toDouble()
        val lastLng = sharedPreferences.getFloat("lastLng", 0f).toDouble()

        if (lastLat != 0.0 && lastLng != 0.0) {
            val lastLatLng = LatLng(lastLat, lastLng)
            maps.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15f))
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
