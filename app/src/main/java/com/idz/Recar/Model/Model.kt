package com.idz.Recar.Model

import android.util.Log
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
    private val firebaseModel = FirebaseModel()
    private val students: LiveData<MutableList<Student>>? = null
    val studentsListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)
    private val usersList: LiveData<MutableList<LocalUser>> = database.userDao().getAll()
    private val usersListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)

    companion object {
        val instance: Model = Model()
    }

    fun observeStudentsList(owner: LifecycleOwner, observer: (List<Student>) -> Unit) {
        students?.observe(owner, observer)
    }

    fun removeStudentsListObservers(owner: LifecycleOwner) {
        students?.removeObservers(owner)
    }

    interface GetAllStudentsListener {
        fun onComplete(students: List<Student>)
    }

    fun getAllStudents(): LiveData<MutableList<Student>> {
        refreshAllStudents()
        return students ?: database.studentDao().getAll()
    }

    fun refreshAllStudents() {

        studentsListLoadingState.value = LoadingState.LOADING

        val lastUpdated: Long = Student.lastUpdated

        firebaseModel.getAllStudents(lastUpdated) { list ->
            Log.i("TAG", "Firebase returned ${list.size}, lastUpdated: $lastUpdated")
            executor.execute {
                var time = lastUpdated
                for (student in list) {
                    database.studentDao().insert(student)

                    student.lastUpdated?.let {
                        if (time < it)
                            time = student.lastUpdated ?: System.currentTimeMillis()
                    }
                }

                Student.lastUpdated = time
                studentsListLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    private fun refreshAllUsers() {
        usersListLoadingState.value = LoadingState.LOADING

        firebaseModel.getAllUsers() { list ->
            Log.i("TAG", "Firebase returned ${list.size} users")

            executor.execute {
                list.forEach { (user, id) ->
                    val localUser = LocalUser(
                        id = id,
                        name = user.name,
                        email = user.email,
                        phoneNumber = user.phoneNumber,
                        imgUrl = user.imgUrl,
                        lastUpdated = user.lastUpdated
                    )
                    database.userDao().insert(localUser)
                }

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

    fun addUser(user: User, uid: String, callback: () -> Unit) {
        firebaseModel.addUser(user, uid) {
            refreshAllUsers()
            callback()
        }
    }

    fun editUserById(userId: String, newUser: User, callback: () -> Unit) {
        firebaseModel.editUserById(userId, newUser) {
            refreshAllUsers()
            callback()
        }
    }

}
