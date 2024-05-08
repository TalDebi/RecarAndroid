package com.idz.Recar.Modules.Car

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.button.MaterialButton
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.storage.UploadTask
import com.idz.Recar.Model.Car
import com.idz.Recar.Model.Model
import com.idz.Recar.Modules.Car.Adapter.ImageAdapter
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper
import com.idz.Recar.base.FireBaseStorage
import com.idz.Recar.base.VollyQueue
import com.idz.Recar.dao.AppLocalDatabase
import com.idz.Recar.dao.CarDao.Companion.MAX_MILEAGE
import com.idz.Recar.dao.CarDao.Companion.MAX_PRICE
import com.idz.Recar.dao.CarDao.Companion.MAX_YEAR
import com.idz.Recar.dao.CarDao.Companion.MIN_MILEAGE
import com.idz.Recar.dao.CarDao.Companion.MIN_PRICE
import com.idz.Recar.dao.CarDao.Companion.MIN_YEAR
import java.util.UUID

class CarFormFragment : Fragment() {
    private var carId: String? = null
    private lateinit var modelText: MaterialAutoCompleteTextView
    private lateinit var makeText: MaterialAutoCompleteTextView
    private lateinit var yearText: EditText
    private lateinit var colorText: EditText
    private lateinit var priceText: EditText
    private lateinit var handText: EditText
    private lateinit var mileageText: EditText
    private lateinit var cityText: EditText
    private lateinit var imageCarousel: RecyclerView
    private var baseUrl =
        "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=5000&group_by="

    private var imageSuccessList = mutableListOf<String>()
    private var imageUris: List<Uri>? = null
    private val carDao = AppLocalDatabase.db.carDao()

    private lateinit var submitButton: MaterialButton
    private lateinit var submitProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            5
        )
    ) { uris ->

        if (uris.isNotEmpty()) {
            imageUris = uris
        }
        imageCarousel?.adapter = imageUris?.let {
            ImageAdapter(it.map {
                it.toString()
            }, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        carId = arguments?.getString("carId")

        val view = inflater.inflate(R.layout.fragment_car_form, container, false)
        setupUI(view)
        return view
    }


    private fun validateForm(): Boolean {

        val make = makeText.text.toString()
        val model = modelText.text.toString()
        val year = yearText.text.toString()
        val color = colorText.text.toString()
        val price = priceText.text.toString()
        val hand = handText.text.toString()
        val mileage = mileageText.text.toString()
        val city = cityText.text.toString()

        if (make.isEmpty() || model.isEmpty() || year.isEmpty() || color.isEmpty() ||
            price.isEmpty() || hand.isEmpty() || mileage.isEmpty() || (imageSuccessList.isEmpty() && imageUris == null) ||
            city.isEmpty()
        ) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            toggleLoading(false)
            return false
        }

        if (year.toInt() < MIN_YEAR || year.toInt() > MAX_YEAR) {
            Toast.makeText(
                requireContext(),
                "Year must be between $MIN_YEAR and $MAX_YEAR",
                Toast.LENGTH_SHORT
            ).show()
            toggleLoading(false)
            return false
        }
        if (mileage.toInt() < MIN_MILEAGE || mileage.toInt() > MAX_MILEAGE) {
            Toast.makeText(
                requireContext(),
                "Mileage must be between $MIN_MILEAGE and $MAX_MILEAGE",
                Toast.LENGTH_SHORT
            ).show()
            toggleLoading(false)
            return false
        }
        if (price.toInt() < MIN_PRICE || price.toInt() > MAX_PRICE) {
            Toast.makeText(
                requireContext(),
                "Price must be between $MIN_PRICE and $MAX_PRICE",
                Toast.LENGTH_SHORT
            ).show()
            toggleLoading(false)
            return false
        }

        if (hand.toInt() < 0) {
            Toast.makeText(
                requireContext(),
                "Hand must be greater than 0",
                Toast.LENGTH_SHORT
            ).show()
        }

        return true
    }

    private fun toggleLoading(isLoading: Boolean) {
        submitProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        submitButton.isEnabled = !isLoading
    }

    private fun setupUI(view: View) {
        val editImageButton: ImageButton = view.findViewById(R.id.editImageButton)
        modelText = view.findViewById(R.id.modelText)
        makeText = view.findViewById(R.id.makeText)
        yearText = view.findViewById(R.id.yearText)
        colorText = view.findViewById(R.id.colorText)
        priceText = view.findViewById(R.id.priceText)
        handText = view.findViewById(R.id.handText)
        mileageText = view.findViewById(R.id.mileageText)
        cityText = view.findViewById(R.id.cityText)
        imageCarousel = view.findViewById(R.id.rvCarousel)
        submitButton = view.findViewById(R.id.submitButton)
        submitProgressBar = view.findViewById(R.id.registerProgressBar)
        getOptions("make", makeText)
        getOptions("model", modelText)
        imageCarousel.layoutManager = CarouselLayoutManager()
        imageCarousel.adapter = imageUris?.let {
            ImageAdapter(it.map {
                it.toString()
            }, true)
        }
        carId?.let { it ->
            Model.instance.getCarById(it)
            carDao.getCarById(it).observe(viewLifecycleOwner, Observer { car ->
                car?.let { currCar ->
                    makeText.setText(currCar.make)
                    modelText.setText(currCar.model)
                    yearText.setText(currCar.year.toString())
                    colorText.setText(currCar.color)
                    priceText.setText(currCar.price.toString())
                    handText.setText(currCar.hand.toString())
                    mileageText.setText(currCar.mileage.toString())
                    cityText.setText(currCar.city)
                    imageCarousel.adapter = ImageAdapter(currCar.imageUrls, false)
                    imageSuccessList = currCar.imageUrls.toMutableList()
                    submitButton.text = "UPDATE"

                }
            })
        }



        editImageButton.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        submitButton.setOnClickListener {
            toggleLoading(true)

            if (validateForm()) {
                val make = makeText.text.toString()
                val model = modelText.text.toString()
                val year = yearText.text.toString()
                val color = colorText.text.toString()
                val price = priceText.text.toString()
                val hand = handText.text.toString()
                val mileage = mileageText.text.toString()
                val city = cityText.text.toString()
                val userId = SharedPreferencesHelper.getUserId(requireContext()) ?: ""

                var tasks: ArrayList<Task<UploadTask.TaskSnapshot>>? = null
                imageUris?.let {
                    tasks = uploadPhotos(it)
                }

                Tasks.whenAll(tasks).addOnCompleteListener { p0 ->
                    if (p0.isSuccessful) {
                        val car = Car(
                            carId ?: UUID.randomUUID().toString(),
                            imageSuccessList,
                            make,
                            model,
                            year.toLong(),
                            price.toLong(),
                            hand.toLong(),
                            color,
                            mileage.toLong(),
                            city,
                            userId
                        )
                        Model.instance.addCar(car) {
                            toggleLoading(false)
                            findNavController().navigate(R.id.action_carFormFragment_to_myCarFragment)
                        }

                    } else {
                        Toast.makeText(requireContext(), "upload failed", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }
    }


    fun uploadPhotos(imageUris: List<Uri>): ArrayList<Task<UploadTask.TaskSnapshot>> {
        val storageRef = FireBaseStorage.getInstance().storage.reference
        val tasks = arrayListOf<Task<UploadTask.TaskSnapshot>>()
        imageSuccessList = mutableListOf()
        for (imageUri in imageUris) {


            val riversRef = storageRef.child("images/${UUID.randomUUID()}")
            var uploadTask = riversRef.putFile(imageUri)
            tasks.add(uploadTask)
// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                val a = taskSnapshot.metadata?.reference.toString()
                imageSuccessList.add(a)
            }
        }
        return tasks
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

}