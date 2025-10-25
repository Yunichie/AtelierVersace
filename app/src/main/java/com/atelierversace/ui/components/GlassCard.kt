package com.atelierversace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = shape
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = shape
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            content()
        }
    }
}