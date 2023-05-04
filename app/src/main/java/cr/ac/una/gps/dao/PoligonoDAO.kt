package cr.ac.una.gps.dao

import androidx.room.*
import cr.ac.una.gps.entity.Poligono
import cr.ac.una.gps.entity.Ubicacion


@Dao
interface PoligonoDao {
    @Insert
    fun insert(entity: Poligono)

    @Query("SELECT * FROM poligono")
    fun getAll(): List<Poligono?>?
    @Update
    fun update(entity: Poligono)

    @Delete
    fun delete(entity: Poligono)

}
