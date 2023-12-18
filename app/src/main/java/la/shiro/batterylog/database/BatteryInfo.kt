package la.shiro.batterylog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A basic class representing an entity that is a row in a one-column database table.
 *
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 *
 * See the documentation for the full rich set of annotations.
 * https://developer.android.com/topic/libraries/architecture/room.html
 */

@Entity(tableName = "battery_info_table")
data class BatteryInfo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    var title: Long,
    var time: Long,
    var status: Int,
    var health: Int,
    var present: Boolean,
    var level: Int,
    var scale: Int,
    var icon: Int,
    var plugged: Int,
    var voltage: Int,
    var temperature: Int,
    var technology: String
)
