package cr.ac.una.gps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.ac.una.gps.adapter.PoligonoAdapter
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Poligono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PoligonoFragment : Fragment() {

    private lateinit var poligonoDao: PoligonoDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var agregarButton: Button
    private lateinit var eliminarButton: Button
    private lateinit var poligonos: List<Poligono>
    private lateinit var adapter: PoligonoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        poligonoDao = AppDatabase.getInstance(requireContext()).poligonoDao()
        return inflater.inflate(R.layout.fragment_poligono, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = requireView().findViewById(R.id.recycler_ubicaciones)
        agregarButton = requireView().findViewById(R.id.btn_agregar)
        eliminarButton = requireView().findViewById(R.id.btn_eliminar)

        agregarButton.setOnClickListener {
            val intent = Intent(requireContext(), AgregarUbicacionesActivity::class.java)
            startActivity(intent)
        }

        eliminarButton.setOnClickListener {
            eliminarUbicacion(poligonos.last())
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            poligonos = obtenerPoligonosDeBaseDeDatos()
            adapter = PoligonoAdapter(poligonos, this@PoligonoFragment)
            recyclerView.adapter = adapter
        }
    }

    private suspend fun obtenerPoligonosDeBaseDeDatos(): List<Poligono> {
        var polygon: List<Poligono> = emptyList()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                polygon = poligonoDao.getAll() as List<Poligono>
            }
        }.join()
        return polygon

    }

    //FUNCIONS PARA ELIMINAR LAS UBICACIONES
    fun eliminarUbicacion(poligono: Poligono) {
        lifecycleScope.launch {
            eliminarUbicacionDeBaseDeDatos(poligono)
            poligonos = obtenerPoligonosDeBaseDeDatos()
            adapter = PoligonoAdapter(poligonos, this@PoligonoFragment)
            recyclerView.adapter = adapter
        }
    }

    private suspend fun eliminarUbicacionDeBaseDeDatos(poligono: Poligono) {
        withContext(Dispatchers.IO) {
            poligonoDao.delete(poligono)
        }
    }
}



