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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.rakcwc.data.remote.resources.Resource
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

    LaunchedEffect(scrollState.value) {
        onScrollOffsetChanged(scrollState.value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White,
            )
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            navigationTitle()
        }

        // Categories Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
                    (1..4).chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }

                is Resource.Success -> {
                    val catalogs = homeState.data?.data ?: emptyList()

                    if (catalogs.isNotEmpty()) {
                        catalogs.chunked(2).forEach { catalog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                catalog.forEach { item ->
                                    CatalogCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(120.dp),
                                        data = item
                                    ) {
                                        navController?.navigate("catalog/${item.id}")
                                    }

                                    if (catalog.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        EmptyCatalogView(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                is Resource.Error -> {

                }
            }

        }
    }
}

