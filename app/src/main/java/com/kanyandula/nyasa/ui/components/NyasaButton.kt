package com.kanyandula.nyasa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme

enum class ButtonStyle { Primary, Secondary, Destructive }

private val PillShape = RoundedCornerShape(50)

@Composable
fun NyasaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    when (style) {
        ButtonStyle.Primary -> PrimaryButton(text, onClick, modifier, enabled, loading, trailingIcon)
        ButtonStyle.Secondary -> SecondaryButton(text, onClick, modifier, enabled, loading, trailingIcon)
        ButtonStyle.Destructive -> DestructiveButton(text, onClick, modifier, enabled, loading)
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    val isActive = enabled && !loading
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val brush = remember(isActive, primary, primaryContainer) {
        val colors = listOf(primary, primaryContainer)
        val gradientColors = if (isActive) colors else colors.map { it.copy(alpha = 0.4f) }
        Brush.linearGradient(
            colors = gradientColors,
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = isActive,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(brush = brush, shape = PillShape),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(NyasaTheme.spacing.s))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !loading,
        shape = PillShape,
        border = BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleSmall)
            if (trailingIcon != null) {
                Spacer(Modifier.width(NyasaTheme.spacing.s))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !loading,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NyasaButtonPreview() {
    NyasaTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.m)
        ) {
            NyasaButton(
                text = "Login",
                onClick = {},
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )
            NyasaButton(text = "Register", onClick = {}, style = ButtonStyle.Secondary)
            NyasaButton(text = "Delete Account", onClick = {}, style = ButtonStyle.Destructive)
            NyasaButton(text = "Loading...", onClick = {}, loading = true)
        }
    }
}
