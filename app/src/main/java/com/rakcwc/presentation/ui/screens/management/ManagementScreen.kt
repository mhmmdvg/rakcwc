package com.rakcwc.presentation.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.ShoppingBag
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.Products
import com.rakcwc.presentation.ui.components.EmptyState
import com.rakcwc.presentation.ui.components.ErrorState
import com.rakcwc.presentation.ui.screens.management.components.ProductsCardListSkeleton
import com.rakcwc.presentation.ui.screens.management.components.SwipeableProductsCardList

@Composable
fun ManagementScreen(
    route: String = "catalog-management",
    navController: NavController? = null,
    navigationTitle: (String) -> Unit = {},
    managementVm: ManagementViewModel = hiltViewModel()
) {
    val title = route
        .split("-")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    val managementState by managementVm.managementState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }
    var deleteItemId by remember { mutableStateOf("") }
    var deleteItemName by remember { mutableStateOf("") }

    LaunchedEffect(route) {
        navigationTitle(title)
        managementVm.getManagementData(route)
    }

    LaunchedEffect(Unit) {
        if ("catalog" in route) {
            managementVm.refreshData()
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete ${if ("catalog" in route) "Catalog" else "Product"}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"$deleteItemName\"? This action cannot be undone.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Call delete API
                        // managementVm.deleteCatalog(deleteItemId) or deleteProduct(deleteItemId)
                        when(route) {
                            "catalog" -> {
                                managementVm.deleteManagement(deleteItemId, "catalog")
                            }
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

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
                                    SwipeableProductsCardList(
                                        imageUrl = catalog.imageUrl ?: "",
                                        title = catalog.name ?: "",
                                        subTitle = "${catalog.products?.size ?: 0} products",
                                        onClick = {
                                            // Navigate to catalog detail
                                            navController?.navigate("catalog-detail/${catalog.id}")
                                        },
                                        onEdit = {
                                            // Navigate to edit catalog
                                            navController?.navigate("edit_catalog/${catalog.id}")
                                        },
                                        onDelete = {
                                            itemToDelete = catalog
                                            deleteItemId = catalog.id
                                            deleteItemName = catalog.name ?: ""
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            "product" in route -> {
                                val products = managementData.filterIsInstance<Products>()

                                items(
                                    items = products,
                                    key = { it.id }
                                ) { product ->
                                    SwipeableProductsCardList(
                                        imageUrl = product.imageUrl ?: "",
                                        title = product.name ?: "",
                                        subTitle = product.code ?: "",
                                        price = product.price,
                                        onClick = {
                                            // Navigate to product detail
                                            navController?.navigate("product-detail/${product.id}")
                                        },
                                        onEdit = {
                                            // Navigate to edit product
                                            navController?.navigate("edit-product/${product.id}")
                                        },
                                        onDelete = {
                                            itemToDelete = product
                                            deleteItemId = product.id
                                            deleteItemName = product.name ?: ""
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is Resource.Error -> {
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