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
    import com.idz.Recar.R
    import com.squareup.picasso.Callback
    import com.squareup.picasso.Picasso

    class ProfileEditFragment : Fragment() {

        private lateinit var nameEditText: EditText
        private lateinit var emailEditText: EditText
        private lateinit var phoneNumberEditText: EditText
        private lateinit var passwordEditText: EditText
        private lateinit var confirmPasswordEditText: EditText
        private lateinit var submitButton: Button
        private var imageView: ShapeableImageView? = null
        private var imageUri: String? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)
            setupUI(view)
            return view
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

            editImageButton.setOnClickListener {
                openImagePicker.launch("image/*")
            }

            submitButton.setOnClickListener {
                if (validateForm()) {
                    val name = nameEditText.text.toString()
                    val email = emailEditText.text.toString()
                    val phoneNumber = phoneNumberEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    val confirmPassword = confirmPasswordEditText.text.toString()

                    println("Name: $name")
                    println("Email: $email")
                    println("Phone Number: $phoneNumber")
                    println("Password: $password")
                    println("Confirm Password: $confirmPassword")
                    println("Image URI: $imageUri")
                }
            }
        }

        private val openImagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                println("Selected image URI: $imageView")
                imageView?.let {
                    Picasso.get()
                        .load(uri)
                        .into(it)
                }
                imageUri = uri.toString()
            }
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

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        }
    }
