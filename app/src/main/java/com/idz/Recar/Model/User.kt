package com.idz.Recar.Model

import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.FieldValue
import com.idz.Recar.dao.FieldValueConverter
import com.squareup.picasso.Picasso

@Entity
data class User(
    var name: String,
    var email: String,
    var phoneNumber: String = "",
    var imgUrl: String = DEFAULT_IMAGE_URL,
    @TypeConverters(FieldValueConverter::class) var lastUpdated: FieldValue? = null,
) {
    companion object {
        const val DEFAULT_IMAGE_URL = "drawable://avatar.png"
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val PHONE_NUMBER_KEY = "phoneNumber"
        const val IMG_URL_KEY = "imgUrl"
        const val LAST_UPDATED = "lastUpdated"

        fun fromJSON(json: Map<String, Any>): User {
            val name = json[NAME_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val phoneNumber = json[PHONE_NUMBER_KEY] as? String ?: ""
            val imgUrl = json[IMG_URL_KEY] as? String ?: DEFAULT_IMAGE_URL // Use default image if imgUrl is not provided

            val user = User(name, email, phoneNumber, imgUrl)

            // Assuming LAST_UPDATED is provided as a Timestamp
            val timestamp = json[LAST_UPDATED] as? Long
            timestamp?.let {
                // Convert Long timestamp to Firestore FieldValue
                user.lastUpdated = FieldValue.serverTimestamp()
            }

            return user
        }
    }

    init {
        require(name.length >= 1) { "Name must have at least 1 character" }
        require(email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) { "Invalid email format" }
        require(phoneNumber.length == 10) { "Phone number must have 10 digits" }
    }

    fun loadImageIntoImageView(imageView: ImageView) {
        if (imgUrl.startsWith("drawable://")) {
            val drawableResourceId = imageView.context.resources.getIdentifier(
                imgUrl.substringAfterLast("/"),
                "drawable",
                imageView.context.packageName
            )
            Picasso.get().load(drawableResourceId).into(imageView)
        } else {
            Picasso.get().load(imgUrl).into(imageView)
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                NAME_KEY to name,
                EMAIL_KEY to email,
                PHONE_NUMBER_KEY to phoneNumber,
                IMG_URL_KEY to imgUrl,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )
        }
}
