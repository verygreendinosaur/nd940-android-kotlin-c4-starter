package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

class AuthenticationActivity : AppCompatActivity() {

    // region Lifecycle / Overrides

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (FirebaseAuth.getInstance().currentUser != null) {
            launchRemindersActivity()
        }

        setupLoginButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTH_REQUEST_CODE) {
            handleValidSignInResult(requestCode, resultCode, data)
        }
    }

    // endregion

    // region Methods

    private fun setupLoginButton() {
        val loginButton: View = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            launchSignIn()
        }
    }

    private fun launchSignIn() {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                AUTH_REQUEST_CODE
        )
    }

    private fun handleValidSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val response = IdpResponse.fromResultIntent(data)

        if (resultCode == Activity.RESULT_OK) {
            handleSignInSuccess()
        } else if (response != null) {
            handleSignInError()
        } else {
            // User abandoned the login flow. Do nothing at this time.
        }
    }

    private fun handleSignInSuccess() {
        launchRemindersActivity()
    }

    private fun handleSignInError() {
        Toast.makeText(applicationContext, ERROR_TEXT, Toast.LENGTH_LONG).show()
    }

    private fun launchRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    // endregion

    // region Constants

    companion object {
        const val AUTH_REQUEST_CODE = 100
        const val ERROR_TEXT = "Something went wrong -- please try again"
    }

    // endregion

}
