package id.fc.pratv.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import id.fc.pratv.ui.theme.Responsive
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.rememberResponsive
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.fc.pratv.data.LoadState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    nav: NavController,
    application: android.app.Application
) {
    val logos by LoadState.logos.collectAsStateWithLifecycle()
    val isLoading by LoadState.isLoading.collectAsStateWithLifecycle()
    val loadError by LoadState.error.collectAsStateWithLifecycle()
    val progress by LoadState.progress.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        LoadState.load(application)
    }

    val logoAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(800), label = "logoAlpha")
    val logoScale by animateFloatAsState(targetValue = 1f, animationSpec = tween(800), label = "logoScale")
    var bylineTarget by remember { mutableStateOf(0f) }
    val bylineAlpha by animateFloatAsState(targetValue = bylineTarget, animationSpec = tween(600), label = "bylineAlpha")

    LaunchedEffect(Unit) {
        delay(500)
        bylineTarget = 1f
    }

    LaunchedEffect(progress, loadError) {
        if (progress >= 1f && loadError == null) {
            delay(300)
            nav.navigate("welcome") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    val r = rememberResponsive()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VSCodeColors.editorBg, VSCodeColors.panelBg)
                )
            )
    ) {
        ChannelCarousel(logos = logos, responsive = r, modifier = Modifier.align(Alignment.Center))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = r.overscanH),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "pranemanTV",
                color = MaterialTheme.colorScheme.primary,
                fontSize = r.textLogo,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale)
            )
            Spacer(Modifier.height(r.spacingMedium))
            Text(
                text = "by vio",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = r.textByline,
                modifier = Modifier.alpha(bylineAlpha)
            )
            Spacer(Modifier.height(r.spacingMedium))
            if (loadError != null) {
                Text(
                    text = "Gagal memuat: ${loadError}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = r.textByline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(Modifier.height(r.spacingSmall))
                Button(onClick = { LoadState.load(application) }) {
                    Text("Coba lagi", fontSize = r.textButton)
                }
            } else if (isLoading) {
                Text(
                    text = "Memuat channel...",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = r.textByline,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (loadError != null) {
            Text(
                text = loadError ?: "Gagal memuat",
                color = MaterialTheme.colorScheme.primary,
                fontSize = r.textByline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.8f)
                    .padding(r.spacingLarge)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelCarousel(
    logos: List<String>,
    responsive: Responsive,
    modifier: Modifier = Modifier
) {
    val r = responsive
    if (logos.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { logos.size })
    val pageCount = logos.size

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            val next = (pagerState.currentPage + 1) % pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(r.channelItem * 1.3f)
            .alpha(0.22f),
        pageSpacing = 16.dp
    ) { page ->
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val base = logos[page]
            repeat(3) { idx ->
                AsyncImage(
                    model = if (idx == 0) base else logos[(page + idx) % pageCount],
                    contentDescription = null,
                    modifier = Modifier
                        .size(r.channelItem * 0.75f)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF444466), CircleShape)
                        .background(Color(0xFF22223a))
                )
            }
        }
    }
}
