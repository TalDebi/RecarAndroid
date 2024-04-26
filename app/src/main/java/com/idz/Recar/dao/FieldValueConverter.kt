package com.idz.Recar.dao

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class FieldValueConverter {
    @TypeConverter
    fun fromFieldValue(value: FieldValue?): Timestamp? {
        return if (value != null && value == FieldValue.serverTimestamp()) {
            Timestamp.now()
        } else {
            null
        }
    }

    @TypeConverter
    fun toFieldValue(timestamp: Timestamp?): FieldValue? {
        return null // You can handle this conversion according to your use case
    }
}






