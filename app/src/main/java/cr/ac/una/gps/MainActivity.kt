package cr.ac.una.gps

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout


class MainActivity : AppCompatActivity() {

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
    }

    override fun onBackPressed() {
    if(drawerLayout.isDrawerOpen(GravityCompat.START))
        drawerLayout.closeDrawer(GravityCompat.START)

        else
            super.onBackPressed()
    }
}