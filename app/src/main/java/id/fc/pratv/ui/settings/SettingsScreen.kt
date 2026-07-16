package id.fc.pratv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import id.fc.pratv.BuildConfig
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import id.fc.pratv.data.AppLogger
import id.fc.pratv.data.SettingsStore
import id.fc.pratv.data.repository.PlaylistRepository
import id.fc.pratv.ui.channels.ChannelsViewModel
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.ThemeMode
import id.fc.pratv.ui.theme.rememberResponsive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import id.fc.pratv.ui.theme.Icons.ArrowRightIcon
import id.fc.pratv.ui.theme.Icons.ClipboardIcon
import id.fc.pratv.ui.theme.Icons.CloseIcon
import id.fc.pratv.ui.theme.Icons.InfoIcon
import id.fc.pratv.ui.theme.Icons.PaletteIcon
import id.fc.pratv.ui.theme.Icons.SaveIcon
import id.fc.pratv.ui.theme.Icons.TvIcon
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(nav: NavController) {
    val context = LocalContext.current
    val r = rememberResponsive()
    val maxFieldWidth = minOf(LocalConfiguration.current.screenWidthDp.dp * 0.9f, 720.dp)
    val urls = remember { SettingsStore.getUrls(context).toMutableList() }
    var activeUrl by remember { mutableStateOf(SettingsStore.getActiveUrl(context)) }
    var newUrl by remember { mutableStateOf("") }
    var showLog by remember { mutableStateOf(SettingsStore.getShowLog(context)) }
    var autoSkip by remember { mutableStateOf(SettingsStore.getAutoSkip(context)) }
    var themeKey by remember { mutableStateOf(SettingsStore.getTheme(context)) }
    val channelsState by viewModel<ChannelsViewModel>().state.collectAsStateWithLifecycle()
    var logLines by remember { mutableStateOf(AppLogger.lines()) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(showLog) {
        if (!showLog) return@LaunchedEffect
        while (true) {
            logLines = AppLogger.lines()
            delay(1000)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(VSCodeColors.editorBg)
            .padding(
                horizontal = r.overscanH,
                vertical = r.overscanV
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Pengaturan",
                color = MaterialTheme.colorScheme.primary,
                fontSize = r.textTitle,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(r.spacingLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TvIcon(contentDescription = null, tint = VSCodeColors.accent, modifier = Modifier.size(r.textSubtitle.value.dp))
                Spacer(Modifier.width(r.spacingSmall))
                Text("Playlist M3U", color = VSCodeColors.textPrimary, fontSize = r.textSubtitle, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(r.spacingMedium))
            val presetLabels = mapOf(
                PlaylistRepository.DEFAULT_URL to "Playlist Utama (lengkap, ada channel V+ ber-DRM)",
                PlaylistRepository.OTT_URL to "Playlist OTT (HLS non-DRM, channel Indonesia jalan)"
            )
            urls.forEach { u ->
                val isPreset = SettingsStore.isPreset(u)
                val label = presetLabels[u] ?: u
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activeUrl = u
                            SettingsStore.setActiveUrl(context, u)
                            PlaylistRepository.clearCache()
                        }
                        .padding(vertical = r.spacingSmall)
                ) {
                    TvIcon(contentDescription = null, tint = VSCodeColors.textMuted, modifier = Modifier.size((r.textSubtitle.value * 0.9f).dp))
                    Spacer(Modifier.width(r.spacingSmall))
                    RadioButton(selected = u == activeUrl, onClick = {
                        activeUrl = u
                        SettingsStore.setActiveUrl(context, u)
                        PlaylistRepository.clearCache()
                    })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, color = VSCodeColors.textPrimary, fontSize = r.textSubtitle)
                        if (isPreset) {
                            Text(u, color = VSCodeColors.textMuted, fontSize = r.textByline)
                        }
                    }
                    if (!isPreset) {
                        Button(onClick = {
                            SettingsStore.removeUrl(context, u)
                            urls.remove(u)
                            if (activeUrl == u) activeUrl = urls.first()
                        }) {
                            Text("Hapus", fontSize = r.textButton)
                        }
                    }
                }
            }
            Spacer(Modifier.height(r.spacingSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newUrl,
                    onValueChange = { newUrl = it },
                    label = { Text("URL baru") },
                    modifier = Modifier.width(maxFieldWidth * 0.7f),
                    singleLine = true
                )
                Spacer(Modifier.width(r.spacingSmall))
                Button(onClick = {
                    val t = newUrl.trim()
                    if (t.isNotBlank()) {
                        SettingsStore.addUrl(context, t)
                        if (t !in urls) urls.add(t)
                        newUrl = ""
                    }
                }) {
                    Text("Tambah", fontSize = r.textButton)
                }
            }
            Spacer(Modifier.height(r.spacingLarge))

            HorizontalDivider(color = VSCodeColors.border)
            Spacer(Modifier.height(r.spacingLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PaletteIcon(contentDescription = null, tint = VSCodeColors.accent, modifier = Modifier.size(r.textSubtitle.value.dp))
                Spacer(Modifier.width(r.spacingSmall))
                Text("Tema", color = VSCodeColors.textPrimary, fontSize = r.textSubtitle, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(r.spacingMedium))
            ThemeMode.values().forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            themeKey = mode.key
                            SettingsStore.setTheme(context, mode.key)
                        }
                        .padding(vertical = r.spacingSmall)
                ) {
                    PaletteIcon(contentDescription = null, tint = VSCodeColors.textMuted, modifier = Modifier.size((r.textSubtitle.value * 0.9f).dp))
                    Spacer(Modifier.width(r.spacingSmall))
                    RadioButton(selected = mode.key == themeKey, onClick = {
                        themeKey = mode.key
                        SettingsStore.setTheme(context, mode.key)
                    })
                    Text(mode.label, color = VSCodeColors.textPrimary, fontSize = r.textSubtitle)
                }
            }
            Spacer(Modifier.height(r.spacingLarge))

            HorizontalDivider(color = VSCodeColors.border)
            Spacer(Modifier.height(r.spacingLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ClipboardIcon(contentDescription = null, tint = VSCodeColors.textMuted, modifier = Modifier.size((r.textSubtitle.value * 0.9f).dp))
                Spacer(Modifier.width(r.spacingSmall))
                Text(
                    text = "Tampilkan log",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = r.textSubtitle
                )
                Spacer(Modifier.width(r.spacingMedium))
                Switch(
                    checked = showLog,
                    onCheckedChange = {
                        showLog = it
                        SettingsStore.setShowLog(context, it)
                    }
                )
            }
            Spacer(Modifier.width(r.spacingMedium))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArrowRightIcon(contentDescription = null, tint = VSCodeColors.textMuted, modifier = Modifier.size((r.textSubtitle.value * 0.9f).dp))
                Spacer(Modifier.width(r.spacingSmall))
                Text(
                    text = "Auto-lanjut ke channel lain bila gagal",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = r.textSubtitle
                )
                Spacer(Modifier.width(r.spacingMedium))
                Switch(
                    checked = autoSkip,
                    onCheckedChange = {
                        autoSkip = it
                        SettingsStore.setAutoSkip(context, it)
                    }
                )
            }
            Spacer(Modifier.height(r.spacingLarge))
            if (showLog) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(r.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(r.spacingSmall)
                ) {
                    Text(
                        text = "Log / Diagnostik",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = r.textSubtitle,
                        fontWeight = FontWeight.Bold
                    )
                    val urlActive = SettingsStore.getActiveUrl(context)
                    LogRow("URL aktif", urlActive, r)
                    LogRow("Status", if (channelsState.isLoading) "Memuat…" else if (channelsState.error != null) "Gagal: ${channelsState.error}" else "Siap", r)
                    LogRow("Total channel", channelsState.totalChannels.toString(), r)
                    LogRow("Channel lokal terdeteksi", channelsState.localCount.toString(), r)
                    LogRow("Grup", channelsState.grouped.keys.size.toString(), r)
                    LogRow("EPG programme", channelsState.epgCount.toString(), r)
                    LogRow("EPG channel", channelsState.epgChannelCount.toString(), r)
                    Spacer(Modifier.height(r.spacingMedium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Logcat (live)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = r.textSubtitle,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Button(onClick = {
                                clipboard.setText(AnnotatedString(logLines.joinToString("\n")))
                            }) {
                                Text("Salin", fontSize = r.textButton)
                            }
                            Spacer(Modifier.width(r.spacingSmall))
                            Button(onClick = { AppLogger.clear(); logLines = emptyList() }) {
                                Text("Bersihkan", fontSize = r.textButton)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((r.textSubtitle.value * 14).dp)
                            .verticalScroll(rememberScrollState())
                            .padding(r.spacingSmall)
                    ) {
                        val text = if (logLines.isEmpty()) "— belum ada log —" else logLines.joinToString("\n")
                        SelectionContainer {
                            Text(
                                text = text,
                                color = VSCodeColors.textMuted,
                                fontSize = (r.textSubtitle.value * 0.8f).sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(r.spacingLarge))

            HorizontalDivider(color = VSCodeColors.border)
            Spacer(Modifier.height(r.spacingLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoIcon(contentDescription = null, tint = VSCodeColors.accent, modifier = Modifier.size(r.textSubtitle.value.dp))
                Spacer(Modifier.width(r.spacingSmall))
                Text(
                    "Tentang pranemanTV",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = r.textSubtitle,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(r.spacingMedium))
            LogRow("Versi", BuildConfig.VERSION_NAME, r)
            LogRow("Dibuat", BuildConfig.BUILD_TIME, r)
            Spacer(Modifier.height(r.spacingSmall))
            Text(
                "Bebas dipakai untuk keperluan pribadi. Untuk penggunaan komersial (dijual/belikan), wajib minta izin terlebih dahulu.",
                color = VSCodeColors.textMuted,
                fontSize = (r.textSubtitle.value * 0.9f).sp
            )
            Spacer(Modifier.height(r.spacingLarge))

            Row {
                Button(onClick = {
                    SettingsStore.setActiveUrl(context, activeUrl)
                    PlaylistRepository.clearCache()
                    nav.navigate("channels") {
                        popUpTo("channels") { inclusive = true }
                    }
                }) {
                    SaveIcon(contentDescription = null, modifier = Modifier.size((r.textButton.value * 1.1f).dp))
                    Spacer(Modifier.width(r.spacingSmall))
                    Text("Simpan", fontSize = r.textButton)
                }
                Spacer(Modifier.height(r.spacingMedium))
                Button(onClick = { nav.popBackStack() }) {
                    CloseIcon(contentDescription = null, modifier = Modifier.size((r.textButton.value * 1.1f).dp))
                    Spacer(Modifier.width(r.spacingSmall))
                    Text("Batal", fontSize = r.textButton)
                }
            }
        }
    }
}

@Composable
private fun LogRow(label: String, value: String, r: id.fc.pratv.ui.theme.Responsive) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(r.spacingSmall)
    ) {
        Text(
            text = "$label:",
            color = VSCodeColors.textMuted,
            fontSize = r.textSubtitle * 0.9f,
            modifier = Modifier.width(180.dp)
        )
        Text(
            text = value,
            color = VSCodeColors.textPrimary,
            fontSize = r.textSubtitle * 0.9f
        )
    }
}
