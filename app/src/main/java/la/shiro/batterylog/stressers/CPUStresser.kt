package la.shiro.batterylog.stressers

import android.content.Context
import android.util.Log
import la.shiro.batterylog.config.TAG
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.random.Random

class CPUStresser(context: Context) : Stresser(context) {
    private var cpuStressThreads: List<Thread> = listOf()

    private val threadRunnable = Runnable {
        val algorithm = "SHA-512"
        val md = MessageDigest.getInstance(algorithm)
        val luckyDigest: ByteArray =
            ByteBuffer.allocate(64).also { bb -> (1..32).forEach { bb.put(0x1A); bb.put(0x2B) } }
                .array() //1A2B1A2B..2B - 64 bytes = 512 bits

        val buffer: ByteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        var randomNum = Random.nextLong()
        var digest: ByteArray

        while (!Thread.interrupted()) {
            buffer.putLong(0, randomNum++)
            digest = md.digest(buffer.array())
            impossibleUIUpdateOnMain(digest.contentEquals(luckyDigest))
        }
    }

    override fun start() {
        super.start()
        val cores = Runtime.getRuntime().availableProcessors()
        Log.d(TAG, "Cores available: $cores. Spawning $cores CPU stresser threads")

        cpuStressThreads = (1..cores).map { Thread(threadRunnable) }.toList()
        cpuStressThreads.forEach { it.start() }
    }

    override fun stop() {
        super.stop()
        cpuStressThreads.forEach { it.interrupt() }
    }
}
