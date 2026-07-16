package id.fc.pratv.ui.channels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.model.isLocal
import id.fc.pratv.data.remote.EpgParser
import id.fc.pratv.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChannelsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val grouped: Map<String, List<Channel>> = emptyMap(),
    val totalChannels: Int = 0,
    val localCount: Int = 0,
    val epgCount: Int = 0,
    val epgChannelCount: Int = 0,
    val epg: Map<String, List<EpgParser.Programme>> = emptyMap()
)

class ChannelsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ChannelsUiState())
    val state: StateFlow<ChannelsUiState> = _state.asStateFlow()

    init { load() }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val channels = PlaylistRepository.getChannels(getApplication(), force)
                val local = channels.filter { it.isLocal() }
                val rest = channels.filterNot { it.isLocal() }
                val grouped = mutableMapOf<String, List<Channel>>().apply {
                    if (local.isNotEmpty()) put("Lokal", local)
                    putAll(rest.groupBy { it.group ?: "Lainnya" }.toSortedMap())
                }
                val epg = runCatching {
                    PlaylistRepository.getEpg(getApplication(), force)
                }.getOrDefault(emptyMap())
                _state.value = _state.value.copy(
                    isLoading = false,
                    grouped = grouped,
                    totalChannels = channels.size,
                    localCount = local.size,
                    epgCount = epg.values.sumOf { it.size },
                    epgChannelCount = epg.size,
                    epg = epg
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat playlist"
                )
            }
        }
    }

    fun currentProgramme(channelId: String?): EpgParser.Programme? {
        if (channelId == null) return null
        val now = System.currentTimeMillis()
        return state.value.epg[channelId]?.firstOrNull { it.startMs <= now && now < it.stopMs }
    }

    private val _pendingNext = MutableStateFlow<Channel?>(null)
    val pendingNext: StateFlow<Channel?> = _pendingNext.asStateFlow()

    fun requestNextFrom(current: Channel) {
        val grouped = state.value.grouped
        val group = current.group ?: "Lainnya"
        val list = grouped[group].orEmpty()
        val idx = list.indexOfFirst { it.url == current.url }
        val next = if (idx >= 0 && idx + 1 < list.size) list[idx + 1] else list.firstOrNull()
        _pendingNext.value = if (next != null && next.url != current.url) next else null
    }

    fun consumePendingNext() {
        _pendingNext.value = null
    }
}
