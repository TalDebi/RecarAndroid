package com.idz.Recar.Modules.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.idz.Recar.Model.FirebaseModel
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper
import com.idz.Recar.dao.AppLocalDatabase
import com.squareup.picasso.Picasso

class ProfileInfoFragment : Fragment() {

    private lateinit var userId: String
    private var imageView: ShapeableImageView? = null
    private val firebaseModel = FirebaseModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        userId = SharedPreferencesHelper.getUserId(requireContext()) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_info, container, false)
        setupUI(view)
        return view
    }

    private fun loadUserImage(imageUrl: String) {
        if (imageUrl.startsWith("gs://") || imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
            firebaseModel.fetchUserImage(imageUrl) { uri ->
                uri?.let {
                    val imageUriString = uri.toString()
                    Picasso.get()
                        .load(imageUriString)
                        .placeholder(R.drawable.avatar)
                        .into(imageView)
                }
            }
        } else {
            Picasso.get()
                .load(R.drawable.avatar)
                .into(imageView)
        }
    }

    private fun setupUI(view: View) {
        val userDao = AppLocalDatabase.db.userDao()
        imageView = view.findViewById(R.id.imageView)

        userDao.getUserById(userId).observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                view.findViewById<TextView>(R.id.name).text = it.name
                view.findViewById<TextView>(R.id.email).text = it.email
                view.findViewById<TextView>(R.id.phoneNumber).text = it.phoneNumber
                loadUserImage(it.imgUrl)
            }
        })

        val editProfileButton: TextView = view.findViewById(R.id.name)
        val actionNavigateToEdit = ProfileInfoFragmentDirections.actionProfileInfoFragmentToProfileEditFragment()
        editProfileButton.setOnClickListener { Navigation.findNavController(view).navigate(actionNavigateToEdit) }

        val logoutButton: TextView = view.findViewById(R.id.logoutButton)
        val actionNavigateToLogin = ProfileInfoFragmentDirections.actionProfileInfoFragmentToLoginFragment()
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Navigation.findNavController(view).navigate(actionNavigateToLogin)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
