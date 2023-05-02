package cr.ac.una.gps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView


class LlamadaFragment : Fragment() {

    private lateinit var textoLlamada: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(cr.ac.una.gps.R.layout.fragment_llamada, container, false)

        textoLlamada = view.findViewById(R.id.phone_number)
        saveButton = view.findViewById(R.id.call_button)
        // Asignar un listener al botón
        saveButton.setOnClickListener {
            guardarNumber()
        }
        return view
    }

    private fun guardarNumber() {
        val textCall = textoLlamada.text.toString()

        // Guardar el texto del marcador en SharedPreferences
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("textoLlamada", textCall)
        editor?.apply()
        // Limpiar el contenido del EditText
        textoLlamada.setText("")
        // Mostrar un mensaje de confirmación
        Toast.makeText(activity, "Número guardado correctamente", Toast.LENGTH_SHORT).show()
    }


}