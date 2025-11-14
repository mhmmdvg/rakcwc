package com.rakcwc.presentation.ui.screens.detailproduct

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.icons.lucide.*
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun DetailProductScreen(
    viewModel: DetailProductViewModel = hiltViewModel(), navController: NavController
) {
    val state = viewModel.state.value
    var showFullDescription by remember { mutableStateOf(false) }

    Log.d("Detail", state.product.toString())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentColor)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.CircleAlert,
                            contentDescription = "Error",
                            tint = AccentColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = state.error, style = MaterialTheme.typography.bodyLarge, color = Color.Gray
                        )
                    }
                }
            }

            state.product != null -> {
                // MAIN BOX
                Box(modifier = Modifier.fillMaxSize()) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        // PRODUCT IMAGE
                        CacheImage(
                            imageUrl = state.product.imageUrl ?: "",
                            description = state.product.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            contentScale = ContentScale.Crop
                        )

                        // WHITE BACKGROUND CONTENT
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(24.dp)
                        ) {

                            // NAME
                            Text(
                                text = state.product.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // PRICE
                            Row(
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rp ${String.format("%,.0f", state.product.price)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // ATTRIBUTES
                            Row(
                                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Variant
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccentColor
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.product.variant,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Variant",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                // Price per unit
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF8E1)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Rp ${String.format("%,.0f", state.product.pricePerUnit)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Per Unit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Catalog
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.product.catalogId,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Catalog",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "About ${state.product.name}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val desc = state.product.description ?: "No description available"
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = if (showFullDescription) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (desc.length > 150) {
                                TextButton(onClick = { showFullDescription = !showFullDescription }) {
                                    Text(
                                        text = if (showFullDescription) "Read less.." else "Read more..",
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    // ----------------------------
                    // TOP ACTION BAR (OVERLAY)
                    // ----------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {

                            // LAYER 1 — Blur Background (behind the icon)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .blur(20.dp)
                                    .border(
                                        width = 1.dp, color = Color.White.copy(alpha = 0.25f), shape = CircleShape
                                    )
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        ambientColor = Color.White.copy(alpha = 0.35f),
                                        spotColor = Color.White.copy(alpha = 0.35f)
                                    )
                            )

                            // LAYER 2 — Your IconButton (NOT blurred)
                            IconButton(
                                onClick = { navController.popBackStack() }, modifier = Modifier.matchParentSize()
                            ) {
                                Icon(
                                    imageVector = Lucide.X, contentDescription = "Back", tint = Color.Black
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

// Unused util from your code
private fun Modifier.border(width: Dp, color: Color, shape: RoundedCornerShape): Modifier {
    return this.then(
        Modifier
            .background(color.copy(alpha = 0.1f), shape)
            .padding(width)
    )
}
