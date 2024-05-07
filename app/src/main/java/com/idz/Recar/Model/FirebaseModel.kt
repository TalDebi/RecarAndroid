package com.idz.Recar.Model

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase

import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.idz.Recar.Model.User.Companion.DEFAULT_IMAGE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class FirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val USERS_COLLECTION_PATH = "users"
        const val CARS_COLLECTION_PATH = "cars"
    }

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        db.firestoreSettings = settings
    }

    fun getAllCars(
        yearStart: Int,
        yearEnd: Int,
        mileageStart: Int,
        mileageEnd: Int,
        priceStart: Int,
        priceEnd: Int,
        color: String?,
        model: String?,
        make: String?,
        owner: String?,
        callback: (MutableList<Car>) -> Unit
    ) {
        var query = db.collection(CARS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Car.YEAR_KEY, yearStart)
            .whereLessThanOrEqualTo(Car.YEAR_KEY, yearEnd)
            .whereGreaterThanOrEqualTo(Car.MILEAGE_KEY, mileageStart)
            .whereLessThanOrEqualTo(Car.MILEAGE_KEY, mileageEnd)
            .whereGreaterThanOrEqualTo(Car.PRICE_KEY, priceStart)
            .whereLessThanOrEqualTo(Car.PRICE_KEY, priceEnd)
        if (color != null) {
            query = query.whereEqualTo(Car.COLOR_KEY, color)
        }
        if (model != null) {
            query = query.whereEqualTo(Car.MODEL_KEY, model)
        }
        if (make != null) {
            query = query.whereEqualTo(Car.MAKE_KEY, make)
        }
        if (owner != null) {
            query = query.whereEqualTo(Car.OWNER_KEY, owner)
        }


        query.get().addOnCompleteListener {
            when (it.isSuccessful) {
                true -> {
                    val cars: MutableList<Car> = mutableListOf()
                    for (json in it.result) {
                        val car = Car.fromJSON(json.data, json.id)
                        cars.add(car)
                    }
                    callback(cars)
                }

                false -> {
                    callback(mutableListOf())
                }
            }
        }
    }

    fun getCarById(id: String, callback: (Map<String, Any>?) -> Unit) {
        val query = db.collection(CARS_COLLECTION_PATH).document(id)
        query.get().addOnCompleteListener {
            when (it.isSuccessful) {
                true -> {

                    callback(it.result.data)
                }

                false -> callback(null)
            }
        }
    }


    fun getAllUsers(callback: (List<Pair<User, String>>) -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error getting users: ", exception)
                    callback(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val usersWithIds = mutableListOf<Pair<User, String>>()
                    for (doc in snapshot.documents) {
                        val user = doc.data?.let { User.fromJSON(it) }
                        val id = doc.id
                        if (user != null) {
                            usersWithIds.add(Pair(user, id))
                        }
                    }
                    callback(usersWithIds)
                } else {
                    callback(emptyList())
                }
            }
    }


    fun fetchUserImage(imageUrl: String?, callback: (Uri?) -> Unit) {
        if (imageUrl.isNullOrEmpty()) {
            callback(null)
            return
        }

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                callback(uri)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user image: $exception")
                callback(null)
            }
    }

    private fun isGoogleAccountUri(uri: String?): Boolean {
        return uri?.startsWith("https://") ?: false
    }

    private fun uploadUserImage(userId: String, imageUri: String?, callback: (String?) -> Unit) {
        if (!imageUri.isNullOrEmpty()) {
            if (isGoogleAccountUri(imageUri)) {
                uploadGooglePhotoToFirebase(imageUri) { imageUrl ->
                    callback(imageUrl)
                }
            } else {
                val storageRef = FirebaseStorage.getInstance().reference
                val imagesRef = storageRef.child("profile_images/$userId")

                val uploadTask = imagesRef.putFile(Uri.parse(imageUri))
                uploadTask.addOnSuccessListener { _ ->
                    imagesRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.e(TAG, uri.toString())
                        val imageUrl = uri.toString()
                        callback(imageUrl)
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error getting download URL: $e")
                        callback(null)
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading image: $e")
                    callback(null)
                }
            }
        } else {
            callback(null)
        }
    }

    private fun uploadGooglePhotoToFirebase(photoUrl: String, callback: (String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = photoUrl.substringAfterLast("/")
            val imageRef = storageRef.child("profile_images/$fileName")

            val client = OkHttpClient()
            val request = Request.Builder().url(photoUrl).build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Failed to download photo: $photoUrl")

                val inputStream = response.body?.byteStream()

                if (inputStream != null) {
                    val uploadTask = imageRef.putStream(inputStream)

                    uploadTask.addOnSuccessListener {
                        println("Photo uploaded successfully to Firebase Storage")
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            callback(uri.toString())
                        }.addOnFailureListener { e ->
                            callback(null)
                            println("Error getting download URL: $e")
                        }
                        inputStream.close()
                    }.addOnFailureListener { exception ->
                        println("Failed to upload photo: $exception")
                        callback(null)
                        inputStream.close()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null)
            }
        }
    }


    fun addUser(user: User, uid: String, callback: () -> Unit) {
        val userRef = db.collection(USERS_COLLECTION_PATH).document(uid)

        uploadUserImage(uid, user.imgUrl) { imageUrl ->
            user.imgUrl = imageUrl ?: DEFAULT_IMAGE_URL
            userRef.set(user.json)
                .addOnSuccessListener {
                    callback()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding document", e)
                }
        }
    }

    fun addCar(car: Car, callback: () -> Unit) {
        db.collection(CARS_COLLECTION_PATH)
            .document(car.id)
            .set(car.json)
            .addOnSuccessListener {
                callback()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }
    }

    fun editUserById(userId: String, newUser: User, callback: () -> Unit) {
        val userRef = db.collection(USERS_COLLECTION_PATH).document(userId)

        uploadUserImage(userId, newUser.imgUrl) { imageUrl ->
            newUser.imgUrl = imageUrl ?: DEFAULT_IMAGE_URL
            userRef.set(newUser.json)
                .addOnSuccessListener {
                    callback()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error editing user document", e)
                }
        }
    }

    fun isEmailTaken(email: String, onComplete: (Boolean) -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                onComplete(!documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking if email is taken: $exception")
                onComplete(false)
            }

    }

    fun deleteCarById(id: String) {
        db.collection(CARS_COLLECTION_PATH).document(id)
            .delete()

    }
}

