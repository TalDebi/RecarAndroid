package com.idz.Recar.dao

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FieldValueConverter {
    @TypeConverter
    fun fromFieldValue(value: FieldValue?): Long? {
        return value?.let {
            val timestamp = Timestamp.now()
            timestamp.toDate().time
        }
    }

    @TypeConverter
    fun toFieldValue(milliseconds: Long?): FieldValue? {
        return null
    }
    @TypeConverter
    fun fromStringList(value: MutableList<String>): String {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        val a = gson.toJson(value, type)
        return a
    }

    @TypeConverter
    fun toStringList(value: String): MutableList<String> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(value, type)
    }
}








