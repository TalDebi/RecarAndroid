package com.idz.Recar.Model

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.ktx.Firebase

import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth

class FirebaseModel {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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

    private fun <T> handleFirestoreRequest(request: () -> T) {
        if (auth.currentUser != null) {
            request()
        } else {
            Log.e(TAG, "Error: User is not authorized")
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

    fun addUser(user: User, uid: String, callback: () -> Unit) {
        handleFirestoreRequest {
            db.collection(USERS_COLLECTION_PATH)
                .document(uid).set(user.json)
                .addOnSuccessListener {
                    callback()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding document", e)
                }
        }
    }

    fun editUserById(userId: String, newUser: User, callback: () -> Unit) {
        handleFirestoreRequest {
            db.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .set(newUser.json)
                .addOnSuccessListener {
                    callback()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error editing user document", e)
                }
        }
    }
}

