package com.example.mealsapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealsapp.dp.MealDatabase
import com.example.mealsapp.pojo.Category
import com.example.mealsapp.pojo.CategoryList
import com.example.mealsapp.pojo.MealsByCategoryList
import com.example.mealsapp.pojo.MealsByCategory
import com.example.mealsapp.pojo.Meal
import com.example.mealsapp.pojo.MealList
import com.example.mealsapp.retrofit.Retrofitinstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(
    private val mealDatabase: MealDatabase
) : ViewModel() {

    private var randomMealLiveData = MutableLiveData<Meal>()
    private var popularItemsLiveData = MutableLiveData<List<MealsByCategory>>()
    private var categoriesLiveData = MutableLiveData<List<Category>>()
    private var favoriteMealsLiveData = mealDatabase.mealDoa().getAllMeals()
    private var bottomSheetMealLiveData = MutableLiveData<Meal>()
    private var searchMealLiveData = MutableLiveData<List<Meal>>()

    // To track ongoing search job
    private var searchJob: Job? = null

    // LiveData to track insert meal status
    private val _insertMealStatus = MutableLiveData<Boolean>()
    val insertMealStatus: LiveData<Boolean> get() = _insertMealStatus

    fun getRandomMeal() {
        Retrofitinstance.api.getRandomMeal().enqueue(object : Callback<MealList> {
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                response.body()?.let {
                    randomMealLiveData.value = it.meals[0]
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.e("HomeViewModel", "Error fetching random meal", t)
            }
        })
    }

    fun getPopularItems() {
        Retrofitinstance.api.getPopularItems("Seafood").enqueue(object : Callback<MealsByCategoryList> {
            override fun onResponse(call: Call<MealsByCategoryList>, response: Response<MealsByCategoryList>) {
                response.body()?.let {
                    popularItemsLiveData.value = it.meals
                }
            }

            override fun onFailure(call: Call<MealsByCategoryList>, t: Throwable) {
                Log.e("HomeViewModel", "Error fetching popular items", t)
            }
        })
    }

    fun getCategories() {
        Retrofitinstance.api.getCategories().enqueue(object : Callback<CategoryList> {
            override fun onResponse(call: Call<CategoryList>, response: Response<CategoryList>) {
                response.body()?.let {
                    categoriesLiveData.postValue(it.categories)
                }
            }

            override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                Log.e("HomeViewModel", "Error fetching categories", t)
            }
        })
    }

    fun getMealById(id: String) {
        Retrofitinstance.api.getMealDetails(id).enqueue(object : Callback<MealList> {
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                response.body()?.meals?.firstOrNull()?.let {
                    bottomSheetMealLiveData.postValue(it)
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.e("HomeViewModel", "Error fetching meal by ID", t)
            }
        })
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            mealDatabase.mealDoa().delete(meal)
        }
    }

    fun observeRandomMealLiveData(): LiveData<Meal> = randomMealLiveData

    fun observePopularItemsLiveData(): LiveData<List<MealsByCategory>> = popularItemsLiveData

    fun observeCategoriesLiveData(): LiveData<List<Category>> = categoriesLiveData

    fun observeFavoriteMealsLiveData(): LiveData<List<Meal>> = favoriteMealsLiveData

    fun insertMeal(meal: Meal) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    mealDatabase.mealDoa().insert(meal)
                }
                Log.d("MealViewModel", "Meal inserted successfully")
                _insertMealStatus.postValue(true) // Indicate success
            } catch (e: Exception) {
                Log.e("MealViewModel", "Error inserting meal", e)
                _insertMealStatus.postValue(false) // Indicate failure
            }
        }
    }

    fun resetInsertMealStatus() {
        _insertMealStatus.value = false  // Reset the insert meal status
    }

    fun observeBottomSheetMeal(): LiveData<Meal> = bottomSheetMealLiveData

    fun searchMeals(searchQuery: String) {
        // Cancel any ongoing search job
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            Retrofitinstance.api.searchMeals(searchQuery).enqueue(object : Callback<MealList> {
                override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                    response.body()?.meals?.let {
                        searchMealLiveData.postValue(it)
                    }
                }

                override fun onFailure(call: Call<MealList>, t: Throwable) {
                    Log.e("HomeViewModel", "Error searching meals", t)
                }
            })
        }
    }

    fun observeSearchMealsLiveData(): LiveData<List<Meal>> = searchMealLiveData

    // New method to stop searching
    fun stopSearching() {
        searchJob?.cancel()  // Cancel any ongoing search jobs
        searchJob = null      // Reset the job reference
        searchMealLiveData.postValue(emptyList()) // Optionally, clear search results
    }
}
