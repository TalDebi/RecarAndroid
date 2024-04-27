package com.idz.Recar.Modules.Register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.Navigation
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.Student
import com.idz.Recar.Model.User
import com.idz.Recar.Modules.Login.LoginDirections
import com.idz.Recar.Modules.Students.StudentsFragmentDirections
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper

class Register : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val loginLink: TextView = view.findViewById(R.id.loginRedirectTextView)
        var action = Navigation.createNavigateOnClickListener(RegisterDirections.actionRegisterFragmentToLoginFragment())
        loginLink.setOnClickListener(action)
        val registerButton: TextView = view.findViewById(R.id.registerButton)
        action = Navigation.createNavigateOnClickListener(LoginDirections.actionRegisterFragmentToStudentsFragment())
        registerButton.setOnClickListener(action)
        setupUI(view)
        return view
    }

    private fun setupUI(view: View) {
        val nameEditText: EditText = view.findViewById(R.id.nameEditText)
        val emailEditText: EditText = view.findViewById(R.id.emailEditText)
        val phoneNumberEditText: EditText = view.findViewById(R.id.phoneNumberEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = view.findViewById(R.id.confirmPasswordEditText)
        val registerButton: Button = view.findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Validate inputs here

            val user = User("", name, email, password, phoneNumber)
            Model.instance.addUser(user) {
                // Handle registration completion
                SharedPreferencesHelper.saveUserId(requireContext(), user.id)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
