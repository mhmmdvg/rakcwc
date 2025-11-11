package com.rakcwc.presentation.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakcwc.domain.models.UserInfo
import com.rakcwc.presentation.ui.components.CacheImage
import com.rakcwc.presentation.ui.theme.AccentColor
import com.rakcwc.utils.GetInitial

@Composable
fun ProfileSection(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    data: UserInfo? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Text(
                        text = "${data?.firstName} ${data?.lastName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.Black,
                    )
                    Text(
                        text = "@${data?.email?.split("@")[0]}",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = GetInitial.usersInitial("${data?.firstName} ${data?.lastName}"),
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor
                ),
            ) {
                Text(
                    text = "Edit",
                    color = Color.White,
                )
            }
        }
    }
}