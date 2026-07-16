package id.fc.pratv.ui.theme

import android.content.Context
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

fun getRefreshRate(context: Context): Float {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    val hz = wm?.defaultDisplay?.refreshRate ?: 60f
    return if (hz > 0f) hz else 60f
}

fun frameDurationMs(frames: Int, context: Context, maxMs: Int = 1000): Int {
    val hz = getRefreshRate(context)
    val ms = (frames / hz) * 1000f
    return ms.toInt().coerceAtLeast(1).coerceAtMost(maxMs)
}
