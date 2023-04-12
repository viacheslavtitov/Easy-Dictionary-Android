package my.dictionary.free.view.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import my.dictionary.free.R
import my.dictionary.free.view.AbstractBaseActivity
import my.dictionary.free.view.ext.visibleSystemBars
import java.util.*

class MainActivity : AbstractBaseActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var navDrawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var userLogo: AppCompatImageView

    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(navController.graph, navDrawerLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.statusBars())
        visibleSystemBars(visible = true, type = WindowInsetsCompat.Type.systemBars())
        toolbar = findViewById(R.id.toolbar)
        navDrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        userLogo = navView.getHeaderView(0).findViewById(R.id.user_logo)
        setSupportActionBar(toolbar)
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        toolbar.setupWithNavController(navController, navDrawerLayout)
        toolbar.setNavigationOnClickListener {
            navDrawerLayout.openDrawer(GravityCompat.START)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->

        }
        navDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        navController.navigate(R.id.simpleFragment)
        drawerToggle.syncState()
        toolbar.title = "Home"
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_settings -> {

                }
                R.id.nav_log_out -> {

                }
            }
            true
        }
        FirebaseAuth.getInstance().currentUser?.photoUrl?.let {
            userLogo.setImageURI(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            navDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (navController.popBackStack().not()) {
                finish()
            } else {
                super.onBackPressed()
            }
        }
    }

    private val drawerToggle: ActionBarDrawerToggle by lazy {
        object : ActionBarDrawerToggle(
            this,
            navDrawerLayout,
            toolbar,
            R.string.menu_drawer_open,
            R.string.menu_drawer_closed
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }
        }
    }
}