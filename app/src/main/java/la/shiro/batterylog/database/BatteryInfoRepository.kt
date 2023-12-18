package la.shiro.batterylog.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class BatteryInfoRepository(private val batteryInfoDao: BatteryInfoDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(batteryInfo: BatteryInfo) {
        batteryInfoDao.insert(batteryInfo)
    }

    @WorkerThread
    suspend fun getBatteryInfo(): BatteryInfo {
        return batteryInfoDao.getBatteryInfo()
    }


    fun getAllTestTitle(): Flow<List<Long>> {
        return batteryInfoDao.getAllTestTitle()
    }

    fun getTestLog(title: Long): Flow<List<BatteryInfo>> {
        return batteryInfoDao.getTestLog(title)
    }

    fun getTestLog1(title: Long): List<BatteryInfo> {
        return batteryInfoDao.getTestLog1(title)
    }

    @WorkerThread
    suspend fun delete(lo: Long) {
        batteryInfoDao.delete(lo)
    }

    @WorkerThread
    suspend fun deleteAll() {
        batteryInfoDao.deleteAll()
    }
}
