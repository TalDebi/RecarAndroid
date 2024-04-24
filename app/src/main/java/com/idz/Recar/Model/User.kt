package com.idz.Recar.Model

import android.content.Context
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.idz.Recar.base.MyApplication
import com.squareup.picasso.Picasso

@Entity
data class User(
    @PrimaryKey var id: String,
    var name: String,
    var email: String,
    var password: String,
    var phoneNumber: String = "",
    var imgUrl: String = DEFAULT_IMAGE_URL,
    var lastUpdated: Long? = null
) {
    companion object {
        const val DEFAULT_IMAGE_URL = "drawable://avatar.png"
        var lastUpdated: Long
            get() {
                return MyApplication.Globals
                    .appContext?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.getLong(GET_LAST_UPDATED, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals
                    ?.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.edit()
                    ?.putLong(GET_LAST_UPDATED, value)?.apply()
            }

        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHONE_NUMBER_KEY = "phoneNumber"
        const val IMG_URL_KEY = "imgUrl"
        const val LAST_UPDATED = "lastUpdated"
        const val GET_LAST_UPDATED = "get_last_updated"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val password = json[PASSWORD_KEY] as? String ?: ""
            val phoneNumber = json[PHONE_NUMBER_KEY] as? String ?: ""
            val imgUrl = json[IMG_URL_KEY] as? String ?: DEFAULT_IMAGE_URL // Use default image if imgUrl is not provided

            val user = User(id, name, email, password, phoneNumber, imgUrl)

            val timestamp: Timestamp? = json[LAST_UPDATED] as? Timestamp
            timestamp?.let {
                user.lastUpdated = it.seconds
            }

            return user
        }
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
                ID_KEY to id,
                NAME_KEY to name,
                EMAIL_KEY to email,
                PASSWORD_KEY to password,
                PHONE_NUMBER_KEY to phoneNumber,
                IMG_URL_KEY to imgUrl,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )
        }
}
