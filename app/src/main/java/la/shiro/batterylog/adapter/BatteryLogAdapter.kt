package la.shiro.batterylog.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import la.shiro.batterylog.R
import la.shiro.batterylog.config.TestNameManager
import java.util.*

class BatteryLogAdapter(private var list: List<Long>, private val context: Context) :
    RecyclerView.Adapter<BatteryLogAdapter.ViewHolder>() {
    private lateinit var itemOnClickListener: ItemOnClickListener
    private val testNameManager = TestNameManager(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView =
            itemView.findViewById<View>(R.id.tv_log_item) as TextView
        var deleteButton: Button =
            itemView.findViewById<View>(R.id.btn_log_item_delete) as Button
        var chartButton: Button =
            itemView.findViewById<View>(R.id.btn_log_item_chart) as Button
        var listButton: Button =
            itemView.findViewById<View>(R.id.btn_log_item_detail) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(
                R.layout.battery_log_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = testNameManager.getDisplayTestName(list[position])
        holder.chartButton.setOnClickListener { itemOnClickListener.onClickChartButton(list[position]) }
        holder.listButton.setOnClickListener { itemOnClickListener.onClickListButton(list[position]) }
        holder.deleteButton.setOnClickListener { itemOnClickListener.onClickDeleteButton(list[position]) }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    private fun longToDate(timeStamp: Long): String? {
        val date = Date(timeStamp)
        val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sd.format(date)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Long>) {
        this.list = list
        notifyDataSetChanged()
    }

    interface ItemOnClickListener {
        fun onClickChartButton(timeStamp: Long)
        fun onClickDeleteButton(timeStamp: Long)
        fun onClickListButton(timeStamp: Long)
    }

    fun setClickListener(itemOnClickListener: ItemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener
    }
}