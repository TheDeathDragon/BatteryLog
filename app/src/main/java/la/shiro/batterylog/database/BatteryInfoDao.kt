/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package la.shiro.batterylog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The Room Magic is in this file, where you map a method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */

@Dao
interface BatteryInfoDao {

    // The flow always holds/caches latest version of data. Notifies its observers when the
    // data has changed.
    @Query("SELECT * FROM battery_info_table ORDER BY time ASC")
    fun getAlphabetizedBatteryInfo(): Flow<List<BatteryInfo>>

    @Query("SELECT title FROM battery_info_table")
    fun getAllTestTitle(): Flow<List<Long>>

    @Query("SELECT * FROM battery_info_table WHERE title =:title")
    fun getTestLog(title: Long): Flow<List<BatteryInfo>>

    @Query("SELECT * FROM battery_info_table WHERE title =:title")
    fun getTestLog1(title: Long): List<BatteryInfo>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(batteryInfo: BatteryInfo)

    @Query("DELETE FROM battery_info_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM battery_info_table LIMIT 1")
    suspend fun getBatteryInfo(): BatteryInfo

    @Query("DELETE FROM battery_info_table WHERE title =:lo")
    suspend fun delete(lo: Long)
}
