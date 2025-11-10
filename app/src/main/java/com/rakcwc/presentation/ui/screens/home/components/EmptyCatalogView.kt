package com.rakcwc.presentation.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.rakcwc.presentation.ui.theme.AccentColor

@Composable
fun EmptyCatalogView(
    modifier: Modifier = Modifier,
    onAddCatalog: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Lucide.Plus,
            contentDescription = "No catalogs",
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Catalogs Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first catalogs to organize\nyour products",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddCatalog,
            modifier = Modifier
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = "Add",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Create Catalog",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}