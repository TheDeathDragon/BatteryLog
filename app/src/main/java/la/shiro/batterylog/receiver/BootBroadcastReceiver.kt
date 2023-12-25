package la.shiro.batterylog.receiver


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import la.shiro.batterylog.config.TAG
import la.shiro.batterylog.service.BatteryLogService

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val service = Intent(context, BatteryLogService::class.java)
            service.putExtra("testTitle", System.currentTimeMillis())
            val isBootRun = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("service_run_on_boot", false)
            Log.d(
                TAG,
                "BootBroadcastReceiver --> onReceive: is battery log service run on boot: $isBootRun"
            )
            if (isBootRun) {
                val manager = context.getSystemService(NotificationManager::class.java)
                if (manager.areNotificationsEnabled()) {
                    context.startForegroundService(service)
                } else {
                    Log.e(
                        TAG,
                        "BootBroadcastReceiver --> onReceive: Notification permission is not granted"
                    )
                }
            }
        }
    }
}