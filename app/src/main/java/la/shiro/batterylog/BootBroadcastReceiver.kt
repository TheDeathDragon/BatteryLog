package la.shiro.batterylog


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val service = Intent(context, BatteryLogService::class.java)
            Log.d(
                "Rin",
                (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("is_boot_run", true)
                    .toString())
            )
            service.putExtra("testTitle", System.currentTimeMillis())
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("is_boot_run", false)
            ) {
                val manager = context.getSystemService(NotificationManager::class.java)
                if (manager.areNotificationsEnabled()) {
                    context.startForegroundService(service)
                }else{
                    Log.d("Rin", "onReceive: 通知权限未开启")
                }
            }
        }
    }
}