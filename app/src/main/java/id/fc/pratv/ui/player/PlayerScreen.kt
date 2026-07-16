package id.fc.pratv.ui.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavController
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.SettingsStore
import id.fc.pratv.ui.channels.ChannelsViewModel
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.rememberResponsive

@Composable
fun PlayerScreen(
    channel: Channel,
    nav: NavController,
    viewModel: PlayerViewModel = viewModel(),
    channelsViewModel: ChannelsViewModel = viewModel()
) {
    val player by viewModel.player.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val diagnostic by viewModel.diagnostic.collectAsStateWithLifecycle()
    val retrying by viewModel.retrying.collectAsStateWithLifecycle()
    val r = rememberResponsive()
    val ctx = LocalContext.current
    var isLive by remember { mutableStateOf(false) }
    val autoSkip = remember { SettingsStore.getAutoSkip(ctx) }

    DisposableEffect(Unit) {
        onDispose { viewModel.release() }
    }

    LaunchedEffect(channel?.url) {
        isLive = false
        viewModel.prepare(channel)
    }

    LaunchedEffect(error) {
        if (error != null && autoSkip) {
            kotlinx.coroutines.delay(1500)
            channelsViewModel.requestNextFrom(channel)
            nav.popBackStack()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (player != null) {
            val exo = player
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = exo
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        controllerShowTimeoutMs = 3000
                        setShowFastForwardButton(false)
                        setShowRewindButton(false)
                        exo?.addListener(object : androidx.media3.common.Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                isLive = exo?.isCurrentMediaItemLive() ?: false
                            }
                            override fun onTimelineChanged(
                                timeline: androidx.media3.common.Timeline,
                                reason: Int
                            ) {
                                isLive = exo?.isCurrentMediaItemLive() ?: false
                            }
                        })
                    }
                },
                update = {
                    it.player = exo
                    isLive = exo?.isCurrentMediaItemLive() ?: false
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isLive && player != null && error == null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(r.spacingMedium)
                    .background(VSCodeColors.accent.copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text("LIVE", color = Color.White, fontSize = r.textByline, fontWeight = FontWeight.Bold)
            }
        }

        if (error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(r.spacingMedium)
            ) {
                Text(
                    text = "Tidak dapat memutar channel",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = r.channelNameText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = r.textSubtitle,
                    textAlign = TextAlign.Center
                )
                if (diagnostic != null) {
                    Text(
                        text = diagnostic ?: "",
                        color = VSCodeColors.textMuted,
                        fontSize = r.textSubtitle * 0.9f,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(r.spacingSmall)
                    )
                }
                Button(onClick = { viewModel.retry(channel) }) {
                    Text("Coba lagi")
                }
                Button(onClick = {
                    channelsViewModel.requestNextFrom(channel)
                    nav.popBackStack()
                }) {
                    Text("Channel lain")
                }
            }
        } else if (player == null && !retrying) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}
