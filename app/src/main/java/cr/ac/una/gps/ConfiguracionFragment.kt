package cr.ac.una.gps

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.content.Context
import android.widget.Toast


class ConfiguracionFragment : Fragment() {

    private lateinit var editMarcador: EditText
    private lateinit var botonGuardar: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(cr.ac.una.gps.R.layout.fragment_configuracion, container, false)

        // Obtener las referencias a los elementos de la interfaz
        editMarcador = view.findViewById(R.id.textmarcador)
        botonGuardar = view.findViewById(R.id.btnguardar)

        // Asignar un listener al botón
        botonGuardar.setOnClickListener {
            guardarMarcador()
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar el texto guardado en SharedPreferences
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val textoMarcador = sharedPreferences?.getString("textoMarcador", "")

        // Establecer el texto en el EditText
        editMarcador.setText(textoMarcador)

        
    }

    private fun guardarMarcador() {
        val textoMarcador = editMarcador.text.toString()

        // Guardar el texto del marcador en SharedPreferences
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("textoMarcador", textoMarcador)
        editor?.apply()

        // Mostrar un mensaje de confirmación
        Toast.makeText(activity, "Marcador guardado correctamente", Toast.LENGTH_SHORT).show()
    }


}