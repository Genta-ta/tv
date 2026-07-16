# pranemanTV

Aplikasi IPTV untuk **Android TV** (Leanback / TV) yang dibangun dengan Jetpack Compose dan Media3 (ExoPlayer). Mendukung beberapa playlist M3U, EPG, tema yang bisa diubah, dan antarmuka yang ramah layar lebar (TV).

> Bebas dipakai untuk keperluan pribadi. Untuk penggunaan komersial (dijual/belikan), wajib minta izin terlebih dahulu.

## Fitur

- **Multi-playlist M3U** — preset Playlist Utama (lengkap, ada channel V+ ber-DRM) dan Playlist OTT (HLS non-DRM, channel Indonesia jalan), plus bisa menambah URL sendiri.
- **EPG** — panduan acara per channel (diambil dari `url-tvg` di playlist).
- **4 tema** — Terang, Gelap, Sepia, dan Sistem. Tema bisa diubah langsung dari layar Pengaturan dan berlaku seketika (reaktif).
- **Antarmuka ramah TV / buta huruf** — daftar channel dengan **logo besar di atas + nama di bawah** (vertical), serta fallback ikon TV bila logo tidak ada.
- **Pemutar tanpa kontrol bawaan** — `PlayerView` diatur `useController = false`; bila gagal memutar, tersedia tombol **Coba lagi** dan **Channel lain** di tengah layar.
- **Auto-lanjut** — bila sebuah channel gagal dan opsi diaktifkan, aplikasi otomatis pindah ke channel berikutnya.
- **Progress muat akurat** — pengunduhan playlist/EPG dilaporkan secara streaming (berdasar `Content-Length`), tampil sebagai progress bar di layar Splash.
- **Transisi adaptif refresh-rate** — durasi animasi navigasi disesuaikan dengan refresh-rate layar (mis. 60 Hz vs 120 Hz).
- **Ikon vektor** — logo pranemanTV dan ikon layar Pengaturan dibuat sebagai `ImageVector` Compose (tanpa dependensi ikon pihak ketiga).
- **Launcher & banner** — ikon aplikasi dan banner (untuk homescreen Android TV) berbasis vektor.

## Persyaratan

- Android TV / perangkat dengan layar lebar (Leanback).
- Kotlin + Jetpack Compose.
- Android Gradle Plugin & Gradle sesuai versi di project.
- Koneksi internet untuk mengambil playlist/EPG (kecuali channel lokal).

## Cara build

```bash
./gradlew :app:assembleDebug      # APK debug
./gradlew :app:assembleRelease    # APK release (perlu signing)
```

Hasil build ada di `app/build/outputs/apk/`.

> Build offline didukung bila dependensi sudah pernah diunduh (`--offline`).

## Struktur singkat

```
app/src/main/java/id/fc/pratv/
├── MainActivity.kt            # NavHost + transisi antar layar
├── data/
│   ├── LoadState.kt          # progress muat (channel -> EPG) + carousel logo
│   ├── SettingsStore.kt      # preferensi (URL, tema, log, auto-skip) + flow tema
│   ├── remote/               # M3UParser, EpgParser
│   └── repository/           # PlaylistRepository (fetch streaming + cache)
└── ui/
    ├── splash/   SplashScreen.kt
    ├── welcome/ WelcomeScreen.kt
    ├── channels/ ChannelsScreen.kt, ChannelItem.kt
    ├── player/   PlayerScreen.kt
    ├── settings/ SettingsScreen.kt
    └── theme/   PratvTheme, RefreshRate, icons/
```

## Catatan lisensi konten

Playlist, logo channel, dan EPG merupakan milik pihak ketiga yang disediakan oleh sumber eksternal. Aplikasi ini hanya menampilkan ulang data tersebut; pengguna bertanggung jawab atas penggunaannya sesuai ketentuan masing-masing sumber.

## Tentang

pranemanTV — oleh **vio**.
