package la.shiro.batterylog.adapter

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.BatteryManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import la.shiro.batterylog.R
import la.shiro.batterylog.database.BatteryInfo
import java.util.*

class BatteryLogDetailAdapter(private var list: List<BatteryInfo>) :
    RecyclerView.Adapter<BatteryLogDetailAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTime: TextView = itemView.findViewById<View>(R.id.tv_time) as TextView
        var textViewLevel: TextView = itemView.findViewById<View>(R.id.tv_level) as TextView
        var textViewTemperature: TextView =
            itemView.findViewById<View>(R.id.tv_temperature) as TextView
        var textViewVoltage: TextView =
            itemView.findViewById<View>(R.id.tv_voltage) as TextView
        var textViewPlugged: TextView =
            itemView.findViewById<View>(R.id.tv_plugged) as TextView
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<BatteryInfo>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(
                R.layout.battery_log_detail_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewTime.text = longToDate(list[position].time)
        holder.textViewLevel.text = list[position].level.toString() + "%"
        holder.textViewTemperature.text =
            ((list[position].temperature).toFloat() / 10).toString() + "℃"
        holder.textViewVoltage.text = list[position].voltage.toString() + "mV"
        when (list[position].status) {
            BatteryManager.BATTERY_STATUS_UNKNOWN -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.battery_status_unknown)

            BatteryManager.BATTERY_STATUS_CHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.battery_status_charging)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.battery_status_discharging)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.battery_status_not_charging)

            BatteryManager.BATTERY_STATUS_FULL -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.battery_status_full)
        }
    }

    private fun longToDate(timeStamp: Long): String {
        val date = Date(timeStamp)
        val sd = SimpleDateFormat("HH:mm:ss" , Locale.getDefault())
        sd.timeZone = TimeZone.GMT_ZONE
        return sd.format(date)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
