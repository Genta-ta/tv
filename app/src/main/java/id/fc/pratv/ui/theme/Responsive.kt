package id.fc.pratv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

data class Responsive(
    val scale: Float,
    val textLogo: TextUnit,
    val textTitle: TextUnit,
    val textSubtitle: TextUnit,
    val textButton: TextUnit,
    val textByline: TextUnit,
    val categoryText: TextUnit,
    val channelNameText: TextUnit,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val overscanH: Dp,
    val overscanV: Dp,
    val channelItem: Dp,
    val panelFraction: Float,
    val gridSpan: Int
)

@Composable
fun rememberResponsive(): Responsive {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val heightDp = LocalConfiguration.current.screenHeightDp
    val isPortrait = heightDp >= widthDp

    return remember(widthDp, heightDp) {
        val ref = if (isPortrait) widthDp else heightDp
        val scale = min(max(ref / 1280f, 0.45f), 2.2f)

        val gridSpan = when {
            widthDp < 420 -> 2
            widthDp < 600 -> 3
            widthDp < 840 -> 4
            widthDp < 1080 -> 5
            widthDp < 1440 -> 7
            else -> 9
        }

        val panelFraction = when {
            widthDp < 600 -> 0.42f
            widthDp < 1080 -> 0.36f
            else -> 0.30f
        }

        Responsive(
            scale = scale,
            textLogo = (56 * scale).sp,
            textTitle = (48 * scale).sp,
            textSubtitle = (24 * scale).sp,
            textButton = (20 * scale).sp,
            textByline = (14 * scale).sp,
            categoryText = (16 * scale).sp,
            channelNameText = (28 * scale).sp,
            spacingSmall = (8 * scale).dp,
            spacingMedium = (16 * scale).dp,
            spacingLarge = (32 * scale).dp,
            overscanH = (16 * scale).dp,
            overscanV = (16 * scale).dp,
            channelItem = (96 * scale).dp,
            panelFraction = panelFraction,
            gridSpan = gridSpan
        )
    }
}
