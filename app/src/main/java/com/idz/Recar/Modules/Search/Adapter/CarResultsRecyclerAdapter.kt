package com.idz.Recar.Modules.Students.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.Model.Student
import com.idz.Recar.Modules.Students.CarResultRcyclerViewActivity
import com.idz.Recar.R

class CarResultRecyclerAdapter(var students: List<Student>?): RecyclerView.Adapter<CarResultViewHolder>() {

    var listener: CarResultRcyclerViewActivity.OnItemClickListener? = null

    override fun getItemCount(): Int = students?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarResultViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.student_layout_row, parent, false)
        return CarResultViewHolder(itemView, listener, students)
    }

    override fun onBindViewHolder(holder: CarResultViewHolder, position: Int) {
        val student = students?.get(position)
        holder.bind(student)
    }
}