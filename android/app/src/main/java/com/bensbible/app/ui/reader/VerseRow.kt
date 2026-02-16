package com.bensbible.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bensbible.app.data.VerseAnnotationEntity
import com.bensbible.app.model.Verse
import com.bensbible.app.ui.theme.BookmarkIconColor
import com.bensbible.app.ui.theme.NoteIconColor
import com.bensbible.app.ui.theme.SelectedVerseBackground

@Composable
fun VerseRow(
    verse: Verse,
    isSelected: Boolean,
    annotation: VerseAnnotationEntity?,
    onTap: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> SelectedVerseBackground
        annotation?.highlightColor != null -> annotation.highlightColor!!.color
        else -> Color.Transparent
    }

    val annotatedText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Superscript,
                fontFamily = FontFamily.Serif,
                color = Color.Gray
            )
        ) {
            append("${verse.verse} ")
        }
        withStyle(
            SpanStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
        ) {
            append(verse.text)
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onTap() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = annotatedText,
            lineHeight = 28.sp,
            modifier = Modifier.weight(1f)
        )

        if (annotation?.isBookmarked == true) {
            Icon(
                Icons.Default.Bookmark,
                contentDescription = "Bookmarked",
                tint = BookmarkIconColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (annotation?.noteText != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.StickyNote2,
                contentDescription = "Has note",
                tint = NoteIconColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
