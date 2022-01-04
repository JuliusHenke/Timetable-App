package juliushenke.smarttt

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        updateActivitySettings()
        util.updateDesign(this, true)
        for (hourTimeInput in hourTimeInputs) {
            hourTimeInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    val hourTimes = arrayOfNulls<String>(hourTimeInputs.size)
                    for (i in hourTimes.indices) {
                        hourTimes[i] = hourTimeInputs[i].text.toString()
                    }
                    val settings = util.readSettings(this@SettingsActivity)
                    settings.hourTimes = hourTimes
                    util.saveSettings(this@SettingsActivity, settings)
                }
            })
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun updateActivitySettings() {
        val settings = util.readSettings(this)
        val a: AppCompatActivity = this
        val days = resources.getStringArray(R.array.days)
        val switchEvenOddWeekSystem = findViewById<SwitchMaterial>(R.id.switchEvenOddWeekSystem)
        val switchWeekDisplay = findViewById<SwitchMaterial>(R.id.switchWeekDisplay)
        val switchHourTimes = findViewById<SwitchMaterial>(R.id.switchHourTimes)

        findViewById<Button>(R.id.btnStartDay).text = days[settings.start_day]
        findViewById<Button>(R.id.btnEndDay).text = days[settings.end_day]
        switchEvenOddWeekSystem.isChecked = settings.weekSystem
        switchWeekDisplay.isChecked = settings.showWeek
        switchHourTimes.isChecked = settings.showHourTimes

        findViewById<LinearLayout>(R.id.hourTimes).visibility =
            if (settings.showHourTimes) View.VISIBLE
            else View.INVISIBLE

        if (settings.showHourTimes) {
            val hourTimeInputs = hourTimeInputs
            val hourTimes = settings.hourTimes
            var i = 0
            while (i < hourTimeInputs.size && i < hourTimes.size) {
                hourTimeInputs[i].setText(hourTimes[i])
                i++
            }
        }
        switchEvenOddWeekSystem.setOnCheckedChangeListener { _, isChecked ->
            settings.weekSystem = isChecked
            util.saveSettings(a, settings)
        }
        switchWeekDisplay.setOnCheckedChangeListener { _, isChecked ->
            settings.showWeek = isChecked
            util.saveSettings(a, settings)
        }
        switchHourTimes.setOnCheckedChangeListener { _, isChecked ->
            settings.showHourTimes = isChecked
            util.saveSettings(a, settings)
            updateActivitySettings()
        }
    }

    fun clickBtnStartDay(view: View) {
        dialogStartDay(true).show()
    }

    fun clickBtnEndDay(view: View) {
        dialogStartDay(false).show()
    }

    private fun dialogStartDay(startDay: Boolean): Dialog {
        val builder = AlertDialog.Builder(this)
        val days = resources.getStringArray(R.array.days)
        val options = arrayOf(days[1], days[2], days[3], days[4], days[5], days[6], days[7])
        val settings = util.readSettings(this)
        val a: AppCompatActivity = this
        builder.setTitle(null).setItems(options) { dialog, which ->
            if (startDay) settings.start_day = which + 1 else settings.end_day = which + 1
            util.saveSettings(a, settings)
            dialog.cancel()
            if (settings.start_day == settings.end_day) dialogWarnManageDays().show()
            updateActivitySettings()
        }
        return builder.create()
    }

    private fun dialogWarnManageDays(): Dialog {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.D_warnManageDays_content))
            .setPositiveButton(R.string.OK) { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    private val hourTimeInputs: Array<EditText>
        get() = arrayOf(
            findViewById(R.id.time1), findViewById(R.id.time2), findViewById(R.id.time3),
            findViewById(R.id.time4), findViewById(R.id.time5), findViewById(R.id.time6),
            findViewById(R.id.time7), findViewById(R.id.time8), findViewById(R.id.time9),
            findViewById(R.id.time10), findViewById(R.id.time11)
        )

    companion object {
        private val util = Util()
    }
}