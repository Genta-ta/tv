package id.fc.pratv.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

object AppLogger {

    private const val MAX_LINES = 500

    private val buffer = ConcurrentLinkedDeque<String>()

    enum class Level { D, I, W, E }

    @Synchronized
    fun log(level: Level, tag: String, message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        buffer.addLast("[$time][${level.name}][$tag] $message")
        while (buffer.size > MAX_LINES) buffer.removeFirst()
    }

    fun d(tag: String, message: String) = log(Level.D, tag, message)
    fun i(tag: String, message: String) = log(Level.I, tag, message)
    fun w(tag: String, message: String) = log(Level.W, tag, message)
    fun e(tag: String, message: String) = log(Level.E, tag, message)

    fun lines(): List<String> = buffer.toList()

    fun clear() = buffer.clear()
}
