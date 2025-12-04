package com.deploy.accu_project.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deploy.accu_project.AcupunctureResponse
import com.deploy.accu_project.AcupunctureViewModel
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
    val searchResults by acupunctureViewModel.searchResults.collectAsState()
    val errorMessage by acupunctureViewModel.error.collectAsState()
    val showErrorSnackBar by acupunctureViewModel.showErrorSnackBar.collectAsState()
    val loading by acupunctureViewModel.loading.collectAsState()

    val authState by authViewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val scrollState = rememberScrollState()
    var isScrollingDown by remember { mutableStateOf(false) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) navController.navigate("login")
    }
    LaunchedEffect(scrollState.value) {
        isScrollingDown = scrollState.value > previousScrollOffset
        previousScrollOffset = scrollState.value
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (!isScrollingDown) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Healing Protocols",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { authViewModel.signout() }) {
                    Text("Sign out", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Pill-Shaped Search Bar
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
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                placeholder = { Text("Search (e.g., 'Liver', 'Headache')") },
                shape = RoundedCornerShape(50), // Pill Shape
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { navController.navigate("documentation") }) {
                    Text("How to use?", fontSize = 12.sp)
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // Updated List UI with Cards
            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults, key = { it.organ }) { result ->
                        AcupunctureResultCard(result)
                    }
                }
            } else if (searchQuery.isEmpty()) {
                // Friendly Empty State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp)
                        )
                        Text("Search for patterns, organs or symptoms", color = Color.Gray)
                    }
                }
            }
        }

        if (showErrorSnackBar) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = {
                    TextButton(onClick = {
                        acupunctureViewModel.search(searchQuery)
                        acupunctureViewModel.dismissSnackBar()
                    }) { Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer) }
                }
            ) { Text(text = errorMessage ?: "Error occurred") }
        }
    }
}

@Composable
fun AcupunctureResultCard(result: AcupunctureResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Organ Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = result.organ.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = result.organ,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Patterns List
            result.patterns.forEach { pattern ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = pattern.pattern,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Labelled Sections using Chip style
                    LabelChip(label = "Symptoms", content = pattern.symptoms.joinToString(", "))
                    Spacer(modifier = Modifier.height(4.dp))
                    LabelChip(label = "Treatment", content = pattern.treatment_points.joinToString(", "), isHighlight = true)
                }
            }
        }
    }
}

@Composable
fun LabelChip(label: String, content: String, isHighlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.Top // Corrected alignment
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.secondary else Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DocumentationPage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        TextButton(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Home", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "App Documentation",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DocSection(title = "Database Scope", content = "Currently covering 11 Major Organs.")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DocSection(title = "How to Search", content = "Type specific organs, patterns, or symptoms. Not case-sensitive.")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DocSection(title = "Abbreviations", content = "LV = Liver\nUB = Urinary Bladder\nGB = Gallbladder\nSI = Small Intestine\nSP = Spleen\nST = Stomach\nLU = Lung\nLI = Large Intestine\nKD = Kidney\nHT = Heart\nPC = Pericardium")
            }
        }
    }
}

@Composable
fun DocSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
    }
}
