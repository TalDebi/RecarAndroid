package com.idz.Recar.Modules.Search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.idz.Recar.Model.Car

class ResultsViewModel: ViewModel() {
    var results: LiveData<MutableList<Car>>? = null
}