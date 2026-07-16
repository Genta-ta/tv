package id.fc.pratv.data

import android.app.Application
import id.fc.pratv.data.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

object LoadState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _logos = MutableStateFlow<List<String>>(emptyList())
    val logos: StateFlow<List<String>> = _logos.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val mutex = Mutex()
    private var started = false

    fun load(app: Application) {
        scope.launch {
            val shouldRun = mutex.withLock {
                if (started) false else { started = true; true }
            }
            if (!shouldRun) return@launch
                try {
                    _isLoading.value = true
                    _error.value = null
                    _progress.value = 0f
                    val result = withTimeoutOrNull(45000L) {
                        try {
                            _progress.value = 0.05f
                            val channels = PlaylistRepository.getChannels(app, force = false) { p ->
                                _progress.value = 0.05f + 0.6f * p
                            }
                            _progress.value = 0.65f
                            _logos.value = channels.mapNotNull { it.logoUrl }.distinct().take(40)
                            PlaylistRepository.getEpg(app) { p ->
                                _progress.value = 0.65f + 0.3f * p
                            }
                            _progress.value = 1f
                            true
                        } catch (e: Exception) {
                            AppLogger.e("Splash", "load failed: ${e.message}")
                            _error.value = e.message ?: "Gagal memuat playlist"
                            false
                        }
                    }
                    if (result == null && _error.value == null) {
                        _error.value = "Waktu muat habis (45 dtk)"
                    }
                    _isLoading.value = false
                } finally {
                mutex.withLock { started = false }
            }
        }
    }
}
