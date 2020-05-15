package com.example.bricklist

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.*
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PartsListViewModel(application: Application, inventoryId: Int)
        : AndroidViewModel(application) {

    private val queue = Volley.newRequestQueue(application)
    private val inventoryPartDao = BrickListDatabase.getDatabase(application).getInventoryPartDao()

    private val image = MutableLiveData<Bitmap>()

    val inventoryParts = MediatorLiveData<List<InventoryPartWithReferences>>()

    init {
        viewModelScope.launch {
            try {
                image.value = requestImage("https://www.lego.com/service/bricks/5/2/300126")
            }
            catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        val parts = inventoryPartDao.getInventoryPartsById(inventoryId)
        inventoryParts.addSource(parts) {
            inventoryParts.value = combine(image.value, it)
        }
        inventoryParts.addSource(image) {
            var data = inventoryParts.value
            if (data == null) {
                data = listOf()
            }
            inventoryParts.value = combine(it, data)
        }
    }

    private fun combine(image: Bitmap?, inventoryParts: List<InventoryPartWithReferences>) = inventoryParts.map { part ->
        if (part.code.image == null) {
            part.copy(code = part.code.copy(image = image))
        }
        else {
            part
        }
    }

    private suspend fun requestImage(url: String): Bitmap = suspendCancellableCoroutine {
        val imageRequest = ImageRequest(url,
            Response.Listener { response ->
                it.resume(response)
            },
            0, 0, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.ARGB_8888,
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        it.invokeOnCancellation {
            imageRequest.cancel()
        }
        queue.add(imageRequest)
    }

    class Factory(private val application: Application, private val inventoryId: Int)
            : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PartsListViewModel::class.java)) {
                return modelClass
                    .getConstructor(Application::class.java, inventoryId::class.java)
                    .newInstance(application, inventoryId)
            }
            else {
                throw IllegalArgumentException("Cannot create instance of $modelClass")
            }
        }
    }
}
