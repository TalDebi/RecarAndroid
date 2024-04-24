package com.idz.Recar.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.ktx.Firebase

import androidx.lifecycle.LifecycleOwner

class FirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val STUDENTS_COLLECTION_PATH = "students"
        const val USERS_COLLECTION_PATH = "users"
    }

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        db.firestoreSettings = settings
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

    fun getAllUsers(callback: (List<User>) -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val users: MutableList<User> = mutableListOf()
                    for (doc in snapshot.documents) {
                        val user = doc.data?.let { User.fromJSON(it) }
                        user?.let { users.add(it) }
                    }
                    callback(users)
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

    fun addUser(user: User, callback: () -> Unit) {
        db.collection(USERS_COLLECTION_PATH)
            .document(user.id ?: "")
            .set(user.json)
            .addOnSuccessListener {
                callback()
            }
    }
}

