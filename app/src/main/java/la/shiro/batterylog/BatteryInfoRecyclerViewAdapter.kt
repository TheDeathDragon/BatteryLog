package la.shiro.batterylog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import la.shiro.batterylog.R

class BatteryInfoRecyclerViewAdapter(
    private var mutableMap: MutableMap<String, String>,
    private var batteryStatNameList: List<String>,
    private var batteryStatValueList: List<String>
) :
    RecyclerView.Adapter<BatteryInfoRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var value: TextView = itemView.findViewById<View>(R.id.textView_battery_info_v) as TextView
        var name: TextView = itemView.findViewById<View>(R.id.textView_battery_info_t) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.battery_info_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = batteryStatValueList[position]
        holder.value.text = mutableMap[batteryStatNameList[position]]
    }

    override fun getItemCount(): Int {
        return mutableMap.size
    }

}