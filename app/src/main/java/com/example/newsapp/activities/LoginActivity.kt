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

        if (savedInstanceState != null) {
            val emailText = savedInstanceState.getString("emailInput")
            val passwordText = savedInstanceState.getString("passwordInput")
            val errorText = savedInstanceState.getString("errorMsg")

            emailTextField.setText(emailText)
            passwordTextField.setText(passwordText)
            errorMsg.text = errorText
        }

        ssoInfo.setOnClickListener {
            ssoPopUpWindow.showPopUpWindow(it)
        }

        signUpRedirect.setOnClickListener {
            val redirectIntent = Intent(this, AccCreationActivity::class.java)
            startActivity(redirectIntent)
        }

        loginBtn.setOnClickListener {
            val inputValidation = checkInputFields(emailTextField, passwordTextField)

            if (inputValidation) {
                auth.signInWithEmailAndPassword(
                    emailTextField.text.toString(),
                    passwordTextField.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")

                            emailTextField.text.clear()
                            passwordTextField.text.clear()

                            val validatedLoginIntent = Intent(this, NewsActivity::class.java)
                            startActivity(validatedLoginIntent)

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", it.exception)

                            emailTextField.setBackgroundResource(R.drawable.error_background)
                            passwordTextField.setBackgroundResource(R.drawable.error_background)

                            errorMsg.text = getString(R.string.no_account_found_by_these_details)
                        }
                    }
            }
        }

        googleLogin.setOnClickListener {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity
                    )

                    handleGoogleSignIn(result)
                }
                catch (error: NoCredentialException) {
                    Log.e(TAG, "Login error occurred: $error")
                    Toast.makeText(baseContext, "No google accounts found", Toast.LENGTH_SHORT).show()
                }
                catch (error: GetCredentialCustomException) {
                    Log.e(TAG, "Login error occurred: $error")
                    Toast.makeText(baseContext, "Sorry, an error occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkInputFields(emailAddressField: EditText, passwordField: EditText): Boolean {
        val emailInput = emailAddressField.text.toString().trim()
        val passwordInput = passwordField.text.toString().trim()

        when {
            emailInput.isEmpty() && passwordInput.isNotEmpty() -> {
                emailAddressField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.email_address_field_is_empty)

                passwordField.setBackgroundResource(R.drawable.rounded_corner)

                return false
            }
            emailInput.isNotEmpty() && passwordInput.isEmpty() -> {
                passwordField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.password_field_is_empty)

                emailAddressField.setBackgroundResource(R.drawable.rounded_corner)

                return false
            }
            emailInput.isEmpty() && passwordInput.isEmpty() -> {
                emailAddressField.setBackgroundResource(R.drawable.error_background)
                passwordField.setBackgroundResource(R.drawable.error_background)
                errorMsg.text = getString(R.string.email_and_password_fields_are_empty)

                return false
            }
            else -> {
                emailAddressField.setBackgroundResource(R.drawable.rounded_corner)
                passwordField.setBackgroundResource(R.drawable.rounded_corner)
                errorMsg.text = ""

                return true
            }
        }
    }

    private suspend fun handleGoogleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        if (credential !is CustomCredential) {
            Log.e(TAG, "Unexpected type of credential")
            return
        }

        val googleIdTokenCredential = try {
            withContext(Dispatchers.IO) {
                GoogleIdTokenCredential.createFrom(credential.data)
            }
        }
        catch (error: GoogleIdTokenParsingException) {
            Log.e(TAG, "Received an invalid google id token response: $error")
            return
        }

        val authResult = try {
            FirebaseAuth.getInstance().signInWithCredential(
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            ).await()
        }
        catch (error: Exception) {
            Log.e(TAG, "Firebase authentication failed: $error")
            return
        }

        if (authResult.user != null) {
            Log.d(TAG, "Successfully signed in with credentials!")

            val user = authResult.user!!

            userDataManager.saveGoogleUserToFirestore(user)

            val googleLoginIntent = Intent(this, NewsActivity::class.java)
            startActivity(googleLoginIntent)
            finish()
        }
        else {
            Log.e(TAG, "Error occurred signing in: $authResult")
            return
        }
    }

    override fun onBackPressed() {
        // If the login activity is active, we want to skip to the next activity (NewsActivity) without showing login
        val intent = Intent(this, NewsActivity::class.java)
        startActivity(intent)
        finish()  // Optional: to remove the current activity from the stack
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("emailInput", emailTextField.text.toString())
        outState.putString("passwordInput", passwordTextField.text.toString())
        outState.putString("errorMsg", errorMsg.text.toString())

        super.onSaveInstanceState(outState)
    }
}
