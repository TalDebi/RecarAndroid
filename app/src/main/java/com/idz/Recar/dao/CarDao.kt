package com.idz.Recar.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Student

@Dao
interface CarDao {
    @Query("SELECT * FROM Car")
    fun getAll(): LiveData<MutableList<Car>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cars: Car)

    @Delete
    fun delete(car: Car)

    @Query("SELECT * FROM Car WHERE id =:id")
    fun getCarById(id: String): LiveData<Car>
}