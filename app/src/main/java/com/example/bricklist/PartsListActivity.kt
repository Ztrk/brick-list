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
            InventoryPart().apply { name = "Name"; color = "Red"; itemCode = "old3001" },
            InventoryPart().apply { name = "Some brick"; color = "Yellow"; itemCode = "231" },
            InventoryPart().apply { name = "Flat piece"; color = "Green"; itemCode = "222" }
        )

        inventoryPartsView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

}
