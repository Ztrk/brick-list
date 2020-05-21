package com.example.bricklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_new_inventory.*
import kotlinx.android.synthetic.main.content_new_inventory.*

class NewInventoryActivity : AppCompatActivity() {
    private lateinit var viewModel: NewInventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_inventory)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this).get(NewInventoryViewModel::class.java)

        checkButton.setOnClickListener { _ ->

        }

        addButton.setOnClickListener {
            val setNumber = setNumberEdit.text
            val name = nameEdit.text.toString()
            viewModel.addInventory(setNumber.toString(), name)
        }

        viewModel.result.observe(this, Observer {
            resultText.text = it
        })

    }
}
