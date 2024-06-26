package com.idz.Recar.base

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VollyQueue constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VollyQueue? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VollyQueue(context).also {
                    INSTANCE = it
                }
            }
    }
//    val imageLoader: ImageLoader by lazy {
//        ImageLoader(requestQueue,
//            object : ImageLoader.ImageCache {
//                private val cache = LruCache<String, Bitmap>(20)
//                override fun getBitmap(url: String): Bitmap? {
//                    return cache.get(url)
//                }
//                override fun putBitmap(url: String, bitmap: Bitmap) {
//                    cache.put(url, bitmap)
//                }
//            })
//    }
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}