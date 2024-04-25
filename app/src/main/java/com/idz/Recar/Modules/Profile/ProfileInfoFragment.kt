package com.idz.Recar.Modules.Profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.User
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper

class ProfileInfoFragment : Fragment() {

    private lateinit var userId: String // User ID to load user details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // Retrieve the user ID from SharedPreferences
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

    private fun setupUI(view: View) {
        // Fetch user details based on the user ID
//        Model.instance.getUserById(userId).observe(viewLifecycleOwner, Observer { user ->
//            // Populate the UI with user details
//            user?.let {
//                view.findViewById<TextView>(R.id.name).text = it.name
//                view.findViewById<TextView>(R.id.email).text = it.email
//                view.findViewById<TextView>(R.id.phoneNumber).text = it.phoneNumber
//            }
//        })

        // Navigate to the profile edit fragment when the edit profile button is clicked
        val editProfileButton: TextView = view.findViewById(R.id.name)
        val action = ProfileInfoFragmentDirections.actionProfileInfoFragmentToProfileEditFragment()
        editProfileButton.setOnClickListener { Navigation.findNavController(view).navigate(action) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
