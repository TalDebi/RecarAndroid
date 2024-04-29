package com.idz.Recar.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase

class FirebaseModel {

    private val db = Firebase.firestore

    companion object {
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

        db.collection(CARS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Student.LAST_UPDATED, Timestamp(since, 0))
            .get().addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val students: MutableList<Student> = mutableListOf()
                        for (json in it.result) {
                            val student = Student.fromJSON(json.data)
                            students.add(student)
                        }
                        callback(students)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun addStudent(student: Student, callback: () -> Unit) {
        db.collection(CARS_COLLECTION_PATH).document(student.id).set(student.json)
            .addOnSuccessListener {
                callback()
            }
    }
}
