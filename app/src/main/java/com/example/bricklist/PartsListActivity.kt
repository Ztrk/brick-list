package com.example.bricklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

        inventoryPartsView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val inventoryId = intent.getIntExtra("inventoryId", 0)
        val factory = PartsListViewModel.Factory(application, inventoryId)
        val viewModel = ViewModelProvider(this, factory).get(PartsListViewModel::class.java)

        viewModel.inventoryParts.observe(this, Observer {
            viewAdapter.inventoryParts = it
        })

    }

}
