package la.shiro.batterylog

import android.icu.text.SimpleDateFormat
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
import la.shiro.batterylog.database.BatteryInfo
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*


class LineListActivity : AppCompatActivity() {

    private lateinit var list: List<BatteryInfo>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LineListRecyclerviewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = longToDate(intent.getLongExtra("testTitle", 0))
            .replace(" ", "-")
            .replace(":", "-")
        recyclerView = findViewById<View>(R.id.lineListActivity_recyclerView) as RecyclerView

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        list = listOf()
        adapter = LineListRecyclerviewAdapter(list)
        recyclerView.adapter = adapter
        val lineCharViewMode: LineChartViewMode by viewModels {
            LineChartViewModeFactory(
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
//                            .setAction(
//                                "打开文件目录"
//                            ) {
//                                //调用系统文件管理器打开指定路径目录
//                                openAssignFolder()
//                            }
                            .show()
                        Log.d(
                            "Rin",
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

    //    private fun openAssignFolder() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.addCategory(Intent.CATEGORY_DEFAULT)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val contentUri = FileProvider.getUriForFile(
//            applicationContext,
//            "la.shiro.batterylog.provider",
//            File(
//                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//                "$title.csv"
//            )
//        )
//        intent.setDataAndType(contentUri, "file/*")
//        try {
//            startActivity(intent)
//            //            startActivity(Intent.createChooser(intent,"选择浏览工具"));
//        } catch (e: ActivityNotFoundException) {
//            e.printStackTrace()
//        }
//    }
    private fun saveAsCSV(fileName: String, dataList: List<BatteryInfo>): Boolean {
        val file = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )
        var isSuccessful: Boolean
        var bufferedWriter: BufferedWriter? = null
        try {
            val fileOutputStream = FileOutputStream(file)
            bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream, "gb2312"))
            val line =
                "datetime" + "," + "level" + "," + "temperature" + "," + "voltage" + "," + "status" + "\r\n"
            bufferedWriter.write(line)
            val status: String = when (dataList.first().status) {
                BatteryManager.BATTERY_STATUS_UNKNOWN ->
                    getString(R.string.BATTERY_STATUS_UNKNOWN)

                BatteryManager.BATTERY_STATUS_CHARGING ->
                    getString(R.string.BATTERY_STATUS_CHARGING)

                BatteryManager.BATTERY_STATUS_DISCHARGING ->
                    getString(R.string.BATTERY_STATUS_DISCHARGING)

                BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                    getString(R.string.BATTERY_STATUS_NOT_CHARGING)

                BatteryManager.BATTERY_STATUS_FULL ->
                    getString(R.string.BATTERY_STATUS_FULL)

                else ->
                    getString(R.string.BATTERY_STATUS_UNKNOWN)
            }
            val line1 =
                longToDate(dataList.first().title + dataList.first().time) + "," + dataList.first().level.toString() + "," + ((dataList.first().temperature).toFloat() / 10).toString() + "," + dataList.first().voltage.toString() + "," + status + "\r\n"
            bufferedWriter.write(line1)

            var level = dataList.first().level
            for (item in dataList) {
                if (item.level != level || level == dataList.last().level) {
                    level = item.level
                    val status1: String = when (item.status) {
                        BatteryManager.BATTERY_STATUS_UNKNOWN ->
                            getString(R.string.BATTERY_STATUS_UNKNOWN)

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            getString(R.string.BATTERY_STATUS_CHARGING)

                        BatteryManager.BATTERY_STATUS_DISCHARGING ->
                            getString(R.string.BATTERY_STATUS_DISCHARGING)

                        BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                            getString(R.string.BATTERY_STATUS_NOT_CHARGING)

                        BatteryManager.BATTERY_STATUS_FULL ->
                            getString(R.string.BATTERY_STATUS_FULL)

                        else ->
                            getString(R.string.BATTERY_STATUS_UNKNOWN)
                    }
                    val line2 =
                        longToDate(item.title + item.time) + "," + item.level.toString() + "," + ((item.temperature.toFloat()) / 10).toString() + "," + item.voltage.toString() + "," + status1 + "\r\n"
                    bufferedWriter.write(line2)
                }
            }
            isSuccessful = true
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccessful = false
        } finally {
            try {
                bufferedWriter?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return isSuccessful
    }

    private fun longToDate(lo: Long): String {
        val date = Date(lo)
        val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sd.format(date)
    }
}