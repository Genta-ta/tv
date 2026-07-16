package id.fc.pratv.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val key: String, val label: String) {
    GELAP("gelap", "Gelap"),
    TERANG("terang", "Terang"),
    VSCODE("vscode", "VS Code"),
    GITHUB("github", "GitHub")
}

data class Palette(
    val editorBg: Color,
    val panelBg: Color,
    val activityBar: Color,
    val accent: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val border: Color
)

private val gelapPalette = Palette(
    editorBg = Color(0xFF1a1a2e),
    panelBg = Color(0xFF16213e),
    activityBar = Color(0xFF0f3460),
    accent = Color(0xFFe94560),
    textPrimary = Color(0xFFFFFFFF),
    textMuted = Color(0xFFb0b0b0),
    border = Color(0xFF0f3460)
)

private val terangPalette = Palette(
    editorBg = Color(0xFFf5f5f5),
    panelBg = Color(0xFFFFFFFF),
    activityBar = Color(0xFFe0e0e0),
    accent = Color(0xFFe94560),
    textPrimary = Color(0xFF1a1a2e),
    textMuted = Color(0xFF666666),
    border = Color(0xFFdddddd)
)

private val vscodePalette = Palette(
    editorBg = Color(0xFF1e1e1e),
    panelBg = Color(0xFF252526),
    activityBar = Color(0xFF333333),
    accent = Color(0xFF007acc),
    textPrimary = Color(0xFFcccccc),
    textMuted = Color(0xFF858585),
    border = Color(0xFF2d2d2d)
)

private val githubPalette = Palette(
    editorBg = Color(0xFF0d1117),
    panelBg = Color(0xFF161b22),
    activityBar = Color(0xFF21262d),
    accent = Color(0xFF58a6ff),
    textPrimary = Color(0xFFc9d1d9),
    textMuted = Color(0xFF8b949e),
    border = Color(0xFF30363d)
)

val LocalPalette = staticCompositionLocalOf { vscodePalette }

val VSCodeColors: Palette
    @Composable
    get() = LocalPalette.current


private fun schemeFor(mode: ThemeMode) = when (mode) {
    ThemeMode.GELAP -> darkColorScheme(
        primary = gelapPalette.accent,
        background = gelapPalette.editorBg,
        surface = gelapPalette.panelBg,
        onBackground = gelapPalette.textPrimary,
        onSurface = gelapPalette.textPrimary
    )
    ThemeMode.TERANG -> lightColorScheme(
        primary = terangPalette.accent,
        background = terangPalette.editorBg,
        surface = terangPalette.panelBg,
        onBackground = terangPalette.textPrimary,
        onSurface = terangPalette.textPrimary
    )
    ThemeMode.VSCODE -> darkColorScheme(
        primary = vscodePalette.accent,
        background = vscodePalette.editorBg,
        surface = vscodePalette.panelBg,
        onBackground = vscodePalette.textPrimary,
        onSurface = vscodePalette.textPrimary
    )
    ThemeMode.GITHUB -> darkColorScheme(
        primary = githubPalette.accent,
        background = githubPalette.editorBg,
        surface = githubPalette.panelBg,
        onBackground = githubPalette.textPrimary,
        onSurface = githubPalette.textPrimary
    )
}

private fun paletteFor(mode: ThemeMode) = when (mode) {
    ThemeMode.GELAP -> gelapPalette
    ThemeMode.TERANG -> terangPalette
    ThemeMode.VSCODE -> vscodePalette
    ThemeMode.GITHUB -> githubPalette
}

fun themeModeFromKey(key: String): ThemeMode =
    ThemeMode.values().firstOrNull { it.key == key } ?: ThemeMode.GELAP

@Composable
fun PratvTheme(
    mode: ThemeMode = ThemeMode.GELAP,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(mode)
    val selectionColors = TextSelectionColors(
        handleColor = palette.accent,
        backgroundColor = palette.accent.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(
        LocalPalette provides palette,
        LocalTextSelectionColors provides selectionColors
    ) {
        MaterialTheme(
            colorScheme = schemeFor(mode),
            content = content
        )
    }
}
