package la.shiro.batterylog

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import la.shiro.batterylog.database.BatteryInfo
import java.util.*


class LineChartActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var lineChart: LineChart
    private lateinit var lineDataSetLevel: LineDataSet
    private lateinit var lineDataSetTemperature: LineDataSet
    private lateinit var xAxis: XAxis
    private lateinit var leftAxis: YAxis
    private lateinit var rightAxis: YAxis
    private var isShowLevel: Boolean = true
    private var isShowTemperature: Boolean = false
    private lateinit var batteryInfo: List<BatteryInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window: Window = window
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_line_chart)
        lineChart = findViewById<View>(R.id.lineChart) as LineChart
        lineChart.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        val intent = intent

        val lineCharViewMode: LineChartViewMode by viewModels {
            LineChartViewModeFactory(
                (application as BatteryLogApplication).repository, intent.getLongExtra(
                    "testTitle",
                    0
                )
            )
        }
        (application as BatteryLogApplication).applicationScope.launch {
            batteryInfo =
                (application as BatteryLogApplication).repository.getTestLog1(
                    intent.getLongExtra(
                        "testTitle",
                        0
                    )
                )
        }

        lineCharViewMode.testLog.observe(owner = this) { testLog ->
            batteryInfo = testLog
            upData()
        }

        initCheckBox()
    }

    private fun initCheckBox() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener(this)
        val checkBoxLevel = findViewById<View>(R.id.checkBoxLevel) as CheckBox
        checkBoxLevel.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putBoolean("checkBoxLevel", isChecked).apply()
        }
        checkBoxLevel.isChecked = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("checkBoxLevel", true)
        isShowLevel = checkBoxLevel.isChecked
        val checkBoxTemperature = findViewById<View>(R.id.checkBoxTemperature) as CheckBox
        checkBoxTemperature.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putBoolean("checkBoxTemperature", isChecked).apply()
        }
        checkBoxTemperature.isChecked =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean("checkBoxTemperature", false)
        isShowTemperature = checkBoxTemperature.isChecked
    }

    override fun onResume() {
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        delayedHide(100)
        super.onResume()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hide() }
    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        //lineChart.visibility = View.GONE


        // Schedule a runnable to remove the status and navigation bar after a delay
        //mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, 300)
    }

    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        lineChart.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun upData() {
        initLineChart(lineChart)
        setLineChartData()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    fun longToDate(lo: Long): String {
        val date = Date(lo)
        val sd = SimpleDateFormat("HH:mm", Locale.getDefault())
        sd.timeZone = TimeZone.GMT_ZONE
        return sd.format(date)
    }

    private fun initLineChart(lineChart: LineChart) {
        //设置手势滑动事件
        //mLineChart.setOnChartGestureListener(this);
        //设置数值选择监听
        //mLineChart.setOnChartValueSelectedListener(this);
        //后台绘制
        lineChart.setDrawGridBackground(false)
        //设置描述文本
        //lineChart.description.text="12345"
        //lineChart.description.textColor=Color.RED
        lineChart.description.isEnabled = false
        //设置支持触控手势
        lineChart.setTouchEnabled(false)
        //设置缩放
        lineChart.isDragEnabled = false
        //设置推动
        lineChart.setScaleEnabled(true);
        //如果禁用,扩展可以在x轴和y轴分别完成
        lineChart.setPinchZoom(true);
        //显示边界
        lineChart.setDrawBorders(false)

        //设置图例
        lineChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        lineChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        lineChart.legend.textColor = getColor(R.color.white)
        //设置背景
        lineChart.setBackgroundColor(getColor(R.color.material_dynamic_primary10))

        xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineWidth = 2f
        xAxis.axisLineColor = getColor(R.color.material_dynamic_primary40)
        xAxis.textColor = getColor(R.color.white)
        xAxis.gridColor = getColor(R.color.material_dynamic_primary40)
        //使用虚线组成的网格线
        //参数：linelength：虚线长度
        // spacelength:虚线间隔长度
        // phase：虚线出发点（从第一根虚线的哪里出发）
        xAxis.enableGridDashedLine(5f, 10f, 0f)
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(false)
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        //xAxis.axisMaximum = list.last().time.toFloat()
        xAxis.axisMinimum = batteryInfo.first().time.toFloat()
        xAxis.setLabelCount(20, false)
        val iAxisValueFormatter = MIAxisValueFormatter()
        xAxis.valueFormatter = iAxisValueFormatter
        xAxis.setAvoidFirstLastClipping(false)


        rightAxis = lineChart.axisRight
        rightAxis.isEnabled = isShowTemperature
        rightAxis.axisLineWidth = 2f
        rightAxis.labelCount = 10
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisLineColor = getColor(R.color.material_dynamic_primary40)
        rightAxis.gridColor = getColor(R.color.material_dynamic_primary40)
        rightAxis.textColor = getColor(R.color.white)
        rightAxis.setDrawGridLines(true)
        rightAxis.axisMaximum = 75f
        rightAxis.axisMinimum = -30f

        leftAxis = lineChart.axisLeft
        leftAxis.isEnabled = isShowLevel
        leftAxis.axisLineWidth = 2f
        leftAxis.labelCount = 10
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisLineColor = getColor(R.color.material_dynamic_primary40)
        leftAxis.gridColor = getColor(R.color.material_dynamic_primary40)
        leftAxis.textColor = getColor(R.color.white)
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMaximum = 105f
        leftAxis.axisMinimum = 0f


    }

    private fun setLineChartData() {
        //设置数据
        val entriesLevel: MutableList<Entry> = ArrayList()
        val entriesTemperature: MutableList<Entry> = ArrayList()
        var level = batteryInfo.first().level
        entriesLevel.add(object : Entry(batteryInfo.first().time.toFloat(), level.toFloat()) {})
        entriesTemperature.add(object : Entry(
            batteryInfo.first().time.toFloat(),
            batteryInfo.first().temperature.toFloat() / 10
        ) {})
        for (i in batteryInfo) {
            if (i.level != level) {
                level = i.level
                entriesLevel.add(object : Entry(i.time.toFloat(), level.toFloat()) {})
                entriesTemperature.add(object :
                    Entry(i.time.toFloat(), i.temperature.toFloat() / 10) {})
            } else if (i.level == batteryInfo.last().level) {
                level = i.level
                entriesLevel.add(object : Entry(i.time.toFloat(), level.toFloat()) {})
                entriesTemperature.add(object :
                    Entry(i.time.toFloat(), i.temperature.toFloat() / 10) {})
            }
        }

        lineDataSetLevel = LineDataSet(entriesLevel, getString(R.string.level) + "(%)")
        lineDataSetLevel.setDrawValues(false)
        lineDataSetLevel.cubicIntensity = 20f
        lineDataSetLevel.setDrawCircles(false)
        lineDataSetLevel.setDrawCircleHole(false)
        lineDataSetLevel.color = getColor(R.color.teal_200)


        lineDataSetTemperature =
            LineDataSet(entriesTemperature, getString(R.string.temperature) + "(℃)")
        lineDataSetTemperature.setDrawValues(false)
        lineDataSetTemperature.cubicIntensity = 20f
        lineDataSetTemperature.setDrawCircles(false)
        lineDataSetTemperature.setDrawCircleHole(false)
        lineDataSetTemperature.axisDependency = AxisDependency.RIGHT
        lineDataSetTemperature.color = getColor(R.color.green_400)

        lineChart.data = LineData()
        if (isShowLevel) {
            lineChart.data.addDataSet(lineDataSetLevel)
        }
        if (isShowTemperature) {
            lineChart.data.addDataSet(lineDataSetTemperature)
        }
        lineChart.data.notifyDataChanged()
    }


    inner class MIAxisValueFormatter : ValueFormatter() {
        // 重写XAxis或yAxis标签的自定义格式设置
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return longToDate(value.toLong())
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "checkBoxLevel" -> {
                if (sharedPreferences != null) {
                    isShowLevel = sharedPreferences.getBoolean("checkBoxLevel", true)
                    upData()
                }
            }

            "checkBoxTemperature" -> {
                if (sharedPreferences != null) {
                    isShowTemperature = sharedPreferences.getBoolean("checkBoxTemperature", false)
                    upData()
                }
            }
        }
    }
}


