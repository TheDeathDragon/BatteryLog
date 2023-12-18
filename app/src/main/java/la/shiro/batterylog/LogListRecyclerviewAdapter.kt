package la.shiro.batterylog

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import la.shiro.batterylog.R
import java.util.*

class LogListRecyclerviewAdapter(private var list: List<Long>) :
    RecyclerView.Adapter<LogListRecyclerviewAdapter.ViewHolder>() {
    private lateinit var itemOnClickListener: ItemOnClickListener

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView =
            itemView.findViewById<View>(R.id.test_list_recyclerview_item_textView) as TextView
        var deleteButton: Button =
            itemView.findViewById<View>(R.id.test_list_recyclerview_item_deleteButton) as Button
        var chartButton: Button =
            itemView.findViewById<View>(R.id.test_list_recyclerview_item_chartButton) as Button
        var listButton: Button =
            itemView.findViewById<View>(R.id.test_list_recyclerview_item_listButton) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(
                R.layout.test_list_recyclerview_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = longToDate(list[position])
        holder.chartButton.setOnClickListener { itemOnClickListener.onClickChartButton(list[position]) }
        holder.listButton.setOnClickListener { itemOnClickListener.onClickListButton(list[position]) }
        holder.deleteButton.setOnClickListener { itemOnClickListener.onClickDeleteButton(list[position]) }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    private fun longToDate(lo: Long): String? {
        val date = Date(lo)
        val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sd.format(date)
    }

    fun setData(list: List<Long>) {
        this.list = list
        notifyDataSetChanged()
    }

    interface ItemOnClickListener {
        fun onClickChartButton(lo: Long)
        fun onClickDeleteButton(lo: Long)
        fun onClickListButton(lo: Long)
    }

    fun setClickListener(itemOnClickListener: ItemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener
    }
}