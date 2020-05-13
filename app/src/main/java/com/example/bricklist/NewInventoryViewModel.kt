package com.example.bricklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NewInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val queue = Volley.newRequestQueue(application)
    private val brickListDao = BrickListDatabase
        .getDatabase(application).getBrickListDao()

    private val _result = MutableLiveData("")
    val result: LiveData<String>
        get() = _result

    fun addInventory(url: String, name: String) = viewModelScope.launch {
        val response = try {
            requestXml(url)
        }
        catch (error: VolleyError) {
            _result.value = error.toString()
            return@launch
        }

        val parser = InventoryXmlParser()
        val inventory = parser.parse(response)
        inventory.inventory.name = name

        insert(inventory)

        _result.value = "New project was created"
    }

    private suspend fun requestXml(url: String): String = suspendCancellableCoroutine {
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                it.resume(response)
            },
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        it.invokeOnCancellation {
            stringRequest.cancel()
        }
        queue.add(stringRequest)
    }

    private suspend fun insert(inventory: InventoryWithParts) {

    }
}
