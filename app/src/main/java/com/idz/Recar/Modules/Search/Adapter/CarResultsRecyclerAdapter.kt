package com.idz.Recar.Modules.Search.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.Model.Car
import com.idz.Recar.R

class CarResultRecyclerAdapter( var results: MutableList<Car>?) :
    RecyclerView.Adapter<CarResultViewHolder>() {

    var listener: OnItemClickListener? = null

    override fun getItemCount(): Int = results?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarResultViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.car_result_row, parent, false)
        return CarResultViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: CarResultViewHolder, position: Int) {
        val result = results?.get(position)
        holder.bind(result, position)
    }
}

interface OnItemClickListener {
    fun onItemClick(position: Int)

}