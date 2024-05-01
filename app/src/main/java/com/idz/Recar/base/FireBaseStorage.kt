package com.idz.Recar.base

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class FireBaseStorage private constructor() {
    val storage = Firebase.storage

    companion object {

        @Volatile
        private var instance: FireBaseStorage? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FireBaseStorage().also { instance = it }
            }
    }

}