package com.example.bricklist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewAdapter: InventoryViewAdapter
    private lateinit var inventoryViewModel: InventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create recycler view
        val viewManager = LinearLayoutManager(this)
        viewAdapter = InventoryViewAdapter { _, inventory ->
            Toast.makeText(this, inventory.name, Toast.LENGTH_LONG).show()
        }

        inventoriesView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Create view model
        inventoryViewModel = ViewModelProvider(this)
            .get(InventoryViewModel::class.java)

        inventoryViewModel.inventories.observe(this, Observer {
            viewAdapter.inventories = it
        })

        inventoryViewModel.insert(Inventory().apply { name = "ASDF" })
        inventoryViewModel.insert(Inventory().apply { name = "Name" })
        inventoryViewModel.insert(Inventory().apply { name = "Some project" })

        addInventoryButton.setOnClickListener {
            val intent = Intent(this, NewInventoryActivity::class.java)
            startActivity(intent)
        }
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
