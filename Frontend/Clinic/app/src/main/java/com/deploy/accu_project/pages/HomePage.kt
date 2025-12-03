package com.deploy.accu_project.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deploy.accu_project.AcupunctureResponse
import com.deploy.accu_project.AcupunctureViewModel
import com.deploy.accu_project.ui.theme.Blue
import com.deploy.accu_project.ui.theme.Green
import com.deploy.accu_project.ui.theme.Red
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    acupunctureViewModel: AcupunctureViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    // UPDATED: Using collectAsState for StateFlow
    val searchResults by acupunctureViewModel.searchResults.collectAsState()
    val errorMessage by acupunctureViewModel.error.collectAsState()
    val showErrorSnackBar by acupunctureViewModel.showErrorSnackBar.collectAsState()
    val loading by acupunctureViewModel.loading.collectAsState()

    // Auth is still LiveData in your AuthViewModel, so keep observeAsState here
    val authState = authViewModel.authState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val scrollState = rememberScrollState()
    var isScrollingDown by remember { mutableStateOf(false) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    LaunchedEffect(scrollState.value) {
        isScrollingDown = scrollState.value > previousScrollOffset
        previousScrollOffset = scrollState.value
    }
    LaunchedEffect(searchQuery) {
        val trimmedQuery = searchQuery.trim()
        if (trimmedQuery.isNotEmpty()) {
            acupunctureViewModel.search(trimmedQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (!isScrollingDown) {
            TextButton(onClick = {
                authViewModel.signout()
            }) {
                Text(text = "Sign out")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(500L)
                        if (searchQuery.isNotEmpty()) {
                            acupunctureViewModel.search(searchQuery.trim())
                        }
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
                },
                label = { Text(text = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
            TextButton(
                onClick = { navController.navigate("documentation") }
            ) {
                Text(text = "Read Documentation")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            if (searchResults.isNotEmpty()) {
                SearchResultsTable(searchResults)
            }
        }

        if (showErrorSnackBar) {
            Snackbar(
                action = {
                    TextButton(onClick = {
                        acupunctureViewModel.search(searchQuery)
                        acupunctureViewModel.dismissSnackBar()
                    }) {
                        Text("Retry")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = errorMessage ?: "Unknown error occurred")
            }
        }
    }
}

@Composable
fun DocumentationPage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back", color = Color.Blue)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Number of Organs: 11")
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "How to Search: You can search for specific organs/patterns/symptoms just make sure to completely type the word/words you are searching for in the search bar (Not case sensitive)")
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "LV for Liver, UB for Urinary bladder, GB for Gallbladder, SI for Small intestine, SP for Spleen, ST for Stomach, LU for Lung, LI for Large intestine, KD for Kidney, HT for Heart, PC for Pericardium")
    }
}

@Composable
fun SearchResultsTable(results: List<AcupunctureResponse>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pattern", Modifier.weight(1f), fontSize = 14.sp, color = Red)
                Text("Symptoms", Modifier.weight(2f), fontSize = 14.sp, color = Blue)
                Text("Treatment Points", Modifier.weight(2f), fontSize = 14.sp, color = Green)
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Added key for performance optimization
        items(results, key = { it.organ }) { result ->
            result.patterns.forEach { pattern ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pattern.pattern,
                        Modifier.weight(1f),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(end = 8.dp)
                    ) {
                        Text(text = pattern.symptoms.joinToString(", "), fontSize = 16.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(end = 8.dp)
                    ) {
                        Text(text = pattern.treatment_points.joinToString(", "), fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
