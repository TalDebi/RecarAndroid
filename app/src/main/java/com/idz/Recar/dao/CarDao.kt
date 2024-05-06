package com.idz.Recar.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idz.Recar.Model.Car

@Dao
interface CarDao {
    companion object{
        val MIN_YEAR = 1994
        val MAX_YEAR = 2024
        val MIN_PRICE = 0
        val MAX_PRICE = 100000
        val MIN_MILEAGE = 0
        val MAX_MILEAGE = 500000

    }
    @Query("SELECT * FROM Car")
    fun getAll(): LiveData<MutableList<Car>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cars: Car)

    @Delete
    fun delete(car: Car)

    @Query("DELETE FROM car WHERE id = :carId")
    suspend fun deleteByCarId(carId: String)

    @Query("DELETE FROM Car")
    fun deleteAll()

    @Query("SELECT * FROM Car WHERE id =:id")
    fun getCarById(id: String): LiveData<Car>
}