package com.idz.Recar.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Student
import com.idz.Recar.dao.User
import com.idz.Recar.base.MyApplication

@Database(entities = [Student::class, Car::class,User::class], version = 5)
@TypeConverters(Converters::class)
@TypeConverters(FieldValueConverter::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun carDao(): CarDao
    abstract fun userDao(): UserDao
}

object AppLocalDatabase {

    val db: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Application context not available")

        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}


class Converters {

    @TypeConverter
    fun fromStringList(value: Array<String>): String {
        val gson = Gson()
        val type = object : TypeToken<Array<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): Array<String> {
        val gson = Gson()
        val type = object : TypeToken<Array<String>>() {}.type
        return gson.fromJson(value, type)
    }
}