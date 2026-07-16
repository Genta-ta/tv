package id.fc.pratv.ui.channels

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.model.isLocal
import id.fc.pratv.data.SettingsStore
import id.fc.pratv.data.repository.PlaylistRepository
import id.fc.pratv.ui.player.PlayerViewModel
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.rememberResponsive

@Composable
fun ChannelsScreen(
    nav: NavController,
    viewModel: ChannelsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingNext by viewModel.pendingNext.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val activeUrl = SettingsStore.getActiveUrl(ctx)
    val previewVm = viewModel<PlayerViewModel>()
    var selected by remember { mutableStateOf<Channel?>(null) }
    var lastClicked by remember { mutableStateOf<Channel?>(null) }
    var clickCount by remember { mutableStateOf(0) }
    val r = rememberResponsive()

    fun navigateToPlayer(channel: Channel) {
        previewVm.release()
        nav.navigate("player")
        nav.currentBackStackEntry?.savedStateHandle?.set("channel", channel)
    }

    LaunchedEffect(pendingNext) {
        pendingNext?.let { ch ->
            viewModel.consumePendingNext()
            navigateToPlayer(ch)
        }
    }

    LaunchedEffect(activeUrl) {
        PlaylistRepository.clearCache()
        viewModel.load(force = true)
    }

    fun handleActivate(channel: Channel) {
        if (channel == lastClicked) {
            clickCount++
            if (clickCount >= 2) {
                clickCount = 0
                lastClicked = null
                navigateToPlayer(channel)
            }
        } else {
            lastClicked = channel
            clickCount = 1
            selected = channel
        }
    }

    LaunchedEffect(state.grouped) {
        if (selected == null) {
            selected = state.grouped.values.firstOrNull()?.firstOrNull()
        }
        if (selected != null) {
            val stillThere = state.grouped.values.any { list -> list.any { it.url == selected?.url } }
            if (!stillThere) selected = null
        }
    }

    BackHandler(enabled = true) { nav.popBackStack() }

    Row(
        Modifier
            .fillMaxSize()
            .background(VSCodeColors.editorBg)
            .padding(vertical = r.spacingSmall)
    ) {
        Sidebar(
            onSettings = { nav.navigate("settings") },
            r = r,
            modifier = Modifier
                .fillMaxHeight()
                .width(r.channelItem * 1.1f)
                .background(VSCodeColors.activityBar)
                .padding(vertical = r.spacingMedium)
        )

            HorizontalDivider(
            color = VSCodeColors.border,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        ChannelList(
            grouped = state.grouped,
            r = r,
            onActivate = { handleActivate(it) },
            onFocus = { selected = it },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.42f)
                .background(VSCodeColors.panelBg)
                .padding(horizontal = r.spacingSmall)
        )

                HorizontalDivider(
            color = VSCodeColors.border,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        PlayerPreview(
            channel = selected,
            playerViewModel = previewVm,
            channelsViewModel = viewModel(),
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .background(VSCodeColors.panelBg)
                .padding(r.spacingMedium)
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        state.error?.let {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = it, color = VSCodeColors.accent)
            }
        }
    }
}

@Composable
private fun Sidebar(
    onSettings: () -> Unit,
    r: id.fc.pratv.ui.theme.Responsive,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .clickable { onSettings() }
                .padding(vertical = r.spacingMedium),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Pengaturan",
                    tint = if (focused) VSCodeColors.accent else VSCodeColors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
                AnimatedVisibility(
                    visible = focused,
                    enter = fadeIn() + slideInHorizontally { -it },
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Pengaturan",
                        color = VSCodeColors.textPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelList(
    grouped: Map<String, List<Channel>>,
    r: id.fc.pratv.ui.theme.Responsive,
    onActivate: (Channel) -> Unit,
    onFocus: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(r.spacingSmall * 0.5f)
    ) {
        grouped.forEach { (group, items) ->
            item {
                Text(
                    text = group,
                    color = VSCodeColors.textPrimary,
                    fontSize = r.categoryText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = r.spacingMedium)
                )
            }
            items(items = items, key = { it.url }) { ch ->
                ChannelItem(
                    channel = ch,
                    onActivate = { onActivate(ch) },
                    onFocus = { onFocus(it) }
                )
            }
        }
    }
}

@Composable
private fun PlayerPreview(
    channel: Channel?,
    playerViewModel: PlayerViewModel,
    channelsViewModel: ChannelsViewModel,
    modifier: Modifier = Modifier
) {
    val player by playerViewModel.player.collectAsStateWithLifecycle()
    val r = rememberResponsive()
    var previewedUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(channel) {
        val url = channel?.url
        if (url != null && url != previewedUrl) {
            previewedUrl = url
            playerViewModel.prepare(channel)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(r.spacingMedium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (player != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setUseController(false)
                        }
                    },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = channel?.let { "Memuat…" } ?: "Pilih channel",
                    color = VSCodeColors.textMuted
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(r.spacingSmall)
        ) {
            Text(
                text = channel?.name ?: "",
                color = VSCodeColors.textPrimary,
                fontSize = r.channelNameText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            channel?.let {
                if (it.isLocal()) {
                    Text(text = "Lokal", color = VSCodeColors.accent, fontSize = r.textByline)
                }
                val prog = channelsViewModel.currentProgramme(it.id)
                prog?.let { p ->
                    Text(
                        text = p.title,
                        color = VSCodeColors.accent,
                        fontSize = r.categoryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatEpgTime(p.startMs, p.stopMs),
                        color = VSCodeColors.textMuted,
                        fontSize = r.textByline
                    )
                }
            }
        }
    }
}

private fun formatEpgTime(startMs: Long, stopMs: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getDefault()
    return "${sdf.format(java.util.Date(startMs))} - ${sdf.format(java.util.Date(stopMs))}"
}
