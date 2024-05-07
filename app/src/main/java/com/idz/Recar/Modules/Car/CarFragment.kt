package com.idz.Recar.Modules.Car

import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonArrayRequest
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Model
import com.idz.Recar.Modules.Car.Adapter.ImageAdapter
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper
import com.idz.Recar.base.VollyQueue
import com.idz.Recar.dao.AppLocalDatabase
import com.idz.Recar.databinding.FragmentCarPageBinding
import org.json.JSONException
import org.json.JSONObject


class CarFragment : Fragment() {
    private var tvModel: TextView? = null
    private var tvMake: TextView? = null
    private var tvPrice: TextView? = null
    private var tvYear: TextView? = null
    private var tvCity: TextView? = null
    private var btnEdit: ImageButton? = null
    private var btnDelete: ImageButton? = null
    private var rvCarousel: RecyclerView? = null
    private var cg: ChipGroup? = null
    private var carId: String? = null
    private var ninjaBaseUrl = "https://api.api-ninjas.com/v1/cars?limit=1&"
    private var _binding: FragmentCarPageBinding? = null

    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarPageBinding.inflate(inflater, container, false)
        val view = binding.root
        carId = arguments?.getString("carId")
        setupUI(view)
        return view
    }

    private fun setupUI(view: View) {
        tvModel = binding.tvModel
        tvMake = binding.tvMake
        tvPrice = binding.tvPrice
        tvYear = binding.tvYear
        tvCity = binding.tvCity
        cg = binding.cg
        btnEdit = binding.btnEdit
        btnDelete = binding.btnDelete
        rvCarousel = binding.rvCarousel

        val userId = SharedPreferencesHelper.getUserId(requireContext()) ?: ""


        val carDao = AppLocalDatabase.db.carDao()

        carId?.let { it ->
            Model.instance.getCarById(it)
            carDao.getCarById(it).observe(viewLifecycleOwner, Observer { car ->
                car?.let { currCar ->
                    tvModel?.text = car.model
                    tvMake?.text = car.make
                    tvYear?.text = car.year.toString()
                    tvCity?.text = car.city

                    if (userId == currCar.owner) {
                        btnEdit?.visibility = View.VISIBLE
                        btnDelete?.visibility = View.VISIBLE
                        btnEdit?.setOnClickListener {
                            val action =
                                CarFragmentDirections.actionGlobalCarFormFragment(carId)
                            findNavController().navigate(action)
                        }
                        btnDelete?.setOnClickListener {
                            Model.instance.deleteCarById(currCar.id)
                            findNavController().popBackStack()
                        }
                    }
                    val format = NumberFormat.getCurrencyInstance()
                    format.maximumFractionDigits = 0
                    format.currency = Currency.getInstance("ILS")
                    tvPrice?.text = format.format(car.price.toDouble())

                    getAdditionalData(currCar)

                    rvCarousel?.layoutManager = CarouselLayoutManager()
                    rvCarousel?.adapter = ImageAdapter(currCar.imageUrls)


                }
            })
        }
    }


    private fun getAdditionalData(car: Car) {
        val uri = ninjaBaseUrl + "make=${car.make}&model=${car.model}&year=${car.year}"

        val request = object : JsonArrayRequest(
            Method.GET, uri, null,
            { response ->
                if (response.length() != 0) {
                    val jsonObj = response.getJSONObject(0)
                    jsonObj.put("mileage", car.mileage)
                    jsonObj.put("color", car.color)
                    jsonObj.put("hand", car.hand)
                    setAdditionalInfo(jsonObj)
                }
            },
            { _ ->
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-Api-Key"] = "QWIPY6iARogbwUPS87IODg==t3D5r0VhHHtpZJQv"
                return headers
            }
        }


        context?.let { VollyQueue.getInstance(it).addToRequestQueue(request) }


    }


    fun setAdditionalInfo(info: JSONObject) {
        val keys = arrayOf(
            "mileage",
            "color",
            "hand",
            "city_mpg",
            "class",
            "combination_mpg",
            "cylinders",
            "displacement",
            "drive",
            "fuel_type",
            "highway_mpg",
            "transmission"
        )

        cg?.let {
            for (key in keys) {
                context?.let { cntx ->
                    var chip = Chip(cntx)
                    var label = key.replace("_", " ")
                    try {

                        var value = info.getString(key)
                        chip.text = "${label}: ${value}"
                        chip.textSize = 20F
                        it.addView(chip)
                    } catch (e: JSONException) {
                        Log.e(javaClass.name, e.toString())
                    }
                }
            }
        }


    }

    override fun onStop() {
        super.onStop()
        findNavController().popBackStack(R.id.action_global_searchFragment, false)
    }


}

