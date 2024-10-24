package com.deploy.accu_project.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

//Navigator for handling app navigation
@Composable
fun Navigator(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    //Navigation Controller
    val navController = rememberNavController()
    //Defining navigation graph
    NavHost(navController = navController, startDestination = "splash", builder = {
        composable("splash") {
            SplashScreen(navController, authViewModel) //Splash Screen
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel) //Login Screen
        }
        composable("signup") {
            SignUp(modifier, navController, authViewModel) //Sign Up Screen
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel) //Home Screen after login/signup
        }
        composable("documentation") {
            DocumentationPage(navController) //Documentation page
        }
    })
}