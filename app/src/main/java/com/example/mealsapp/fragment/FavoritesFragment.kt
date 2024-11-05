package com.example.mealsapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mealsapp.activity.MainActivity
import com.example.mealsapp.adapters.MealsAdapter
import com.example.mealsapp.databinding.FragmentFavoritesBinding
import com.example.mealsapp.viewModel.HomeViewModel
import com.google.android.material.snackbar.Snackbar

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var favoritesAdapter: MealsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareRecyclerView()
        observeFavorites()
        observeInsertMealStatus()  // Observe insert meal status

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedMeal = favoritesAdapter.differ.currentList[position]

                // Delete the meal from favorites
                viewModel.deleteMeal(deletedMeal)

                // Show Snackbar with Undo option
                Snackbar.make(requireView(), "Meal deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Re-insert the meal when 'Undo' is clicked
                        viewModel.insertMeal(deletedMeal)
                    }.show()
            }
        }

        // Attach ItemTouchHelper to the RecyclerView
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.recFavourites)
    }

    private fun prepareRecyclerView() {
        favoritesAdapter = MealsAdapter()
        binding.recFavourites.apply {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            adapter = favoritesAdapter
        }
    }

    private fun observeFavorites() {
        viewModel.observeFavoriteMealsLiveData().observe(viewLifecycleOwner) { meals ->
            favoritesAdapter.differ.submitList(meals)
        }
    }

    private fun observeInsertMealStatus() {
        viewModel.insertMealStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Snackbar.make(requireView(), "Meal added to favorites", Snackbar.LENGTH_SHORT).show()
                viewModel.resetInsertMealStatus()  // Reset status after showing Snackbar
            } else {
                Snackbar.make(requireView(), "Failed to add meal to favorites", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
