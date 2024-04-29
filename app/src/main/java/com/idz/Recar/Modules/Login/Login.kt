package com.idz.Recar.Modules.Login
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
import com.idz.Recar.Modules.Profile.ProfileInfoFragmentDirections
import com.idz.Recar.Modules.Students.StudentsFragmentDirections
import com.idz.Recar.R

class Login : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val signupLink: TextView = view.findViewById(R.id.signupLink)
        var action = Navigation.createNavigateOnClickListener(LoginDirections.actionLoginFragmentToRegisterFragment())
        signupLink.setOnClickListener(action)
        val loginButton: TextView = view.findViewById(R.id.loginButton)
        action = Navigation.createNavigateOnClickListener(LoginDirections.actionLoginFragmentToStudentsFragment())
        loginButton.setOnClickListener(action)
        setupUI(view)
        return view
    }

    private fun setupUI(view: View) {
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}