package com.idz.Recar.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.FieldValue
import com.idz.Recar.Model.User

@Entity(tableName = "local_users")
data class User(
    @PrimaryKey(autoGenerate = false) var id: String = "",
    var name: String,
    var email: String,
    var phoneNumber: String = "",
    var imgUrl: String = User.DEFAULT_IMAGE_URL,
    @TypeConverters(FieldValueConverter::class) var lastUpdated: FieldValue? = null,
)