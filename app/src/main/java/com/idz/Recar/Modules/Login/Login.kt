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
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.Student
import com.idz.Recar.Model.User
import com.idz.Recar.Modules.Profile.ProfileInfoFragmentDirections
import com.idz.Recar.Modules.Register.DEFAULT_IMAGE_URL
import com.idz.Recar.Modules.Students.StudentsFragmentDirections
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper

class Login : Fragment() {
    private lateinit var  auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private val navController by lazy { Navigation.findNavController(requireView()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        auth= FirebaseAuth.getInstance()
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

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun setupUI(view: View) {
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        val loginButton: Button = view.findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            if (validateForm()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid ?: ""
                        SharedPreferencesHelper.saveUserId(requireContext(), userId)
                        navController.navigate(LoginDirections.actionLoginFragmentToStudentsFragment())
                        Toast.makeText(requireContext(), "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Incorrect email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}