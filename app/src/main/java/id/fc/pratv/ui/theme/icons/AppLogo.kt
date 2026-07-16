package id.fc.pratv.ui.theme.Icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val PRATV_LOGO_PATH =
    "M3 2h14a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z " +
    "M4.5 4h13a.5.5 0 0 1.5.5v5a.5.5 0 0 1-.5.5H4.5a.5.5 0 0 1-.5-.5V4.5a.5.5 0 0 1.5-.5z " +
    "M9 5.5l5 2.8-5 2.8z " +
    "M9.5 13h5v1h-5zM10.5 14h3v1h-3z"

val PratvLogo: ImageVector by lazy {
    ImageVector.Builder(
        name = "PratvLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(PRATV_LOGO_PATH).toNodes(),
            fill = SolidColor(Color(0xFFe94560))
        )
    }.build()
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFe94560)
) {
    Image(
        imageVector = PratvLogo,
        contentDescription = "pranemanTV",
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint)
    )
}
