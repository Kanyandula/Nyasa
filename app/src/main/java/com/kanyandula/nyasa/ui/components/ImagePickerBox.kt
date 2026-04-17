package com.kanyandula.nyasa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun ImagePickerBox(
    imageModel: Any?,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
    showPlaceholderText: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onPickImage),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Blog image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (imageModel == null && showPlaceholderText) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Select photo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                Text(
                    text = "Select Photo",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "High resolution (16:9) recommended",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = "Change image",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePickerBoxEmptyPreview() {
    NyasaTheme {
        ImagePickerBox(imageModel = null, onPickImage = {})
    }
}
