package com.kanyandula.nyasa.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.ActivityMainBinding
import com.kanyandula.nyasa.ui.BaseActivity
import com.kanyandula.nyasa.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class MainActivity : BaseActivity()
{

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var bottomNavigationView: BottomNavigationView



    override fun displayProgressBar(bool: Boolean) {
        if(bool){
            binding.progressBar.visibility = View.VISIBLE
        }
        else{
            binding.progressBar.visibility = View.GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val  view = binding.root
        setContentView(view)

        setupActionBar()

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.main_nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController


        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.blogFragment, R.id.createBlogFragment,  R.id.accountFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        subscribeObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }



    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolBar)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }




    fun subscribeObservers(){
        sessionManager.cachedToken.observe(this, Observer{ authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if(authToken == null || authToken.account_pk == -1 || authToken.token == null){
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


}
