package com.idz.Recar.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Student
import com.idz.Recar.base.MyApplication

@Database(entities = [Student::class, Car::class, User::class], version = 5)
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


