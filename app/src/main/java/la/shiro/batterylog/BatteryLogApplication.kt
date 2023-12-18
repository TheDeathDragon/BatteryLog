package la.shiro.batterylog

import android.app.Application
import la.shiro.batterylog.database.BatteryInfoDatabase
import la.shiro.batterylog.database.BatteryInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BatteryLogApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { BatteryInfoDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BatteryInfoRepository(database.batteryInfoDao()) }

}
