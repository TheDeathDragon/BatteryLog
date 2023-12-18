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
import java.io.File

class LogListActivity : AppCompatActivity() {
    private val logListViewMode: LogListViewMode by viewModels {
        LogListViewModeFactory((application as BatteryLogApplication).repository)
    }
    private lateinit var list: List<Long>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LogListRecyclerviewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById<View>(R.id.test_list_recyclerview) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        list = listOf()
        adapter = LogListRecyclerviewAdapter(list)
        adapter.setClickListener(object : LogListRecyclerviewAdapter.ItemOnClickListener {
            override fun onClickChartButton(lo: Long) {
                startLineChartActivity(lo)
            }

            override fun onClickDeleteButton(lo: Long) {


                val builder = AlertDialog.Builder(this@LogListActivity)
                builder.setTitle(getString(R.string.alert))
                builder.setIcon(R.drawable.ic_baseline_warning_24)
                builder.setMessage(R.string.alert_massage)
                builder.setPositiveButton(
                    getString(R.string.confirm)
                ) { _, _ -> delete(lo) }
                builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                builder.create().show()

            }

            override fun onClickListButton(lo: Long) {
                startLineListActivity(lo)
            }

        })
        recyclerView.adapter = adapter
        logListViewMode.allTestTitle.observe(owner = this) { testTitle ->
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
                val builder = AlertDialog.Builder(this@LogListActivity)
                builder.setTitle(getString(R.string.alert))
                builder.setIcon(R.drawable.ic_baseline_warning_24)
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

    private fun startLineListActivity(lo: Long) {
        val intent = Intent(this@LogListActivity, LineListActivity::class.java)
        intent.putExtra("testTitle", lo)
        startActivity(intent)
    }

    private fun delete(lo: Long) {
        (application as BatteryLogApplication).applicationScope.launch(Dispatchers.IO) {
            logListViewMode.delete(
                lo
            )
        }
    }

    private fun deleteAll() {
        (application as BatteryLogApplication).applicationScope.launch(Dispatchers.IO) { logListViewMode.deleteAll() }
        val fileList = File(applicationContext.getExternalFilesDir(null), "Documents").listFiles()
        if (fileList != null) {
            for (file in fileList) {
                if (file.name.endsWith(".csv")) {
                    file.delete()
                }
            }
        }
    }

    private fun startLineChartActivity(long: Long) {
        val intent = Intent(this@LogListActivity, LineChartActivity::class.java)
        intent.putExtra("testTitle", long)
        startActivity(intent)
    }
}