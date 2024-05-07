package com.idz.Recar.Modules.Register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.idz.Recar.Model.FirebaseModel
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.User
import com.idz.Recar.Model.User.Companion.DEFAULT_IMAGE_URL
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper
import com.squareup.picasso.Picasso

class Register : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private val navController by lazy { Navigation.findNavController(requireView()) }
    private var profileImage: ShapeableImageView? = null
    private var imageUri: String? = null
    private lateinit var registerButton: MaterialButton
    private lateinit var registerProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        auth= FirebaseAuth.getInstance()
    }

    private val openImagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImage?.let {
                Picasso.get()
                    .load(uri)
                    .into(it)
            }
            imageUri = uri.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val loginLink: TextView = view.findViewById(R.id.loginLink)
        val action = Navigation.createNavigateOnClickListener(RegisterDirections.actionRegisterFragmentToLoginFragment())
        loginLink.setOnClickListener(action)
        setupUI(view)
        return view
    }

    private fun validateForm(): Boolean {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phoneNumber = phoneNumberEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phoneNumber.length != 10) {
            Toast.makeText(requireContext(), "Phone number must have 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must have at least 6 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun toggleLoading(isLoading: Boolean) {
        registerProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerButton.isEnabled = !isLoading
    }

    private fun handleRegiter() {
        toggleLoading(true)

        if (validateForm()) {
            val email = emailEditText.text.toString()

            FirebaseModel().isEmailTaken(email) { isTaken ->
                if (!isTaken) {
                    val name = nameEditText.text.toString()
                    val phoneNumber = phoneNumberEditText.text.toString()
                    val password = passwordEditText.text.toString()

                    val user = User(name, email, phoneNumber, imageUri ?: DEFAULT_IMAGE_URL)

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                val userId = firebaseUser?.uid ?: ""

                                Model.instance.addUser(user, userId) {
                                    SharedPreferencesHelper.saveUserId(requireContext(), userId)
                                    navController.navigate(RegisterDirections.actionRegisterFragmentToMyCarFragment())
                                    toggleLoading(false)
                                    Toast.makeText(requireContext(), "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                toggleLoading(false)
                                Toast.makeText(requireContext(), authTask.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Email already taken", Toast.LENGTH_SHORT).show()
                    toggleLoading(false)
                }
            }
        } else {
            toggleLoading(false)
        }
    }

    private fun setupUI(view: View) {
        val editImageButton: ImageButton = view.findViewById(R.id.editImageButton)
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        registerButton = view.findViewById(R.id.registerButton)
        registerProgressBar = view.findViewById(R.id.registerProgressBar)
        profileImage = view.findViewById(R.id.profileImage)

        editImageButton.setOnClickListener {
            openImagePicker.launch("image/*")
        }

        registerButton.setOnClickListener {
            handleRegiter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
