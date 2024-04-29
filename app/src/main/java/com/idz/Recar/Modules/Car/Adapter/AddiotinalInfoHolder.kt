package com.idz.Recar.Modules.Car.Adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.R

class AdditionalInfoHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

    var tvLabel: TextView? = null
    var tvData: TextView? = null

    init {
        tvLabel = itemView.findViewById(R.id.tv_label)
        tvData = itemView.findViewById(R.id.tv_data)
    }

    fun bind(label: String, value: String) {

        tvLabel?.text = label.replace('_', ' ')
        tvData?.text = value
    }
}