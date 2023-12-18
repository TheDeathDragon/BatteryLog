package la.shiro.batterylog

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.BatteryManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import la.shiro.batterylog.database.BatteryInfo
import java.util.*

class LineListRecyclerviewAdapter(private var list: List<BatteryInfo>) :
    RecyclerView.Adapter<LineListRecyclerviewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTime: TextView = itemView.findViewById<View>(R.id.textViewTime) as TextView
        var textViewLevel: TextView = itemView.findViewById<View>(R.id.textViewLevel) as TextView
        var textViewTemperature: TextView =
            itemView.findViewById<View>(R.id.textViewTemperature) as TextView
        var textViewVoltage: TextView =
            itemView.findViewById<View>(R.id.textViewVoltage) as TextView
        var textViewPlugged: TextView =
            itemView.findViewById<View>(R.id.textViewPlugged) as TextView
    }

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
                R.layout.line_list_recyclerview_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewTime.text = longToDate(list[position].time)
        holder.textViewLevel.text = list[position].level.toString() + "%"
        holder.textViewTemperature.text =
            ((list[position].temperature).toFloat() / 10).toString() + "℃"
        holder.textViewVoltage.text = list[position].voltage.toString() + "mV"

//        when (list[position].plugged) {
//            BatteryManager.BATTERY_PLUGGED_AC -> holder.textViewPlugged.text =
//                holder.textViewPlugged.context.getString(R.string.BATTERY_PLUGGED_AC)
//
//            BatteryManager.BATTERY_PLUGGED_USB -> holder.textViewPlugged.text =
//                holder.textViewPlugged.context.getString(R.string.BATTERY_PLUGGED_USB)
//
//            BatteryManager.BATTERY_PLUGGED_WIRELESS -> holder.textViewPlugged.text =
//                holder.textViewPlugged.context.getString(R.string.BATTERY_PLUGGED_WIRELESS)
//
//            else -> holder.textViewPlugged.text =
//                holder.textViewPlugged.context.getString(R.string.BATTERY_PLUGGED_NONE)
//        }

        when (list[position].status) {
            BatteryManager.BATTERY_STATUS_UNKNOWN -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.BATTERY_STATUS_UNKNOWN)

            BatteryManager.BATTERY_STATUS_CHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.BATTERY_STATUS_CHARGING)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.BATTERY_STATUS_DISCHARGING)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.BATTERY_STATUS_NOT_CHARGING)

            BatteryManager.BATTERY_STATUS_FULL -> holder.textViewPlugged.text =
                holder.textViewPlugged.context.getString(R.string.BATTERY_STATUS_FULL)
        }
    }

    private fun longToDate(time: Long): String {
        val date = Date(time)
        val sd = SimpleDateFormat("HH:mm:ss")
        sd.timeZone = TimeZone.GMT_ZONE
        return sd.format(date)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
