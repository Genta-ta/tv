# Rencana: UI Responsif untuk TV (tetap dukung ponsel)

**Tujuan:** UI menyesuaikan ukuran layar karena target TV, tanpa kehilangan
dukungan ponsel. Pendekatan *responsif ringan*: tetap AppCompat/Material,
`LinearLayout` → `ConstraintLayout`, ukuran pakai `dimens` per bucket resolusi,
margin aman overscan, dan styling fokus D-pad. App tetap diinstal di ponsel
(`leanback` `required="false"` + launcher `LEANBACK_LAUNCHER` sebagai launcher kedua).

## File yang diubah / dibuat

### 1. `app/src/main/AndroidManifest.xml`
- Tambah `<uses-feature android:name="android.software.leanback" android:required="false" />`.
- `<application android:banner="@drawable/banner" ...>` + theme `@style/Theme.Pratv`.
- Activity dapat 2 intent-filter:
  - `MAIN` + `LAUNCHER` (ponsel)
  - `MAIN` + `LEANBACK_LAUNCHER` (TV)

### 2. `app/src/main/res/values/styles.xml` (baru)
`Theme.Pratv` (parent `Theme.MaterialComponents.NoActionBar`), dark:
`colorPrimary #e94560`, `colorPrimaryDark #1a1a2e`, `android:windowBackground #1a1a2e`.
Tanpa action bar (cocok TV & ponsel).

### 3. `app/src/main/res/layout/activity_main.xml`
Ubah ke `androidx.constraintlayout.widget.ConstraintLayout`, center vertikal
(title → subtitle → button, `chainStyle="packed"`). Ukuran & spacing pakai
`@dimen/*`. Akar pakai `@dimen/overscan_horizontal` / `@dimen/overscan_vertical`
sebagai padding aman overscan. Button:
`android:focusable="true"`, `android:focusedByDefault="true"`,
`android:background="@drawable/button_background"`, `android:textColor="#ffffff"`.

### 4. `app/src/main/res/drawable/button_background.xml` (baru)
Selector: default vs `state_focused` (solid `#e94560`, stroke putih 3dp, radius 8dp)
agar fokus remote D-pad terlihat jelas.

### 5. `app/src/main/res/drawable/banner.xml` (baru)
Vector 320×180, bg `#1a1a2e` + aksen `#e94560`. Wajib untuk homescreen TV.

### 6. `app/src/main/res/values/dimens.xml` (baru, default/ponsel)
- title 48sp, subtitle 24sp, button 20sp
- spacing_medium 16dp, spacing_large 32dp
- overscan_horizontal 16dp, overscan_vertical 16dp

### 7. `app/src/main/res/values-w820dp/dimens.xml` (baru, TV 720p/1080p ≈ 853–960dp)
- title 64sp, subtitle 32sp, button 28sp
- spacing_medium 24dp, spacing_large 48dp
- overscan 48dp / 48dp

### 8. `app/src/main/res/values-w1280dp/dimens.xml` (baru, TV 4K ≈ 1280dp)
- title 96sp, subtitle 48sp, button 40sp
- spacing_medium 36dp, spacing_large 72dp
- overscan 96dp / 96dp

### 9. `app/build.gradle.kts`
Tambah `androidx.constraintlayout:constraintlayout:2.1.4`.

### 10. `app/src/main/java/id/fc/pratv/ui/main/MainActivity.kt`
Tidak berubah (view binding tetap jalan; fokus tombol otomatis via `focusedByDefault`).

## Verifikasi
- `./gradlew assembleDebug` harus BUILD SUCCESSFUL.
- Cek preview layout di width 360dp, 853dp, 960dp, 1280dp (overscan & teks diskalakan).
- Pastikan app masih muncul di launcher ponsel (tidak cuma TV).
