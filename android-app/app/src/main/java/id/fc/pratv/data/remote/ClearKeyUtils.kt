package id.fc.pratv.data.remote

import android.util.Base64
import java.nio.charset.StandardCharsets

object ClearKeyUtils {

    fun buildClearKeyJson(keyIdHex: String, keyHex: String): String {
        val kid = Base64.encodeToString(toBytes(keyIdHex), Base64.NO_WRAP)
        val key = Base64.encodeToString(toBytes(keyHex), Base64.NO_WRAP)
        return """{"keys":[{"kty":"oct","k":"$key","kid":"$kid"}]}"""
    }

    fun toBytes(value: String): ByteArray {
        val s = value.trim().replace(":", "").replace(" ", "")
        return if (s.length % 2 == 0 && s.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            hexToBytes(s)
        } else {
            Base64.decode(s, Base64.NO_WRAP)
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val s = hex.replace(":", "").replace(" ", "")
        val data = ByteArray(s.length / 2)
        for (i in data.indices) {
            val hi = s[i * 2].digitToInt(16) shl 4
            val lo = s[i * 2 + 1].digitToInt(16)
            data[i] = (hi + lo).toByte()
        }
        return data
    }
}
