package la.shiro.batterylog

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import la.shiro.batterylog.adapter.BatteryLogDetailAdapter
import la.shiro.batterylog.config.TAG
import la.shiro.batterylog.config.TestNameManager
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
    private lateinit var testNameManager: TestNameManager
    private var currentTimestamp: Long = 0
    private val CREATE_CSV_FILE_REQUEST_CODE = 1001
    private val CREATE_DAT_FILE_REQUEST_CODE = 1002
    
    private val createCSVFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveCSVToUri(uri, list)
                Snackbar.make(
                    recyclerView, 
                    getString(R.string.save_data_as) + uri.path, 
                    Snackbar.LENGTH_LONG
                ).show()
                saveDataFileToSystem("$title.dat", list)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        testNameManager = TestNameManager(this)
        currentTimestamp = intent.getLongExtra("testTitle", 0)
        val fullTitle = testNameManager.getTestName(currentTimestamp)
        title = if (fullTitle.length > 20) {
            fullTitle.substring(0, 17) + "..."
        } else {
            fullTitle
        }
        
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

            R.id.rename -> {
                showRenameDialog()
            }

            R.id.save_as -> {
                val fileName = "${testNameManager.getTestName(currentTimestamp).replace(" ", "-").replace(":", "-")}.csv"
                createCSVFile(fileName)
            }
        }
        return true
    }
    
    private fun createCSVFile(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createCSVFileLauncher.launch(intent)
    }
    
    private fun saveCSVToUri(uri: Uri, dataList: List<BatteryInfo>) {
        var bufferedWriter: BufferedWriter? = null
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "gb2312"))
            
            val line = "datetime" + "," + "time" + "," + "level" + "," + "temperature" + "," + "voltage" + "," + "status" + "\r\n"
            bufferedWriter.write(line)
            
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
                }
            }
            
            Log.d(TAG, "保存CSV文件成功: ${uri.path}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "保存CSV文件失败: ${e.message}")
        } finally {
            try {
                bufferedWriter?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun saveDataFileToSystem(fileName: String, dataList: List<BatteryInfo>): Boolean {
        val datFile = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )
        var isSuccessful: Boolean
        var datBufferedWriter: BufferedWriter? = null
        try {
            val datFileOutputStream = FileOutputStream(datFile)
            datBufferedWriter = BufferedWriter(OutputStreamWriter(datFileOutputStream, "utf-8"))
            val line =
                "datetime" + "," + "time" + "," + "level" + "," + "temperature" + "," + "voltage" + "," + "status" + "\r\n"
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
                    datBufferedWriter.write(line2)
                }
            }
            isSuccessful = true
            Log.d(TAG, "保存DAT文件成功: ${datFile.path}")
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccessful = false
            Log.e(TAG, "保存DAT文件失败: ${e.message}")
        } finally {
            try {
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

    private fun showRenameDialog() {
        val editText = EditText(this)
        editText.setText(testNameManager.getTestName(currentTimestamp))
        editText.selectAll()
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rename_log))
            .setMessage(getString(R.string.enter_new_name))
            .setView(editText)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    testNameManager.saveTestName(currentTimestamp, newName)
                    title = if (newName.length > 20) {
                        newName.substring(0, 17) + "..."
                    } else {
                        newName
                    }
                    Snackbar.make(recyclerView, getString(R.string.rename_success), Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}