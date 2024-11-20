package com.example.newsapp.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.newsapp.BuildConfig
import com.example.newsapp.R
import com.example.newsapp.data.UserDataManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var signUpRedirect: TextView
    private lateinit var loginBtn: Button
    private lateinit var googleLogin: ImageButton
    private lateinit var ssoInfo: TextView
    private lateinit var errorMsg: TextView

    private lateinit var auth: FirebaseAuth

    private val userDataManager = UserDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailTextField = findViewById(R.id.emailAddressField)
        passwordTextField = findViewById(R.id.passwordField)
        signUpRedirect = findViewById(R.id.signUpRedirect)
        loginBtn = findViewById(R.id.loginButton)
        googleLogin = findViewById(R.id.googleLogin)
        ssoInfo = findViewById(R.id.SSOInfo)
        errorMsg = findViewById(R.id.errorMsg)

        auth = Firebase.auth
        val ssoPopUpWindow = SSOPopUpWindow(this)

        val credentialManager = CredentialManager.create(this)

        // Ensures elements retain their state on orientation change
        if (savedInstanceState != null) {
            val emailText = savedInstanceState.getString("emailInput")
            val passwordText = savedInstanceState.getString("passwordInput")
            val errorText = savedInstanceState.getString("errorMsg")

            emailTextField.setText(emailText)
            passwordTextField.setText(passwordText)
            errorMsg.text = errorText
        }

        // Shows pop up window with information for Single Sign On (See SSOPopUpWindow class)
        ssoInfo.setOnClickListener {
            ssoPopUpWindow.showPopUpWindow(it)
        }

        // Redirects to the Sign Up page
        signUpRedirect.setOnClickListener {
            val redirectIntent = Intent(this, AccCreationActivity::class.java)
            startActivity(redirectIntent)
        }

        loginBtn.setOnClickListener {
            // Checks validity of input fields (i.e., not empty)
            val inputValidation = checkInputFields(emailTextField, passwordTextField)

            if (inputValidation) { // If inputs are valid
                auth.signInWithEmailAndPassword( // Starts firebase sign in process using the given details
                    emailTextField.text.toString(),
                    passwordTextField.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) { // If the details are correct, successfully log in
                            Log.d(TAG, "signInWithEmail:success")

                            emailTextField.text.clear()
                            passwordTextField.text.clear()

                            val validatedLoginIntent = Intent(this, NewsActivity::class.java)
                            startActivity(validatedLoginIntent)

                        } else { // If details are not correct, display appropriate errors for user clarity
                            Log.w(TAG, "signInWithEmail:failure", it.exception)

                            emailTextField.setBackgroundResource(R.drawable.error_background)
                            passwordTextField.setBackgroundResource(R.drawable.error_background)

                            errorMsg.text = getString(R.string.no_account_found_by_these_details)
                        }
                    }
            }
        }

        googleLogin.setOnClickListener { // On "Sign in with Google" button being pressed
            // Gets Google ID details
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Don't filter by authorized accounts only

                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                // Gets the server client ID from properties, temporary approach to hide google client id on github

                .setAutoSelectEnabled(true) // Enable auto-selecting the account if available
                .build()

           val request: GetCredentialRequest = GetCredentialRequest.Builder()
               .addCredentialOption(googleIdOption) // Adds Google ID option to request
               .build()

            lifecycleScope.launch {
                try {
                    // Attempts to get credentials
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity
                    )

                    // If successful, handled in this function
                    handleGoogleSignIn(result)
                }
                catch (error: NoCredentialException) { // Handles case where no google accounts are found on the device.
                    Log.e(TAG, "Login error occurred: $error")
                    Toast.makeText(baseContext, "No google accounts found", Toast.LENGTH_SHORT).show()
                }
                catch (error: GetCredentialCustomException) { // Handles other errors
                    Log.e(TAG, "Login error occurred: $error")
                    Toast.makeText(baseContext, "Sorry, an error occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Checks the input fields to ensure that front-end input validation conditions are met
    private fun checkInputFields(emailAddressField: EditText, passwordField: EditText): Boolean {
        val emailInput = emailAddressField.text.toString().trim()
        val passwordInput = passwordField.text.toString().trim()

        when {
            emailInput.isEmpty() && passwordInput.isNotEmpty() -> { // If the email field is empty but the password field is not
                emailAddressField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.email_address_field_is_empty)

                passwordField.setBackgroundResource(R.drawable.rounded_corner)

                return false
            }
            emailInput.isNotEmpty() && passwordInput.isEmpty() -> { // If the password field is empty but the email field is not
                passwordField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.password_field_is_empty)

                emailAddressField.setBackgroundResource(R.drawable.rounded_corner)

                return false
            }
            emailInput.isEmpty() && passwordInput.isEmpty() -> { // If both fields are empty
                emailAddressField.setBackgroundResource(R.drawable.error_background)
                passwordField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.email_and_password_fields_are_empty)

                return false
            }
            else -> { // If none of the above conditions are true, then the inputs are valid
                emailAddressField.setBackgroundResource(R.drawable.rounded_corner)
                passwordField.setBackgroundResource(R.drawable.rounded_corner)
                errorMsg.text = ""

                return true
            }
        }
    }

    private suspend fun handleGoogleSignIn(result: GetCredentialResponse) {
        val credential = result.credential // Gets the credentials

        // Ensures that credential received is a CustomCredential
        if (credential !is CustomCredential) {
            Log.e(TAG, "Unexpected type of credential")
            return
        }

        // Attempts to parse the Google ID token from credential data
        val googleIdTokenCredential = try {
            withContext(Dispatchers.IO) {
                GoogleIdTokenCredential.createFrom(credential.data)
            }
        }
        catch (error: GoogleIdTokenParsingException) { // If parsing fails
            Log.e(TAG, "Received an invalid google id token response: $error")
            return
        }

        // Authenticate with Firebase using Google ID Token
        val authResult = try {
            FirebaseAuth.getInstance().signInWithCredential(
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            ).await() // Await the result of signInWithCredential
        }
        catch (error: Exception) {
            Log.e(TAG, "Firebase authentication failed: $error")
            return // If firebase auth fails
        }

        if (authResult.user != null) { // If firebase auth successful and user exists
            Log.d(TAG, "Successfully signed in with credentials!")

            val user = authResult.user!! // Retrieves the user details

            userDataManager.saveGoogleUserToFirestore(user) // Saves the user details into firestore

            // Starts the NewsActivity
            val googleLoginIntent = Intent(this, NewsActivity::class.java)
            startActivity(googleLoginIntent)
            finish() // Ends this activity
        }
        else {
            Log.e(TAG, "Error occurred signing in: $authResult")
            return
        }
    }

    // Save data on orientation change
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("emailInput", emailTextField.text.toString())
        outState.putString("passwordInput", passwordTextField.text.toString())
        outState.putString("errorMsg", errorMsg.text.toString())

        super.onSaveInstanceState(outState)
    }
}