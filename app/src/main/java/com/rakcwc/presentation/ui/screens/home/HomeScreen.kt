package com.rakcwc.presentation.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.components.EmptyState
import com.rakcwc.presentation.ui.components.ErrorState
import com.rakcwc.presentation.ui.screens.home.components.CatalogCard
import com.rakcwc.presentation.ui.screens.home.components.EmptyCatalogView
import com.rakcwc.utils.shimmerEffect

@Composable
fun HomeScreen(
    onScrollOffsetChanged: (Int) -> Unit = {},
    navigationTitle: @Composable () -> Unit = {},
    navController: NavController? = null,
    homeVm: HomeViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val homeState by homeVm.catalogs.collectAsState()

    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Calculate responsive values
    val columnsPerRow = when {
        screenWidth < 600.dp -> 2  // Phone portrait
        screenWidth < 840.dp -> 3  // Phone landscape / Small tablet
        screenWidth < 1200.dp -> 4 // Tablet
        else -> 5                   // Large tablet / Desktop
    }

    val horizontalPadding = when {
        screenWidth < 600.dp -> 16.dp
        screenWidth < 840.dp -> 24.dp
        else -> 32.dp
    }

    val cardSpacing = when {
        screenWidth < 600.dp -> 12.dp
        screenWidth < 840.dp -> 16.dp
        else -> 20.dp
    }

    LaunchedEffect(scrollState.value) {
        onScrollOffsetChanged(scrollState.value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            navigationTitle()
        }

        // Categories Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "Katalog",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            when (homeState) {
                is Resource.Loading -> {
                    // Show shimmer loading based on columns per row
                    (1..8).chunked(columnsPerRow).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = cardSpacing),
                            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
                        ) {
                            rowItems.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmerEffect()
                                )
                            }
                            // Fill remaining space if row is incomplete
                            repeat(columnsPerRow - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                is Resource.Success -> {
                    val catalogs = homeState.data?.data ?: emptyList()

                    if (catalogs.isNotEmpty()) {
                        catalogs.chunked(columnsPerRow).forEach { catalogRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = cardSpacing),
                                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
                            ) {
                                catalogRow.forEach { item ->
                                    CatalogCard(
                                        modifier = Modifier.weight(1f),
                                        data = item
                                    ) {
                                        navController?.navigate("catalog/${item.id}")
                                    }
                                }

                                // Fill remaining space if row is incomplete
                                repeat(columnsPerRow - catalogRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        EmptyState()
                    }
                }

                is Resource.Error -> {
                    ErrorState(
                        errorMessage = "Oops! Something Went Wrong",
                        errorDescription = homeState.message
                            ?: "We couldn't load the data. Please check your connection and try again.",
                        onRetryClick = {
                            homeVm.getCatalogs()
                        }
                    )
                }
            }
        }
    }
}