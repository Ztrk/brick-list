package com.example.bricklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.activity_parts_list.*
import kotlinx.android.synthetic.main.content_parts_list.*

class PartsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parts_list)
        setSupportActionBar(toolbar)

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = InventoryPartViewAdapter()
        viewAdapter.inventoryParts = listOf(
            InventoryPartWithReferences(
                item = Item(name = "Name", code = "old3001"),
                color = Color(name = "Red")
            ),
            InventoryPartWithReferences(
                item = Item(name = "Some brick", code = "231"),
                color = Color(name = "Yellow")
            ),
            InventoryPartWithReferences(
                item = Item(name = "Flat piece", code = "222"),
                color = Color(name = "Green")
            )
        )

        inventoryPartsView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

}
