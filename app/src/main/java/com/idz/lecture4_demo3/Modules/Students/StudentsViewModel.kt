package com.idz.Recar.Modules.Students

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.Recar.Model.Student

class StudentsViewModel: ViewModel() {
    var students: LiveData<MutableList<Student>>? = null
}