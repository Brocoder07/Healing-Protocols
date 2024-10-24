package com.deploy.accu_project

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.deploy.accu_project.pages.AuthViewModel
import com.deploy.accu_project.pages.Navigator
import com.deploy.accu_project.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {/*If device is android 12+ then
           do not show the custom splash screen*/ installSplashScreen()
        }
        else{
            null
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            MyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigator(modifier = Modifier.padding(innerPadding),authViewModel = authViewModel)
                }
            }
        }
    }
}