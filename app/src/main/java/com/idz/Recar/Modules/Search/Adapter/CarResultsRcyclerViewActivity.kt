package com.idz.Recar.Modules.Search.Adapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Student
import com.idz.Recar.Modules.Students.Adapter.CarResultRecyclerAdapter
import com.idz.Recar.databinding.ActivityStudentsRcyclerViewBinding

class CarResultRecyclerViewActivity : AppCompatActivity() {

    var studentsRcyclerView: RecyclerView? = null
    var results: MutableList<Car>? = null
    var adapter: CarResultRecyclerAdapter? = null

    private lateinit var binding: ActivityStudentsRcyclerViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentsRcyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentsRcyclerView = binding.rvStudentREcyclerList
        studentsRcyclerView?.setHasFixedSize(true)
        studentsRcyclerView?.layoutManager = LinearLayoutManager(this)

        adapter = CarResultRecyclerAdapter(results)
        adapter?.listener = object : OnItemClickListener {

            override fun onItemClick(position: Int) {
                Log.i("TAG", "StudentsRecyclerAdapter: Position clicked $position")
            }

            override fun onCarClicked(result: Car?) {
                Log.i("TAG", "STUDENT $result")
            }
        }

        studentsRcyclerView?.adapter = adapter
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int) // Student
        fun onCarClicked(result: Car?)
    }

    override fun onResume() {
        super.onResume()

//        Model.instance.getAllStudents { students ->
//            this.students = students
//            adapter?.students = students
//            adapter?.notifyDataSetChanged()
//        }
    }
}