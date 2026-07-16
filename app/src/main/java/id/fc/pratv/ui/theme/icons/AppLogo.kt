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
    "M4 3h16a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z " +
    "M7 8.5l6 3.5-6 3.5z"

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
