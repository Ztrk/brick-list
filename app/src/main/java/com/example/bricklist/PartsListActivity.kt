package com.example.bricklist

import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_parts_list.*
import kotlinx.android.synthetic.main.content_parts_list.*
import java.io.File

class PartsListActivity : AppCompatActivity() {

    private lateinit var viewModel: PartsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parts_list)
        setSupportActionBar(toolbar)

        // Create view model
        val inventoryId = intent.getIntExtra("inventoryId", 0)
        val factory = PartsListViewModel.Factory(application, inventoryId)
        viewModel = ViewModelProvider(this, factory).get(PartsListViewModel::class.java)

        // Create view adapter for recycler view
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = InventoryPartViewAdapter(
            onPlusButtonClickListener = { _, position ->
                viewModel.incrementQuantity(position)
            },
            onMinusButtonClickListener = { _, position ->
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

        viewModel.exportReady.observe(this, Observer {
            if (it) {
                exportData()
                viewModel.handledExport()
            }
        })

    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_parts_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.actionExport -> {
                viewModel.exportToXml()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        val drawable =
            if (image != null) {
                BitmapDrawable(resources, image)
            } else {
                getDrawable(R.drawable.ic_broken_image_black_24dp)
            }

        val background =
            if (part.inventoryPart.quantityInSet == part.inventoryPart.quantityInStore) {
                getDrawable(R.color.colorCompleted)
            } else {
                ColorDrawable(0)
            }

        return InventoryPartViewAdapter.InventoryPartData(
            part.item.name,
            colorAndCodeString,
            quantityString,
            drawable,
            background
        )
    }

    private fun exportData() {
        val file = File(this.filesDir, EXPORT_PATH)
        val uri = FileProvider.getUriForFile(this,
            "com.example.bricklist.fileprovider", file)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(contentResolver, getString(R.string.export_uri_label), uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val title = getString(R.string.export_chooser_title)
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
        else {
            val toast = Toast.makeText(this,
                R.string.export_no_application, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}
