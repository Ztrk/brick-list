package com.example.bricklist

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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

        // Create view model
        inventoryViewModel = ViewModelProvider(this)
            .get(InventoryViewModel::class.java)

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
