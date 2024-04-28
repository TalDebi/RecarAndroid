package com.idz.Recar.Model

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.idz.Recar.dao.AppLocalDatabase
import java.util.concurrent.Executors
import com.idz.Recar.dao.User as LocalUser

class Model private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    private val database = AppLocalDatabase.db
    private var executor = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val firebaseModel = FirebaseModel()
    private val students: LiveData<MutableList<Student>>? = null
    val studentsListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)
    private val usersList: LiveData<MutableList<LocalUser>> = database.userDao().getAll()
    val usersListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)

    companion object {
        val instance: Model = Model()
    }

    fun observeStudentsList(owner: LifecycleOwner, observer: (List<Student>) -> Unit) {
        students?.observe(owner, observer)
    }

    fun observeUsersList(owner: LifecycleOwner, observer: (List<LocalUser>) -> Unit) {
        usersList.observe(owner, observer)
    }

    fun removeStudentsListObservers(owner: LifecycleOwner) {
        students?.removeObservers(owner)
    }

    fun removeUsersListObservers(owner: LifecycleOwner) {
        usersList.removeObservers(owner)
    }
    interface GetAllStudentsListener {
        fun onComplete(students: List<Student>)
    }

    fun getAllStudents(): LiveData<MutableList<Student>> {
        refreshAllStudents()
        return students ?: database.studentDao().getAll()
    }

    fun getAllUsers(): LiveData<MutableList<LocalUser>> {
        refreshAllUsers()
        return usersList
    }

    fun refreshAllStudents() {

        studentsListLoadingState.value = LoadingState.LOADING

        // 1. Get last local update
        val lastUpdated: Long = Student.lastUpdated

        // 2. Get all updated records from firestore since last update locally
        firebaseModel.getAllStudents(lastUpdated) { list ->
            Log.i("TAG", "Firebase returned ${list.size}, lastUpdated: $lastUpdated")
            // 3. Insert new record to ROOM
            executor.execute {
                var time = lastUpdated
                for (student in list) {
                    database.studentDao().insert(student)

                    student.lastUpdated?.let {
                        if (time < it)
                            time = student.lastUpdated ?: System.currentTimeMillis()
                    }
                }

                // 4. Update local data
                Student.lastUpdated = time
                studentsListLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun refreshAllUsers() {
        usersListLoadingState.value = LoadingState.LOADING

        // Get all updated records from Firestore since the last update locally
        firebaseModel.getAllUsers() { list ->
            Log.i("TAG", "Firebase returned ${list.size} users")

            // Insert or update records in the local database
            executor.execute {
                list.forEach { (user, id) ->
                    val localUser = LocalUser(
                        id = id,
                        name = user.name,
                        email = user.email,
                        password = user.password,
                        phoneNumber = user.phoneNumber,
                        imgUrl = user.imgUrl,
                        lastUpdated = user.lastUpdated
                    )
                        database.userDao().insert(localUser)
                }

                // Update the loading state
                usersListLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun addStudent(student: Student, callback: () -> Unit) {
        firebaseModel.addStudent(student) {
            refreshAllStudents()
            callback()
        }
    }

    fun addUser(user: User, callback: (String) -> Unit) {
        firebaseModel.addUser(user) { documentId ->
            refreshAllUsers()
            callback(documentId)
        }
    }
}
