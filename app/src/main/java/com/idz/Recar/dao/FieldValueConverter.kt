package com.idz.Recar.dao

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FieldValueConverter {
    @TypeConverter
    fun fromFieldValue(value: FieldValue?): Long? {
        // Convert FieldValue to milliseconds since the Unix epoch
        return value?.let {
            val timestamp = Timestamp.now() // Create a Timestamp representing current time
            timestamp.toDate().time // Convert Timestamp to milliseconds
        }
    }

    @TypeConverter
    fun toFieldValue(milliseconds: Long?): FieldValue? {
        // Convert milliseconds to FieldValue (not supported)
        // You may need additional logic here based on your requirements
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








