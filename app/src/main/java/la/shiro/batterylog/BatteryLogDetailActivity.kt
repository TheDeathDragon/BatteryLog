package la.shiro.batterylog

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.BatteryManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import la.shiro.batterylog.adapter.BatteryLogDetailAdapter
import la.shiro.batterylog.config.TAG
import la.shiro.batterylog.database.BatteryInfo
import la.shiro.batterylog.viewmodel.ChartViewModel
import la.shiro.batterylog.viewmodel.ChartViewModelFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*


class BatteryLogDetailActivity : AppCompatActivity() {

    private lateinit var list: List<BatteryInfo>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BatteryLogDetailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = longToDate(intent.getLongExtra("testTitle", 0))
            .replace(" ", "-")
            .replace(":", "-")
        recyclerView = findViewById<View>(R.id.log_detail_recycler_view) as RecyclerView

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        list = listOf()
        adapter = BatteryLogDetailAdapter(list)
        recyclerView.adapter = adapter
        val lineCharViewMode: ChartViewModel by viewModels {
            ChartViewModelFactory(
                (application as BatteryLogApplication).repository, intent.getLongExtra(
                    "testTitle",
                    0
                )
            )
        }
        lineCharViewMode.testLog.observe(owner = this) { testLog ->
            testLog.let {
                adapter.setData(it)
                list = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.linelistactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.save_as -> {
                (application as BatteryLogApplication).applicationScope.launch {

                    if (withContext(this.coroutineContext) {
                            saveAsCSV("$title.csv", list)
                        }) {
                        Snackbar.make(
                            recyclerView, getString(R.string.save_data_as) +
                                    File(
                                        applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                        "$title.csv"
                                    ).path, Snackbar.LENGTH_LONG
                        )
                            .show()
                        Log.d(
                            TAG,
                            "BatteryLogDetailActivity --> onOptionsItemSelected: save data as " +
                            File(
                                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                "$title.csv"
                            ).path
                        )
                    }
                }
            }
        }
        return true
    }

    private fun saveAsCSV(fileName: String, dataList: List<BatteryInfo>): Boolean {
        val file = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )
        val datFile = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$title.dat"
        )
        var isSuccessful: Boolean
        var bufferedWriter: BufferedWriter? = null
        var datBufferedWriter: BufferedWriter? = null
        try {
            val fileOutputStream = FileOutputStream(file)
            val datFileOutputStream = FileOutputStream(datFile)
            bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream, "gb2312"))
            datBufferedWriter = BufferedWriter(OutputStreamWriter(datFileOutputStream, "utf-8"))
            val line =
                "datetime" + "," + "time" + "," + "level" + "," + "temperature" + "," + "voltage" + "," + "status" + "\r\n"
            bufferedWriter.write(line)
            datBufferedWriter.write(line)
            val status: String = when (dataList.first().status) {
                BatteryManager.BATTERY_STATUS_UNKNOWN ->
                    getString(R.string.battery_status_unknown)

                BatteryManager.BATTERY_STATUS_CHARGING ->
                    getString(R.string.battery_status_charging)

                BatteryManager.BATTERY_STATUS_DISCHARGING ->
                    getString(R.string.battery_status_discharging)

                BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                    getString(R.string.battery_status_not_charging)

                BatteryManager.BATTERY_STATUS_FULL ->
                    getString(R.string.battery_status_full)

                else ->
                    getString(R.string.battery_status_unknown)
            }
            val line1 =
                longToDate(dataList.first().title + dataList.first().time) + "," +
                        longToTime(dataList.first().time) + "," +
                        dataList.first().level.toString() + "," +
                        ((dataList.first().temperature).toFloat() / 10).toString() + "," +
                        dataList.first().voltage.toString() + "," + status + "\r\n"

            bufferedWriter.write(line1)
            datBufferedWriter.write(line1)

            var level = dataList.first().level
            for (item in dataList) {
                if (item.level != level || level == dataList.last().level) {
                    level = item.level
                    val status1: String = when (item.status) {
                        BatteryManager.BATTERY_STATUS_UNKNOWN ->
                            getString(R.string.battery_status_unknown)

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            getString(R.string.battery_status_charging)

                        BatteryManager.BATTERY_STATUS_DISCHARGING ->
                            getString(R.string.battery_status_discharging)

                        BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                            getString(R.string.battery_status_not_charging)

                        BatteryManager.BATTERY_STATUS_FULL ->
                            getString(R.string.battery_status_full)

                        else ->
                            getString(R.string.battery_status_unknown)
                    }
                    val line2 =
                        longToDate(item.title + item.time) + "," +
                                longToTime(item.time) + "," +
                                item.level.toString() + "," +
                                ((item.temperature.toFloat()) / 10).toString() + "," +
                                item.voltage.toString() + "," +
                                status1 + "\r\n"
                    bufferedWriter.write(line2)
                    datBufferedWriter.write(line2)
                }
            }
            isSuccessful = true
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccessful = false
        } finally {
            try {
                bufferedWriter?.close()
                datBufferedWriter?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return isSuccessful
    }

    private fun longToDate(lo: Long): String {
        val date = Date(lo)
        val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sd.format(date)
    }

    private fun longToTime(lo: Long): String {
        val date = Date(lo)
        val sd = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sd.timeZone = TimeZone.GMT_ZONE
        return sd.format(date)
    }
}