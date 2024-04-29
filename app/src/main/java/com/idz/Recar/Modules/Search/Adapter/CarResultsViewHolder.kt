package com.idz.Recar.Modules.Students.Adapter

import android.R.id
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.chip.Chip
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.idz.Recar.Model.Car
import com.idz.Recar.Modules.Search.Adapter.CarResultRecyclerViewActivity
import com.idz.Recar.R
import com.squareup.picasso.Picasso


class CarResultViewHolder(
    val itemView: View,
    val listener: CarResultRecyclerViewActivity.OnItemClickListener?,
    var results: List<Car>?
) : RecyclerView.ViewHolder(itemView) {

    var image: ImageView? = null
    var makeChip: Chip? = null
    var modelChip: Chip? = null
    var yearChip: Chip? = null
    var priceChip: Chip? = null
    var colorChip: Chip? = null
    var mileageChip: Chip? = null
    var result: Car? = null
    var storage = Firebase.storage("gs://recar-46bcf.appspot.com")


    init {
         image  = itemView.findViewById(R.id.ivCar)
         makeChip = itemView.findViewById(R.id.make_chip)
         modelChip = itemView.findViewById(R.id.model_chip)
         yearChip = itemView.findViewById(R.id.year_chip)
         priceChip = itemView.findViewById(R.id.price_chip)
         colorChip = itemView.findViewById(R.id.color_chip)
         mileageChip = itemView.findViewById(R.id.mileage_chip)



        itemView.setOnClickListener {
            Log.i("TAG", "StudentViewHolder: Position clicked $adapterPosition")

            listener?.onItemClick(adapterPosition)
            listener?.onCarClicked(result)
        }
    }

    fun bind(result: Car?) {
        this.result = result
        val load = result?.imageUrls?.let { storage.getReferenceFromUrl(it[0]) }


        load?.getDownloadUrl()
            ?.addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                Picasso.get().load(uri.toString()).into(image)
            })?.addOnFailureListener(OnFailureListener { })
        makeChip?.text = result?.make
        modelChip?.text = result?.model
        yearChip?.text = result?.year.toString()
        priceChip?.text = result?.price.toString()
        colorChip?.text = result?.color
        mileageChip?.text = result?.mileage.toString()


    }
}