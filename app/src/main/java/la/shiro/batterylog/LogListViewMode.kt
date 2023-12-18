package la.shiro.batterylog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import la.shiro.batterylog.database.BatteryInfoRepository

class LogListViewMode(private val repository: BatteryInfoRepository) : ViewModel() {
    suspend fun delete(lo: Long) {
        repository.delete(lo)
    }

    suspend fun deleteAll() {
        repository.deleteAll()
    }

    val allTestTitle: LiveData<List<Long>> = repository.getAllTestTitle().asLiveData()

}

class LogListViewModeFactory(private val repository: BatteryInfoRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogListViewMode::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogListViewMode(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}