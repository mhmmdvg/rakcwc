package com.rakcwc.presentation.ui.screens.createproduct

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.icons.lucide.*
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.components.ImageCropDialog
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun CreateEditProductScreen(
    navController: NavController,
    productId: String? = null,
    catalogId: String? = null,
    catalogName: String? = null,
    screenTitle: (String) -> Unit = {},
    viewModel: CreateEditProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val catalogsState by viewModel.catalogsState.collectAsState()

    var showCropDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val isEditMode = productId != null

    // Set catalog if coming from catalog detail screen
    LaunchedEffect(catalogId, catalogName) {
        if (catalogId != null && catalogName != null) {
            viewModel.setCatalog(catalogId, catalogName)
        }
    }

    // Load product data if in edit mode
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
            screenTitle("Edit Product")
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            showCropDialog = true
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is Resource.Success && saveState.data != null) {
            navController.popBackStack()
        }
    }

    if (showCropDialog && selectedImageUri != null) {
        ImageCropDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                showCropDialog = false
                selectedImageUri = null
            },
            onCropComplete = { croppedUri ->
                viewModel.onImageSelected(croppedUri)
                showCropDialog = false
                selectedImageUri = null
            }
        )
    }

    // Catalog Selector Dialog
    if (state.showCatalogSelector) {
        CatalogSelectorDialog(
            catalogsState = catalogsState,
            selectedCatalogId = state.catalogId,
            onDismiss = { viewModel.toggleCatalogSelector() },
            onCatalogSelected = { catalog ->
                viewModel.setCatalog(catalog.id, catalog.name ?: "")
            }
        )
    }

    // Show loading overlay when loading product in edit mode
    if (state.isLoadingProduct) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentColor)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 86.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Image Upload Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Product Image",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(
                        width = 2.dp,
                        color = if (state.imageError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                            alpha = 0.5f
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = state.uploadState !is Resource.Loading && saveState !is Resource.Loading) {
                        imagePicker.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.uploadState is Resource.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { state.uploadProgress },
                                modifier = Modifier.size(48.dp),
                                color = AccentColor,
                                strokeWidth = 4.dp,
                            )
                            Text(
                                text = "${(state.uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Uploading image...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    state.imageUrl != null -> {
                        CacheImage(
                            imageUrl = state.imageUrl!!,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.Camera,
                                    contentDescription = "Change image",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Tap to change",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Lucide.ImagePlus,
                                contentDescription = "Add image",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Tap to upload product image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Recommended: 1:1 ratio (square)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (state.imageError != null) {
                Text(
                    text = state.imageError!!,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Catalog Selector
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Catalog",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = if (state.catalogError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                            alpha = 0.8f
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = saveState !is Resource.Loading) {
                        viewModel.toggleCatalogSelector()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.catalogName.isNotEmpty()) state.catalogName else "Select a catalog",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.catalogName.isNotEmpty()) Color.Black else Color.Gray,
                        fontWeight = if (state.catalogName.isNotEmpty()) FontWeight.Medium else FontWeight.Normal
                    )
                    Icon(
                        imageVector = Lucide.ChevronDown,
                        contentDescription = "Select catalog",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (state.catalogError != null) {
                Text(
                    text = state.catalogError!!,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Product Name Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Product Name",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "e.g., Iced Coffee Latte") },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (state.nameError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                        alpha = 0.8f
                    ),
                    focusedBorderColor = if (state.nameError != null) Color(0xFFD32F2F) else Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                isError = state.nameError != null,
                supportingText = if (state.nameError != null) {
                    {
                        Text(
                            text = state.nameError!!,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else null
            )
        }

        // Variant Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Variant",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = state.variant,
                onValueChange = { viewModel.onVariantChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "e.g., Medium, Large, Chocolate") },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (state.variantError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                        alpha = 0.8f
                    ),
                    focusedBorderColor = if (state.variantError != null) Color(0xFFD32F2F) else Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                isError = state.variantError != null,
                supportingText = if (state.variantError != null) {
                    {
                        Text(
                            text = state.variantError!!,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else null
            )
        }

        // Product Code Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Product Code",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = state.code,
                onValueChange = { viewModel.onCodeChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "e.g., PRD-001") },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (state.codeError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                        alpha = 0.8f
                    ),
                    focusedBorderColor = if (state.codeError != null) Color(0xFFD32F2F) else Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                isError = state.codeError != null,
                supportingText = if (state.codeError != null) {
                    {
                        Text(
                            text = state.codeError!!,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else null
            )
        }

        // Price Fields Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price Per Unit
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Price Per Unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = state.pricePerUnit,
                    onValueChange = { viewModel.onPricePerUnitChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "10000") },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (state.pricePerUnitError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                            alpha = 0.8f
                        ),
                        focusedBorderColor = if (state.pricePerUnitError != null) Color(0xFFD32F2F) else Color.Gray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        errorBorderColor = Color(0xFFD32F2F),
                        errorContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = state.pricePerUnitError != null,
                    supportingText = if (state.pricePerUnitError != null) {
                        {
                            Text(
                                text = state.pricePerUnitError!!,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else null
                )
            }

            // Price
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = state.price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "10000") },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (state.priceError != null) Color(0xFFD32F2F) else Color.LightGray.copy(
                            alpha = 0.8f
                        ),
                        focusedBorderColor = if (state.priceError != null) Color(0xFFD32F2F) else Color.Gray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        errorBorderColor = Color(0xFFD32F2F),
                        errorContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = state.priceError != null,
                    supportingText = if (state.priceError != null) {
                        {
                            Text(
                                text = state.priceError!!,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else null
                )
            }
        }

        // Description Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Description (Optional)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text(text = "Add a brief description about this product...") },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f),
                    focusedBorderColor = Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )
        }

        // General Error Message
        if (saveState is Resource.Error) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.CircleAlert,
                    contentDescription = "Error",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = saveState.message ?: "An error occurred",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = { viewModel.saveProduct() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                disabledContainerColor = AccentColor.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = saveState !is Resource.Loading && state.uploadState !is Resource.Loading
        ) {
            if (saveState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isEditMode) "Update Product" else "Create Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CatalogSelectorDialog(
    catalogsState: Resource<List<com.rakcwc.domain.models.CatalogsResponse>>,
    selectedCatalogId: String?,
    onDismiss: () -> Unit,
    onCatalogSelected: (com.rakcwc.domain.models.CatalogsResponse) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Catalog",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                when (catalogsState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentColor)
                        }
                    }

                    is Resource.Success -> {
                        val catalogs = catalogsState.data ?: emptyList()

                        if (catalogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Lucide.FolderOpen,
                                        contentDescription = "No catalogs",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "No catalogs available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(catalogs) { catalog ->
                                    CatalogItem(
                                        catalog = catalog,
                                        isSelected = catalog.id == selectedCatalogId,
                                        onClick = {
                                            onCatalogSelected(catalog)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.CircleAlert,
                                    contentDescription = "Error",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = catalogsState.message ?: "Failed to load catalogs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItem(
    catalog: com.rakcwc.domain.models.CatalogsResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) AccentColor.copy(alpha = 0.1f) else Color.White
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AccentColor else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Catalog Icon or Image
            if (catalog.imageUrl != null) {
                CacheImage(
                    imageUrl = catalog.imageUrl!!,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Folder,
                        contentDescription = "Catalog",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = catalog.name ?: "Unnamed Catalog",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (catalog.description != null && catalog.description!!.isNotBlank()) {
                    Text(
                        text = catalog.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = "Selected",
                tint = AccentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}