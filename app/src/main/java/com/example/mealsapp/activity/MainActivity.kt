package com.example.mealsapp.activity

import com.example.mealsapp.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.mealsapp.dp.MealDatabase
import com.example.mealsapp.viewModel.HomeViewModel
import com.example.mealsapp.viewModel.HomeViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView



class MainActivity : AppCompatActivity() {
    val viewModel: HomeViewModel by lazy {
        val mealDatabase = MealDatabase.getInstance(this)
        val homeViewModelProviderFactory = HomeViewModelFactory(mealDatabase)
        ViewModelProvider(this, homeViewModelProviderFactory)[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.btm_nav)
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        NavigationUI.setupWithNavController(bottomNavigation, navController)
    }

    // هنا أضفنا onBackPressed
    override fun onBackPressed() {
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        if (!navController.popBackStack()) {
            super.onBackPressed() // يخرج من التطبيق إذا لم يكن هناك شاشة سابقة
        }
    }
}
