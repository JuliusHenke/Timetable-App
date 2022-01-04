package juliushenke.smarttt.model

import java.io.Serializable

internal class Settings : Serializable {
    var start_day = 1
        set(value) {
            field = value
            if (end_day < field) end_day = field
        }
    var end_day = 5
        set(value) {
            field = value
            if (start_day > field) start_day = field
        }
    var weekSystem = false
    var showWeek = false
    var showHourTimes = false
    var hourTimes: Array<String?> = arrayOf()

    companion object {
        private const val serialVersionUID = -29238982928391L
    }

    init {
        hourTimes = arrayOf()
    }
}