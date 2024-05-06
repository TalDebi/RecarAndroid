package com.idz.Recar.dao

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class FieldValueConverter {
    @TypeConverter
    fun fromFieldValue(value: FieldValue?): Long? {
        return value?.let {
            val timestamp = Timestamp.now()
            timestamp.toDate().time
        }
    }
}








