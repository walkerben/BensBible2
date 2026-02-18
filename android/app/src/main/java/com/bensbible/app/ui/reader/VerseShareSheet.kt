package com.bensbible.app.ui.reader

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.bensbible.app.model.ShareFont
import com.bensbible.app.model.ShareGradient
import java.io.File

@Composable
fun VerseShareSheet(
    verses: List<Pair<Int, String>>,
    reference: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedGradient by remember { mutableStateOf(ShareGradient.NAVY) }
    var selectedFont by remember { mutableStateOf(ShareFont.SERIF) }
    var fontSize by remember { mutableFloatStateOf(28f) }

    val previewBitmap = remember(verses, reference, selectedGradient, selectedFont, fontSize) {
        renderShareImage(verses, reference, selectedGradient, selectedFont, fontSize)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Text(
                text = "Share Verse",
                style = MaterialTheme.typography.titleMedium
            )
            // Invisible spacer for centering
            TextButton(onClick = {}, enabled = false) {
                Text("Cancel", color = Color.Transparent)
            }
        }

        // Preview
        Image(
            bitmap = previewBitmap.asImageBitmap(),
            contentDescription = "Share preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(270.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(8.dp, RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gradient picker
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShareGradient.entries.forEach { gradient ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(gradient.colors)
                        )
                        .then(
                            if (selectedGradient == gradient)
                                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier.border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                        )
                        .clickable { selectedGradient = gradient }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Font picker
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Font",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(ShareFont.entries) { font ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedFont == font) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedFont = font }
                    ) {
                        Text(
                            text = font.displayName,
                            fontSize = 14.sp,
                            color = if (selectedFont == font) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Size slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Size",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 24f..56f,
                    steps = 15,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Text("A", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Share button
        Button(
            onClick = {
                val bitmap = renderShareImage(verses, reference, selectedGradient, selectedFont, fontSize)
                shareImage(context, bitmap)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Share", style = MaterialTheme.typography.titleSmall)
        }
    }
}

private fun shareImage(context: Context, bitmap: Bitmap) {
    val cacheDir = File(context.cacheDir, "shared_images")
    cacheDir.mkdirs()
    val file = File(cacheDir, "verse_share.png")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Verse"))
}
