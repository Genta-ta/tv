# Rencana: Fitur OTT (IPTV Player) — pranemanTV

**Tujuan:** Aplikasi bisa memutar playlist IPTV (M3U) dari URL
`https://raw.githubusercontent.com/dhasap/dhanytv/main/dhanytv.m3u`, menampilkan
daftar channel per kategori, dan memutar stream (HLS/DASH, termasuk ClearKey DRM).

**Alur:** `SplashActivity` (2 dtk) → `MainActivity` (welcome, tombol "Lihat Channel")
→ `ChannelsActivity` (grid per kategori, D-pad) → `PlayerActivity` (ExoPlayer).

**Prasyarat:** Terapkan dulu perubahan responsif TV yang sudah disetujui
(`styles.xml`, `dimens.xml` + bucket `w820dp`/`w1280dp`, `banner.xml`, manifest
banner/leanback) karena `activity_splash.xml` sudah dibuat namun butuh `dimens`
tersebut. (Lihat `.opencode/plans/tv-responsive-ui.md`.)

---

## 1. Dependencies — `app/build.gradle.kts`
Tambah:
- `androidx.media3:media3-exoplayer:1.3.1`
- `androidx.media3:media3-exoplayer-hls:1.3.1`
- `androidx.media3:media3-exoplayer-dash:1.3.1`
- `androidx.media3:media3-ui:1.3.1`
- `androidx.recyclerview:recyclerview:1.3.2`
- `io.coil-kt:coil:2.6.0` (load logo channel)

## 2. Manifest — `AndroidManifest.xml`
- `<uses-permission android:name="android.permission.INTERNET" />`
- `<application ... android:usesCleartextTraffic="true">` (ada stream `http://`)
- Daftarkan `ChannelsActivity` & `PlayerActivity` (`exported="false"`).
- `MainActivity` tetap (diluncurkan dari Splash, bukan launcher).

## 3. Data layer
**`data/model/Channel.kt`**
`data class Channel(val id: String?, val name: String, val logoUrl: String?,
val group: String?, val url: String, val drmType: String?, val drmKeyId: String?,
val drmKey: String?, val userAgent: String?, val referer: String?, val origin: String?)`
(jadikan `Serializable`/`Parcelable` untuk dikirim ke Player via Intent.)

**`data/remote/M3UParser.kt`**
`fun parse(text: String): List<Channel>`. Aturan:
- Baris `#EXTINF:-1 tvg-id=".." tvg-logo=".." group-title="..",Nama` → ambil atribut + nama.
- Baris berikutnya hingga URL:
  - `#EXTVLCOPT:http-user-agent=..`, `http-referrer=..`, `http-origin=..` → header.
  - `#KODIPROP:inputstream.adaptive.license_type=..` → `drmType` (clearkey/widevine).
  - `#KODIPROP:inputstream.adaptive.license_key=..` → `drmKeyId:drmKey` (format `keyId:key` atau JSON).
  - `#KODIPROP:inputstream.adaptive.stream_headers=Key=Val|Key=Val` → header.
  - `#EXTHTTP:{...}` → parse JSON header.
- Baris pertama non-`#` non-kosong = URL → finalisasi Channel.

**`data/remote/ClearKeyUtils.kt`**
`buildClearKeyJson(keyIdHex, keyHex)` → `{"keys":[{"kty":"oct","k":"<b64>","kid":"<b64>"}]}`
(Media3 ClearKey butuh base64). Handle juga bila `license_key` sudah JSON.

**`data/repository/PlaylistRepository.kt`**
`const PLAYLIST_URL = "https://raw.githubusercontent.com/dhasap/dhanytv/main/dhanytv.m3u"`
`suspend fun getChannels(): List<Channel>` — fetch via `HttpURLConnection`
(`Dispatchers.IO`, user-agent browser), parse, cache di memori.

## 4. UI — Welcome (existing `MainActivity`)
- `activity_main.xml`: tambah `Button` "Lihat Channel" (`@+id/buttonBrowse`, fokus D-pad).
- `MainUiState`/`MainViewModel`: tambah event `onBrowseClicked()` → callback ke Activity
  untuk `startActivity(ChannelsActivity)`.
- `MainActivity.kt`: wiring tombol + `focusedByDefault` pada salah satu tombol.

## 5. UI — Channels (grid per kategori)
**`ui/channels/ChannelsActivity.kt`** + **`activity_channels.xml`** (RecyclerView).
**`ui/channels/ChannelAdapter.kt`**:
- `ListItem` sealed: `Header(category)` | `ChannelItem(Channel)`.
- `GridLayoutManager` + `spanSizeLookup` (header = full span, item = 1).
- Item: logo (`ImageView.load(logoUrl)` Coil) + nama; `focusable`, background fokus.
- Klik → `PlayerActivity` dengan Channel (parcel).
**`ui/channels/ChannelsViewModel.kt`** + **`ChannelsUiState`**: load repo, kelompokkan
per `group`, ekspos `StateFlow<List<ListItem>>` + loading/error.
**Dimens**: tambah `channel_grid_span` (phone 2–3, TV 4–6) & ukuran item ke bucket dimens.

## 6. UI — Player
**`ui/player/PlayerActivity.kt`** + **`activity_player.xml`**
(`androidx.media3.ui.PlayerView`, `android:id/playerView`, fokusable).
- Build `MediaItem`: `MediaItem.Builder().setUri(url).setHttpHeaders(headers)
  .setDrmConfiguration(drm).build()` (header: User-Agent/Referer/Origin).
- ClearKey: `DrmConfiguration.Builder(C.CLEARKEY_UUID).setKeySet(buildClearKeyJson(...))`.
  Widevine (bila ada `license_uri`): `C.WIDEVINE_UUID` + `setLicenseUri` (best-effort).
- `ExoPlayer.Builder(this).build()` → `playerView.player` → `prepare()` + `play()`.
- `onPause/onStop`: `player.release()`. Tombol back → `finish()`.

## 7. Verifikasi
- `./gradlew assembleDebug` → BUILD SUCCESSFUL.
- Preview layout Channels di 360dp / 960dp / 1280dp (grid & fokus).
- Catatan: pemutaran penuh butuh perangkat/emu + jaringan; tidak bisa divalidasi
  otomatis di sini. EPG (dari `url-tvg`) di luar scope (fitur mendatang).
