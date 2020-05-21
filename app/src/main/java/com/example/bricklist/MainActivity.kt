package com.example.bricklist

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewAdapter: InventoryViewAdapter
    private lateinit var inventoryViewModel: InventoryViewModel

    private var clickedItem: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create view model
        inventoryViewModel = ViewModelProvider(this)
            .get(InventoryViewModel::class.java)

        // Create recycler view
        val viewManager = LinearLayoutManager(this)
        viewAdapter = InventoryViewAdapter { view, inventory ->
            clickedItem?.setBackgroundColor(0)
            clickedItem = view

            val transition = TransitionDrawable(arrayOf(
                ColorDrawable(0x0), getDrawable(R.color.colorAccentTransparent)
            ))
            view.background = transition
            transition.startTransition(150)

            val intent = Intent(this, PartsListActivity::class.java)
            intent.putExtra("inventoryId", inventory.id)
            startActivity(intent)
        }

        inventoriesView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            val itemDecoration = DividerItemDecoration(context, viewManager.orientation)
            getDrawable(R.drawable.divider)?.let { itemDecoration.setDrawable(it) }
            addItemDecoration(itemDecoration)
        }

        val swipeHelper = createSwipeHelper()

        // Add swiping
        inventoryViewModel.showArchived.observe(this, Observer {
            if (!it) {
                swipeHelper.attachToRecyclerView(inventoriesView)
            } else {
                swipeHelper.attachToRecyclerView(null)
            }
        })

        inventoryViewModel.inventories.observe(this, Observer {
            viewAdapter.inventories = it
        })

        addInventoryButton.setOnClickListener {
            val intent = Intent(this, NewInventoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        clickedItem?.setBackgroundColor(0)
        val showArchived = PreferenceManager.getDefaultSharedPreferences(application)
            .getBoolean("archived", false)
        inventoryViewModel.setArchive(showArchived)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun createSwipeHelper(): ItemTouchHelper {
        return ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val inventory = inventoryViewModel.archive(viewHolder.adapterPosition)
                    inventory?.let {
                        val snackbar = Snackbar.make(
                            coordinatorLayout,
                            getString(R.string.archived_message, inventory.name),
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction(R.string.archived_undo_action) {
                            inventoryViewModel.dearchive()
                        }
                        snackbar.show()
                    }
                }
            }
        )
    }
}
