package com.idz.Recar.Modules.Search.Adapter

import android.graphics.Color
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.chip.Chip
import com.idz.Recar.Model.Car
import com.idz.Recar.R
import com.idz.Recar.base.FireBaseStorage
import com.squareup.picasso.Picasso


class CarResultViewHolder(
    val itemView: View,
    val listener: OnItemClickListener?,
) : RecyclerView.ViewHolder(itemView) {

    var image: ImageView? = null
    var makeChip: Chip? = null
    var modelChip: Chip? = null
    var yearChip: Chip? = null
    var priceView: TextView? = null
    var colorChip: Chip? = null
    var mileageChip: Chip? = null
    var result: Car? = null
    var loader: CircularProgressDrawable = CircularProgressDrawable(itemView.context)
    var storage = FireBaseStorage.getInstance().storage
    private var position: Int = 0


    init {
        image = itemView.findViewById(R.id.ivCar)
        makeChip = itemView.findViewById(R.id.make_chip)
        modelChip = itemView.findViewById(R.id.model_chip)
        yearChip = itemView.findViewById(R.id.year_chip)
        priceView = itemView.findViewById(R.id.tvPrice)
        colorChip = itemView.findViewById(R.id.color_chip)
        mileageChip = itemView.findViewById(R.id.mileage_chip)
        loader.strokeWidth = 5f
        loader.centerRadius = 60f
        loader.setColorSchemeColors(Color.GREEN)
        loader.start()



        itemView.setOnClickListener {
            Log.i("TAG", "StudentViewHolder: Position clicked $adapterPosition")

            listener?.onItemClick(this.position)
        }
    }

    fun bind(result: Car?, position: Int) {
        this.result = result
        this.position = position
        result?.let { car ->
            val load = car.imageUrls.getOrNull(0)?.let { storage.getReferenceFromUrl(it) }



            load?.getDownloadUrl()
                ?.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).placeholder(loader).into(image)
                }
            makeChip?.text = car.make
            modelChip?.text = car.model
            yearChip?.text = car.year.toString()
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("ILS")
            priceView?.text = format.format(car.price.toDouble())
            colorChip?.text = car.color
            mileageChip?.text = car.mileage.toString()


        }
    }
}
