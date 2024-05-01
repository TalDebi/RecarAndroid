    package com.idz.Recar.Modules.Profile

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import com.google.android.material.imageview.ShapeableImageView
    import com.idz.Recar.Model.Model
    import com.idz.Recar.R
    import com.idz.Recar.Utils.SharedPreferencesHelper
    import com.idz.Recar.dao.AppLocalDatabase
    import com.squareup.picasso.Callback
    import com.squareup.picasso.Picasso
    import androidx.navigation.fragment.findNavController
    import com.google.firebase.auth.FirebaseAuth
    import com.idz.Recar.dao.User as LocalUser
    import com.idz.Recar.Model.User

    const val DEFAULT_IMAGE_URL = "drawable://avatar.png"

    class ProfileEditFragment : Fragment() {
        private lateinit var auth: FirebaseAuth
        private lateinit var nameEditText: EditText
        private lateinit var emailEditText: EditText
        private lateinit var phoneNumberEditText: EditText
        private lateinit var passwordEditText: EditText
        private lateinit var confirmPasswordEditText: EditText
        private lateinit var submitButton: Button
        private var imageView: ShapeableImageView? = null
        private var imageUri: String? = null
        private lateinit var userId: String

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
            // Retrieve the user ID from SharedPreferences
            auth= FirebaseAuth.getInstance()
            userId = SharedPreferencesHelper.getUserId(requireContext()) ?: ""
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)
            setupUI(view)
            return view
        }

        private val openImagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                println("Selected image URI: $uri")
                imageView?.let {
                    Picasso.get()
                        .load(uri)
                        .into(it)
                }
                imageUri = uri.toString()
            }
        }

        private fun isEmailTaken(email: String): Boolean {
            val signInMethods = auth.fetchSignInMethodsForEmail(email).getResult()?.signInMethods ?: emptyList()
            return signInMethods.isNotEmpty()
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

            if (isEmailTaken(email)) {
                Toast.makeText(requireContext(), "Email already taken", Toast.LENGTH_SHORT).show()
                return false
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        }

        private fun setupUI(view: View) {
            val editImageButton: ImageButton = view.findViewById(R.id.editImageButton)
            nameEditText = view.findViewById(R.id.nameEditText)
            emailEditText = view.findViewById(R.id.emailEditText)
            phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText)
            passwordEditText = view.findViewById(R.id.passwordEditText)
            confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
            submitButton = view.findViewById(R.id.submitButton)
            imageView = view.findViewById(R.id.imageView)
            val userDao = AppLocalDatabase.db.userDao()
            var currentUser: LocalUser? = null

            userDao.getUserById(userId).observe(viewLifecycleOwner, { user ->
                user?.let {
                    nameEditText.setText(it.name)
                    emailEditText.setText(it.email)
                    phoneNumberEditText.setText(it.phoneNumber)
                    currentUser = it
                }
            })

            editImageButton.setOnClickListener {
                openImagePicker.launch("image/*")
            }

            submitButton.setOnClickListener {
                if (validateForm()) {
                    val name = nameEditText.text.toString()
                    val email = emailEditText.text.toString()
                    val phoneNumber = phoneNumberEditText.text.toString()
                    val password = passwordEditText.text.toString()

                    val modefiedUser = User(
                        name = name,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        imgUrl = imageUri ?: DEFAULT_IMAGE_URL
                        )
                    if (email != currentUser?.email) {
                        auth.currentUser?.updateEmail(email)
                            ?.addOnSuccessListener {
                                Model.instance.editUserById(userId, modefiedUser) {
                                    Toast.makeText(requireContext(), "User details updated successfully", Toast.LENGTH_SHORT).show()
                                    findNavController().popBackStack()
                                }
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error updating email: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        Model.instance.editUserById(userId, modefiedUser) {
                            Toast.makeText(
                                requireContext(),
                                "User details updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }
