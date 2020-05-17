package com.example.bricklist

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
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

        // Create view model
        val inventoryId = intent.getIntExtra("inventoryId", 0)
        val factory = PartsListViewModel.Factory(application, inventoryId)
        val viewModel = ViewModelProvider(this, factory).get(PartsListViewModel::class.java)

        // Create view adapter for recycler view
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = InventoryPartViewAdapter(
            onPlusButtonClickListener = { view, position ->
                viewModel.incrementQuantity(position)
            },
            onMinusButtonClickListener = { view, position ->
                viewModel.decrementQuantity(position)
            }
        )

        // Create recycler view
        inventoryPartsView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            val itemDecoration = DividerItemDecoration(context, viewManager.orientation)
            getDrawable(R.drawable.divider)?.let { itemDecoration.setDrawable(it) }
            addItemDecoration(itemDecoration)
        }

        // Create observer of inventory parts
        viewModel.inventoryParts.observe(this, Observer {
            viewAdapter.inventoryParts = it.map { part -> toViewAdapterData(part) }
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
