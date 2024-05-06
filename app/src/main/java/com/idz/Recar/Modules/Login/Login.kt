package com.idz.Recar.Modules.Login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.idz.Recar.Model.FirebaseModel
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.User
import com.idz.Recar.R
import com.idz.Recar.Utils.SharedPreferencesHelper

const val RC_SIGN_IN: Int = 1
const val SERVER_CLIENT_ID: String = "778157402602-8h29nsc2dvddoiv85tsmhb5s6apm9qr7.apps.googleusercontent.com"
const val PHONE_NUMBER_PLACEHOLDER: String = "0000000000"

class Login : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private val navController by lazy { Navigation.findNavController(requireView()) }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var loginButton: MaterialButton
    private lateinit var loginProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)
    }

    private fun setupUI(view: View) {
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val signupLink: TextView = view.findViewById(R.id.signupLink)
        loginButton = view.findViewById(R.id.loginButton)
        loginProgressBar = view.findViewById(R.id.loginProgressBar)

        signupLink.setOnClickListener {
            val action = LoginDirections.actionLoginFragmentToRegisterFragment()
            navController.navigate(action)
        }

        loginButton.setOnClickListener {
            signInWithEmailPassword()
        }

        val googleButton: SignInButton = view.findViewById(R.id.loginWithGoogleButton)
        googleButton.setOnClickListener {
            signInWithGoogle()
        }

        setupGoogleSignIn()
        setupUI(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firebaseUser = auth.currentUser

        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            SharedPreferencesHelper.saveUserId(requireContext(), userId)
            val action = LoginDirections.actionLoginFragmentToMyCarFragment()
            navController.navigate(action)
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !isLoading
    }

    private fun signInWithEmailPassword() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        toggleLoading(true)

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
            Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                val userId = firebaseUser?.uid ?: ""
                SharedPreferencesHelper.saveUserId(requireContext(), userId)
                val action = LoginDirections.actionLoginFragmentToMyCarFragment()
                navController.navigate(action)
                Toast.makeText(requireContext(), "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Incorrect email or password", Toast.LENGTH_SHORT)
                    .show()
            }

            toggleLoading(false)
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        toggleLoading(true)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "Google Sign-In failed: ${e.statusCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val email = account.email ?: ""
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: ""

                    FirebaseModel().isEmailTaken(email) { isTaken ->
                        if (!isTaken) {
                            val name = account.displayName ?: ""
                            val imageUrl = account.photoUrl.toString()

                            val user = User(name, email, PHONE_NUMBER_PLACEHOLDER, imageUrl)

                            Model.instance.addUser(user, userId) {
                                toggleLoading(false)
                                SharedPreferencesHelper.saveUserId(requireContext(), userId)
                                val action = LoginDirections.actionLoginFragmentToMyCarFragment()
                                navController.navigate(action)
                                Toast.makeText(
                                    requireContext(),
                                    "Successfully LoggedIn",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            toggleLoading(false)
                            SharedPreferencesHelper.saveUserId(requireContext(), userId)
                            val action = LoginDirections.actionLoginFragmentToMyCarFragment()
                            navController.navigate(action)
                            Toast.makeText(
                                requireContext(),
                                "Successfully LoggedIn",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    toggleLoading(false)
                    Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
