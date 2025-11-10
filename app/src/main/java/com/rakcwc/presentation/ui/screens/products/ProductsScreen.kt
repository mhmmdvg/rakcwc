package com.rakcwc.presentation.ui.screens.products

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.screens.products.components.EmptyProductView
import com.rakcwc.presentation.ui.screens.products.components.ProductCard
import com.rakcwc.utils.shimmerEffect

@Composable
fun ProductsScreen(
    onScrollOffsetChanged: (Int) -> Unit = {},
    navigationTitle: @Composable (String) -> Unit = {},
    catalogId: String? = null,
    productVm: ProductsViewModel = hiltViewModel()
) {
    val productsState by productVm.catalogDetail.collectAsState()
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

    LaunchedEffect(catalogId) {
        catalogId?.let { productVm.getCatalogDetail(it) }
    }

    LaunchedEffect(totalScrollOffset.value) {
        onScrollOffsetChanged(totalScrollOffset.value)
    }

    Log.d("ProductsScreen", "ProductsScreen: ${productsState.data?.data}")

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {


        when (productsState) {
            is Resource.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .height(120.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerEffect()
                        )
                    }
                }

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

            is Resource.Success -> {
                val products = productsState.data?.data?.products ?: emptyList()

                item(span = { GridItemSpan(maxLineSpan) }) {
                    productsState.data?.data?.name?.let { navigationTitle(it) }
                }

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
                        EmptyProductView()
                    }
                }
            }

            is Resource.Error -> {

            }
        }
    }
}