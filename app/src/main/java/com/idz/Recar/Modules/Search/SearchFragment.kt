package com.idz.Recar.Modules.Search

import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.chip.Chip
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.Student
import com.idz.Recar.Modules.Students.Adapter.CarResultRecyclerAdapter
import com.idz.Recar.Modules.Students.StudentsFragmentDirections
import com.idz.Recar.R
import com.idz.Recar.Modules.Search.Adapter.CarResultRecyclerViewActivity


class SearchFragment : Fragment() {

    var resultsRecyclerView: RecyclerView? = null
    var adapter: CarResultRecyclerAdapter? = null
    var progressBar: ProgressBar? = null
    private lateinit var viewModel: ResultsViewModel

    private var filtersButton: Button? = null
    private var filterSheet: SideSheetDialog? = null
    private var priceSlider: RangeSlider? = null
    private var yearSlider: RangeSlider? = null
    private var mileageSlider: RangeSlider? = null
    private var makeSelect: MaterialAutoCompleteTextView? = null
    private var modelSelect: MaterialAutoCompleteTextView? = null
    private var colorSelect: MaterialAutoCompleteTextView? = null
    private var priceChip: Chip? = null
    private var makeChip: Chip? = null
    private var modelChip: Chip? = null
    private var colorChip: Chip? = null
    private var yearChip: Chip? = null
    private var mileageChip: Chip? = null
    private var baseUrl =
        "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=5000&group_by="
    private var modelOptions: Array<String> = arrayOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    private fun setupUI(view: View) {


        filtersButton = view.findViewById(R.id.filterButton)
        filterSheet = SideSheetDialog(requireContext())
        val sideSheetView =
            LayoutInflater.from(filterSheet?.context).inflate(R.layout.filters_layout, null)
        filterSheet?.setContentView(sideSheetView)
        priceSlider = sideSheetView.findViewById(R.id.price_slider)
        yearSlider = sideSheetView.findViewById(R.id.year_slider)
        mileageSlider = sideSheetView.findViewById(R.id.mileage_slider)
        makeSelect = sideSheetView.findViewById(R.id.make_select)
        modelSelect = sideSheetView.findViewById(R.id.model_select)
        colorSelect = sideSheetView.findViewById(R.id.color_select)
        priceChip = view.findViewById(R.id.price_chip)
        makeChip = view.findViewById(R.id.make_chip)
        modelChip = view.findViewById(R.id.model_chip)
        yearChip = view.findViewById(R.id.year_chip)
        mileageChip = view.findViewById(R.id.mileage_chip)
        colorChip = view.findViewById(R.id.color_chip)




        filterSheet?.setSheetEdge(Gravity.START)

        priceSlider?.let {
            it.values = listOf<Float>(0F, 100000F)
            it.setLabelFormatter { value: Float ->
                val format = NumberFormat.getCurrencyInstance()
                format.maximumFractionDigits = 0
                format.currency = Currency.getInstance("ILS")
                format.format(value.toDouble())
            }
            it.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {}

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val format = NumberFormat.getCurrencyInstance()
                    format.maximumFractionDigits = 0
                    format.currency = Currency.getInstance("ILS")
                    priceChip?.text =
                        "${format.format(slider.values[0].toDouble())}-${format.format(slider.values[1].toDouble())}"
                }
            })
        }

        mileageSlider?.let {
            it.values = listOf<Float>(0F, 500000F)
            it.setLabelFormatter { value: Float ->
                val format = NumberFormat.getIntegerInstance()
                format.maximumFractionDigits = 0
                format.format(value.toDouble())
            }
            it.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {}

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    val format = NumberFormat.getIntegerInstance()
                    format.maximumFractionDigits = 0
                    mileageChip?.text =
                        "${format.format(slider.values[0].toDouble())}-${format.format(slider.values[1].toDouble())}"
                }
            })
        }
        yearSlider?.let {
            it.values = listOf<Float>(1994F, 2024F)
            it.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {}

                override fun onStopTrackingTouch(slider: RangeSlider) {

                    yearChip?.text = "${slider.values[0].toInt()}-${slider.values[1].toInt()}"
                }
            })
        }
        modelSelect?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                modelChip?.text = parent?.getItemAtPosition(position) as String
                modelChip?.visibility = View.VISIBLE
            }
        modelSelect?.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == "") {
                modelChip?.visibility = View.GONE
            }
        }


        makeSelect?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                makeChip?.text = parent?.getItemAtPosition(position) as String
                makeChip?.visibility = View.VISIBLE
            }
        makeSelect?.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == "") {
                makeChip?.visibility = View.GONE
            }
        }

        colorSelect?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                colorChip?.text = parent?.getItemAtPosition(position) as String
                colorChip?.visibility = View.VISIBLE
            }
        colorSelect?.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == "") {
                colorChip?.visibility = View.GONE
            }
        }

        filtersButton?.setOnClickListener {
            filterSheet?.show()


        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.search, container, false)
        setupUI(view)
        getOptions("make", makeSelect)
        getOptions("model", modelSelect)
//        setResultList(inflater, container, savedInstanceState, view)

        return view
    }

    fun getOptions(type: String, input: MaterialAutoCompleteTextView?) {
        val uri = baseUrl + type

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, uri, null,
            { response ->
                val optionsObjList = response.optJSONArray("results")
                var optionsList = arrayOf<String>()
                optionsObjList?.let {
                    for (i in 0..<it.length()) {
                        optionsList += it.getJSONObject(i).getString(type)
                    }
                }
                input?.setSimpleItems(optionsList)


            },
            { _ ->
            }
        )


        Volley.newRequestQueue(this.context).add(jsonObjectRequest)
    }

//    fun setResultList(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?, view: View
//    ) {
//
//
//        viewModel = ViewModelProvider(this)[ResultsViewModel::class.java]
//
//
//        progressBar?.visibility = View.VISIBLE
//
//        viewModel.results = Model.instance.getAllCars()
//
//        resultsRecyclerView = view.findViewById(R.id.result_list)
//        resultsRecyclerView?.setHasFixedSize(true)
//        resultsRecyclerView?.layoutManager = LinearLayoutManager(context)
//        adapter = CarResultRecyclerAdapter(viewModel.results?.value)
//        adapter?.listener = object : CarResultRecyclerViewActivity.OnItemClickListener {
//
//            override fun onItemClick(position: Int) {
//                Log.i("TAG", "StudentsRecyclerAdapter: Position clicked $position")
//                val student = viewModel.results?.value?.get(position)
//                student?.let {
//                    val action =
//                        StudentsFragmentDirections.actionStudentsFragmentToBlueFragment(it.id)
//                    Navigation.findNavController(view).navigate(action)
//                }
//            }
//
//            override fun onCarClicked(result: Car?) {
//                TODO("Not yet implemented")
//            }
//        }
//
//        resultsRecyclerView?.adapter = adapter
//
//        val addStudentButton: ImageButton = view.findViewById(R.id.ibtnStudentsFragmentAddStudent)
//        val action =
//            Navigation.createNavigateOnClickListener(StudentsFragmentDirections.actionGlobalAddStudentFragment())
//        addStudentButton.setOnClickListener(action)
//
//        viewModel.results?.observe(viewLifecycleOwner) {
//            adapter?.results = it
//            adapter?.notifyDataSetChanged()
//            progressBar?.visibility = View.GONE
//        }
//
//
//    }


}