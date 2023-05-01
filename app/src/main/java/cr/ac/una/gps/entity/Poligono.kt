package cr.ac.una.gps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Poligono (
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val latitude: Double,
    val longitude: Double,
    )

