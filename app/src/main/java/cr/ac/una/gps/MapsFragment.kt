package cr.ac.una.gps

import android.Manifest
import android.content.*
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.slider.RangeSlider
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil


class MapsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var maps: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var ubicacionDao: UbicacionDao
    private lateinit var locationReceiver: BroadcastReceiver
    private lateinit var polygon: Polygon


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        // Crear instancia de SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        return view
    }

    private val callback = OnMapReadyCallback { googleMap ->

        // Guardar una referencia al mapa
        maps = googleMap
        //  Recuperar el valor guardado en SharedPreferences
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")

        // Crear un marcador en el mapa con el texto recuperado
        val latLng = LatLng(-14.0095923, 108.8152324)
        val costaRica = LatLng(9.87466235556157, -83.97806864895828)
        googleMap.addMarker(MarkerOptions().position(latLng).title(textoMarcador))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        polygon = createPolygon()

        if(isLocationInsidePolygon(latLng)) {
            Toast.makeText(requireContext(), "Dentro del poligono", Toast.LENGTH_LONG).show()
        }

      /*  if (!isLocationInsidePolygon(costaRica)){
                Toast.makeText(requireContext(), "Fuera del poligono", Toast.LENGTH_LONG).show()
        }*/

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ubicacionDao = AppDatabase.getInstance(requireContext()).ubicacionDao()

        // Obtén una referencia al RangeSlider
        val rsHeight = view.findViewById<RangeSlider>(R.id.rs_height)
        // Agrega un listener al RangeSlider
        rsHeight.addOnChangeListener { slider, value, fromUser ->
            // Obtén una referencia al mapa
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { googleMap ->
                // Actualiza el nivel de zoom del mapa
                val zoom = value.toFloat()
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom))
            }
        }
        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para ser usado.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        iniciaServicio()
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitud = intent?.getDoubleExtra("latitud", 0.0) ?: 0.0
                val longitud = intent?.getDoubleExtra("longitud", 0.0) ?: 0.0
                val polygon = createPolygon().points

                var inside = false
                for (i in polygon.indices) {
                    val j = if (i == polygon.size - 1) 0 else i + 1
                    val xi = polygon[i].latitude
                    val yi = polygon[i].longitude
                    val xj = polygon[j].latitude
                    val yj = polygon[j].longitude

                    val intersect = ((yi > longitud) != (yj > longitud)) &&
                            (latitud < (xj - xi) * (longitud - yi) / (yj - yi) + xi)
                    if (intersect) {
                        inside = !inside
                    }
                }

                val entity = Ubicacion(
                    id = null,
                    latitud = latitud,
                    longitud = longitud,
                    fecha = Date(),
                    poligono = inside
                )
                insertEntity(entity)
                getAllLocations()

                println(latitud.toString() +"    " +longitud)

            }
        }
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))


    }

    private fun insertEntity(entity: Ubicacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicacionDao.insert(entity)
            }
        }
    }

    private fun getAllLocations(): List<Ubicacion> {
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")
        var ubicaciones: List<Ubicacion> = emptyList()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
            }
            val markerOptionsList = mutableListOf<MarkerOptions>()
            for (ubicacion in ubicaciones) {
                val latLng = LatLng(ubicacion.latitud + 0.0010, ubicacion.longitud + 0.0010)
                markerOptionsList.add(
                    MarkerOptions().position(latLng)
                        .title(textoMarcador)
                )
            }
            // Agrega los marcadores al mapa
            for (markerOptions in markerOptionsList) {
                maps.addMarker(markerOptions)
            }
        }
        return ubicaciones
    }

    private fun createPolygon(): Polygon {
        val polygonOptions = PolygonOptions()
        polygonOptions.add(LatLng(-14.0095923,108.8152324))
        polygonOptions.add(LatLng( -43.3897529,104.2449199))
        polygonOptions.add(LatLng( -51.8906238,145.7292949))
        polygonOptions.add(LatLng( -31.7289525,163.3074199))
        polygonOptions.add(LatLng( -7.4505398,156.2761699))
        polygonOptions.add(LatLng( -14.0095923,108.8152324))
        return maps.addPolygon(polygonOptions)


    }
    private fun isLocationInsidePolygon(latLng: LatLng): Boolean {
        return PolyUtil.containsLocation(latLng, polygon.points, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }
    /*private fun getLocation() {
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Pedir permisos de ubicación
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )

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
    }*/

    private fun addMarkerToMap(latLng: LatLng, title: String?) {
        maps.addMarker(MarkerOptions().position(latLng).title(title))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    iniciaServicio()
                }
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Registrar el receptor para recibir actualizaciones de ubicación
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar el receptor al pausar el fragmento
        context?.unregisterReceiver(locationReceiver)
    }
    private fun iniciaServicio() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            val intent = Intent(context, LocationService::class.java)
            context?.startService(intent)
        }
    }
}
