package com.example.bricklist

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
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
            val itemDecoration = DividerItemDecoration(context, viewManager.orientation)
            getDrawable(R.drawable.divider)?.let { itemDecoration.setDrawable(it) }
            addItemDecoration(itemDecoration)
        }

        val inventoryId = intent.getIntExtra("inventoryId", 0)
        val factory = PartsListViewModel.Factory(application, inventoryId)
        val viewModel = ViewModelProvider(this, factory).get(PartsListViewModel::class.java)

        viewModel.inventoryParts.observe(this, Observer {
            viewAdapter.inventoryParts = it.map { part -> toViewAdapterData(part) }
            println("Converted to view adapter data")
        })

    }

    private fun toViewAdapterData(part: InventoryPartWithReferences)
            : InventoryPartViewAdapter.InventoryPartData {

        val colorAndCodeString = resources.getString(
            R.string.color_and_code_string,
            part.color.name,
            part.item.code
        )
        val quantityString = resources.getString(
            R.string.quantity_string,
            part.inventoryPart.quantityInStore,
            part.inventoryPart.quantityInSet
        )
        val image = part.code?.image
        val drawable = if (image != null)
            BitmapDrawable(resources, image)
        else
            getDrawable(R.drawable.ic_broken_image_black_24dp)

        return InventoryPartViewAdapter.InventoryPartData(
            part.item.name,
            colorAndCodeString,
            quantityString,
            drawable
        )
    }

}
