package com.atelierversace.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atelierversace.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.4f),
    borderColor: Color = Periwinkle.copy(alpha = 0.4f),
    borderWidth: Dp = 1.5.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = shape
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
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
                    width = borderWidth,
                    color = borderColor,
                    shape = shape
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            content()
        }
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            ElectricSapphire,
            Cornflower
        )
    ),
    content: @Composable RowScope.() -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = Color.Transparent,
        borderColor = ElectricSapphire.copy(alpha = 0.3f),
        borderWidth = 1.5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, shape = RoundedCornerShape(20.dp))
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

@Composable
fun OutlinedGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Cornflower.copy(alpha = 0.5f),
    content: @Composable RowScope.() -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = Color.White.copy(alpha = 0.3f),
        borderColor = borderColor,
        borderWidth = 1.5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

@Composable
fun GlassSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.5f),
    borderColor: Color = Periwinkle.copy(alpha = 0.5f),
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun GlassDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Periwinkle.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            BabyBlueIce.copy(alpha = 0.6f)
        } else {
            Color.White.copy(alpha = 0.6f)
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) Cornflower.copy(alpha = 0.7f) else Periwinkle.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) ElectricSapphire else TextPrimary
        )
    }
}

@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElectricSapphire.copy(alpha = 0.15f),
    borderColor: Color = ElectricSapphire.copy(alpha = 0.4f),
    textColor: Color = ElectricSapphire
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isActive: Boolean = false,
    activeColor: Color = ElectricSapphire,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        onClick = onClick,
        modifier = modifier.size(size),
        backgroundColor = if (isActive) {
            activeColor.copy(alpha = 0.2f)
        } else {
            Color.White.copy(alpha = 0.5f)
        },
        borderColor = if (isActive) {
            activeColor.copy(alpha = 0.6f)
        } else {
            Periwinkle.copy(alpha = 0.5f)
        }
    ) {
        content()
    }
}

@Composable
fun GlassIconContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElectricSapphire.copy(alpha = 0.15f),
    borderColor: Color = ElectricSapphire.copy(alpha = 0.4f),
    size: Dp = 40.dp,
    cornerRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.size(size)
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ElectricSapphire.copy(alpha = 0.3f)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        content()
    }
}

@Composable
fun GlassHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LightCyan.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                content()
            }
        }
    }
}