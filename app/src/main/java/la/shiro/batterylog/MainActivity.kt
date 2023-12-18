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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    private lateinit var startLog: Button
    private lateinit var testList: Button
    private var testTitle: Long = 0 // title of the test, based on the time when the test started
    private var mutableMap: MutableMap<String, String> = mutableMapOf()
    private lateinit var listname: List<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BatteryInfoRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        adapter = BatteryInfoRecyclerViewAdapter(
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
        startLog = findViewById<View>(R.id.startLog) as Button
        testList = findViewById<View>(R.id.test_list) as Button
        startLog.setOnClickListener {
            if ((getSystemService(BATTERY_SERVICE) as BatteryManager).getIntProperty(
                    BATTERY_PROPERTY_STATUS
                ) != BATTERY_STATUS_FULL
            ) {
                toggle()
            } else {
                Snackbar.make(
                    recyclerView,
                    getString(R.string.battery_full_mas),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
        testList.setOnClickListener {
            startTestListActivity()
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
            }

            R.id.main_menu_about_item -> {
                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun startTestListActivity() {
        val intent = Intent(this@MainActivity, LogListActivity::class.java)
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
        if (isServiceWork(this, "la.shiro.batterylog.BatteryLogService")) {
            stopService(service)
            startLog.text = getString(R.string.startBatteryLogService)
        } else {
            testTitle = System.currentTimeMillis()
            service.putExtra("testTitle", testTitle)
            startService(service)
            startLog.text = getString(R.string.stopBatteryLogService)
        }
    }

    private fun isNotificationPermissionEnabled(): Boolean {
        val manager = getSystemService(NotificationManager::class.java)
        return manager.areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivityForResult(intent, 10085)
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
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, 10086)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10085) {
            // 用户在设置页面进行了操作，重新检查权限
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
        if (requestCode == 10086) {
            // 用户在设置页面进行了操作，重新检查权限
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

    private fun isServiceWork(mContext: Context, serviceName: String): Boolean {
        var isWork = false
        val myAM = mContext
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myList: List<ActivityManager.RunningServiceInfo> = myAM.getRunningServices(40)
        if (myList.isEmpty()) {
            return false
        }
        for (i in myList.indices) {
            val mName: String = myList[i].service.className
            if (mName == serviceName) {
                isWork = true
                break
            }
        }
        return isWork
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
                                    context.getString(R.string.BATTERY_STATUS_UNKNOWN)

                                BATTERY_STATUS_CHARGING -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_STATUS_CHARGING)

                                BATTERY_STATUS_DISCHARGING -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_STATUS_DISCHARGING)

                                BATTERY_STATUS_NOT_CHARGING -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_STATUS_NOT_CHARGING)

                                BATTERY_STATUS_FULL -> {
                                    mutableMap[v] =
                                        context.getString(R.string.BATTERY_STATUS_FULL)
                                    if (!isServiceWork(
                                            context,
                                            "la.shiro.batterylog.BatteryLogService"
                                        )
                                    ) {
                                        startLog.text = getString(R.string.startBatteryLogService)
                                    }
                                }
                            }
                        }

                        "health" -> {
                            when (intent.getIntExtra(v, 0)) {
                                BATTERY_HEALTH_UNKNOWN -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_UNKNOWN)

                                BATTERY_HEALTH_GOOD -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_GOOD)

                                BATTERY_HEALTH_OVERHEAT -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_OVERHEAT)

                                BATTERY_HEALTH_DEAD -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_DEAD)

                                BATTERY_HEALTH_OVER_VOLTAGE -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_OVER_VOLTAGE)

                                BATTERY_HEALTH_UNSPECIFIED_FAILURE -> mutableMap[v] =
                                    context.getString(
                                        R.string.BATTERY_HEALTH_UNSPECIFIED_FAILURE
                                    )

                                BATTERY_HEALTH_COLD -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_HEALTH_COLD)
                            }
                        }

                        "plugged" -> {
                            when (intent.getIntExtra(v, 0)) {
                                BATTERY_PLUGGED_AC -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_PLUGGED_AC)

                                BATTERY_PLUGGED_USB -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_PLUGGED_USB)

                                BATTERY_PLUGGED_WIRELESS -> mutableMap[v] =
                                    context.getString(R.string.BATTERY_PLUGGED_WIRELESS)

                                0 -> mutableMap[v] = context.getString(R.string.BATTERY_PLUGGED_NONE)
                            }
                        }

                        "present" -> {
                            if (intent.getBooleanExtra(v, false)) {
                                mutableMap[v] = context.getString(R.string.BATTERY_HAVE_BATTERY)
                            } else {
                                mutableMap[v] = context.getString(R.string.NO_BATTERY)
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
        if (isServiceWork(this, "la.shiro.batterylog.BatteryLogService")) {
            startLog.text = getString(R.string.stopBatteryLogService)
        } else {
            startLog.text = getString(R.string.startBatteryLogService)
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
                    getString(R.string.BATTERY_STATUS_UNKNOWN)

                BATTERY_STATUS_CHARGING -> mutableMap["status"] =
                    getString(R.string.BATTERY_STATUS_CHARGING)

                BATTERY_STATUS_DISCHARGING -> mutableMap["status"] =
                    getString(R.string.BATTERY_STATUS_DISCHARGING)

                BATTERY_STATUS_NOT_CHARGING -> mutableMap["status"] =
                    getString(R.string.BATTERY_STATUS_NOT_CHARGING)

                BATTERY_STATUS_FULL -> mutableMap["status"] =
                    getString(R.string.BATTERY_STATUS_FULL)
            }
            mutableMap["capacity"] =
                (batteryManager.getIntProperty(BATTERY_PROPERTY_CHARGE_COUNTER) / 1000).toString() + "mAh"
            adapter.notifyItemChanged(0)
            adapter.notifyItemChanged(6)
            adapter.notifyItemChanged(8)
        }
    }
}