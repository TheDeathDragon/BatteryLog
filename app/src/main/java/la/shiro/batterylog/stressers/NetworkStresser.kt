package la.shiro.batterylog.stressers

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import la.shiro.batterylog.config.TAG
import java.io.InterruptedIOException
import java.lang.StrictMath.min
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.SocketException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException

class NetworkStresser(context: Context) : Stresser(context) {
    private lateinit var networkExecutorService: ExecutorService
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val runnable = object : Runnable {
        private val SERVER_URL = URL("https://www.ivanomalavolta.com/files/garbage.blob")

        override fun run() {
            val memoryClass = activityManager.memoryClass
            val bufferSz = min(memoryClass, 32) * 1024 * 1024// in bytes
            Log.d(TAG, "Memory class: $memoryClass MB, Buffer Size: $bufferSz Bytes")

            val dataChunk = ByteArray(bufferSz / 2)
            while (!Thread.interrupted()) {
                val con: HttpsURLConnection = SERVER_URL.openConnection() as HttpsURLConnection

                con.requestMethod = "GET"
                con.setRequestProperty("cache-control", "no-cache,must-revalidate");
                con.setRequestProperty(
                    "accept-encoding",
                    "identity"
                ); //prevent compression on server-side
                con.setRequestProperty("connection", "close")

                try {
                    val status = con.responseCode //execute the request
                    Log.d(TAG, "Status: $status")

                    if (status != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Unexpected status code in network request. Aborting.")
                        break
                    }

                    val inputStream = con.inputStream.buffered(bufferSz / 2)
                    var readSize: Int
                    while (true) { // read the response
                        readSize = inputStream.read(dataChunk, 0, dataChunk.size)
                        if (readSize == -1)
                            break;
                        /* This if condition is impossible to occur but we keep it to prevent the JVM from
                         * optimizing out the entire loop
                         */
                        impossibleUIUpdateOnMain(dataChunk[0].toInt() xor dataChunk[readSize - 1].toInt() == 300)
                    }
                    inputStream.close()
                } catch (ex: InterruptedIOException) {
                    Log.d(TAG, "Network thread interrupted")
                    break
                } catch (ex: SSLHandshakeException) {
                    // may happen if the phone has wrong date/time or invalid certificate is presented
                    // may also happen if the user has WiFi connection, but they must sign-in into the network first (e.g. airport)
                    Log.w(TAG, "SSL Handshake exception. Aborting.")
                    break
                } catch (ex: ConnectException) {
                    Log.w(TAG, "Connect exception. Aborting.")
                    break
                } catch (ex: UnknownHostException) {
                    Log.w(TAG, "Unknown host exception. Aborting.")
                    break
                } catch (ex: ProtocolException) {
                    // Can be thrown sometimes due to the large repetitive download. Simply re-download
                    Log.w(TAG, "Protocol exception. Retrying.")
                    continue
                } catch (ex: SocketException) {
                    Log.e(TAG, "Socket exception. Aborting.")
                    continue
                } catch (ex: Exception) {
                    Log.e(TAG, "Unexpected exception. Aborting.")
                    break
                } finally {
                    con.disconnect()
                }
            }
            Log.i(TAG, "Network thread stopped")
        }
    }

    override fun start() {
        super.start()
        networkExecutorService = Executors.newSingleThreadExecutor()
        networkExecutorService.execute(runnable)
    }

    override fun stop() {
        super.stop()
        networkExecutorService.shutdownNow()
        while (!networkExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            /* Wait for termination */
        }
    }
}
