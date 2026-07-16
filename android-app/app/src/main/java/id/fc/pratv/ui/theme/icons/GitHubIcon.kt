package id.fc.pratv.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GITHUB_PATH = "M12 0C5.37 0 0 5.37 0 12c0 5.3 3.44 9.8 8.21 11.39.6.11 0.82-.27.82-.58 0-.28-.01-1.02-.02-2-.21-1.02 2-.7 2.42-1.64.13-.95.5-2.51.5-2.51 1.64-.7 2.73-1.86 2.73-1.86.55-1.34 1.34-1.7 1.34-1.7 1.06-.73 2.13-.2 2.13-.2.42 1.16.16 2.02.08 2.24.51 1.04 1.51 1.74 2.73 1.74.95 0 1.79-.07 2.49-.21.08-.61.31-1.21.56-1.66-2.2-.25-4.51-1.1-4.51-4.9 0-1.08.39-1.97 1.02-2.66-.1-.25-.44-1.26.1-2.63 0 0 1.65-.53 5.41 2.03a11.9 11.9 0 0 1 4.93 0c3.76-2.56 5.41-2.03 5.41-2.03.54 1.37.2 2.38.1 2.63.64.69 1.02 1.58 1.02 2.66 0 3.81-2.32 4.65-4.53 4.89.36.31.68.92.68 1.85 0 1.33-.01 2.41-.01 2.74 0 .31.21.68.82.56A12.01 12.01 0 0 0 24 12c0-6.63-5.37-12-12-12z"

val GitHubIcon: ImageVector by lazy(LazyThreadSafetyMode.PUBLICATION) {
    ImageVector.Builder(
        name = "GitHub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(GITHUB_PATH).toNodes(),
            fill = SolidColor(Color.Black)
        )
    }.build()
}
