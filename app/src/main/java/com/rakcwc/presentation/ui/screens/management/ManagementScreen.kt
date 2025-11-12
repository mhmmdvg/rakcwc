package com.rakcwc.presentation.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakcwc.presentation.ui.screens.management.components.ProductsCardList

@Composable
fun ManagementScreen(
    route: String = "catalog-management",
    navigationTitle: (String) -> Unit = {},
) {
    val title = route
        .split("-")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() }  }

    LaunchedEffect(navigationTitle) {
        navigationTitle(title)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(82.dp))
        }

        items(10) {
            ProductsCardList()
        }
    }
}

@Preview
@Composable
fun PreviewManagementScreen() {
    ManagementScreen()
}