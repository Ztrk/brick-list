package com.example.bricklist

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NetworkRequests(context: Context) {
    private val queue = Volley.newRequestQueue(context.applicationContext)

    suspend fun requestString(url: String): String = suspendCancellableCoroutine {
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

    suspend fun requestImage(url: String, maxWidth: Int = 0, maxHeight: Int = 0,
                             scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_INSIDE,
                             bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap
            = suspendCancellableCoroutine {
        val imageRequest = ImageRequest(url,
            Response.Listener { response ->
                it.resume(response)
            },
            maxWidth, maxHeight, scaleType, bitmapConfig,
            Response.ErrorListener { error ->
                it.resumeWithException(error)
            }
        )
        it.invokeOnCancellation {
            imageRequest.cancel()
        }
        queue.add(imageRequest)
    }

    companion object {
        private var instance: NetworkRequests? = null

        @Synchronized fun getInstance(context: Context): NetworkRequests {
            val tmpInstance = instance
            if (tmpInstance != null) {
                return tmpInstance
            }
            val newInstance = NetworkRequests(context)
            instance = newInstance
            return newInstance
        }
    }
}