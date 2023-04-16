package cr.ac.una.gps

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.SharedPreferences
import android.widget.Button

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

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
        //
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))


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
        mapFragment?.getMapAsync(callback)

    }
}
