package id.fc.pratv.ui.theme.Icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private fun vector(path: String, color: Color = Color(0xFFe94560)): ImageVector =
    ImageVector.Builder(
        name = "LocalIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            fill = SolidColor(color)
        )
    }.build()

private val TV by lazy {
    vector(
        "M3 6h18a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1zm2 2v8h14V8H5z " +
        "M8 11l3 2-3 2z"
    )
}

private val GEAR by lazy {
    vector(
        "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm9 4l-2-1 1-2-2-1-2 1-2-1-1-2-2 1v2l-2 1-1 2 1 2 2-1 2 1 1 2 2-1v-2l2-1 1-2z"
    )
}

private val INFO by lazy {
    vector("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z")
}

private val SAVE by lazy {
    vector(
        "M5 3h11l3 3v15H5V3zm2 2v4h8V5H7zm8 2h2l-2-2v2zm-7 8h6v2H8v-2z"
    )
}

private val CLOSE by lazy {
    vector("M6 6l12 12M18 6L6 18")
}

private val PALETTE by lazy {
    vector(
        "M12 3a9 9 0 0 0 0 18c1 0 2-1 2-2 0-1-1-2-1-2s1-2 2-2h2a4 4 0 0 0 4-4c0-5-4-9-9-9zm-4 9a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm0-5a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm5-2a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm5 3a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3z"
    )
}

private val CLIPBOARD by lazy {
    vector(
        "M9 4h6a1 1 0 0 1 1 1h2a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h2a1 1 0 0 1 1-1zm1 1v1h4V5h-4z"
    )
}

private val ARROW_RIGHT by lazy {
    vector("M5 12h12M13 6l6 6-6 6")
}

@Composable
fun TvIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = TV, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun GearIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = GEAR, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun InfoIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = INFO, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun SaveIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = SAVE, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun CloseIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = CLOSE, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun PaletteIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = PALETTE, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun ClipboardIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = CLIPBOARD, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))

@Composable
fun ArrowRightIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFe94560), contentDescription: String? = null) =
    Image(imageVector = ARROW_RIGHT, contentDescription = contentDescription, modifier = modifier, colorFilter = ColorFilter.tint(tint))
