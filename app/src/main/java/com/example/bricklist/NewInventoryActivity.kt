package com.example.bricklist

import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
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

        addButton.setOnClickListener { _ ->
            val setNumber = "615"
            val url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/$setNumber.xml"

            val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    resultText.text = response
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
