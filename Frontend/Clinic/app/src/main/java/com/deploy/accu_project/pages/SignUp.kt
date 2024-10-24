package com.deploy.accu_project.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Visibility
import androidx.navigation.NavController

@Composable //This is the signup page
fun SignUp(modifier: Modifier = Modifier,navController: NavController,authViewModel: AuthViewModel) {
    //Variables to hold the user input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState = authViewModel.authState.observeAsState() //Used to observe the authState of the user
    val context = LocalContext.current
    //This is used to navigate to the home page if the user is authenticated
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message,Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    //This is the layout of the signup page
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //This is the text at the top of the page
        Text(text = "Signup Page", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(16.dp))
        //This is the text fields for the user to enter their email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        //This is the text field for the user to enter their password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        //This is the text field for the user to confirm their password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Confirm Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        //This is the button to create an account
        Button(onClick = {
            if(password == confirmPassword){
                authViewModel.signup(email,password)}
            else{
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        },
            enabled = authState.value != AuthState.Loading //This is to disable the button if the user is loading
        ) {
            Text(text = "Create account")
        }
        Spacer(modifier = Modifier.height(8.dp))
        //This is the button to navigate to the login page
        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text(text = "Already have an account? Login")
        }
    }
}