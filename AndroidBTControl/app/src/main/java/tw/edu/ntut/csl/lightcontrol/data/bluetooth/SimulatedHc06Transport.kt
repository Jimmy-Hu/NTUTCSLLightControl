package tw.edu.ntut.csl.lightcontrol.data.bluetooth

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Built-in virtual Arduino Due + HC-06 serial session for emulator testing and hardware-free demos.
 * Implements the exact same [SerialSession] contract as physical RFCOMM sockets.
 */
class SimulatedHc06Session(
    private val responseDelayMs: Long = 35L,
) : SerialSession {

    private val closed = AtomicBoolean(false)
    private val rxFlow = MutableSharedFlow<ByteArray>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val lineBuffer = StringBuilder()

    @Volatile
    var isLightPoweredOn: Boolean = false
        private set

    @Volatile
    var brightnessPercent: Int = 100
        private set

    init {
        rxFlow.tryEmit("CSL-HC06:READY\r\n".toByteArray(StandardCharsets.UTF_8))
    }

    override val incomingBytes: Flow<ByteArray> = rxFlow.asSharedFlow()

    override suspend fun write(bytes: ByteArray) {
        if (closed.get()) {
            throw IOException("Simulated HC-06 socket is closed")
        }
        if (bytes.isEmpty()) return

        val chunk = String(bytes, StandardCharsets.UTF_8)
        val commandsToProcess = mutableListOf<String>()

        synchronized(lineBuffer) {
            for (ch in chunk) {
                if (ch == '\n' || ch == '\r') {
                    if (lineBuffer.isNotEmpty()) {
                        commandsToProcess.add(lineBuffer.toString())
                        lineBuffer.clear()
                    }
                } else {
                    lineBuffer.append(ch)
                }
            }
            // Also support raw mode when LineEnding.NONE is configured
            if (!chunk.contains('\n') && !chunk.contains('\r') && lineBuffer.isNotEmpty()) {
                commandsToProcess.add(lineBuffer.toString())
                lineBuffer.clear()
            }
        }

        for (cmd in commandsToProcess) {
            if (responseDelayMs > 0) {
                delay(responseDelayMs)
            }
            if (!closed.get()) {
                val reply = evaluateArduinoCommand(cmd.trim())
                rxFlow.emit(reply.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    private fun evaluateArduinoCommand(command: String): String {
        val normalized = command.lowercase(Locale.ROOT)
        return when {
            normalized == "poweron" -> {
                isLightPoweredOn = true
                "OK:LIGHT=ON\r\n"
            }
            normalized == "powerof" || normalized == "poweroff" -> {
                isLightPoweredOn = false
                "OK:LIGHT=OFF\r\n"
            }
            normalized == "status" -> {
                val powerLabel = if (isLightPoweredOn) "ON" else "OFF"
                "STATUS:LIGHT=$powerLabel,BRIGHT=$brightnessPercent%\r\n"
            }
            normalized.startsWith("bright") -> {
                val digits = normalized.removePrefix("bright").trim().toIntOrNull()
                if (digits != null && digits in 0..100) {
                    brightnessPercent = digits
                    if (digits > 0) isLightPoweredOn = true
                    "OK:BRIGHT=$brightnessPercent%\r\n"
                } else {
                    "ERR:BRIGHT_RANGE_0_100\r\n"
                }
            }
            else -> "ACK:$command\r\n"
        }
    }

    override fun close() {
        closed.set(true)
    }
}
