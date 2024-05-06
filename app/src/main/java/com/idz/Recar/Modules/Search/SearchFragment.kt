package com.idz.Recar.Modules.Search

import android.content.DialogInterface
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
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.chip.Chip
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.idz.Recar.Model.Model
import com.idz.Recar.Modules.Search.Adapter.CarResultRecyclerAdapter
import com.idz.Recar.Modules.Search.Adapter.OnItemClickListener
import com.idz.Recar.R
import com.idz.Recar.base.VollyQueue
import com.idz.Recar.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private val MIN_YEAR = 1994
    private val MAX_YEAR = 2024
    private val MIN_PRICE = 0
    private val MAX_PRICE = 100000
    private val MIN_MILEAGE = 0
    private val MAX_MILEAGE = 500000

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
    private var colorSelect: EditText? = null
    private var priceChip: Chip? = null
    private var makeChip: Chip? = null
    private var modelChip: Chip? = null
    private var colorChip: Chip? = null
    private var yearChip: Chip? = null
    private var mileageChip: Chip? = null
    private var baseUrl =
        "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=5000&group_by="
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var yearStart: Int = MIN_YEAR
    private var yearEnd: Int = MAX_YEAR
    private var mileageStart: Int = MIN_MILEAGE
    private var mileageEnd: Int = MAX_MILEAGE
    private var priceStart: Int = MIN_PRICE
    private var priceEnd: Int = MAX_PRICE
    private var color: String? = null
    private var model: String? = null
    private var make: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    private fun setupUI(view: View) {


        filtersButton = binding.filterButton
        progressBar = binding.progressBar2

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
        priceChip = binding.priceChip
        makeChip = binding.makeChip
        modelChip = binding.modelChip
        yearChip = binding.yearChip
        mileageChip = binding.mileageChip
        colorChip = binding.colorChip

        yearSlider?.valueFrom = MIN_YEAR.toFloat()
        yearSlider?.valueTo = MAX_YEAR.toFloat()
        mileageSlider?.valueFrom = MIN_MILEAGE.toFloat()
        mileageSlider?.valueTo = MAX_MILEAGE.toFloat()
        priceSlider?.valueFrom = MIN_PRICE.toFloat()
        priceSlider?.valueTo = MAX_PRICE.toFloat()




        filterSheet?.setSheetEdge(Gravity.START)
        filterSheet?.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface?) {
                reloadData()
            }

        })

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
                    priceStart = slider.values[0].toInt()
                    priceEnd = slider.values[1].toInt()
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
                    mileageStart = slider.values[0].toInt()
                    mileageEnd = slider.values[1].toInt()
                }
            })
        }
        yearSlider?.let {
            it.values = listOf<Float>(1994F, 2024F)
            it.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {}

                override fun onStopTrackingTouch(slider: RangeSlider) {

                    yearChip?.text = "${slider.values[0].toInt()}-${slider.values[1].toInt()}"
                    yearStart = slider.values[0].toInt()
                    yearEnd = slider.values[1].toInt()
                }
            })
        }
        modelSelect?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                modelChip?.text = parent?.getItemAtPosition(position) as String
                modelChip?.visibility = View.VISIBLE
                model = parent.getItemAtPosition(position) as String
            }
        modelSelect?.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == "") {
                modelChip?.visibility = View.GONE
                model = null
            }
        }


        makeSelect?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                makeChip?.text = parent?.getItemAtPosition(position) as String
                makeChip?.visibility = View.VISIBLE
                make = parent.getItemAtPosition(position) as String
            }
        makeSelect?.doOnTextChanged { text, _, _, _ ->
            if (text.toString() == "") {
                makeChip?.visibility = View.GONE
                make = null
            }
        }


        colorSelect?.doOnTextChanged { text, _, _, _ ->
            colorChip?.text = text
            color = text.toString()
            colorChip?.visibility = View.VISIBLE
            if (text.toString() == "") {
                colorChip?.visibility = View.GONE
                color = null
            }
        }

        filtersButton?.setOnClickListener {
            filterSheet?.show()


        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
        setupUI(view)
        getOptions("make", makeSelect)
        getOptions("model", modelSelect)
        setResultList(view)

        return view
    }

    private fun getOptions(type: String, input: MaterialAutoCompleteTextView?) {
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


        this.context?.let { VollyQueue.getInstance(it).addToRequestQueue((jsonObjectRequest)) }
    }

    fun setResultList(view: View) {


        viewModel = ViewModelProvider(this)[ResultsViewModel::class.java]


        progressBar?.visibility = View.VISIBLE

        viewModel.results = Model.instance.getAllCars(
            yearStart,
            yearEnd,
            mileageStart,
            mileageEnd,
            priceStart,
            priceEnd,
            color,
            model,
            make
        )

        resultsRecyclerView = binding.resultList
        resultsRecyclerView?.setHasFixedSize(true)
        resultsRecyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = CarResultRecyclerAdapter(viewModel.results?.value)
        adapter?.listener = object : OnItemClickListener {

            override fun onItemClick(position: Int) {
                Log.i("TAG", "StudentsRecyclerAdapter: Position clicked $position")
                val result = viewModel.results?.value?.get(position)
                result?.let {
                    val action = SearchFragmentDirections.actionSearchFragmentToCarFragment(it.id)
                    view.findNavController().navigate(action)
                }
            }


        }

        resultsRecyclerView?.adapter = adapter



        viewModel.results?.observe(viewLifecycleOwner) {
            adapter?.results = it
            adapter?.notifyDataSetChanged()
            progressBar?.visibility = View.GONE
        }

        binding.pullToRefresh.setOnRefreshListener {
            reloadData()
        }

        Model.instance.studentsListLoadingState.observe(viewLifecycleOwner) { state ->
            binding.pullToRefresh.isRefreshing = state == Model.LoadingState.LOADING
        }


    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    private fun reloadData() {
        progressBar?.visibility = View.VISIBLE
        Model.instance.refreshAllCars(
            yearStart,
            yearEnd,
            mileageStart,
            mileageEnd,
            priceStart,
            priceEnd,
            color,
            model,
            make
        )
        progressBar?.visibility = View.GONE
        binding.pullToRefresh.isRefreshing = false
    }


}



