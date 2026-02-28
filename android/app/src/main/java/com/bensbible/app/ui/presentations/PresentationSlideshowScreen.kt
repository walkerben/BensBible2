package com.bensbible.app.ui.presentations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bensbible.app.data.PresentationSlideEntity

@Composable
fun PresentationSlideshowScreen(
    slides: List<PresentationSlideEntity>,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (slides.isEmpty()) {
            Text(
                "No slides",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val slide = slides[currentIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / slides.size },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = slide.reference,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = slide.verseText,
                    color = Color.White,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp)
                ) {
                    FilledIconButton(
                        onClick = { currentIndex-- },
                        enabled = currentIndex > 0,
                        modifier = Modifier.padding(8.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous",
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    Text(
                        "${currentIndex + 1} / ${slides.size}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )

                    FilledIconButton(
                        onClick = { currentIndex++ },
                        enabled = currentIndex < slides.size - 1,
                        modifier = Modifier.padding(8.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            contentDescription = "Next",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }

        // Done button top-right
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Done", color = Color.White)
        }
    }
}
