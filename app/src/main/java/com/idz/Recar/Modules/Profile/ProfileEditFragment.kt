    package com.idz.Recar.Modules.Profile

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.ProgressBar
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import com.google.android.material.imageview.ShapeableImageView
    import com.idz.Recar.Model.Model
    import com.idz.Recar.R
    import com.idz.Recar.Utils.SharedPreferencesHelper
    import com.idz.Recar.dao.AppLocalDatabase
    import com.squareup.picasso.Picasso
    import androidx.navigation.fragment.findNavController
    import com.google.android.material.button.MaterialButton
    import com.google.firebase.auth.FirebaseAuth
    import com.idz.Recar.Model.FirebaseModel
    import com.idz.Recar.dao.User as LocalUser
    import com.idz.Recar.Model.User
    import com.idz.Recar.Model.User.Companion.DEFAULT_IMAGE_URL

    class ProfileEditFragment : Fragment() {
        private lateinit var auth: FirebaseAuth
        private lateinit var nameEditText: EditText
        private lateinit var emailEditText: EditText
        private lateinit var phoneNumberEditText: EditText
        private lateinit var passwordEditText: EditText
        private lateinit var confirmPasswordEditText: EditText
        private var imageView: ShapeableImageView? = null
        private var imageUri: String? = null
        private lateinit var userId: String
        private lateinit var editButton: MaterialButton
        private lateinit var editProgressBar: ProgressBar
        private val firebaseModel = FirebaseModel()
        private var currentUser: LocalUser? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
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
                imageView?.let {
                    Picasso.get()
                        .load(uri)
                        .into(it)
                }
                imageUri = uri.toString()
            }
        }

        private fun isEmailTaken(email: String, onComplete: (Boolean) -> Unit) {
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        val signInMethods = result?.signInMethods ?: emptyList()
                        onComplete(signInMethods.isNotEmpty())
                    } else {
                        onComplete(false)
                    }
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

        private fun handleEmailAndPasswordUpdate(currentUser: LocalUser?, email: String, password: String) {
            if (email != currentUser?.email) {
                updateEmail(email) { success ->
                    if (success) {
                        handlePasswordUpdate(email, password)
                    } else {
                        toggleLoading(false)
                        showErrorMessage("Error updating email")
                    }
                }
            } else {
                handlePasswordUpdate(email, password)
            }
        }

        private fun handlePasswordUpdate(email: String, password: String) {
            val name = nameEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()

            val modifiedUser = User(
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                imgUrl = imageUri ?: DEFAULT_IMAGE_URL
            )

            updatePassword(password) {
                if (it) {
                    updateUser(modifiedUser) {
                        toggleLoading(false)
                    }
                } else {
                    toggleLoading(false)
                    showErrorMessage("Error updating password")
                }
            }
        }

        private fun checkEmailAvailability(email: String, onComplete: (Boolean) -> Unit) {
            isEmailTaken(email) { isTaken ->
                onComplete(!isTaken)
            }
        }

        private fun updateEmail(email: String, onComplete: (Boolean) -> Unit) {
            auth.currentUser?.updateEmail(email)
                ?.addOnSuccessListener {
                    onComplete(true)
                }
                ?.addOnFailureListener {
                    onComplete(false)
                }
        }

        private fun updatePassword(password: String, onComplete: (Boolean) -> Unit) {
            auth.currentUser?.updatePassword(password)
                ?.addOnSuccessListener {
                    onComplete(true)
                }
                ?.addOnFailureListener {
                    onComplete(false)
                }
        }

        private fun updateUser(user: User, onComplete: () -> Unit) {
            Model.instance.editUserById(userId, user) {
                onComplete()
                Toast.makeText(requireContext(), "User details updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        private fun showErrorMessage(message: String) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        private fun toggleLoading(isLoading: Boolean) {
            editProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            editButton.isEnabled = !isLoading
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

        private fun handleEditUser() {
            toggleLoading(true)

            if (validateForm()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                checkEmailAvailability(email) { isAvailable ->
                    if (isAvailable) {
                        handleEmailAndPasswordUpdate(currentUser, email, password)
                    } else {
                        handlePasswordUpdate(email, password)
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
            editButton = view.findViewById(R.id.editButton)
            editProgressBar = view.findViewById(R.id.editProgressBar)
            imageView = view.findViewById(R.id.imageView)
            val userDao = AppLocalDatabase.db.userDao()

            userDao.getUserById(userId).observe(viewLifecycleOwner) { user ->
                user?.let {
                    nameEditText.setText(it.name)
                    emailEditText.setText(it.email)
                    phoneNumberEditText.setText(it.phoneNumber)
                    loadUserImage(it.imgUrl)
                    currentUser = it
                }
            }

            editImageButton.setOnClickListener {
                openImagePicker.launch("image/*")
            }

            editButton.setOnClickListener {
                handleEditUser()
            }
        }
    }
