package id.fc.pratv.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import id.fc.pratv.data.LoadState
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.icons.GitHubIcon
import id.fc.pratv.ui.theme.rememberResponsive
import androidx.compose.material3.MaterialTheme

@Composable
fun WelcomeScreen(nav: NavController) {
    val focusRequester = remember { FocusRequester() }
    val r = rememberResponsive()
    val progress by LoadState.progress.collectAsStateWithLifecycle()
    val loadError by LoadState.error.collectAsStateWithLifecycle()
    val ready = progress >= 1f && loadError == null
    val uriHandler = LocalUriHandler.current
    val app = LocalContext.current.applicationContext as android.app.Application

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VSCodeColors.editorBg)
            .padding(
                horizontal = r.overscanH,
                vertical = r.overscanV
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "pranemanTV",
                color = MaterialTheme.colorScheme.primary,
                fontSize = r.textTitle,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(r.spacingMedium))
            Text(
                text = "Selamat datang di pranemanTV",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = r.textSubtitle
            )
            Spacer(Modifier.height(r.spacingLarge))
            Button(
                onClick = { nav.navigate("channels") },
                enabled = ready,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable()
            ) {
                Text(
                    text = if (ready) "Lihat Channel" else "Memuat…",
                    fontSize = r.textButton
                )
            }
            Spacer(Modifier.height(r.spacingMedium))
            Button(
                onClick = { nav.navigate("settings") }
            ) {
                Text(
                    text = "Pengaturan",
                    fontSize = r.textButton
                )
            }
            Spacer(Modifier.height(r.spacingLarge))
            Icon(
                GitHubIcon,
                contentDescription = "Repository GitHub",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .width(28.dp)
                    .clickable { uriHandler.openUri("https://github.com/Genta-ta/tv") }
            )
            if (loadError != null) {
                Spacer(Modifier.height(r.spacingMedium))
                Text(
                    text = "Gagal: $loadError",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = r.textByline
                )
                Spacer(Modifier.height(r.spacingSmall))
                Button(onClick = { LoadState.load(app) }) {
                    Text("Coba lagi", fontSize = r.textButton)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = r.overscanH, vertical = r.spacingLarge)
        ) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = VSCodeColors.border
            )
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
