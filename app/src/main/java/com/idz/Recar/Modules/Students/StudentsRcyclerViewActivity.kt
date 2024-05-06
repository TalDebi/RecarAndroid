package com.idz.Recar.Modules.Students

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.Model.Student
import com.idz.Recar.Modules.Students.Adapter.StudentsRecyclerAdapter
import com.idz.Recar.databinding.ActivityStudentsRcyclerViewBinding

class StudentsRcyclerViewActivity : AppCompatActivity() {

    var studentsRcyclerView: RecyclerView? = null
    var students: List<Student>? = null
    var adapter: StudentsRecyclerAdapter? = null

    private lateinit var binding: ActivityStudentsRcyclerViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentsRcyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentsRcyclerView = binding.rvStudentREcyclerList
        studentsRcyclerView?.setHasFixedSize(true)
        studentsRcyclerView?.layoutManager = LinearLayoutManager(this)

        adapter = StudentsRecyclerAdapter(students)
        adapter?.listener = object : OnItemClickListener {

            override fun onItemClick(position: Int) {
                Log.i("TAG", "StudentsRecyclerAdapter: Position clicked $position")
            }

            override fun onStudentClicked(student: Student?) {
                Log.i("TAG", "STUDENT $student")
            }
        }

        studentsRcyclerView?.adapter = adapter
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int) // Student
        fun onStudentClicked(student: Student?)
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