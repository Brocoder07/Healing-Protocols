package com.deploy.accu_project.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

//ViewModel class to manage authentication state using Firebase Authentication
class AuthViewModel : ViewModel() {
    //FirebaseAuth instance to handle authentication operations
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    //LiveData to expose the current authentication state
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    //Initialization block to check the authentication status when the ViewModel is created
    init {
        checkAuthStatus() //Check if the user is already logged in
    }
    //Function to check if the user is authenticated when the app starts
    private fun checkAuthStatus() {
        //If there's no current user, set state as unauthenticated; otherwise, authenticated
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    //Function to validate that only specific email domains can log in or sign up
    private fun isValidEmailDomain(email: String): Boolean {
        //Check if the email ends with the specified domain
        return email.endsWith("@gmail.com", ignoreCase = true)
    }
    //Function to handle user login with email and password
    fun login(email: String, password: String) {
        //Check if the email or password is empty, and show error if true
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }
        //Validate if the email belongs to the correct domain
        if (!isValidEmailDomain(email)) {
            _authState.value = AuthState.Error("Only Interns/Students/Practitioners of the clinic are allowed to use the app")
            return
        }
        //Set state to loading while trying to sign in
        _authState.value = AuthState.Loading
        //Firebase sign-in operation
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                //If sign-in is successful, set state to authenticated
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    //If sign-in fails, set an error message
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    //Function to handle user sign-up with email and password
    fun signup(email: String, password: String) {
        //Check if email or password is empty, and show error if true
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return
        }
        //Validate if the email belongs to the correct domain
        if (!isValidEmailDomain(email)) {
            _authState.value = AuthState.Error("Only Interns/Students/Practitioners of the clinic are allowed to use the app")
            return
        }
        //Set state to loading while trying to sign up
        _authState.value = AuthState.Loading
        //Firebase sign-up operation
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                //If sign-up is successful, set state to authenticated
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    //If sign-up fails, set an error message
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }
    //Function to sign out the user
    fun signout() {
        //Firebase sign-out operation
        auth.signOut()
        //Set state to unauthenticated after sign-out
        _authState.value = AuthState.Unauthenticated
    }
}
//Sealed class representing different authentication states
sealed class AuthState {
    //Represents an authenticated state
    data object Authenticated : AuthState()
    //Represents an unauthenticated state
    data object Unauthenticated : AuthState()
    //Represents a loading state during authentication actions
    data object Loading : AuthState()
    //Represents an error state with an error message
    data class Error(val message: String) : AuthState()
}