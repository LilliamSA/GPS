package cr.ac.una.gps

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Poligono
import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class dataPicker_Activity : AppCompatActivity() {

    private lateinit var filterButton: Button
    private lateinit var cancelButton: Button
    lateinit var fecha: DatePicker


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.datapicker)


        filterButton = findViewById(R.id.btn_filter)
        cancelButton = findViewById(R.id.btn_cancel)
        fecha = findViewById(R.id.datePicker)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        filterButton.setOnClickListener {
            val intent = Intent()
            val dateString = "%02d/%02d/%d".format(fecha.dayOfMonth, fecha.month + 1, fecha.year)
            intent.putExtra("fecha", dateString)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}

