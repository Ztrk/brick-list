package com.example.bricklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_new_inventory.*
import kotlinx.android.synthetic.main.content_new_inventory.*

class NewInventoryActivity : AppCompatActivity() {
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_inventory)
        setSupportActionBar(toolbar)

        queue = Volley.newRequestQueue(this)

        checkButton.setOnClickListener { _ ->

        }

        addButton.setOnClickListener {
            val setNumber = setNumberEdit.text
            val name = nameEdit.text.toString()
            val url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/$setNumber.xml"

            val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val parser = InventoryXmlParser()
                    val inventory = parser.parse(response)
                    inventory.inventory.name = name
                    resultText.text = inventory.parts[0].item.code
                },
                Response.ErrorListener { error ->
                    resultText.text = error.toString()
                }
            )
            stringRequest.tag = this

            queue.add(stringRequest)
        }
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(this)
    }
}
