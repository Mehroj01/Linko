package com.neb.linko

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.neb.linko.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BottomNavigationClick {
    lateinit var mainBinding: ActivityMainBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        auth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = mainBinding.navView

        val navController = findNavController(R.id.my_nav_host_fragment)

        navView.setupWithNavController(navController)
        navView.itemIconTintList = null

        mainBinding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    findNavController(R.id.my_nav_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.navigation_bookmark -> {
                    findNavController(R.id.my_nav_host_fragment).navigate(R.id.saveFragment)
                }
                R.id.navigation_profile -> {
                    findNavController(R.id.my_nav_host_fragment).navigate(R.id.profileFragment)
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    override fun homeClick() {
        finish()
    }

    override fun saveClick() {
        mainBinding.navView.selectedItemId = R.id.navigation_home
    }

    override fun profileClick() {
        mainBinding.navView.selectedItemId = R.id.navigation_home
    }
}