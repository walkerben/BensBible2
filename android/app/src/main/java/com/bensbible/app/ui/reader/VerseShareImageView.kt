package com.bensbible.app.ui.reader

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import com.bensbible.app.model.ShareFont
import com.bensbible.app.model.ShareGradient
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle

fun renderShareImage(
    verses: List<Pair<Int, String>>,
    reference: String,
    gradient: ShareGradient,
    shareFont: ShareFont,
    fontSize: Float
): Bitmap {
    val size = 1080
    val bitmap = ImageBitmap(size, size)
    val canvas = Canvas(bitmap)
    val nativeCanvas = canvas.nativeCanvas

    // Draw gradient background
    val colors = gradient.colors
    val gradientShader = AndroidLinearGradient(
        0f, 0f, size.toFloat(), size.toFloat(),
        intArrayOf(
            colorToArgb(colors[0]),
            colorToArgb(colors[1])
        ),
        null,
        Shader.TileMode.CLAMP
    )
    val bgPaint = android.graphics.Paint().apply {
        shader = gradientShader
    }
    nativeCanvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    val typeface = resolveTypeface(shareFont)
    val italicTypeface = Typeface.create(typeface, Typeface.ITALIC)
    val textArgb = colorToArgb(gradient.textColor)
    val accentArgb = colorToArgb(gradient.accentColor)

    // Open quote mark
    val quotePaint = TextPaint().apply {
        color = (accentArgb and 0x00FFFFFF) or 0x80000000.toInt()
        textSize = 72f * 3f
        this.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    nativeCanvas.drawText("\u201C", size / 2f, 280f, quotePaint)

    // Verse text
    val verseText = if (verses.size == 1) {
        verses[0].second
    } else {
        verses.joinToString(" ") { "${it.first} ${it.second}" }
    }

    val textPaint = TextPaint().apply {
        color = textArgb
        textSize = fontSize * 3f
        this.typeface = typeface
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.LEFT
    }

    val textWidth = size - 180
    val textLayout = StaticLayout.Builder
        .obtain(verseText, 0, verseText.length, textPaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(fontSize * 0.3f * 3f, 1f)
        .setIncludePad(false)
        .setMaxLines(20)
        .build()

    // Center the text block vertically in the available space (between quote and rule)
    val contentTop = 320f
    val contentBottom = size - 260f
    val availableHeight = contentBottom - contentTop
    val textHeight = textLayout.height.toFloat()
    val textY = contentTop + (availableHeight - textHeight) / 2f

    nativeCanvas.save()
    nativeCanvas.translate(90f, textY)
    textLayout.draw(nativeCanvas)
    nativeCanvas.restore()

    // Accent rule
    val ruleY = textY + textHeight + 40f
    val rulePaint = android.graphics.Paint().apply {
        color = (accentArgb and 0x00FFFFFF) or 0x66000000
        strokeWidth = 6f
        isAntiAlias = true
    }
    nativeCanvas.drawLine(
        (size - 180f) / 2f, ruleY,
        (size + 180f) / 2f, ruleY,
        rulePaint
    )

    // Reference text
    val refPaint = TextPaint().apply {
        color = accentArgb
        textSize = fontSize * 0.7f * 3f
        this.typeface = italicTypeface
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    nativeCanvas.drawText(reference, size / 2f, ruleY + 60f, refPaint)

    return bitmap.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
}

private fun colorToArgb(color: androidx.compose.ui.graphics.Color): Int {
    return android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}

private fun resolveTypeface(font: ShareFont): Typeface {
    val base = when (font.fontFamily) {
        FontFamily.Serif -> Typeface.SERIF
        FontFamily.SansSerif -> Typeface.SANS_SERIF
        FontFamily.Monospace -> Typeface.MONOSPACE
        else -> Typeface.SERIF
    }
    val style = if (font.fontStyle == FontStyle.Italic) Typeface.ITALIC else Typeface.NORMAL
    return Typeface.create(base, style)
}
