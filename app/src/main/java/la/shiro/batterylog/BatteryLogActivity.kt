package la.shiro.batterylog

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import la.shiro.batterylog.adapter.BatteryLogAdapter
import la.shiro.batterylog.viewmodel.LogViewModel
import la.shiro.batterylog.viewmodel.LogViewModelFactory
import java.io.File

class BatteryLogActivity : AppCompatActivity() {
    private val logViewModel: LogViewModel by viewModels {
        LogViewModelFactory((application as BatteryLogApplication).repository)
    }
    private lateinit var list: List<Long>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BatteryLogAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById<View>(R.id.log_list_recyclerview) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        list = listOf()
        adapter = BatteryLogAdapter(list)
        adapter.setClickListener(object : BatteryLogAdapter.ItemOnClickListener {
            override fun onClickChartButton(timeStamp: Long) {
                startLineChartActivity(timeStamp)
            }

            override fun onClickDeleteButton(timeStamp: Long) {


                val builder = AlertDialog.Builder(this@BatteryLogActivity)
                builder.setTitle(getString(R.string.alert))
                builder.setIcon(R.drawable.ic_baseline_warning)
                builder.setMessage(R.string.alert_massage)
                builder.setPositiveButton(
                    getString(R.string.confirm)
                ) { _, _ -> delete(timeStamp) }
                builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                builder.create().show()

            }

            override fun onClickListButton(timeStamp: Long) {
                startLineListActivity(timeStamp)
            }

        })
        recyclerView.adapter = adapter
        logViewModel.allTestTitle.observe(owner = this) { testTitle ->
            testTitle.distinct().let { adapter.setData(it) }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.testlistactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.delete_all -> {
                val builder = AlertDialog.Builder(this@BatteryLogActivity)
                builder.setTitle(getString(R.string.alert))
                builder.setIcon(R.drawable.ic_baseline_warning)
                builder.setMessage(getString(R.string.alert_massage_all))
                builder.setPositiveButton(
                    getString(R.string.confirm)
                ) { _, _ -> deleteAll() }
                builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                builder.create().show()
            }
        }
        return true
    }

    private fun startLineListActivity(timeStamp: Long) {
        val intent = Intent(this@BatteryLogActivity, BatteryLogDetailActivity::class.java)
        intent.putExtra("testTitle", timeStamp)
        startActivity(intent)
    }

    private fun delete(timeStamp: Long) {
        (application as BatteryLogApplication).applicationScope.launch(Dispatchers.IO) {
            logViewModel.delete(timeStamp)
        }
    }

    private fun deleteAll() {
        (application as BatteryLogApplication).applicationScope.launch(Dispatchers.IO) {
            logViewModel.deleteAll()
            val fileList =
                File(applicationContext.getExternalFilesDir(null), "Documents").listFiles()
            if (fileList != null) {
                for (file in fileList) {
                    if (file.name.endsWith(".csv")) {
                        file.delete()
                    }
                    if (file.name.endsWith(".dat")) {
                        file.delete()
                    }
                }
            }
        }
    }

    private fun startLineChartActivity(long: Long) {
        val intent = Intent(this@BatteryLogActivity, BatteryLogChartActivity::class.java)
        intent.putExtra("testTitle", long)
        startActivity(intent)
    }
}