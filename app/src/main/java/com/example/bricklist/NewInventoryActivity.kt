package com.example.bricklist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_new_inventory.*
import kotlinx.android.synthetic.main.content_new_inventory.*
import kotlin.math.min
import kotlin.text.StringBuilder

class NewInventoryActivity : AppCompatActivity() {
    private lateinit var viewModel: NewInventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_inventory)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this).get(NewInventoryViewModel::class.java)

        addButton.setOnClickListener {
            val setNumber = setNumberEdit.text
            val name = nameEdit.text.toString()
            viewModel.addInventory(setNumber.toString(), name)
        }

        viewModel.result.observe(this, Observer {
            spinner.visibility =
                if (it == NewInventoryViewModel.State.IN_PROGRESS)
                    View.VISIBLE
                else
                    View.GONE

            when (it) {
                NewInventoryViewModel.State.WRONG_URL -> {
                    Snackbar.make(coordinatorLayout, R.string.add_wrong_url, Snackbar.LENGTH_SHORT)
                        .show()
                }
                NewInventoryViewModel.State.NETWORK_ERROR -> {
                    Snackbar.make(coordinatorLayout, R.string.add_network_error, Snackbar.LENGTH_SHORT)
                        .show()
                }
                NewInventoryViewModel.State.SUCCESS -> {
                    Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }

            if (it != NewInventoryViewModel.State.NONE &&
                it != NewInventoryViewModel.State.IN_PROGRESS) {
                viewModel.resetResult()
            }
        })

        viewModel.bricksNotFound.observe(this, Observer {
            if (it.isNotEmpty()) {
                val text = StringBuilder(getString(R.string.add_bricks_not_found))
                text.append('\n')

                val maxVisible = min(3, it.count())
                for (code in it.subList(0, maxVisible)) {
                    text.append(code)
                    text.append('\n')
                }
                if (it.count() > maxVisible) {
                    text.append(getString(R.string.add_bricks_not_found_others,
                        it.count() - maxVisible))
                }

                Toast.makeText(this, text.toString(), Toast.LENGTH_LONG)
                    .show()

                viewModel.resetBricksNotFound()
            }
        })

        viewModel.addWhenInProgress.observe(this, Observer {
            if (it) {
                Snackbar.make(coordinatorLayout, R.string.add_already_in_progress,
                    Snackbar.LENGTH_SHORT).show()
                viewModel.handledAddWhenInProgress()
            }
        })
    }
}
