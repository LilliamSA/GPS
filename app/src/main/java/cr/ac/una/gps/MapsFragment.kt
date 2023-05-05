package cr.ac.una.gps

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.pm.PackageManager
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
import android.net.Uri
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.entity.Poligono
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MapsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var maps: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var ubicacionDao: UbicacionDao
    private lateinit var poligonoDao: PoligonoDao
    private lateinit var locationReceiver: BroadcastReceiver
    private lateinit var polygon: Polygon
    lateinit var filterButton: Button


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

        lifecycleScope.launch {
            // llamada a dibujarPoligono() dentro de una corrutina
            polygon = dibujarPoligono()
            getAllLocations()
        }
       /* var poligonos: List<Poligono> = emptyList()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                poligonos = poligonoDao.getAll() as List<Poligono>
            }
            val polygonOptions = PolygonOptions()
            if (poligonos.isNotEmpty()) {
                for (ubicacion in poligonos) {
                    polygonOptions.add(LatLng(ubicacion.latitude, ubicacion.longitude))
                }
               polygon = maps.addPolygon(polygonOptions)
            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage("No hay poligono registrado")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            }

        }*/
        googleMap.setOnMarkerClickListener { marker ->
            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            if (PolyUtil.containsLocation(latLng, polygon?.points, true)) {
                AlertDialog.Builder(requireContext())
                    .setMessage("El marcador esta dentro del poligono")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                        makePhoneCall()
                    }
                    .show()

            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage("El marcador esta fuera del poligono")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            false
        }


    }

    // Define un número de solicitud para la actividad de selección de fecha
    companion object {
        const val REQUEST_DATE_PICKER = 1
        const val FILTER_REQUEST_CODE = 123
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ubicacionDao = AppDatabase.getInstance(requireContext()).ubicacionDao()
        poligonoDao = AppDatabase.getInstance(requireContext()).poligonoDao()

        //hacer un setOnClickListener al boton de filtrar
        filterButton = view.findViewById(R.id.btn_filtro)

        filterButton.setOnClickListener {
            val intent = Intent(requireContext(), dataPicker_Activity::class.java)
            startActivityForResult(intent, FILTER_REQUEST_CODE)
        }


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

                var inside = isLocationInsidePolygon(LatLng(latitud, longitud))


                val dataFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dataFormat.timeZone = TimeZone.getTimeZone("GMT-06:00")

                val entity = Ubicacion(
                    id = null,
                    latitud = latitud,
                    longitud = longitud,
                    fecha = dataFormat.format(Date()),
                    area_restringida = inside
                )
                if (latitud != 0.0 && longitud != 0.0) {
                    insertEntity(entity)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "No hay ubicaciones registradas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                /*  if(inside) {
                   makePhoneCall()
                }*/

                println(latitud.toString() + "    " + longitud)
            }
        }
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))
    }
    suspend fun dibujarPoligono(): Polygon {
        val poligonoDao = AppDatabase.getInstance(requireContext()).poligonoDao()
        var poligonos: List<Poligono> = emptyList()
        withContext(Dispatchers.IO) {
            poligonos = poligonoDao.getAll() as List<Poligono>
        }
        val polygonOptions = PolygonOptions()
        if (poligonos.isNotEmpty()) {
            for (ubicacion in poligonos) {
                polygonOptions.add(LatLng(ubicacion.latitude, ubicacion.longitude))
            }
            return maps.addPolygon(polygonOptions)
        } else {
            AlertDialog.Builder(requireContext())
                .setMessage("No hay poligono registrado")
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        return maps.addPolygon(polygonOptions)
    }


    private fun getAllLocations() : List<Ubicacion> {
        // Cambiar el color de los marcadores dentro del polígono
        val insideColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        val outsideColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

        val textoMarcador = sharedPreferences.getString("textoMarcador", "")
        var ubicaciones: List<Ubicacion> = emptyList()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
            }
            if (ubicaciones.isNotEmpty()) {
                val markerOptionsList = mutableListOf<MarkerOptions>()
                for (ubicacion in ubicaciones) {
                    val latLng = LatLng(ubicacion.latitud, ubicacion.longitud)
                    if (isLocationInsidePolygon(LatLng(ubicacion.latitud, ubicacion.longitud))) {
                        markerOptionsList.add(
                            MarkerOptions().position(latLng)
                                .title(textoMarcador)
                                .icon(insideColor)
                        )
                    } else {
                        markerOptionsList.add(
                            MarkerOptions().position(latLng)
                                .title(textoMarcador)
                                .icon(outsideColor)
                        )
                    }
                }
                // Agrega los marcadores al mapa
                for (markerOptions in markerOptionsList) {
                    maps.addMarker(markerOptions)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "No hay ubicaciones registradas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return ubicaciones
    }


    private fun insertEntity(entity: Ubicacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicacionDao.insert(entity)
            }
        }
    }

    private fun isLocationInsidePolygon(latLng: LatLng): Boolean {
        return polygon != null && PolyUtil.containsLocation(latLng, polygon?.points, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
                    iniciaServicio()
                    makePhoneCall()
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        } else {
            val intent = Intent(context, LocationService::class.java)
            context?.startService(intent)
        }
    }

    fun makePhoneCall() {
        val textoLlamada = sharedPreferences.getString("textoLlamada", "")
        val intent = Intent(Intent.ACTION_CALL)
        val permission = android.Manifest.permission.CALL_PHONE
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 1)
        } else {
            intent.data = Uri.parse("tel:$textoLlamada")
            startActivity(intent)
        }
    }

    // Maneja el resultado de la actividad de selección de fecha
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DATE_PICKER && resultCode == Activity.RESULT_OK) {
            // Obtiene la fecha seleccionada de la actividad de selección de fecha
            val selectedDate = sharedPreferences.getString("fecha", "")
            if (selectedDate != null) {
                filterPinsByDate(selectedDate)
            }
        }
    }

    // Filtra los pines en el mapa por la fecha especificada (en formato "dd/MM/yyyy")
    private fun filterPinsByDate(date: String) {
        val dataFormat = SimpleDateFormat("dd/MM/yyyy")
        dataFormat.timeZone = TimeZone.getTimeZone("GMT-06:00")
        dataFormat.isLenient = false
        val fecha = dataFormat.parse(date)
        val ubicaciones=ubicacionDao.getByDate(date)
        val filteredPins = ubicaciones.filter { ubicacion ->
            val ubicacionDate = dataFormat.parse(ubicacion.fecha)
            ubicacionDate == fecha
        }

        // Cambiar el color de los marcadores dentro del polígono
        val insideColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        val outsideColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        val textoMarcador = sharedPreferences.getString("textoMarcador", "")

        if (filteredPins.isNotEmpty()) {
            val markerOptionsList = mutableListOf<MarkerOptions>()
            for (ubicacion in filteredPins) {
                val latLng = LatLng(ubicacion.latitud, ubicacion.longitud)
                if (isLocationInsidePolygon(LatLng(ubicacion.latitud, ubicacion.longitud))) {
                    markerOptionsList.add(
                        MarkerOptions().position(latLng)
                            .title(textoMarcador)
                            .icon(insideColor)
                    )
                } else {
                    markerOptionsList.add(
                        MarkerOptions().position(latLng)
                            .title(textoMarcador)
                            .icon(outsideColor)
                    )
                }
            }
            // Agrega los marcadores al mapa
            for (markerOptions in markerOptionsList) {
                maps.addMarker(markerOptions)
            }
        } else {
            Toast.makeText(requireContext(), "No hay ubicaciones registradas", Toast.LENGTH_SHORT)
                .show()
        }
    }
}