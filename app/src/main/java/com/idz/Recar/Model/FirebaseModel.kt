package com.idz.Recar.Model

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase

import androidx.lifecycle.LifecycleOwner

class FirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val STUDENTS_COLLECTION_PATH = "students"
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
        callback: (MutableList<Car>) -> Unit
    ) {
        val query = db.collection(CARS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Car.YEAR_KEY, yearStart)
            .whereLessThanOrEqualTo(Car.YEAR_KEY, yearEnd)
            .whereGreaterThanOrEqualTo(Car.MILEAGE_KEY, mileageStart)
            .whereLessThanOrEqualTo(Car.MILEAGE_KEY, mileageEnd)
            .whereGreaterThanOrEqualTo(Car.PRICE_KEY, priceStart)
            .whereLessThanOrEqualTo(Car.PRICE_KEY, priceEnd)
        if (color != null) {
            query.whereEqualTo(Car.COLOR_KEY, color)
        }
        if (model != null) {
            query.whereEqualTo(Car.MODEL_KEY, model)
        }
        if (make != null) {
            query.whereEqualTo(Car.MAKE_KEY, make)
        }

        query.get().addOnCompleteListener {
            when (it.isSuccessful) {
                true -> {
                    val cars: MutableList<Car> = mutableListOf()
                    for (json in it.result) {
                        val car = Car.fromJSON(json.data)
                        cars.add(car)
                    }
                    callback(cars)
                }

                false -> callback(mutableListOf())
            }
        }
    }

    fun getAllStudents(since: Long, callback: (List<Student>) -> Unit) {
        db.collection(STUDENTS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Student.LAST_UPDATED, Timestamp(since, 0))
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val students: MutableList<Student> = mutableListOf()
                    for (doc in snapshot.documents) {
                        val student = doc.data?.let { Student.fromJSON(it) }
                        student?.let { students.add(it) }
                    }
                    callback(students)
                } else {
                    callback(emptyList())
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

    fun addStudent(student: Student, callback: () -> Unit) {
        db.collection(STUDENTS_COLLECTION_PATH)
            .document(student.id)
            .set(student.json)
            .addOnSuccessListener {
                callback()
            }
    }

    fun addUser(user: User, callback: (String) -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .add(user.json) // Omitting the document ID
            .addOnSuccessListener { documentReference ->
                // Document successfully added with generated ID
                val generatedId = documentReference.id
                callback(generatedId)
            }
            .addOnFailureListener { e ->
                // Handle errors here
                Log.e(TAG, "Error adding document", e)
            }
    }

    fun editUserById(userId: String, newUser: User, callback: () -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .document(userId)
            .set(newUser.json) // Overwrites the existing document with the new user data
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener { e ->
                // Handle errors here
                Log.e(TAG, "Error editing user document", e)
            }
    }
}

