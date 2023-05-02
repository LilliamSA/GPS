package cr.ac.una.gps

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Poligono
import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgregarUbicacionesActivity : AppCompatActivity() {

    private lateinit var latitudEditText: EditText
    private lateinit var longitudEditText: EditText
    private lateinit var guardarButton: Button
    private lateinit var atrasButton: Button
    private lateinit var poligonoDao: PoligonoDao

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        poligonoDao = AppDatabase.getInstance(this).poligonoDao()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregarubicaciones)

        latitudEditText = findViewById(R.id.edit_text_latitud)
        longitudEditText = findViewById(R.id.edit_text_longitud)
        latitudEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        longitudEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

        guardarButton = findViewById(R.id.btn_guardar)
        atrasButton = findViewById(R.id.btn_atras)


        //boton atras que me lleve donde se listan las ubicaciones
        atrasButton.setOnClickListener {
            val intent = Intent(this, PoligonoFragment::class.java)
            startActivity(intent)
        }
        guardarButton.setOnClickListener {
            val latitudString = latitudEditText.text.toString()
            val longitudString = longitudEditText.text.toString()

            if (latitudString.isNotBlank() && longitudString.isNotBlank()) {
                val entidad = Poligono(
                    id = null,
                    latitude = latitudString.toDouble(),
                    longitude = longitudString.toDouble()
                )

                guardarUbicacionEnBaseDeDatos(entidad)
                // Limpiar el contenido del EditText
                latitudEditText.setText("")
                longitudEditText.setText("")
                //mensaje de confirmacion
                Toast.makeText(this, "Ubicacion guardada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Los campos de latitud y longitud son requeridos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun guardarUbicacionEnBaseDeDatos(entidad: Poligono) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                poligonoDao.insert(entity = entidad)
            }
        }
    }
}

