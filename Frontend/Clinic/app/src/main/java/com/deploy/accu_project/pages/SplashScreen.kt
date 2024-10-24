package com.deploy.accu_project.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.deploy.accu_project.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.observeAsState()
    LaunchedEffect(Unit) {
        delay(3000) //3 seconds delay to show the splash screen
        when (authState) {
            is AuthState.Authenticated -> {
                //Navigate directly to home if the user is authenticated
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true } //Remove splash from backstack
                }
            }
            is AuthState.Unauthenticated -> {
                //Navigate to login if not authenticated
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true } //Remove splash from backstack
                }
            }
            else -> Unit
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.accu_logo), //Specifying the logo
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}
