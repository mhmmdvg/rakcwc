package com.rakcwc.presentation.ui.screens.createcatalog

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.ImagePlus
import com.composables.icons.lucide.Lucide
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.components.ImageCropDialog
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun CreateCatalogScreen(
    navController: NavController,
    viewModel: CreateCatalogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val createState by viewModel.createState.collectAsState()
    var showCropDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            showCropDialog = true
        }
    }

    LaunchedEffect(createState) {
        if (createState is Resource.Success && createState.data != null) {
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

    Log.d("CreateCatalogScreen", "${createState.data}")

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
                text = "Catalog Image",
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
                    .clickable(enabled = state.uploadState !is Resource.Loading && createState !is Resource.Loading) {
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
                        // Edit overlay
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
                                text = "Tap to upload catalog image",
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

        // Catalog Name Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Catalog Name",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "e.g., Summer Collection 2024") },
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
                placeholder = { Text(text = "Add a brief description about this catalog...") },
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
        if (createState is Resource.Error) {
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
                    text = createState.message ?: "An error occurred",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Create Button
        Button(
            onClick = { viewModel.createCatalog() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                disabledContainerColor = AccentColor.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = createState !is Resource.Loading && state.uploadState !is Resource.Loading
        ) {
            if (createState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Catalog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}