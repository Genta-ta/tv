package id.fc.pratv.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val id: String?,
    val name: String,
    val logoUrl: String?,
    val group: String?,
    val url: String,
    val drmType: String? = null,
    val drmKeyId: String? = null,
    val drmKey: String? = null,
    val drmLicenseUri: String? = null,
    val userAgent: String? = null,
    val referer: String? = null,
    val origin: String? = null,
    val extraHeaders: Map<String, String> = emptyMap()
) : Parcelable

private val LOCAL_KEYWORDS = listOf("indo", "indonesia", "local", "lokal")

fun Channel.isLocal(): Boolean {
    val hay = listOf(group, name).joinToString(" ").lowercase()
    return LOCAL_KEYWORDS.any { it in hay }
}
