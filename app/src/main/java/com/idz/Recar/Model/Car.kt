package com.idz.Recar.Model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FieldValue

@Entity
class Car(
    @PrimaryKey val id: String,
    val imageUrls: ArrayList<String>,
    val make: String,
    val model: String,
    val year: Long,
    val price: Long,
    val hand: Long,
    val color: String,
    val mileage: Long,
    val city: String,
    val owner: String,


    ) {
    companion object {

        const val ID_KEY = "id"
        const val HAND_KEY = "hand"
        const val IMAGE_URLS_KEY = "imageUrls"
        const val MAKE_KEY = "make"
        const val MODEL_KEY = "model"
        const val YEAR_KEY = "year"
        const val PRICE_KEY = "price"
        const val COLOR_KEY = "color"
        const val MILEAGE_KEY = "mileage"
        const val CITY_KEY = "city"
        const val OWNER_KEY = "owner"
        fun fromJSON(json: Map<String, Any>, id: String = ""): Car {
            val id = json[ID_KEY] as? String ?: id
            val imageUrls = json[IMAGE_URLS_KEY] as? ArrayList<String> ?: arrayListOf<String>()
            val make = json[MAKE_KEY] as? String ?: ""
            val model = json[MODEL_KEY] as? String ?: ""
            val year = json[YEAR_KEY] as? Long ?: 2000
            val price = json[PRICE_KEY] as? Long  ?: 0
            val hand = json[HAND_KEY] as? Long ?: 1
            val color = json[COLOR_KEY] as? String ?: ""
            val mileage = json[MILEAGE_KEY] as? Long ?: 0
            val city = json[CITY_KEY] as? String ?: ""
            val owner = json[OWNER_KEY] as? String ?: ""
            return Car(id, imageUrls, make, model, year, price, hand, color, mileage, city, owner)
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                ID_KEY to id,
                HAND_KEY to hand,
                IMAGE_URLS_KEY to imageUrls,
                MAKE_KEY to make,
                MODEL_KEY to model,
                YEAR_KEY to year,
                PRICE_KEY to price,
                COLOR_KEY to color,
                MILEAGE_KEY to mileage,
                CITY_KEY to city,
                OWNER_KEY to owner
            )
        }


}