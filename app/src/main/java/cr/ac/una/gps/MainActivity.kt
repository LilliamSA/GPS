package cr.ac.una.gps

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)

        var toogle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        lateinit var fragment: Fragment

        when (item.itemId) {
            R.id.maps -> {
                fragment = MapsFragment()
            }
            R.id.Acercade -> {
                fragment = AcercaDeFragment()
                fragment.show(supportFragmentManager, "dialogo_acerca_de")
                return true
            }

            R.id.Configurations -> {
                fragment = ConfiguracionFragment()
            }
            R.id.Polygon -> {
                fragment = PoligonoFragment()
            }
            R.id.Llamada -> {
                fragment = LlamadaFragment()
            }
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_content, fragment)
            .commit()
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

}