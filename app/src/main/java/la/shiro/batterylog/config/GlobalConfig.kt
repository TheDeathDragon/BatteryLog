package la.shiro.batterylog.config

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

const val TAG = "Battery LOG"

/**
 * 管理自定义测试名称
 */
class TestNameManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("test_names", Context.MODE_PRIVATE)
    
    /**
     * 保存自定义测试名称
     */
    fun saveTestName(timestamp: Long, customName: String) {
        sharedPreferences.edit {
            putString(timestamp.toString(), customName)
        }
    }
    
    /**
     * 获取测试名称，如果有自定义名称则返回自定义名称，否则返回格式化的日期时间
     */
    fun getTestName(timestamp: Long): String {
        val customName = sharedPreferences.getString(timestamp.toString(), null)
        return if (customName.isNullOrEmpty()) {
            longToDate(timestamp)
        } else {
            customName
        }
    }
    
    /**
     * 删除自定义测试名称
     */
    fun removeTestName(timestamp: Long) {
        sharedPreferences.edit {
            remove(timestamp.toString())
        }
    }
    
    /**
     * 获取显示用的测试名称（带日期后缀）
     */
    fun getDisplayTestName(timestamp: Long): String {
        val customName = sharedPreferences.getString(timestamp.toString(), null)
        return if (customName.isNullOrEmpty()) {
            longToDate(timestamp)
        } else {
            "$customName (${longToDate(timestamp)})"
        }
    }
    
    private fun longToDate(timeStamp: Long): String {
        val date = Date(timeStamp)
        val sd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sd.format(date)
    }
}

const val CHANNEL_ID = "channel_1"
const val SERVICE_NAME = "BatteryLogService"
const val WAKE_LOG_TAG = "Rin:BatteryLogWakeLock"

const val NOTIFICATION_PERMISSION_REQUEST_CODE = 10085
const val BATTERY_OPTIMIZATION_REQUEST_CODE = 10086

const val COORDINATES_PER_VERTEX = 3

const val PI_50 : Double = 3.1415926535897932384626433832795028841971
const val REQUEST_CHECK_SETTINGS = 0x1