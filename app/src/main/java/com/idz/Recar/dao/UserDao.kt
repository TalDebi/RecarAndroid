package com.idz.Recar.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.idz.Recar.Model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User")
    fun getAll(): LiveData<MutableList<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg user: User)

    @Query("SELECT * FROM User WHERE id = :userId")
    fun getUserById(userId: String): LiveData<User>
}
