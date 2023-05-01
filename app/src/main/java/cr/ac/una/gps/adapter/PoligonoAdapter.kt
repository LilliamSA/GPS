package cr.ac.una.gps.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cr.ac.una.gps.R
import cr.ac.una.gps.entity.Poligono
import android.widget.TextView
import cr.ac.una.gps.PoligonoFragment


class PoligonoAdapter(
    private val poligonos: List<Poligono>,
    poligonoFragment: PoligonoFragment,
) : RecyclerView.Adapter<PoligonoAdapter.PoligonoViewHolder>() {

    class PoligonoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewLatitud: TextView = view.findViewById(R.id.text_latitud)
        private val textViewLongitud: TextView = view.findViewById(R.id.text_longitud)


        fun bind(poligono: Poligono) {
            textViewLatitud.text = poligono.latitude.toString()
            textViewLongitud.text = poligono.longitude.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoligonoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poligono, parent, false)
        return PoligonoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoligonoViewHolder, position: Int) {
        val poligono = poligonos[position]
        holder.bind(poligono)
    }

    override fun getItemCount(): Int {
        return poligonos.size
    }

}


