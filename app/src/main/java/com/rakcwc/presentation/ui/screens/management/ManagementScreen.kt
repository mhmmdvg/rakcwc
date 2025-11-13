package com.rakcwc.presentation.ui.screens.management

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.ShoppingBag
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.Products
import com.rakcwc.presentation.ui.components.EmptyState
import com.rakcwc.presentation.ui.components.ErrorState
import com.rakcwc.presentation.ui.screens.management.components.ProductsCardList
import com.rakcwc.presentation.ui.screens.management.components.ProductsCardListSkeleton

@Composable
fun ManagementScreen(
    route: String = "catalog-management",
    navigationTitle: (String) -> Unit = {},
    managementVm: ManagementViewModel = hiltViewModel()
) {
    val title = route
        .split("-")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    val managementState by managementVm.managementState.collectAsState()

    LaunchedEffect(route) {
        navigationTitle(title)
        managementVm.getManagementData(route)
    }

    Log.d("ManagementScreen", "ManagementScreen: ${managementState.data}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (managementState) {
            is Resource.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(82.dp))
                    }

                    items(5) {
                        ProductsCardListSkeleton(
                            modifier = Modifier.fillParentMaxWidth(),
                            showPrice = "catalog" !in route
                        )
                    }
                }
            }

            is Resource.Success -> {
                val managementData = managementState.data?.data ?: emptyList()

                if (managementData.isEmpty()) {
                    // Empty State
                    EmptyState(
                        icon = if ("catalog" in route) Lucide.Package else Lucide.ShoppingBag,
                        title = if ("catalog" in route) "No Catalogs Yet" else "No Products Yet",
                        description = if ("catalog" in route)
                            "There are no catalogs available at the moment. Start by adding your first catalog."
                        else
                            "There are no products available at the moment. Start by adding your first product.",
                        actionText = "Refresh",
                        onActionClick = {
                            managementVm.getManagementData(route)
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(82.dp))
                        }

                        when {
                            "catalog" in route -> {
                                val catalogs = managementData.filterIsInstance<CatalogsResponse>()

                                items(
                                    items = catalogs,
                                    key = { it.id }
                                ) { catalog ->
                                    ProductsCardList(
                                        imageUrl = catalog.imageUrl ?: "",
                                        title = catalog.name,
                                        subTitle = catalog.name
                                    )
                                }
                            }

                            "product" in route -> {
                                val products = managementData.filterIsInstance<Products>()

                                items(
                                    items = products,
                                    key = { it.id }
                                ) { product ->
                                    ProductsCardList(
                                        imageUrl = product.imageUrl ?: "",
                                        title = product.name,
                                        subTitle = product.code,
                                        price = product.price
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is Resource.Error -> {
                // Error State
                ErrorState(
                    errorMessage = "Oops! Something Went Wrong",
                    errorDescription = managementState.message
                        ?: "We couldn't load the data. Please check your connection and try again.",
                    onRetryClick = {
                        managementVm.getManagementData(route)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewManagementScreen() {
    ManagementScreen()
}