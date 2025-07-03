package la.shiro.batterylog

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_HEALTH_COLD
import android.os.BatteryManager.BATTERY_HEALTH_DEAD
import android.os.BatteryManager.BATTERY_HEALTH_GOOD
import android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT
import android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE
import android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN
import android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE
import android.os.BatteryManager.BATTERY_PLUGGED_AC
import android.os.BatteryManager.BATTERY_PLUGGED_USB
import android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS
import android.os.BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
import android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
import android.os.BatteryManager.BATTERY_PROPERTY_STATUS
import android.os.BatteryManager.BATTERY_STATUS_CHARGING
import android.os.BatteryManager.BATTERY_STATUS_DISCHARGING
import android.os.BatteryManager.BATTERY_STATUS_FULL
import android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING
import android.os.BatteryManager.BATTERY_STATUS_UNKNOWN
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import la.shiro.batterylog.adapter.BatteryStatsAdapter
import la.shiro.batterylog.config.BATTERY_OPTIMIZATION_REQUEST_CODE
import la.shiro.batterylog.config.NOTIFICATION_PERMISSION_REQUEST_CODE
import la.shiro.batterylog.service.BatteryLogService
import androidx.core.net.toUri


class MainActivity : AppCompatActivity() {
    private lateinit var startServiceButton: Button
    private lateinit var logHistoryButton: Button
    private var testTitle: Long = 0 // title of the test, based on the time when the test started
    private var mutableMap: MutableMap<String, String> = mutableMapOf()
    private lateinit var listname: List<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BatteryStatsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        listname = listOf(
            "status",
            "health",
            "present",
            "level",
            "plugged",
            "voltage",
            "current",
            "temperature",
            "capacity",
        )
        for (v in listname) {
            mutableMap[v] = v
        }
        recyclerView = findViewById<View>(R.id.battery_info_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        (recyclerView.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        adapter = BatteryStatsAdapter(
            mutableMap, listname, listOf(
                getString(R.string.status),
                getString(R.string.health),
                getString(R.string.present),
                getString(R.string.level),
                getString(R.string.plugged),
                getString(R.string.voltage),
                getString(R.string.current),
                getString(R.string.temperature),
                getString(R.string.capacity),
            )
        )
        recyclerView.adapter = adapter
        startServiceButton = findViewById<View>(R.id.btn_start_service) as Button
        logHistoryButton = findViewById<View>(R.id.btn_log_history) as Button
        startServiceButton.setOnClickListener {
            if ((getSystemService(BATTERY_SERVICE) as BatteryManager).getIntProperty(
                    BATTERY_PROPERTY_STATUS
                ) != BATTERY_STATUS_FULL
            ) {
                toggle()
            } else {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.battery_full_info),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
        logHistoryButton.setOnClickListener {
            startBatteryLogActivity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_menu_setting_item -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.main_menu_about_item -> {
                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.main_menu_version_item -> {
                showVersionDialog()
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun startBatteryLogActivity() {
        val intent = Intent(this@MainActivity, BatteryLogActivity::class.java)
        startActivity(intent)
    }


    private fun toggle() {
        if (!isNotificationPermissionEnabled()) {
            requestNotificationPermission()
            return
        }
        if (!isBackgroundRunPermissionEnabled()) {
            requestBackgroundRunPermission()
            return
        }

        val service = Intent(this, BatteryLogService::class.java)
        if (isServiceWork(this)) {
            stopService(service)
            startServiceButton.text = getString(R.string.start_service)
        } else {
            testTitle = System.currentTimeMillis()
            service.putExtra("testTitle", testTitle)
            startService(service)
            startServiceButton.text = getString(R.string.stop_service)
        }
    }

    private fun isNotificationPermissionEnabled(): Boolean {
        val manager = getSystemService(NotificationManager::class.java)
        return manager.areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE)
    }

    private fun isBackgroundRunPermissionEnabled(): Boolean {
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    private fun requestBackgroundRunPermission() {
        val packageName = packageName
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = "package:$packageName".toUri()
        startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (isNotificationPermissionEnabled()) {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.notification_permission_granted),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.notification_permission_denied),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
        if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            if (isBackgroundRunPermissionEnabled()) {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.background_run_permission_granted),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.background_run_permission_not_granted),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun isServiceWork(mContext: Context): Boolean {
        val serviceName = BatteryLogService::class.java.name
        var isServiceRunning = false
        val activityManager = mContext
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningServiceInfoList: List<ActivityManager.RunningServiceInfo> =
            activityManager.getRunningServices(40)
        if (runningServiceInfoList.isEmpty()) {
            return false
        }
        for (i in runningServiceInfoList.indices) {
            val mServiceName: String = runningServiceInfoList[i].service.className
            if (mServiceName == serviceName) {
                isServiceRunning = true
                break
            }
        }
        return isServiceRunning
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if (action == Intent.ACTION_BATTERY_CHANGED && context != null) {
                for (v in listname) {
                    when (v) {
                        "status" -> {
                            when (intent.getIntExtra(v, 0)) {
                                BATTERY_STATUS_UNKNOWN -> mutableMap[v] =
                                    context.getString(R.string.battery_status_unknown)

                                BATTERY_STATUS_CHARGING -> mutableMap[v] =
                                    context.getString(R.string.battery_status_charging)

                                BATTERY_STATUS_DISCHARGING -> mutableMap[v] =
                                    context.getString(R.string.battery_status_discharging)

                                BATTERY_STATUS_NOT_CHARGING -> mutableMap[v] =
                                    context.getString(R.string.battery_status_not_charging)

                                BATTERY_STATUS_FULL -> {
                                    mutableMap[v] =
                                        context.getString(R.string.battery_status_full)
                                    if (!isServiceWork(context)
                                    ) {
                                        startServiceButton.text = getString(R.string.start_service)
                                    }
                                }
                            }
                        }

                        "health" -> {
                            when (intent.getIntExtra(v, 0)) {
                                BATTERY_HEALTH_UNKNOWN -> mutableMap[v] =
                                    context.getString(R.string.battery_health_unknown)

                                BATTERY_HEALTH_GOOD -> mutableMap[v] =
                                    context.getString(R.string.battery_health_good)

                                BATTERY_HEALTH_OVERHEAT -> mutableMap[v] =
                                    context.getString(R.string.battery_health_overheat)

                                BATTERY_HEALTH_DEAD -> mutableMap[v] =
                                    context.getString(R.string.battery_health_dead)

                                BATTERY_HEALTH_OVER_VOLTAGE -> mutableMap[v] =
                                    context.getString(R.string.battery_health_over_voltage)

                                BATTERY_HEALTH_UNSPECIFIED_FAILURE -> mutableMap[v] =
                                    context.getString(
                                        R.string.battery_health_unspecified_failure
                                    )

                                BATTERY_HEALTH_COLD -> mutableMap[v] =
                                    context.getString(R.string.battery_health_cold)
                            }
                        }

                        "plugged" -> {
                            when (intent.getIntExtra(v, 0)) {
                                BATTERY_PLUGGED_AC -> mutableMap[v] =
                                    context.getString(R.string.battery_plugged_ac)

                                BATTERY_PLUGGED_USB -> mutableMap[v] =
                                    context.getString(R.string.battery_plugged_usb)

                                BATTERY_PLUGGED_WIRELESS -> mutableMap[v] =
                                    context.getString(R.string.battery_plugged_wireless)

                                0 -> mutableMap[v] =
                                    context.getString(R.string.battery_plugged_none)
                            }
                        }

                        "present" -> {
                            if (intent.getBooleanExtra(v, false)) {
                                mutableMap[v] = context.getString(R.string.have_battery)
                            } else {
                                mutableMap[v] = context.getString(R.string.no_battery)
                            }
                        }

                        "level" -> mutableMap[v] = intent.getIntExtra(v, 0).toString() + "%"
                        "voltage" -> mutableMap[v] = intent.getIntExtra(v, 0).toString() + "mV"
                        "temperature" -> mutableMap[v] =
                            (intent.getIntExtra(v, 0).toFloat() / 10).toString() + "℃"
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onPause() {
        unregisterReceiver(mBroadcastReceiver)
        mCalHandler.removeCallbacks(mTicker)
        super.onPause()
    }

    override fun onResume() {
        if (isServiceWork(this)) {
            startServiceButton.text = getString(R.string.stop_service)
        } else {
            startServiceButton.text = getString(R.string.start_service)
        }
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(mBroadcastReceiver, filter)
        mCalHandler.post(mTicker)
        super.onResume()
    }

    // 精确修正时间
    private val mCalHandler: Handler = Handler(Looper.getMainLooper())
    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            val now = SystemClock.uptimeMillis()
            val next = now + (1000 - now % 1000)
            mCalHandler.postAtTime(this, next)
            val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
            mutableMap["current"] =
                (batteryManager.getIntProperty(BATTERY_PROPERTY_CURRENT_NOW) / 1000).toString() + "mA"
            when (batteryManager.getIntProperty(BATTERY_PROPERTY_STATUS)) {
                BATTERY_STATUS_UNKNOWN -> mutableMap["status"] =
                    getString(R.string.battery_status_unknown)

                BATTERY_STATUS_CHARGING -> mutableMap["status"] =
                    getString(R.string.battery_status_charging)

                BATTERY_STATUS_DISCHARGING -> mutableMap["status"] =
                    getString(R.string.battery_status_discharging)

                BATTERY_STATUS_NOT_CHARGING -> mutableMap["status"] =
                    getString(R.string.battery_status_not_charging)

                BATTERY_STATUS_FULL -> mutableMap["status"] =
                    getString(R.string.battery_status_full)
            }
            mutableMap["capacity"] =
                (batteryManager.getIntProperty(BATTERY_PROPERTY_CHARGE_COUNTER) / 1000).toString() + "mAh"
            adapter.notifyItemChanged(0)
            adapter.notifyItemChanged(6)
            adapter.notifyItemChanged(8)
        }
    }

    private fun showVersionDialog() {
        val appName = BuildConfig.APP_NAME
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildTime = BuildConfig.BUILD_TIME
        
        val message = getString(R.string.app_name_label) + ": $appName\n" +
                     getString(R.string.version_name) + ": $versionName\n" +
                     getString(R.string.version_code) + ": $versionCode\n" +
                     getString(R.string.build_time) + ": $buildTime"
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.version_info))
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}