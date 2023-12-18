package la.shiro.batterylog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import la.shiro.batterylog.database.BatteryInfo
import la.shiro.batterylog.database.BatteryInfoRepository

class LineChartViewMode(repository: BatteryInfoRepository, long: Long) : ViewModel() {
    val testLog: LiveData<List<BatteryInfo>> = repository.getTestLog(long).asLiveData()
}

class LineChartViewModeFactory(
    private val repository: BatteryInfoRepository,
    private val long: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LineChartViewMode::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LineChartViewMode(repository, long) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}