package com.example.bricklist

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.*
import com.android.volley.ClientError
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PartsListViewModel(application: Application, inventoryId: Int)
        : AndroidViewModel(application) {

    private val queue = Volley.newRequestQueue(application)
    private val inventoryPartDao: InventoryPartDao
    private val brickListDao: BrickListDao


    init {
        val database = BrickListDatabase.getDatabase(application)
        inventoryPartDao = database.getInventoryPartDao()
        brickListDao = database.getBrickListDao()
    }

    val inventoryParts = MediatorLiveData<List<InventoryPartWithReferences>>()
    private val _inventoryParts = inventoryPartDao.getInventoryPartsById(inventoryId)
    private val codes = MutableLiveData<HashMap<Pair<Int, Int>, Code>>(hashMapOf())

    init {
        inventoryParts.addSource(_inventoryParts) {
            val codesData = codes.value
            if (codesData != null) {
                inventoryParts.value = combine(it, codesData)
            }
            else {
                inventoryParts.value = it
            }
        }
        inventoryParts.addSource(codes) {
            val parts = _inventoryParts.value
            if (parts != null) {
                inventoryParts.value = combine(parts, it)
            }
        }
    }

    private fun combine(parts: List<InventoryPartWithReferences>,
                        codes: MutableMap<Pair<Int, Int>, Code>)
            : List<InventoryPartWithReferences> {
        println("Data updated, doing combine")
        parts.forEach { part ->
            val ids = Pair(part.item.id, part.color.id)
            if (ids !in codes) {
                codes[ids] = Code()
                fetchCode(ids.first, ids.second)
            }
            else {
                part.code = codes[ids]
            }
        }
        println("Combine done")
        return parts
    }

    private fun fetchCode(itemId: Int, colorId: Int) = viewModelScope.launch(Dispatchers.Main) {
        var code = brickListDao.getCodeByIds(itemId, colorId)
        if (code == null) {
            code = Code(itemId = itemId, colorId = colorId)
            brickListDao.insertCode(code)
        }

        if (code.image == null) {
            val url = "https://www.lego.com/service/bricks/5/2/${code.code}"
            try {
                val newCode = code.copy(image = requestImage(url))
                setCode(newCode)
                brickListDao.updateCode(newCode)
            }
            catch (e: ClientError) {
                println("Image not found at url: $url")
            }
        }
        else {
            println("Setting code, found in database")
            setCode(code)
        }
    }

    private suspend fun setCode(code: Code) = withContext(Dispatchers.Main) {
        val codesData = codes.value
        val ids = Pair(code.itemId, code.colorId ?: 0)
        if (codesData != null) {
            codesData[ids] = code
            codes.postValue(codesData)
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
