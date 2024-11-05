package com.example.mealsapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealsapp.dp.MealDatabase
import com.example.mealsapp.pojo.Meal
import com.example.mealsapp.pojo.MealList
import com.example.mealsapp.retrofit.Retrofitinstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealViewModel(
    val mealDatabase: MealDatabase
):ViewModel() {
    private var mealDetailsLiveData = MutableLiveData<Meal>()

    fun getMealDetail(id: String) {
        Retrofitinstance.api.getMealDetails(id).enqueue(object : Callback<MealList> {
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                if (response.body() != null) {
                    mealDetailsLiveData.value = response.body()!!.meals[0]
                } else {
                    return
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("MealActivity", t.message.toString())
            }
        })
    }


    fun observeMealDetailLiveData(): LiveData<Meal> {
        return mealDetailsLiveData
    }


    fun insertMeal(meal: Meal) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    mealDatabase.mealDoa().insert(meal)
                }
                Log.d("MealViewModel", "Meal inserted successfully: ${meal.strMeal}")
            } catch (e: Exception) {
                Log.e("MealViewModel", "Error inserting meal", e)
            }
        }
    }



}