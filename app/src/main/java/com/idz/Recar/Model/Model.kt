package com.idz.Recar.Model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.idz.Recar.dao.AppLocalDatabase
import com.idz.Recar.dao.CarDao.Companion.MAX_MILEAGE
import com.idz.Recar.dao.CarDao.Companion.MAX_PRICE
import com.idz.Recar.dao.CarDao.Companion.MAX_YEAR
import com.idz.Recar.dao.CarDao.Companion.MIN_MILEAGE
import com.idz.Recar.dao.CarDao.Companion.MIN_PRICE
import com.idz.Recar.dao.CarDao.Companion.MIN_YEAR
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val results: LiveData<MutableList<Car>>? = null
    val resultsLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)
    val studentsListLoadingState: MutableLiveData<LoadingState> =
        MutableLiveData(LoadingState.LOADED)
    private val usersList: LiveData<MutableList<LocalUser>> = database.userDao().getAll()
    private val usersListLoadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.LOADED)
    private val currCar: LiveData<Car>? = null

    companion object {
        val instance: Model = Model()
    }












    fun getAllCars(
        yearStart: Int = MIN_YEAR,
        yearEnd: Int = MAX_YEAR,
        mileageStart: Int = MIN_MILEAGE,
        mileageEnd: Int = MAX_MILEAGE,
        priceStart: Int = MIN_PRICE,
        priceEnd: Int = MAX_PRICE,
        color: String? = null,
        model: String? = null,
        make: String? = null,
        owner: String? = null
    ): LiveData<MutableList<Car>> {
        refreshAllCars(
            yearStart,
            yearEnd,
            mileageStart,
            mileageEnd,
            priceStart,
            priceEnd,
            color,
            model,
            make,
            owner
        )
        val a = results ?: database.carDao().getAll()
        return a
    }

    fun getCarById(id: String): LiveData<Car> {
        refreshCurrCar(id)
        return currCar ?: database.carDao().getCarById(id)
    }

    fun deleteCarById(id: String) {
        firebaseModel.deleteCarById(id)
        runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                database.carDao().deleteByCarId(id)
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

    fun refreshAllCars(
        yearStart: Int = MIN_YEAR,
        yearEnd: Int = MAX_YEAR,
        mileageStart: Int = MIN_MILEAGE,
        mileageEnd: Int = MAX_MILEAGE,
        priceStart: Int = MIN_PRICE,
        priceEnd: Int = MAX_PRICE,
        color: String? = null,
        model: String? = null,
        make: String? = null,
        owner: String? = null
    ) {
        resultsLoadingState.value = LoadingState.LOADING
        firebaseModel.getAllCars(
            yearStart,
            yearEnd,
            mileageStart,
            mileageEnd,
            priceStart,
            priceEnd,
            color,
            model,
            make,
            owner
        ) { list ->
            Log.i("TAG", "Firebase returned ${list.size} cars")
            executor.execute {
                database.carDao().deleteAll()
                list.forEach { car ->
                    val localCar = Car(
                        car.id,
                        car.imageUrls,
                        car.make,
                        car.model,
                        car.year,
                        car.price,
                        car.hand,
                        car.color,
                        car.mileage,
                        car.city,
                        car.owner
                    )
                    database.carDao().insert(localCar)
                }
                resultsLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun refreshCurrCar(id: String) {
        usersListLoadingState.value = LoadingState.LOADING

        // Get all updated records from Firestore since the last update locally
        firebaseModel.getCarById(id) { car ->
            Log.i("TAG", "Firebase returned 1 car")
            car?.let {
                // Insert or update records in the local database
                executor.execute {
                    val temp = car.toMutableMap()
                    temp.put(Car.ID_KEY, id)
                    val localCar = Car.fromJSON(temp)

                    database.carDao().insert(localCar)
                }

                // Update the loading state
                usersListLoadingState.postValue(LoadingState.LOADED)
            }
        }
    }


    fun addUser(user: User, uid: String, callback: () -> Unit) {
        firebaseModel.addUser(user, uid) {
            refreshAllUsers()
            callback()
        }
    }

    fun addCar(car: Car, callback: () -> Unit) {
        firebaseModel.addCar(car) {
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
