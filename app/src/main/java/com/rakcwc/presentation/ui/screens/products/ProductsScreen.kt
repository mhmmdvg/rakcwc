package com.rakcwc.presentation.ui.screens.products

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakcwc.presentation.ui.components.EmptyState
import com.rakcwc.presentation.ui.components.ErrorState
import com.rakcwc.presentation.ui.components.FilterChip
import com.rakcwc.presentation.ui.screens.products.components.EmptyProductView
import com.rakcwc.presentation.ui.screens.products.components.ProductCard
import com.rakcwc.utils.shimmerEffect

@Composable
fun ProductsScreen(
    onScrollOffsetChanged: (Int) -> Unit = {},
    navigationTitle: (String) -> Unit = {},
    catalogId: String? = null,
    navController: NavController? = null,
    productVm: ProductsViewModel = hiltViewModel()
) {
    val uiState by productVm.uiState.collectAsState()
    val lazyGridState = rememberLazyGridState()

    val totalScrollOffset = remember {
        derivedStateOf {
            val firstVisibleItem = lazyGridState.layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstVisibleItem != null) {
                lazyGridState.firstVisibleItemIndex * firstVisibleItem.size.height +
                        lazyGridState.firstVisibleItemScrollOffset
            } else {
                0
            }
        }
    }

    // Detect when user scrolls near bottom to load more
    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            val layoutInfo = lazyGridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Trigger when user is 3 items away from bottom
            lastVisibleIndex >= totalItems - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && uiState.hasNextPage && !uiState.isLoadingMore) {
                Log.d("ProductsScreen", "Loading more products (page ${uiState.currentPage + 1})")
                productVm.loadNextPage()
            }
        }
    }

    LaunchedEffect(catalogId) {
        catalogId?.let { productVm.getCatalogDetail(it) }
    }

    LaunchedEffect(uiState.catalogName) {
        navigationTitle(uiState.catalogName)
    }

    LaunchedEffect(totalScrollOffset.value) {
        onScrollOffsetChanged(totalScrollOffset.value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 86.dp)
                .padding(horizontal = 20.dp),
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                uiState.isLoading -> {
                    // Initial loading state
                    items(8) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(32.dp)
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .shimmerEffect()
                                )
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }

                uiState.products.isNotEmpty() -> {
                    // Filter chips row
                    if (uiState.filters.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = uiState.filters,
                                    key = { it.name }
                                ) { filter ->
                                    Log.d("ProductsScreen", "Filter $filter")
                                    Log.d("ProductsScreen", "Selected ${uiState.selectedFilter}")
                                    FilterChip(
                                        label = "${filter.name}",
                                        isSelected = uiState.selectedFilter == filter.name,
                                        onClick = {
                                            productVm.applyFilter(filter.name)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Products grid
                    items(
                        items = uiState.products,
                        key = { it.id }
                    ) { product ->
                        ProductCard(
                            data = product,
                            onClick = { navController?.navigate("product/${product.id}") },
                        )
                    }

                    // Loading more indicator at bottom
                    if (uiState.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // End of list indicator
                    if (!uiState.hasNextPage && uiState.products.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No more products",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                uiState.products.isEmpty() && !uiState.isLoading -> {
                    // Empty state
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState() {
                            navController?.navigate("settings/product-management")
                        }
                    }
                }
            }

            // Error state
            if (uiState.error != null && uiState.products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState(
                        errorMessage = "Oops! Something Went Wrong",
                        errorDescription = uiState.error
                            ?: "We couldn't load the data. Please check your connection and try again.",
                    ) {
                        productVm.retry()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = Color.Red
                            )
                            // Optional: Add retry button
                            // Button(onClick = { productVm.retry() }) {
                            //     Text("Retry")
                            // }
                        }
                    }
                }
            }
        }
    }
}