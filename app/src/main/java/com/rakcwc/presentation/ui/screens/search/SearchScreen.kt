package com.rakcwc.presentation.ui.screens.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.components.EmptyState
import com.rakcwc.presentation.ui.components.ErrorState
import com.rakcwc.presentation.ui.screens.products.components.EmptyProductView
import com.rakcwc.presentation.ui.screens.products.components.ProductCard
import com.rakcwc.utils.shimmerEffect

@Composable
fun SearchScreen(
    onScrollOffsetChanged: (Int) -> Unit = {},
    navigationTitle: @Composable () -> Unit = {},
    searchVm: SearchViewModel = hiltViewModel()
) {
    val searchState by searchVm.products.collectAsState()
    val searchQuery = searchVm.searchQuery.value
    val lazyGridState = rememberLazyGridState()

    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Calculate responsive values
    val columns = when {
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

    val gridSpacing = when {
        screenWidth < 600.dp -> 12.dp
        screenWidth < 840.dp -> 16.dp
        else -> 20.dp
    }

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

    LaunchedEffect(totalScrollOffset.value) {
        onScrollOffsetChanged(totalScrollOffset.value)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = lazyGridState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            navigationTitle()
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = searchQuery.query,
                onValueChange = { searchVm.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search by name, code, or variant...") },
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = "Search",
                    )
                },
                trailingIcon = {
                    if (searchQuery.query.isNotEmpty()) {
                        IconButton(
                            onClick = { searchVm.onSearchQueryChanged("") }
                        ) {
                            Icon(
                                imageVector = Lucide.X,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f),
                    focusedBorderColor = Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }

        when(searchState) {
            is Resource.Loading -> {
                items(columns * 4) { // Show more shimmer items on larger screens
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // Maintain aspect ratio
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
                                    .fillMaxWidth(0.6f)
                                    .height(32.dp)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .shimmerEffect()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }

            is Resource.Success -> {
                val products = searchState.data?.data?.products ?: emptyList()

                if (products.isNotEmpty()) {
                    items(
                        items = products,
                        key = { it.id }
                    ) {
                        ProductCard(
                            data = it,
                            onClick = { Log.d("ProductCard", "ProductCard: Clicked") }
                        )
                    }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState()
                    }
                }
            }

            is Resource.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState {}
                }
            }
        }
    }
}