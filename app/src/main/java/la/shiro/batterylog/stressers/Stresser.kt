package la.shiro.batterylog.stressers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import la.shiro.batterylog.config.TAG

abstract class Stresser (protected val context: Context) {
    protected val handlerUI = Handler(Looper.getMainLooper())
    var isRunning = false

    /**
     * This function exists so that each Stresser does not get optimized out
     */
    protected fun impossibleUIUpdateOnMain(impossibleCondition : Boolean) {
        if(impossibleCondition) {
            val s = "Impossible result from ${javaClass.name}"
            Log.d(TAG, s)
            handlerUI.post { Toast.makeText(context, s, Toast.LENGTH_LONG).show() }
        }
    }

    open fun permissionsGranted(): Boolean {
        return true
    }

    open fun start() {
        assert(!isRunning)
        Log.d(TAG, "${this.javaClass.name} started")
        isRunning = true
    }
    open fun stop() {
        assert(isRunning)
        Log.d(TAG, "${this.javaClass.name} stopped")
        isRunning = false
    }
}
