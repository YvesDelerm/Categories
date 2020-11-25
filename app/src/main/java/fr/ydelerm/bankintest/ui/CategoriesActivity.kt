package fr.ydelerm.bankintest.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import fr.ydelerm.bankintest.BankinApplication
import fr.ydelerm.bankintest.R
import fr.ydelerm.bankintest.model.Category
import fr.ydelerm.bankintest.viewmodel.CategoriesViewModel
import fr.ydelerm.bankintest.vo.Status
import kotlinx.android.synthetic.main.activity_categories.*

class CategoriesActivity : AppCompatActivity(){

    companion object {
        const val PARAM_SELECTED_CATEGORY_ID = "selectedCategoryId"
    }

    private var selectedCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as BankinApplication).appGraph.inject(this)
        setContentView(R.layout.activity_categories)
        if (intent.hasExtra(PARAM_SELECTED_CATEGORY_ID)) {
            selectedCategoryId = intent.getIntExtra(PARAM_SELECTED_CATEGORY_ID, 0)
        }

        val tripViewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)
        tripViewModel.selectedCategoryId = selectedCategoryId

        supportActionBar?.setDisplayHomeAsUpEnabled(tripViewModel.isDisplayHomeAsUpEnabled())

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isSaveEnabled = true

        tripViewModel.getCategories().observe(this) {
            swipeContainer.isRefreshing = false //(Status.LOADING == it.status)

            val categoriesAdapter = CategoriesAdapter(it ?: ArrayList(), tripViewModel.getCategoryClickListener(this))
            recyclerView.swapAdapter(categoriesAdapter, true)
        }

        tripViewModel.getRequestStatus().observe(this) {
            buttonRefresh.visibility = boolToVisibility(Status.ERROR == it)
            textviewError.visibility = boolToVisibility(Status.ERROR == it)

            if (Status.ERROR == it) {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            }
        }

        buttonRefresh.setOnClickListener { tripViewModel.refreshData() }
        swipeContainer.setOnRefreshListener { tripViewModel.refreshData() }

        tripViewModel.refreshData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun boolToVisibility(b: Boolean): Int {
        return if (b) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}